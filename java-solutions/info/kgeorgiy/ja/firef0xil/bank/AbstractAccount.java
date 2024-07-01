package info.kgeorgiy.ja.firef0xil.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Abstract class for {@link Account}.
 * @author FireF0xIl
 */
public abstract class AbstractAccount implements Account, Serializable {
    /** Account id */
    protected final String id;
    /** Account amount of money */
    protected int amount = 0;

    /** Constructs new instance of {@link Account}. */
    public AbstractAccount(String id) {
        this.id = id;
    }

    /** Returns account identifier. */
    @Override
    public String getId() throws RemoteException {
        return id;
    }
}
