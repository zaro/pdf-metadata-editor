package pmedit.mcp;

/**
 * An expected failure while executing an MCP tool. The message is returned to
 * the client as a tool error (isError=true), not as a JSON-RPC protocol error.
 */
public class McpToolException extends Exception {

    public McpToolException(String message) {
        super(message);
    }

    public McpToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
