package info.kgeorgiy.ja.firef0xil.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.firef0xil.hello.StaticFunctions.CHARSET;

/**
 * Non-blocking implementation for interface {@link HelloServer}.
 * @author FireF0xIl
 */
public class HelloUDPNonblockingServer extends HelloUDPServerAbstract {
    private final static byte[] PREFIX = "Hello, ".getBytes(CHARSET);
    private Selector selector = null;
    private DatagramChannel datagramChannel = null;
    private Thread server = null;

    /**
     * Start point of {@link HelloUDPServer}.
     *
     * Start a HelloNonblockingUDPServer
     * @param args port threads
     */
    public static void main(String[] args) {
        main(args, new HelloUDPNonblockingServer());
    }


    /**
     * Specific implementation details to start Hello server.
     *
     * @param port server port.
     * @param threads number of working threads.
     */
    @Override
    protected void run(int port, int threads) {
        try {
            selector = Selector.open();
            (datagramChannel = DatagramChannel.open())
                    .bind(new InetSocketAddress(port))
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .configureBlocking(false)
                    .register(
                            selector,
                            SelectionKey.OP_READ,
                            new Context(threads, datagramChannel.socket().getReceiveBufferSize()));
            // особенно таких конструкций
            (server = new Thread(() -> {

                while (!datagramChannel.socket().isClosed() && !Thread.currentThread().isInterrupted()) {
                    try {
                        selector.select();

                        for (Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                             selectionKeyIterator.hasNext(); ) {
                            try {
                                SelectionKey selectionKey = selectionKeyIterator.next();

                                if (selectionKey.isReadable()) {
                                    Context context = (Context) selectionKey.attachment();
                                    Context.Pair pair = context.requestsIn.remove();

                                    if (context.requestsIn.isEmpty()) {
                                        selectionKey.interestOpsAnd(~SelectionKey.OP_READ);
                                    }
                                    try {
                                        pair.socketAddress = datagramChannel.receive(pair.byteBuffer.clear());

                                        service.submit(() -> {
                                            byte[] byteArray = pair.byteBuffer.flip().array();
                                            // код невероятно уехал направо, это явный признак того, что что-то идёт не так
                                            System.arraycopy(
                                                    byteArray,
                                                    pair.byteBuffer.arrayOffset(),
                                                    byteArray,
                                                    PREFIX.length,
                                                    pair.byteBuffer.limit());
                                            System.arraycopy(
                                                    PREFIX,
                                                    0,
                                                    byteArray,
                                                    0,
                                                    PREFIX.length);
                                            pair.byteBuffer.limit(pair.byteBuffer.limit() + PREFIX.length);

                                            synchronized (context.requestsOut) {
                                                context.requestsOut.add(pair);
                                                if (context.requestsOut.size() == 1) {
                                                    selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
                                                    selector.wakeup();
                                                }
                                            }
                                        });

                                    } catch (IOException e) {
                                        System.err.println("I/O exception in receive: " + e.getMessage());
                                        context.requestsIn.add(pair);
                                        selectionKey.interestOpsOr(SelectionKey.OP_READ);
                                    }
                                }

                                if (selectionKey.isWritable()) {
                                    Context context = (Context) selectionKey.attachment();
                                    Context.Pair pair;

                                    synchronized (context.requestsOut) {
                                        pair = context.requestsOut.remove();
                                        if (context.requestsOut.isEmpty()) {
                                            selectionKey.interestOpsAnd(~SelectionKey.OP_WRITE);
                                        }
                                    }

                                    try {
                                        datagramChannel.send(pair.byteBuffer, pair.socketAddress);
                                    } catch (IOException e) {
                                        System.err.println("I/O exception in send: " + e.getMessage());
                                    } finally {
                                        context.requestsIn.add(pair);
                                        selectionKey.interestOpsOr(SelectionKey.OP_READ);
                                    }
                                }

                            } finally {
                                selectionKeyIterator.remove();
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Selector I/O exception: " + e.getMessage());
                    }
                }
            })).start();

        } catch (IOException e) {
            System.err.println("Datagram Channel I/O exception: " + e.getMessage());
        }
    }

    /**
     * Specific implementation details to stop server and deallocate all resources.
     */
    @Override
    protected void closeServer() {
        if (server != null) {
            server.interrupt();
            while (true) {
                try {
                    server.join();
                    break;
                } catch (InterruptedException e) {
                    System.err.println("Server thread interrupted exception: " + e.getMessage());
                }
            }
        }

        if (datagramChannel != null) {
            try {
                datagramChannel.close();
            } catch (IOException e) {
                System.err.println("Datagram Channel I/O exception: " + e.getMessage());
            }
        }

        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                System.err.println("Selector I/O exception: " + e.getMessage());
            }
        }
    }

    private static class Context {
        private final Queue<Pair> requestsIn;
        private final Queue<Pair> requestsOut;

        public Context(int threads, int bufferSize) {
            requestsIn = new ArrayDeque<>(threads);
            IntStream.range(0, threads).forEach(i -> requestsIn.add(new Pair(ByteBuffer.allocate(bufferSize))));
            requestsOut = new ArrayDeque<>(threads);
        }

        private static class Pair {
            private SocketAddress socketAddress = null;
            private final ByteBuffer byteBuffer;

            public Pair(ByteBuffer byteBuffer) {
                this.byteBuffer = byteBuffer;
            }
        }
    }
}
