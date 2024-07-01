package info.kgeorgiy.ja.firef0xil.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;

/**
 * Base function implementation for interface {@link HelloServer}.
 * @author FireF0xIl
 */
public abstract class HelloUDPServerAbstract implements HelloServer {
    private boolean start = false;
    private boolean finish = false;
    private final static int AWAIT_TIME = 10;
    private final static TimeUnit AWAIT_UNIT = TimeUnit.SECONDS;
    /** {@link ExecutorService} for multithreaded processing  */
    protected ExecutorService service = null;

    /**
     * Specific implementation details to start Hello server.
     *
     * @param port server port.
     * @param threads number of working threads.
     */
    protected abstract void run(int port, int threads);

    /**
     * Specific implementation details to stop server and deallocate all resources.
     */
    protected abstract void closeServer();


    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port server port.
     * @param threads number of working threads.
     * @throws IllegalStateException if called after close.
     */
    @Override
    public void start(int port, int threads) {
        if (finish) {
            throw new IllegalStateException("Server already has closed");
        }
        if (!start) {
            start = true;
            if (threads < 1) {
                throw new IllegalArgumentException("Number of threads should be positive number");
            }
            service = Executors.newFixedThreadPool(threads);
            run(port, threads);
        }
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        if (!finish) {
            finish = true;
            closeServer();
            StaticFunctions.close(service, AWAIT_TIME, AWAIT_UNIT);
        }
    }


    /**
     * Start point of Hello server.
     *
     * Start an implementation of hello server
     * @param args port threads
     */
    protected static void main(String[] args, HelloServer helloServer) {
        if (args == null || args.length != 2) {
            System.err.println("Usage: HelloUDPClient port threads.");
        } else {
            try (helloServer) {
                helloServer.start(parseInt(args[0]), parseInt(args[1]));
            } catch (NumberFormatException e) {
                System.err.println("Some of arguments cannot converted to int.");
            }
        }
    }
}
