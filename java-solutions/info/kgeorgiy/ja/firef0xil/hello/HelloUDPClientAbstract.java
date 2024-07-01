package info.kgeorgiy.ja.firef0xil.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base function implementation for interface {@link HelloClient}.
 * @author FireF0xIl
 */
public abstract class HelloUDPClientAbstract implements HelloClient {
    private final static Pattern RESPONSE_TEMPLATE = Pattern.compile("\\D*(\\d+)\\D*(\\d+)\\D*");
    /** Timeout for waiting a response */
    protected final static int TIMEOUT = 50;

    /**
     * Build a request {@link String} for server.
     *
     * @param prefix    common prefix
     * @param threadID  thread number
     * @param requestID current request number
     * @return built string
     */
    protected static String requestCreation(String prefix, int threadID, int requestID) {
        return prefix + threadID + "_" + requestID;
    }

    /**
     * Check given String from server respond.
     *
     * @param toCheck   actual respond
     * @param threadId  thread number
     * @param requestId current request number
     * @return {@code true} if respond match a pattern {@code *<Number>*<Number>*} and {@code false} otherwise
     */
    protected static boolean isValid(final String toCheck, final String threadId, final String requestId) {
        final Matcher checked = RESPONSE_TEMPLATE.matcher(toCheck);
        if (checked.find()) {
            return threadId.equals(checked.group(1)) && requestId.equals(checked.group(2));
        } else {
            return false;
        }
    }
}
