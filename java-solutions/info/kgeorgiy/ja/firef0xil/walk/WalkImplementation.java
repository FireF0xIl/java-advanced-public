package info.kgeorgiy.ja.firef0xil.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;

public class WalkImplementation {

    public static void run(String[] args, boolean recursive) {
        if (args == null || args.length != 2) {
            System.err.println("Wrong usage: <path to input file> <path to output file>");
        } else if (args[0] == null) {
            System.err.println("Null argument as input filename");
        } else if (args[1] == null) {
            System.err.println("Null argument as output filename");
        } else {
            try {
                WalkImplementation.walk(args, MessageDigest.getInstance("SHA-1"), recursive);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static Path getPath(String path) throws WalkException {
        try {
            return Path.of(path);
        } catch (InvalidPathException e) {
            throw new WalkException("Path '" + path + "' is invalid", e);
        }
    }

    private static String readLine(BufferedReader reader) throws WalkException {
        try {
            return reader.readLine();
        } catch (IOException e){
            throw new WalkException("Interaction error with input file", e);
        }
    }

    private static void walk(String[] args, MessageDigest messageDigest, boolean recursive) throws WalkException {
        Path inputPath = getPath(args[0]);
        Path outputPath = getPath(args[1]);
        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new WalkException("Output directory is not available", e);
        }
        try (BufferedReader input = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            try (BufferedWriter output = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                Writer writer = new Writer(output);
                String rawFilePath;
                HashImplementation hashProducer = new HashImplementation(messageDigest);
                FileVisitor fileVisitor = new FileVisitor(writer, hashProducer);
                while ((rawFilePath = readLine(input)) != null) {
                    if (recursive) {
                        try {
                            Files.walkFileTree(getPath(rawFilePath), fileVisitor);
                        } catch (WalkException e) {
                            writer.writeZeroHash(rawFilePath);
                        }
                    } else {
                        writer.writeHash(rawFilePath, hashProducer.hash(rawFilePath));
                    }
                }
            } catch (IOException e) {
                System.err.println("Interaction error with output file");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input file not exists");
        } catch (IOException e) {
            System.err.println("Interaction error with input file");
        }
    }
}
