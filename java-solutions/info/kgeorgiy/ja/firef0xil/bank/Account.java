package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.*;

public interface Account extends Remote {

    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money at the account. */
    int getAmount() throws RemoteException;

    /** Sets amount of money at the account. */
    void setAmount(int amount) throws RemoteException;

    /** Change amount of money at the account. */
    void changeAmount(int amount) throws RemoteException;
}