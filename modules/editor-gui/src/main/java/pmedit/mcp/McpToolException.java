package pmedit.mcp;

/**
 * An expected failure while executing an MCP tool. The message is returned to
 * the client as a tool error (isError=true), not as a JSON-RPC protocol error.
 */
public class McpToolException extends Exception {

    private final String code;

    public McpToolException(String message) {
        this(null, message, null);
    }

    public McpToolException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public McpToolException(String code, String message) {
        this(code, message, null);
    }

    public McpToolException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
