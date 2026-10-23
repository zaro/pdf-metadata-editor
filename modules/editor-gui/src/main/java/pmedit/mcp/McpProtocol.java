package pmedit.mcp;

import java.util.List;

/**
 * Protocol constants and version negotiation for the MCP stdio server.
 */
public final class McpProtocol {

    public static final String JSONRPC = "2.0";

    public static final String LATEST_VERSION = "2025-11-25";

    public static final List<String> SUPPORTED_VERSIONS = List.of(
            "2025-11-25",
            "2025-06-18",
            "2025-03-26",
            "2024-11-05"
    );

    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    public static final int SERVER_NOT_INITIALIZED = -32002;

    private McpProtocol() {
    }

    public static String negotiateVersion(String requested) {
        if (requested != null && SUPPORTED_VERSIONS.contains(requested)) {
            return requested;
        }
        return LATEST_VERSION;
    }
}
