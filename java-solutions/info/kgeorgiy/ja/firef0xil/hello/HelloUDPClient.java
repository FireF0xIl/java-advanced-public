package info.kgeorgiy.ja.firef0xil.hello;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;

/**
 * Implementation for interface {@link HelloUDPClient}.
 * @author FireF0xIl
 */
public class HelloUDPClient extends HelloUDPClientAbstract {
    private final static int AWAIT_TIME = 100;
    private final static TimeUnit AWAIT_UNIT = TimeUnit.SECONDS;

    /**
     * Runs Hello client.
     * This method should return when all requests completed.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress address = new InetSocketAddress(host, port);
        ExecutorService service = Executors.newFixedThreadPool(threads);

        IntStream.range(0, threads).forEach(threadID -> service.submit(() -> {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT);
                int bufferSize = socket.getReceiveBufferSize();
                byte[] buffer = new byte[bufferSize];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, bufferSize, address);

                IntStream.range(0, requests).forEach(requestID -> {

                    String expected = requestCreation(prefix, threadID, requestID);
                    String threadIDString = Integer.toString(threadID);
                    String requestIDString = Integer.toString(requestID);

                    while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                        try {
                            StaticFunctions.send(socket, datagramPacket, expected);
                            String actual = StaticFunctions.receive(socket, datagramPacket, buffer);
                            if (isValid(actual, threadIDString, requestIDString)) {
                                System.out.println("Response successful: " + actual);
                                break;
                            }
                        } catch (SocketTimeoutException ignored) {
                        } catch (IOException e) {
                            System.err.println("I/O exception occurs: " + e.getMessage());
                        }
                    }
                });
            } catch (SocketException e) {
                System.err.println("Socket exception occurs: " + e.getMessage());
            }
        }));

        StaticFunctions.close(service, AWAIT_TIME, AWAIT_UNIT);
    }

    /**
     * Start point of {@link HelloUDPClient}.
     *
     * Start a UDPClient
     * @param args server port prefix threads requests
     */
    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Usage: HelloUDPClient server port prefix threads requests.");
        } else {
            try {
                new HelloUDPClient().run(args[0], parseInt(args[1]), args[2], parseInt(args[3]), parseInt(args[4]));
            } catch (NumberFormatException e) {
                System.err.println("Some of arguments cannot converted to int.");
            }
        }
    }
}
