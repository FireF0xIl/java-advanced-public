package info.kgeorgiy.ja.firef0xil.walk;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static info.kgeorgiy.ja.firef0xil.walk.HashImplementation.ZERO_HASH;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private final Writer writer;
    private final HashImplementation hashProducer;

    public FileVisitor(Writer writer, HashImplementation hashProducer) {
        this.writer = writer;
        this.hashProducer = hashProducer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return fileProcess(file.toString(), hashProducer.hash(file.toString()));
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return fileProcess(file.toString(), ZERO_HASH);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    private FileVisitResult fileProcess(String file, String hash) throws IOException {
        writer.writeHash(file, hash);
        return FileVisitResult.CONTINUE;
    }
}
