package info.kgeorgiy.ja.firef0xil.bank;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Test runner for package bank.
 * @author FireF0xIl
 */
public class BankTests {
    public static void main(String[] args) {
        final Result result = new JUnitCore().run(Tests.class);
        if (result.wasSuccessful()) {
            System.exit(0);
            return;
        }
        result.getFailures().forEach(failure -> {
            System.err.println("Test " + failure.getDescription().getMethodName() + " failed: " + failure.getMessage());
            if (failure.getException() != null) {
                failure.getException().printStackTrace();
            }
        });
        System.exit(1);
        throw new AssertionError("Exit");
    }
}
