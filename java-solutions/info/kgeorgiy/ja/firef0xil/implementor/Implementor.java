package info.kgeorgiy.ja.firef0xil.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation for interfaces {@link JarImpler} and {@link Impler}.
 * @author FireF0xIl
 */
public class Implementor implements JarImpler, Impler {
    /**
     * Represent {@link String} value of the generating class
     */
    private String classNameImpl;

    /**
     * Represent {@link String} value of the current OS line separator
     */
    private final String EOL = System.lineSeparator();


    /**
     * Implementation of {@link SimpleFileVisitor} for files deletion
     */
    private final static SimpleFileVisitor<Path> DELETER = new SimpleFileVisitor<>() {
        /**
         * Remove specified file in a directory.
         *
         * @param file file to delete
         * @param attrs attributes
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if an I/O error occurs
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Remove specified directory.
         *
         * @param dir a reference to the directory
         * @param exc {@code null} if the iteration of the directory completes without
         * an error; otherwise the I/O exception that caused the iteration
         * of the directory to complete prematurely
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if an I/O error occurs
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Generate class header include package, class name and parent source to be implemented.
     *
     * @param token provided instance of {@link Class} for generating
     * @return {@link String} represent of class header
     */
    private String getHeader(Class<?> token) {
        String pack = token.getPackageName();
        StringBuilder stringBuilder = new StringBuilder();
        if (!pack.isEmpty()) {
            stringBuilder.append("package ").append(pack).append(";").append(EOL);
        }
        stringBuilder
                .append("class ")
                .append(classNameImpl)
                .append(" ")
                .append(token.isInterface() ? "implements " : "extends ")
                .append(token.getCanonicalName())
                .append(" {")
                .append(EOL);
        return stringBuilder.toString();
    }

    /**
     * Remove unnecessary modifiers from given {@code Executable}.
     *
     * @param executable instance of {@link Executable}
     * @return {@link String} all modifiers of {@code Executable}
     * except {@code Modifier.TRANSIENT} and {@code Modifier.ABSTRACT}
     */
    private String getVisibility(Executable executable) {
        int mod = executable.getModifiers() & ~(Modifier.TRANSIENT | Modifier.ABSTRACT);
        return mod != 0 ? Modifier.toString(mod) + " " : "";
    }

    /**
     * Generate {@link String} of parameters for {@link Executable}.
     *
     * @param executable instance of {@link Executable}
     * @param type if {@code type == true} generate names with specified type parameter otherwise only names
     * @return {@link String} representation of all params of {@link Executable#getParameters()}
     */
    private String genParams(Executable executable, boolean type) {
        Parameter[] params = executable.getParameters();
        StringJoiner stringJoiner = new StringJoiner(", ");
        Arrays.stream(params)
                .forEach(param -> stringJoiner.add(
                        (type ? param.getType().getCanonicalName() + " " : "") + param.getName()));
        return stringJoiner.toString();
    }

    /**
     * Generate specified constructor.
     *
     * @param constructor provided instance of {@link Constructor} for generating
     * @return {@link String} representation of {@link Constructor}
     */
    private String generateConstructor(Constructor<?> constructor) {
        return getVisibility(constructor) +
                classNameImpl +
                "(" +
                genParams(constructor, true) +
                ") " +
                getExceptions(constructor) +
                "{ super(" +
                genParams(constructor, false) +
                "); }" + EOL;
    }

    /**
     * Generate {@link String} representation of specified executable.
     *
     * @param constructor provided instance of {@link Constructor} for generating
     * @return {@link String} representation of {@link Constructor}
     */
    private String getExceptions(Constructor<?> constructor) {
        Class<?>[] list = constructor.getExceptionTypes();
        if (list.length == 0) {
            return "";
        } else {
            StringJoiner stringJoiner = new StringJoiner(", ");
            Arrays.stream(list).forEach(exception -> stringJoiner.add(exception.getCanonicalName()));
            return "throws " + stringJoiner;
        }
    }

    /**
     * Generate {@link String} representation of all inherited non-private constructors.
     *
     * @param token provided instance of {@link Class} for generating
     * @return {@link String} representation of all non-private {@link Constructor}
     * @throws ImplerException if there are only private constructors
     */
    private String getConstructors(Class<?> token) throws ImplerException {
        if (token.isInterface()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .forEach(constructor -> stringBuilder.append(generateConstructor(constructor)));
        if (stringBuilder.isEmpty()) {
            throw new ImplerException("Only private constructors found");
        }
        return stringBuilder.toString();
    }

    /**
     * Return default value suitable for specified token.
     *
     * @param token provided instance of {@link Method} for generating
     * @return {@link String} representation of default value
     */
    private static String getDefaultValue(Method token) {
        Class<?> ret = token.getReturnType();
        if (ret.equals(void.class)) {
            return "";
        } else if (ret.equals(boolean.class)) {
            return " false";
        } else if (ret.isPrimitive()) {
            return " 0";
        } else {
            return " null";
        }

    }

    /**
     * Generate {@link String} representation of specified method.
     *
     * @param token provided instance of {@link Method} for generating
     * @return {@link String} representation of specified method
     */
    private String genMethod(Method token) {
        return getVisibility(token) +
                token.getReturnType().getCanonicalName() +
                " " +
                token.getName() +
                " (" +
                genParams(token, true) +
                ") { return" +
                getDefaultValue(token) +
                "; }" + EOL;
    }

    /**
     * Generate {@link String} representation of all inherited non-private methods of specified token.
     *
     * @param token provided instance of {@link Class} for generating
     * @return {@link String} representation of all inherited non-private methods
     */
    private String getMethods(Class<?> token) {
        Class<?> cur = token;
        HashSet<MethodContainer> setAbstract = new HashSet<>();
        HashSet<MethodContainer> setFinal = new HashSet<>();
        Consumer<Method> methodConsumer = method -> {
            if (Modifier.isFinal(method.getModifiers())) {
                setFinal.add(new MethodContainer(method));
            } else if (Modifier.isAbstract(method.getModifiers())) {
                setAbstract.add(new MethodContainer(method));
            }
        };
        while (cur != null) {
            Arrays.stream(cur.getMethods()).forEach(methodConsumer);
            Arrays.stream(cur.getDeclaredMethods()).forEach(methodConsumer);
            cur = cur.getSuperclass();
        }
        StringBuilder stringBuilder = new StringBuilder();
        setAbstract
                .stream()
                .filter(Predicate.not(setFinal::contains))
                .forEach(method -> stringBuilder.append(genMethod(method.method)));
        return stringBuilder.toString();
    }

    /**
     * Container for {@link Method} with specified
     *
     * {@link MethodContainer#hashCode()} and {@link MethodContainer#equals(Object)}.
     */
    private static class MethodContainer {
        /**
         * Storage instance of {@link Method}
         */
        private final Method method;

        /**
         * Constructs new {@link MethodContainer} with specified {@code method}
         *
         * @param method instance of {@link Method}
         */
        public MethodContainer(Method method) {
            this.method = method;
        }

        /**
         * Returns a hashcode for this {@link MethodContainer}.
         * The hashcode is computed as the {@link Objects#hash(Object...)} of the hashcodes.
         *
         * @return {@code int} representation of hash
         */
        @Override
        public int hashCode() {
            return Objects.hash(method.getName(), method.getReturnType(), Arrays.hashCode(method.getParameterTypes()));
        }

        /**
         * Compares this {@link MethodContainer} against the specified object.
         * Returns {@code true} if the objects are the same. Two {@link MethodContainer} are the same if
         * they have the same name, formal parameter types and return type.
         *
         * @return {@code true} if two objects equals {@code false} otherwise
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MethodContainer) {
                Method tmp = ((MethodContainer) obj).method;
                return method.getName().equals(tmp.getName()) &&
                        method.getReturnType().equals(tmp.getReturnType()) &&
                        Arrays.equals(method.getParameterTypes(), tmp.getParameterTypes());
            } else {
                return false;
            }
        }
    }

    /**
     * Generate jar-file for specified java.class
     *
     * @param token token provided instance of {@link Class}
     * @param tempDir temporary directory of generated .class and .java
     * @param jar target <var>.jar</var> file.
     * @throws ImplerException when jar-file cannot be generated.
     */
    private void generateJar(Class<?> token, Path tempDir, Path jar) throws ImplerException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "FireF0xIl");
        try (JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            outputStream.putNextEntry(new ZipEntry(getClassNameImpl(token, "/")));
            Files.copy(tempDir.resolve(getClassNameImpl(token, File.separator)), outputStream);
        } catch (IOException e) {
            throw new ImplerException("I/O exception while making jar", e);
        }
    }

    /**
     * Convert java-style package to {@code String} with specified {@code separator}
     *
     * @param token token provided instance of {@link Class}
     * @param separator specified {@code separator} for generating java-class
     * @return {@link String}
     */
    private String getClassNameImpl(Class<?> token, String separator) {
        return getImplName(token).replace(".", separator) + ".class";
    }

    /**
     * Extract {@code path} to specified {@code token}.
     *
     * @param token provided instance of {@link Class}
     * @return {@link String} representation of {@code token}
     * @throws ImplerException if can't resolve path to generated .class
     */
    private String getClassPath(Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException("Can't resolve path to generated .class", e);
        }
    }

    /**
     * Extract {@code absolute path} to specified {@code token}
     *
     * @param root root directory provided for {@code token}
     * @param token provided instance of {@link Class}
     * @return {@link Path} representation of {@code absolute path} to specified java source-file
     */
    private Path getFile(final Path root, final Class<?> token) {
        return root.resolve(getImplName(token).replace(".", File.separator) + ".java").toAbsolutePath();
    }

    /**
     * Extract {@code path} to specified {@code token}
     *
     * @param token provided instance of {@link Class}
     * @return {@link String} representation of full specified {@code className with package}
     */
    private String getImplName(final Class<?> token) {
        return token.getPackageName() + "." + classNameImpl;
    }

    /**
     * Compile java source-file to {@code java.class}.
     *
     * @param root root directory provided for {@code token}
     * @param token provided instance of {@link Class}
     * @throws ImplerException when implementation cannot be compiled.
     */
    private void compile(final Class<?> token, final Path root) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("No available compilers provided by current System");
        }
        final String file = getFile(root, token).toString();
        final String classpath = root + File.pathSeparator + getClassPath(token);
        final String[] argument = {file, "-cp", classpath};
        if (compiler.run(null, null, null, argument) != 0) {
            throw new ImplerException("Failed to compile java class");
        }
    }

    /**
     * Implementation for interface method.
     * @see JarImpler#implementJar(Class, Path)
     * @param token type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path rootDir;
        try {
            rootDir = Files.createDirectories(jarFile.getParent());
        } catch (IOException e) {
            throw new ImplerException("Output directory is not available", e);
        }
        Path jarDir;
        try {
            jarDir = Files.createTempDirectory(rootDir, "jar-compile");
        } catch (IOException e) {
            throw new ImplerException("Output directory is not available", e);
        }
        try {
            implement(token, jarDir);
            compile(token, jarDir);
            generateJar(token, jarDir, jarFile);
        } finally {
            if (Files.exists(jarDir)) {
                try {
                    Files.walkFileTree(jarDir, DELETER);
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Implementation for interface method.
     *
     * @see Impler#implement(Class, Path)
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Arguments should be non null");
        }
        int modifiers = token.getModifiers();
        if (token.isPrimitive() || token.isArray() || token.isEnum() || token.equals(Enum.class)
                || Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("Not an available token to implement");
        }
        Path dir = root.resolve(String.join(File.separator, token.getPackageName().split("\\.")));
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new ImplerException("Output directory is not available", e);
        }
        classNameImpl = token.getSimpleName() + "Impl";
        Path file = dir.resolve(classNameImpl + ".java");
        try (BufferedWriter output = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            customWrite(output, getHeader(token));
            customWrite(output, getConstructors(token));
            customWrite(output, getMethods(token));
            customWrite(output, "}");

        } catch (IOException e) {
            throw new ImplerException("I/O exception found", e);
        }
    }

    /**
     * Write UTF-8 chars to file with changing symbols with code >= 128 to {@code \uffff}
     *
     * @param writer specified target to write
     * @param string specified {@link String} to write
     * @throws IOException if an I/O error occurs
     */
    private static void customWrite(BufferedWriter writer, String string) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        for (char c : string.toCharArray()) {
            if (c < 128) {
                writer.write(String.valueOf(c));
            } else {
                writer.write(formatter.format("\\u%04x", (int) c).toString());
                stringBuilder.setLength(0);
            }
        }
    }

    /**
     * Print class usage
     */
    private static void usage() {
        System.err.println("Usage: -jar <class-name> <file.jar> \nor <class-name> <directory>");
    }

    /**
     * Start point of {@link Implementor}.
     *
     * Generate implementation of given {@code class} or {@code interface}. Then compile source code and
     * make new {@code jar-file}
     * @param args Usage: {@code -jar <class-name> <file.jar>} or {@code <class-name> <directory>}
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 3) {
            usage();
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Parameters should be non null");
                return;
            }
        }
        try {
            if (args.length == 3) {
                if (!args[0].equals("-jar")) {
                    usage();
                } else {
                    new Implementor().implementJar(Class.forName(args[1]), Paths.get(args[2]));
                }
            } else {
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        }  catch (ClassNotFoundException e) {
            System.err.println("Unknown class found: " + e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("Invalid path found: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Failed to generate java class");
        }
    }
}
