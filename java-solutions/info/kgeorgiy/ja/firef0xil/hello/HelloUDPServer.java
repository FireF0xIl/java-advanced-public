package info.kgeorgiy.ja.firef0xil.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.firef0xil.hello.StaticFunctions.CHARSET;

/**
 * Implementation for interface {@link HelloServer}.
 * @author FireF0xIl
 */
public class HelloUDPServer extends HelloUDPServerAbstract {
    private final static int TIMEOUT = 50;
    private DatagramSocket socket;

    /**
     * Specific implementation details to start Hello server.
     *
     * @param port server port.
     * @param threads number of working threads.
     */
    @Override
    protected void run(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(TIMEOUT);
            int bufferSize = socket.getReceiveBufferSize();

            IntStream.range(0, threads).forEach(i -> service.submit(() -> {
                byte[] buffer = new byte[bufferSize];
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, bufferSize);
                    while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                        StaticFunctions.receive(socket, datagramPacket, buffer);
                        StaticFunctions.send(socket,
                                datagramPacket,
                                "Hello, " +
                                        new String(datagramPacket.getData(),
                                                datagramPacket.getOffset(),
                                                datagramPacket.getLength(),
                                                CHARSET));

                    }

                } catch (SocketTimeoutException ignored) {
                } catch (SocketException e) {
                    System.err.println("Socket exception occurs: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("I/O exception occurs: " + e.getMessage());
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
        } catch (SocketException e) {
            System.err.println("Failed to create UDP server: " + e.getMessage());
        }
    }

    /**
     * Specific implementation details to stop server and deallocate all resources.
     */
    @Override
    protected void closeServer() {
        socket.close();
    }

    /**
     * Start point of {@link HelloUDPServer}.
     *
     * Start a HelloUDPServer
     * @param args port threads
     */
    public static void main(String[] args) {
        main(args, new HelloUDPServer());
    }
}
