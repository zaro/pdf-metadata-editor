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

    public static ObjectNode error(String message) {
        ObjectNode result = McpJson.object();
        result.set("content", textContent("Error: " + message));
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
