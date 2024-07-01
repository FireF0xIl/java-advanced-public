package info.kgeorgiy.ja.firef0xil.bank;
/**
 * Additional static functions bank package.
 * @author FireF0xIl
 */
public class StaticFunctions {

    /**
     * Check if given id is non-null or empty
     *
     * @param checkId specified id to check
     * @return {@code true} if id is correct anf {@code false} otherwise
     */
    public static boolean checkString(String checkId) {
        return checkId != null && !checkId.isEmpty();
    }

    /**
     * Create an account full {@link String} id representation.
     *
     * @param personId specified person's id
     * @param accountId account's id
     * @return account full {@link String} id representation.
     */
    public static String getAccountId(String personId, String accountId) {
        return personId + ":" + accountId;
    }

}
