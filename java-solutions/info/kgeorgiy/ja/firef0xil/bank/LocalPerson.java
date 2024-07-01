package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;

/**
 * Local version of {@link Person}.
 * @author FireF0xIl
 */
public class LocalPerson extends AbstractPerson {

    /** Constructs new instance of {@link Person}. */
    public LocalPerson(String firstName, String lastName, String passportId) {
        super(firstName, lastName, passportId);
    }

    /** Constructs new instance of {@link Person}. */
    public LocalPerson(Person person) throws RemoteException {
        super(person.getFirstName(), person.getLastName(), person.getPassportId());
        for (Map.Entry<String, Account> entry : person.getAccountAll().entrySet()) {
            accounts.put(entry.getKey(), new LocalAccount(entry.getValue()));
        }
    }

    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    @Override
    public Account createAccount(String id) throws RemoteException {
        if (StaticFunctions.checkString(id)) {
            String accountId = getAccountId(id);
            return accounts.computeIfAbsent(accountId, k -> new RemoteAccount(accountId));
        } else {
            return null;
        }
    }

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    @Override
    public Account getAccount(String id) throws RemoteException {
        return accounts.get(getAccountId(id));
    }

    /**
     * Returns all accounts of specified person.
     * @return unmodifiable {@link java.util.Map}{@code <String, Account>} of all person's accounts.
     */
    @Override
    public Map<String, Account> getAccountAll() throws RemoteException {
        return Collections.unmodifiableMap(accounts);
    }
}
