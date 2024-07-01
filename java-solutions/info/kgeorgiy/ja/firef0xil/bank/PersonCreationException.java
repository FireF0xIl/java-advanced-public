package info.kgeorgiy.ja.firef0xil.bank;

/**
 * {@link RuntimeException} for bad arguments' creation.
 * @author FireF0xIl
 */
public class PersonCreationException extends RuntimeException {

    /**
     * Construct a new exception instance.
     * @param message error message
     */
    public PersonCreationException(String message) {
        super(message);
    }
}
