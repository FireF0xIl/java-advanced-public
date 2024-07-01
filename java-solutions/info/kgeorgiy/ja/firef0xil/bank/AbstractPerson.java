package info.kgeorgiy.ja.firef0xil.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation for {@link Person}
 * @author FireF0xIl
 */
public abstract class AbstractPerson implements Person, Serializable {
    /** Client first name */
    protected final String firstName;
    /** Client last name */
    protected final String lastName;
    /** Client passport id */
    protected final String passportId;
    /** Client's accounts */
    protected final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    /** Constructs new instance of {@link Person}. */
    public AbstractPerson(String firstName, String lastName, String passportId) {
        if (!StaticFunctions.checkString(firstName)
                || !StaticFunctions.checkString(lastName)
                || !StaticFunctions.checkString(passportId)) {
            throw new PersonCreationException("Null or empty identifier");
        } else {
            this.firstName = firstName;
            this.lastName = lastName;
            this.passportId = passportId;
        }
    }

    /** Returns student client first name. */
    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    /** Returns student client last name. */
    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }

    /** Returns student client passport Id. */
    @Override
    public String getPassportId() throws RemoteException {
        return passportId;
    }

    /** Redirect passportId and accountId to StaticFunctions.getAccountId */
    protected String getAccountId(String id) {
        return StaticFunctions.getAccountId(passportId, id);
    }
}
