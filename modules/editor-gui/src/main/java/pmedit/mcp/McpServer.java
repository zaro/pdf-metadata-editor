package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Newline-delimited JSON-RPC 2.0 server for the Model Context Protocol.
 *
 * <p>stdin carries requests and notifications; stdout carries responses only.
 * Every response is flushed immediately. Logging and diagnostics must never be
 * written to stdout, otherwise the client stream is corrupted.</p>
 */
public final class McpServer {

    private static final Logger LOG = LoggerFactory.getLogger(McpServer.class);
    private static final int MAX_FRAME_LENGTH = 16 * 1024 * 1024;

    private static final String INSTRUCTIONS =
            "Tools operate on PDF files inside the configured root. "
                    + "Call pdf_list_fields before writing to discover valid field IDs. "
                    + "Mutating tools require a Pro license.";

    private final BufferedReader reader;
    private final PrintStream out;
    private final McpTools tools;

    private State state = State.NEW;

    public McpServer(InputStream in, OutputStream out, McpOptions options) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = new PrintStream(out, false, StandardCharsets.UTF_8);
        McpPaths paths = McpPaths.forRoot(options.root());
        this.tools = new McpTools(new McpMetadata(paths, options));
    }

    public void run() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (line.length() > MAX_FRAME_LENGTH) {
                    sendError(null, McpProtocol.PARSE_ERROR, "Message exceeds the maximum allowed size");
                    continue;
                }
                handleLine(line);
            }
        } catch (IOException e) {
            LOG.error("MCP server I/O error", e);
        }
    }

    private void handleLine(String line) {
        JsonNode request;
        try {
            request = McpJson.parse(line);
        } catch (Exception e) {
            sendError(null, McpProtocol.PARSE_ERROR, "Invalid JSON: " + safeMessage(e));
            return;
        }

        if (request == null || !request.isObject()) {
            sendError(null, McpProtocol.INVALID_REQUEST, "Invalid JSON-RPC request");
            return;
        }

        boolean notification = !request.has("id");
        JsonNode id = request.get("id");

        JsonNode jsonrpc = request.get("jsonrpc");
        JsonNode methodNode = request.get("method");
        if (jsonrpc == null || !jsonrpc.isTextual() || !McpProtocol.JSONRPC.equals(jsonrpc.asText())
                || methodNode == null || !methodNode.isTextual()) {
            if (!notification) {
                sendError(id, McpProtocol.INVALID_REQUEST, "Invalid JSON-RPC request");
            }
            return;
        }

        String method = methodNode.asText();
        if (notification) {
            handleNotification(method);
            return;
        }

        try {
            if (state == State.NEW && !"initialize".equals(method) && !"ping".equals(method)) {
                sendError(id, McpProtocol.SERVER_NOT_INITIALIZED, "Server not initialized");
                return;
            }

            switch (method) {
                case "initialize" -> handleInitialize(id, request);
                case "ping" -> sendResult(id, McpJson.object());
                case "tools/list" -> sendResult(id, tools.listResult());
                case "tools/call" -> handleCall(id, request);
                default -> sendError(id, McpProtocol.METHOD_NOT_FOUND, "Method not found: " + method);
            }
        } catch (McpTools.UnknownToolException e) {
            sendError(id, McpProtocol.INVALID_PARAMS, e.getMessage());
        } catch (Exception e) {
            LOG.error("MCP request failed: {}", method, e);
            sendError(id, McpProtocol.INTERNAL_ERROR, "Internal server error");
        }
    }

    private void handleNotification(String method) {
        if ("notifications/initialized".equals(method)) {
            state = State.INITIALIZED;
        }
        // Other notifications (cancelled, roots/list_changed, ...) are ignored.
    }

    private void handleInitialize(JsonNode id, JsonNode request) {
        JsonNode params = request.get("params");
        if (params != null && !params.isNull() && !params.isObject()) {
            sendError(id, McpProtocol.INVALID_PARAMS, "initialize params must be an object");
            return;
        }

        String requested = params != null && params.isObject()
                ? params.path("protocolVersion").asText(null)
                : null;
        String negotiated = McpProtocol.negotiateVersion(requested);

        ObjectNode result = McpJson.object();
        result.put("protocolVersion", negotiated);

        ObjectNode capabilities = McpJson.object();
        ObjectNode toolsCapability = McpJson.object();
        toolsCapability.put("listChanged", false);
        capabilities.set("tools", toolsCapability);
        result.set("capabilities", capabilities);

        ObjectNode serverInfo = McpJson.object();
        serverInfo.put("name", "pdf-metadata-editor");
        serverInfo.put("version", Version.get().getAsString());
        result.set("serverInfo", serverInfo);

        result.put("instructions", INSTRUCTIONS);

        state = State.INITIALIZED;
        sendResult(id, result);
    }

    private void handleCall(JsonNode id, JsonNode request) throws McpTools.UnknownToolException {
        JsonNode params = request.get("params");
        if (params == null || !params.isObject()) {
            sendError(id, McpProtocol.INVALID_PARAMS, "tools/call params must be an object");
            return;
        }

        JsonNode nameNode = params.get("name");
        if (nameNode == null || !nameNode.isTextual()) {
            sendError(id, McpProtocol.INVALID_PARAMS, "Missing tools/call name");
            return;
        }

        JsonNode argumentsNode = params.get("arguments");
        if (argumentsNode != null && !argumentsNode.isNull() && !argumentsNode.isObject()) {
            sendError(id, McpProtocol.INVALID_PARAMS, "tools/call arguments must be an object");
            return;
        }

        ObjectNode arguments = argumentsNode != null && argumentsNode.isObject()
                ? (ObjectNode) argumentsNode
                : McpJson.object();

        sendResult(id, tools.call(nameNode.asText(), arguments));
    }

    private void sendResult(JsonNode id, ObjectNode result) {
        ObjectNode response = McpJson.object();
        response.put("jsonrpc", McpProtocol.JSONRPC);
        response.set("id", id == null ? NullNode.getInstance() : id);
        response.set("result", result);
        send(response);
    }

    private void sendError(JsonNode id, int code, String message) {
        ObjectNode response = McpJson.object();
        response.put("jsonrpc", McpProtocol.JSONRPC);
        response.set("id", id == null ? NullNode.getInstance() : id);

        ObjectNode error = McpJson.object();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);

        send(response);
    }

    private void send(ObjectNode message) {
        out.print(McpJson.write(message));
        out.print('\n');
        out.flush();
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private enum State {
        NEW,
        INITIALIZED
    }
}
