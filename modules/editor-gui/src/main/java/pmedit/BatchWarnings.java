package pmedit;

/**
 * Warnings produced by batch operations.
 */
public final class BatchWarnings {

    private BatchWarnings() {
    }

    public static String unsupportedFields() {
        return "The active extension may not support some requested fields "
                + "(encryption, permissions, rights, or file properties). A license may be required.";
    }
}
