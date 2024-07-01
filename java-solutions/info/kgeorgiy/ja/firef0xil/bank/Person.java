package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {

    /** Returns student client first name. */
    String getFirstName() throws RemoteException;

    /** Returns student client last name. */
    String getLastName() throws RemoteException;

    /** Returns student client passport Id. */
    String getPassportId() throws RemoteException;

    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Returns all accounts of specified person.
     * @return unmodifiable {@link java.util.Map}{@code <String, Account>} of all person's accounts.
     */
    Map<String, Account> getAccountAll() throws RemoteException;
}
