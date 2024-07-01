package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation for interface {@link Bank}.
 * @author FireF0xIl
 */
public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Person> people = new ConcurrentHashMap<>();

    /**
     * Constructs new {@link Bank} with specified port to listen.
     * @param port specified port to listen
     */
    public RemoteBank(final int port) {
        this.port = port;
    }

    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param personId person id
     * @param accountId account id
     * @return created or existing account.
     */
    @Override
    public Account createAccount(final String personId, final String accountId) throws RemoteException {
        System.out.println("Creating account `" + accountId + "` for person `" + personId + "`");
        final Person person = getPerson(personId);
        if (person == null) {
            System.out.println("No person with `" + personId + "`");
            return null;
        } else {
            final Account account = person.getAccount(accountId);
            if (person.getAccount(accountId) != null) {
                System.out.println("Creating new account with `" + accountId +"` for person `" + personId + "`");
                return account;
            } else {
                final Account newAccount = person.createAccount(accountId);
                UnicastRemoteObject.exportObject(newAccount, port);
                return newAccount;
            }
        }
    }

    /**
     * Returns account by identifier.
     * @param personId person id
     * @param accountId account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    @Override
    public Account getAccount(final String personId, final String accountId) throws RemoteException {
        System.out.println("Retrieving account `" + accountId + "` for person `" + personId + "`");
        Person person = getPerson(personId);
        if (person == null) {
            System.out.println("No person with `" + personId + "`");
            return null;
        } else {
            return person.getAccount(accountId);
        }
    }

    /**
     * Returns person by identifier.
     * @param personId person id
     * @return person with specified identifier or {@code null} if such person does not exists.
     */
    @Override
    public Person getPerson(final String personId) throws RemoteException {
        System.out.println("Searching person with `" + personId + "`");
        return people.get(personId);
    }

    /**
     * Creates a new person with specified identifiers if it is not already exists.
     *
     * @param firstName person's first name
     * @param lastName person's last name
     * @param passportId person's passport id
     * @return created or existing account.
     * @throws PersonCreationException if any of arguments is null or empty
     */
    @Override
    public synchronized Person createPerson(final String firstName, final String lastName, final String passportId)
            throws RemoteException {
        System.out.println("Creating account with: first name `" + firstName +
                "`, last name `" + lastName + "`, passport id `" + passportId + "`");
        final Person account = new RemotePerson(firstName, lastName, passportId);
        if (people.putIfAbsent(passportId, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getPerson(passportId);
        }
    }

    /**
     * Creates a new {@link LocalPerson} from specified identifier or null if there is no person in this bank.
     *
     * @param personId passport id
     * @return {@link LocalPerson} from existing person
     */
    @Override
    public synchronized Person getLocalPerson(String personId) throws RemoteException {
        System.out.println("Searching person with id `" + personId + "` to make a LocalPerson");
        Person person = people.get(personId);
        if (person == null) {
            System.out.println("Person with id `" + personId + "` does not exist");
            return null;
        } else {
            System.out.println("Creating LocalPerson with `" + personId + "`");
            return new LocalPerson(person);
        }
    }
}
