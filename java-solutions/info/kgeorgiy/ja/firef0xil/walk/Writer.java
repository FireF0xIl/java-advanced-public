package info.kgeorgiy.ja.firef0xil.walk;

import java.io.BufferedWriter;
import java.io.IOException;

import static info.kgeorgiy.ja.firef0xil.walk.HashImplementation.ZERO_HASH;

public class Writer {
    private final BufferedWriter writer;

    public Writer(BufferedWriter writer) {
        this.writer = writer;
    }

    public void writeHash(String file, String hash) throws IOException {
        writer.write(hash + " " + file);
        writer.newLine();
    }

    public void writeZeroHash(String file) throws IOException {
        writeHash(file, ZERO_HASH);
    }
}
