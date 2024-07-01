package info.kgeorgiy.ja.firef0xil.bank;

import org.junit.*;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.firef0xil.bank.StaticFunctions.getAccountId;
import static org.junit.Assert.*;

/**
 * Tests for package bank.
 * @author FireF0xIl
 */
public class Tests {
    private static Registry registry;
    private Bank bank;
    private final static int port = 8887;
    private final static String testFirstName = "first";
    private final static String testLastName = "second";
    private final static String testPassportId = "passport";
    private final static String account1 = "1234";
    private final static String account2 = "abac";
    private final static int setAmount = 17;
    private final static int changeAmount = 19;
    private final static int threads = 8;

    @BeforeClass
    public static void registry() throws RemoteException {
        registry = LocateRegistry.createRegistry(port);
    }

    @Before
    public void bankOpen() throws RemoteException, AlreadyBoundException {
        this.bank = new RemoteBank(port);
        Bank bind = (Bank) UnicastRemoteObject.exportObject(bank, 0);
        registry.bind("bank", bind);
    }

    @After
    public void bankClose() throws RemoteException, NotBoundException {
        registry.unbind("bank");
    }

    private Person createPerson() throws RemoteException {
        return bank.createPerson(testFirstName, testLastName, testPassportId);
    }

    @Test
    public void test1_simplePersonCreation() throws RemoteException {
        assertNull(bank.getPerson(testPassportId));
        assertNotNull(createPerson());
        Person person = bank.getPerson(testPassportId);
        assertEquals(testPassportId, person.getPassportId());
        assertEquals(testFirstName, person.getFirstName());
        assertEquals(testLastName, person.getLastName());
        assertTrue(person.getAccountAll().isEmpty());
    }

    @Test
    public void test2_simpleAccountCreation() throws RemoteException {
        Person person = createPerson();
        assertNotNull(person);
        Account account = person.createAccount(account1);
        assertNotNull(account);
        assertEquals(getAccountId(testPassportId, account1), person.getAccount(account1).getId());
        assertEquals(1, person.getAccountAll().size());
        assertEquals(0, person.getAccount(account1).getAmount());
    }


    @Test
    public void test3_setAmount() throws RemoteException {
        Person person = createPerson();
        assertNotNull(person);
        Account account = person.createAccount(account1);
        assertNotNull(account);
        account.setAmount(setAmount);
        assertEquals(1, person.getAccountAll().size());
        assertEquals(setAmount, account.getAmount());
    }

    @Test
    public void test4_doubleAccountCreation() throws RemoteException {
        Person person = createPerson();
        assertNotNull(person);
        Account account = person.createAccount(account1);
        assertNotNull(account);
        account.setAmount(setAmount);
        assertEquals(setAmount, account.getAmount());
        Account accountRepeat = person.createAccount(account1);
        assertNotNull(accountRepeat);
        accountRepeat.setAmount(2 * setAmount);
        assertEquals(1, person.getAccountAll().size());
        assertEquals(2 * setAmount, account.getAmount());
    }

    @Test
    public void test5_doublePersonCreation() throws RemoteException {
        Person person = createPerson();
        assertNotNull(person);
        Person personRepeated = createPerson();
        assertEquals(person, personRepeated);
    }

    @Test(expected = PersonCreationException.class)
    public void test6_invalidPersonCreation1() throws RemoteException {
        bank.createPerson(null, testLastName, testPassportId);
    }

    @Test(expected = PersonCreationException.class)
    public void test7_invalidPersonCreation2() throws RemoteException {
        bank.createPerson(testFirstName, "", testPassportId);
    }

    @Test
    public void test8_changeAccountMoneySimple() throws RemoteException {
        Person person = createPerson();
        Account account = bank.createAccount(person.getPassportId(), account1);
        account.setAmount(2 * setAmount);
        assertEquals(2 * setAmount, account.getAmount());
        account.changeAmount(-changeAmount);
        assertEquals(2 * setAmount - changeAmount, account.getAmount());
    }

    @Test
    public void test9_changeAccountMoney() throws RemoteException {
        Person person = createPerson();
        Account account = bank.createAccount(person.getPassportId(), account1);
        account.changeAmount(changeAmount);
        assertEquals(changeAmount, account.getAmount());
        Account accountNew = bank.createAccount(person.getPassportId(), account2);
        accountNew.changeAmount(2 * changeAmount);
        assertEquals(2 * changeAmount, accountNew.getAmount());
        account.changeAmount(-changeAmount);
        assertEquals(0, account.getAmount());
    }

    @Test
    public void test10_negativeAmountOfMoney() throws RemoteException {
        Person person = createPerson();
        Account account = bank.createAccount(person.getPassportId(), account1);
        account.changeAmount(-changeAmount);
        assertEquals(0, account.getAmount());
    }

    @Test
    public void test11_LocalCreation() throws RemoteException {
        Person person = createPerson();
        Account account = bank.createAccount(person.getPassportId(), account1);
        account.changeAmount(changeAmount);
        Person personLocal = bank.getLocalPerson(person.getPassportId());
        assertEquals(testPassportId, personLocal.getPassportId());
        assertEquals(testFirstName, personLocal.getFirstName());
        assertEquals(testLastName, personLocal.getLastName());
        assertEquals(1, personLocal.getAccountAll().size());
        account.changeAmount(changeAmount);
        person.createAccount(account2);
        assertEquals(1, personLocal.getAccountAll().size());
        Account accountLocal = personLocal.getAccount(account1);
        assertEquals(changeAmount, accountLocal.getAmount());
        accountLocal.setAmount(setAmount);
        assertEquals(2 * changeAmount, account.getAmount());
    }

    @Test
    public void test12_parallelPersonCreation() throws RemoteException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        IntStream.range(0, threads).<Runnable>mapToObj(i -> () -> {
            try {
                bank.createPerson(testFirstName, testLastName, testPassportId + i);
            } catch (RemoteException ignored) {
            } finally {
                latch.countDown();
            }
        }).forEach(executorService::submit);
        latch.await();
        for (int i = 0; i < threads; i++) {
            assertNotNull(bank.getPerson(testPassportId + i));
        }
    }

    @Test
    public void test13_parallelAccountCreation() throws RemoteException, InterruptedException {
        Person person = createPerson();
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        IntStream.range(0, threads).<Runnable>mapToObj(i -> () -> {
            try {
                bank.createAccount(person.getPassportId(), account1 + i);
            } catch (RemoteException ignored) {
            } finally {
                latch.countDown();
            }
        }).forEach(executorService::submit);
        latch.await();
        assertEquals(threads, person.getAccountAll().size());
        for (int i = 0; i < threads; i++) {
            assertNotNull(person.getAccount(account1 + i));
        }
    }

    @Test
    public void test14_parallelChangeMoneyOnAccount() throws RemoteException, InterruptedException {
        Person person = createPerson();
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    Account account = bank.createAccount(person.getPassportId(), account1);
                    account.changeAmount(changeAmount);
                } catch (RemoteException ignored) {}
                finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        assertEquals(threads * changeAmount, person.getAccount(account1).getAmount());
        assertEquals(1, person.getAccountAll().size());
    }
}
