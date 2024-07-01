package info.kgeorgiy.ja.firef0xil.bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public final class Server {
    private final static int DEFAULT_PORT = 8888;
    private final static int REGISTRY_PORT = 8889;

    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        final Bank bank = new RemoteBank(port);
        try {
            Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            Bank bind = (Bank) UnicastRemoteObject.exportObject(bank, port);
            registry.rebind("bank", bind);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
