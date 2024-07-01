package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param personId person id
     * @param accountId account id
     * @return created or existing account.
     */
    Account createAccount(String personId, String accountId) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param personId person id
     * @param accountId account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String personId, String accountId) throws RemoteException;

    /**
     * Returns person by identifier.
     * @param personId person id
     * @return person with specified identifier or {@code null} if such person does not exists.
     */
    Person getPerson(String personId) throws RemoteException;

    /**
     * Creates a new person with specified identifiers if it is not already exists.
     *
     * @param firstName person's first name
     * @param lastName person's last name
     * @param passportId person's passport id
     * @return created or existing account.
     * @throws PersonCreationException if any of arguments is null or empty
     */
    Person createPerson(String firstName, String lastName, String passportId) throws RemoteException;

    /**
     * Creates a new {@link LocalPerson} from specified identifier or null if there is no person in this bank.
     *
     * @param personId passport id
     * @return {@link LocalPerson} from existing person
     */
    Person getLocalPerson(String personId) throws RemoteException;
}
