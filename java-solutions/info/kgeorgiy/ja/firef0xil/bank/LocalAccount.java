package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.RemoteException;

/**
 * Local version for {@link Account}.
 * @author FireF0xIl
 */
public class LocalAccount extends AbstractAccount {

    /** Constructs new instance of {@link Account}. */
    public LocalAccount(String id) {
        super(id);
    }

    /** Constructs new instance of {@link Account}. */
    public LocalAccount(Account account) throws RemoteException {
        super(account.getId());
        this.amount = account.getAmount();
    }

    /** Returns amount of money at the account. */
    @Override
    public int getAmount() throws RemoteException {
        return this.amount;
    }

    /** Sets amount of money at the account. */
    @Override
    public void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }

    /** Change amount of money at the account. */
    @Override
    public void changeAmount(int amount) throws RemoteException {
        int test = this.amount + amount;
        if (test >= 0) {
            this.amount = test;
        }
    }
}
