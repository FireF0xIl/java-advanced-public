package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.RemoteException;

/**
 * Remote version for {@link Account}.
 * @author FireF0xIl
 */
public class RemoteAccount extends AbstractAccount {

    /** Constructs new instance of {@link Account}. */
    public RemoteAccount(final String id) {
        super(id);
    }

    /** Returns amount of money at the account. */
    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    /** Sets amount of money at the account. */
    @Override
    public synchronized void setAmount(int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }

    /** Change amount of money at the account. */
    @Override
    public synchronized void changeAmount(int amount) throws RemoteException {
        if (amount < 0 && this.amount < Math.abs(amount)) {
            System.out.println("Change not successful for account " + id + " : not enough money. Current on account: " +
                    this.amount + ", and attempt to change on " + amount);
        } else {
            int old = this.amount;
            this.amount += amount;
            System.out.println("Changing amount of money for account " + id + ". Old amount: " + old +
                    ", new amount: " + this.amount);
        }
    }
}
