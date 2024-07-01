package info.kgeorgiy.ja.firef0xil.walk;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

public class HashImplementation {
    public final static String ZERO_HASH = "0".repeat(40);

    private final int bufferLength = 1024;
    private final byte[] buffer = new byte[bufferLength];
    private final MessageDigest messageDigest;
    private final HexFormat formatter = HexFormat.of();

    public HashImplementation(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    public String hash(String file) {
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            int read = input.read(buffer, 0, bufferLength);
            while (read > -1) {
                messageDigest.update(buffer, 0, read);
                read = input.read(buffer, 0, bufferLength);
            }
            return formatter.formatHex(messageDigest.digest());
        } catch (IOException | SecurityException e) {
            messageDigest.reset();
            return ZERO_HASH;
        }
    }
}
