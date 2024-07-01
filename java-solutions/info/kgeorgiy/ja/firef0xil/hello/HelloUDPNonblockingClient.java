package info.kgeorgiy.ja.firef0xil.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static info.kgeorgiy.ja.firef0xil.hello.StaticFunctions.CHARSET;

/**
 * Non-blocking implementation for interface {@link HelloClient}.
 * @author FireF0xIl
 */
public class HelloUDPNonblockingClient extends HelloUDPClientAbstract {

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
        try {
            List<DatagramChannel> datagramChannels = new ArrayList<>(threads);
            for (int i = 0; i < threads; i++) {
                DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannels.add(datagramChannel);
            }

            try (Selector selector = Selector.open()) {
                SocketAddress socketAddress = new InetSocketAddress(host, port);

                for (int i = 0; i < threads; i++) {
                    DatagramChannel datagramChannel = datagramChannels.get(i);
                    datagramChannel.register(selector, SelectionKey.OP_WRITE,
                            new Context(i, datagramChannel.socket().getReceiveBufferSize()));
                    datagramChannel.connect(socketAddress);
                }

                while (!Thread.currentThread().isInterrupted() && !selector.keys().isEmpty()) {
                    try {
                        if (selector.select(TIMEOUT) == 0) {
                            for (SelectionKey selectionKey : selector.keys()) {
                                if (selectionKey.isWritable()) {
                                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                                }
                            }
                        } else {
                            for (Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                                 selectionKeyIterator.hasNext(); ) {

                                SelectionKey selectionKey = selectionKeyIterator.next();

                                if (selectionKey.isWritable()) {
                                    Context context = (Context) selectionKey.attachment();
                                    ((DatagramChannel) selectionKey.channel()).send(
                                            context.byteBuffer
                                                    .clear()
                                                    .put(requestCreation(
                                                            prefix,
                                                            context.thread,
                                                            context.currentRequest).getBytes(CHARSET))
                                                    .flip(),
                                            socketAddress
                                    );
                                    selectionKey.interestOps(SelectionKey.OP_READ);
                                }

                                if (selectionKey.isReadable()) {
                                    DatagramChannel datagramChannel = (DatagramChannel) selectionKey.channel();
                                    Context context = (Context) selectionKey.attachment();
                                    datagramChannel.receive(context.byteBuffer.clear());

                                    String actual = CHARSET.decode(context.byteBuffer.flip()).toString();
                                    if (isValid(actual,
                                            Integer.toString(context.thread),
                                            Integer.toString(context.currentRequest))) {
                                        System.out.println("Response successful: " + actual);
                                        context.increment();
                                    }

                                    if (context.currentRequest < requests) {
                                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                                    } else {
                                        datagramChannel.close();
                                        selectionKey.cancel();
                                    }
                                }

                                selectionKeyIterator.remove();
                            }
                        }
                    } catch (IOException e){
                        System.err.println("I/O exception during processing requests: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("I/O exception: " + e.getMessage());
            } finally {
                for (DatagramChannel datagramChannel : datagramChannels) {
                    try {
                        datagramChannel.close();
                    } catch (IOException e) {
                        System.err.println("I/O exception during DatagramChannel close: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("I/O exception for channels creation: " + e.getMessage());
        }
    }

    private static class Context {
        private final int thread;
        private final ByteBuffer byteBuffer;
        private int currentRequest = 0;

        public Context(int thread, int bufferSize) {
            this.thread = thread;
            byteBuffer = ByteBuffer.allocate(bufferSize);
        }

        public void increment() {
            currentRequest++;
        }
    }
}
