package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Client {
    /** Utility class. */
    private Client() {}

    public static void main(final String[] args) throws RemoteException {
        if (args == null) {
            System.out.println("Expected non null array");
        } else if (args.length != 5) {
            System.out.println("Expected 5 params: firstName, lastName, passportId, accountId, amountToChange");
        } else {
            final Bank bank;
            try {
                Registry registry = LocateRegistry.getRegistry(8889);
                bank = (Bank) registry.lookup("bank");
            } catch (final NotBoundException e) {
                System.out.println("Bank is not bound");
                return;
            }

            final String accountId = args[3];
            final String personId = args[2];

            Account account = bank.getAccount(personId, accountId);
            if (account == null) {
                System.out.println("Creating account");
                account = bank.createPerson(args[0], args[1], personId).createAccount(accountId);
            } else {
                System.out.println("Account already exists");
            }
            System.out.println("Account id: " + account.getId());
            System.out.println("Money: " + account.getAmount());
            System.out.println("Adding money");
            account.setAmount(account.getAmount() + 100);
            System.out.println("Money: " + account.getAmount());
        }
    }
}
