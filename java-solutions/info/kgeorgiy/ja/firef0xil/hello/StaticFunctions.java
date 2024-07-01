package info.kgeorgiy.ja.firef0xil.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Additional static functions for UDPServer and Client.
 * @author FireF0xIl
 */
public class StaticFunctions {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Receive data from remote source.
     *
     * @param socket specified {@link DatagramSocket} socket to receive.
     * @param datagramPacket specified {@link DatagramPacket} packet to receive.
     * @param buffer {@code byte[]} for buffer to {@code datagramPacket}.
     * @return {@link String} representation of response.
     *
     * @throws IOException if I/O error occurs.
     */
    public static String receive(DatagramSocket socket, DatagramPacket datagramPacket, byte[] buffer) throws IOException {
        datagramPacket.setData(buffer);
        datagramPacket.setLength(buffer.length);
        socket.receive(datagramPacket);
        return new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), CHARSET);
    }

    /**
     * Send data from remote source.
     *
     * @param socket specified {@link DatagramSocket} socket to receive.
     * @param datagramPacket specified {@link DatagramPacket} packet to receive.
     * @param message {@link String} to send via {@code datagramPacket}.
     *
     * @throws IOException if I/O error occurs.
     */
    public static void send(DatagramSocket socket, DatagramPacket datagramPacket, String message) throws IOException {
        datagramPacket.setData(message.getBytes(CHARSET));
        socket.send(datagramPacket);
    }

    /**
     * Stop specified {@link ExecutorService} and deallocates all resources.
     *
     * @param service {@link ExecutorService} to stop.
     * @param await_time specified {@code int} to wait.
     * @param await_unit specified {@link TimeUnit} to wait.
     */
    public static void close(ExecutorService service, int await_time, TimeUnit await_unit) {
        service.shutdown();
        while (true) {
            try {
                if (service.awaitTermination(await_time, await_unit)) {
                    break;
                }
            } catch (InterruptedException ignored) {}
            service.shutdownNow();
        }
    }
}
