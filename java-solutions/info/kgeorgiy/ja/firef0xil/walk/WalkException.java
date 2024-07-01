package info.kgeorgiy.ja.firef0xil.walk;

public class WalkException extends Exception {
    WalkException(final String message) {
        super(message);
    }

    WalkException(final String message, Exception e) {
        super(message, e);
    }
}
