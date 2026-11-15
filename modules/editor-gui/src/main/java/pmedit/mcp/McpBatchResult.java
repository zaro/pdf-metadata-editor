package pmedit.mcp;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects per-file results for a batch MCP tool call.
 *
 * <p>The tool result envelope is the same for every batch tool: a summary plus
 * one entry per input file. A partial failure is still a successful tool call;
 * {@link #isError()} is only true when every file failed.</p>
 */
public final class McpBatchResult {

    private final ArrayNode results = McpJson.array();
    private final Map<String, Integer> failureCodes = new LinkedHashMap<>();
    private int succeeded;
    private int failed;

    public void addReadSuccess(Path file, ObjectNode payload, McpPaths paths) {
        ObjectNode result = base(paths.relative(file));
        result.put("ok", true);
        result.set("file", payload.get("file"));
        result.set("fields", payload.get("fields"));
        if (payload.get("warnings") != null) {
            result.set("warnings", payload.get("warnings"));
        }
        results.add(result);
        succeeded++;
    }

    public void addMutationSuccess(
            Path input,
            Path output,
            McpPaths paths,
            String action,
            List<String> changed,
            List<String> warnings) {
        ObjectNode result = base(paths.relative(input));
        if (output != null) {
            result.put("output", paths.relative(output));
        }
        result.put("ok", true);
        result.put("action", action);
        if (changed != null) {
            ArrayNode values = McpJson.array();
            for (String value : changed) {
                values.add(value);
            }
            result.set("fields", values);
        }
        if (warnings != null && !warnings.isEmpty()) {
            ArrayNode warningNodes = McpJson.array();
            for (String warning : warnings) {
                warningNodes.add(warning);
            }
            result.set("warnings", warningNodes);
        }
        results.add(result);
        succeeded++;
    }

    public void addError(Path input, McpPaths paths, Throwable error) {
        addError(paths.relative(input), error);
    }

    public void addError(String input, Throwable error) {
        String code = "tool_error";
        String message;
        if (error instanceof McpToolException toolError) {
            if (toolError.code() != null) {
                code = toolError.code();
            }
            message = toolError.getMessage();
        } else {
            message = error == null ? "Unknown error" : error.getMessage();
        }
        addError(input, code, message);
    }

    public void addError(String input, String code, String message) {
        ObjectNode result = base(input);
        result.put("ok", false);

        ObjectNode errorNode = McpJson.object();
        errorNode.put("code", code == null ? "tool_error" : code);
        errorNode.put("message", message == null || message.isBlank() ? "Unknown error" : message);
        result.set("error", errorNode);
        results.add(result);
        failed++;
        failureCodes.merge(errorNode.get("code").asText(), 1, Integer::sum);
    }

    public boolean isError() {
        return succeeded == 0 && failed > 0;
    }

    public String summaryText() {
        int total = succeeded + failed;
        if (total == 0) {
            return "No files were processed.";
        }
        StringBuilder summary = new StringBuilder()
                .append("Processed ").append(total).append(" file(s): ")
                .append(succeeded).append(" succeeded, ").append(failed).append(" failed");
        if (!failureCodes.isEmpty()) {
            summary.append(" (");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : failureCodes.entrySet()) {
                if (!first) {
                    summary.append(", ");
                }
                summary.append(entry.getKey()).append(": ").append(entry.getValue());
                first = false;
            }
            summary.append(')');
        }
        return summary.append('.').toString();
    }

    public ObjectNode toNode() {
        ObjectNode node = McpJson.object();

        ObjectNode summary = McpJson.object();
        summary.put("total", succeeded + failed);
        summary.put("succeeded", succeeded);
        summary.put("failed", failed);
        summary.put("truncated", false);
        node.set("summary", summary);

        node.set("results", results);
        return node;
    }

    private static ObjectNode base(String input) {
        ObjectNode result = McpJson.object();
        result.put("input", input == null ? "" : input);
        return result;
    }
}
