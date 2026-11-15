package pmedit;

/**
 * An expected batch failure with a machine-readable code.
 *
 * <p>Codes are shared between the CLI batch engine and the MCP adapter so a
 * single operation implementation can report structured results.</p>
 */
public class BatchException extends Exception {

    private final String code;

    public BatchException(String code, String message) {
        this(code, message, null);
    }

    public BatchException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code == null ? "tool_error" : code;
    }

    public String code() {
        return code;
    }
}
