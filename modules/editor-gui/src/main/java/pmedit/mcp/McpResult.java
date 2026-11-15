package pmedit.mcp;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds MCP tool results. Tool execution failures are reported inside the
 * result with {@code isError=true} so the model can read the message.
 */
public final class McpResult {

    private McpResult() {
    }

    public static ObjectNode success(ObjectNode structured) {
        return success(McpJson.pretty(structured), structured);
    }

    public static ObjectNode success(String text, ObjectNode structured) {
        ObjectNode result = McpJson.object();
        result.set("content", textContent(text));
        if (structured != null) {
            result.set("structuredContent", structured);
        }
        result.put("isError", false);
        return result;
    }

    public static ObjectNode batch(McpBatchResult batch) {
        ObjectNode result = McpJson.object();
        result.set("content", textContent(batch.summaryText()));
        result.set("structuredContent", batch.toNode());
        result.put("isError", batch.isError());
        return result;
    }

    public static ObjectNode error(String message) {
        ObjectNode result = McpJson.object();
        ObjectNode error = McpJson.object();
        error.put("error", message == null ? "Unknown error" : message);
        result.set("content", textContent(McpJson.write(error)));
        result.put("isError", true);
        return result;
    }

    public static ObjectNode error(String code, String message) {
        ObjectNode result = McpJson.object();
        ObjectNode error = McpJson.object();
        error.put("error", code == null ? "tool_error" : code);
        error.put("message", message == null ? "" : message);
        result.set("content", textContent(McpJson.write(error)));
        result.put("isError", true);
        return result;
    }

    private static ArrayNode textContent(String text) {
        ArrayNode content = McpJson.array();
        ObjectNode item = McpJson.object();
        item.put("type", "text");
        item.put("text", text == null ? "" : text);
        content.add(item);
        return content;
    }
}
