package pmedit.mcp;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pmedit.ext.CommandResult;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpLicenseToolTest {

    @TempDir
    Path tempDir;

    @Test
    public void statusMapsToShowLicense() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.success("Licensed to: Jane"));

        ObjectNode result = tools(runner).call("pdf_license", McpJson.object());

        assertEquals("showLicense", runner.command);
        assertEquals("", runner.argument);
        assertFalse(result.get("isError").asBoolean());
        assertTrue(result.get("structuredContent").get("ok").asBoolean());
        assertEquals("Licensed to: Jane", result.get("structuredContent").get("message").asText());
    }

    @Test
    public void statusWithoutLicenseIsNotAnError() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.failure("No license installed."));

        ObjectNode result = tools(runner).call("pdf_license", McpJson.object().put("action", "status"));

        assertFalse(result.get("isError").asBoolean());
        assertFalse(result.get("structuredContent").get("ok").asBoolean());
        assertEquals("No license installed.", result.get("structuredContent").get("message").asText());
    }

    @Test
    public void activatePassesTheLicenseUnchanged() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.success("Installed license."));

        ObjectNode args = McpJson.object()
                .put("action", "activate")
                .put("license", "jane@example.com,KEY-123");
        ObjectNode result = tools(runner).call("pdf_license", args);

        assertEquals("license", runner.command);
        assertEquals("jane@example.com,KEY-123", runner.argument);
        assertFalse(result.get("isError").asBoolean());
        assertTrue(result.get("structuredContent").get("ok").asBoolean());
    }

    @Test
    public void activateRequiresTheLicenseArgument() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.success("unused"));

        ObjectNode result = tools(runner).call("pdf_license", McpJson.object().put("action", "activate"));

        assertTrue(result.get("isError").asBoolean());
        assertNull(runner.command);
        assertTrue(result.get("content").toString().contains("invalid_argument"));
    }

    @Test
    public void activateRequiresAComma() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.success("unused"));

        ObjectNode args = McpJson.object().put("action", "activate").put("license", "jane@example.com");
        ObjectNode result = tools(runner).call("pdf_license", args);

        assertTrue(result.get("isError").asBoolean());
        assertNull(runner.command);
        assertTrue(result.get("content").toString().contains("comma"));
    }

    @Test
    public void releaseMapsToReleaseLicense() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.success("Released license for : Jane"));

        ObjectNode result = tools(runner).call("pdf_license", McpJson.object().put("action", "release"));

        assertEquals("releaseLicense", runner.command);
        assertEquals("", runner.argument);
        assertFalse(result.get("isError").asBoolean());
    }

    @Test
    public void failedActivationIsReportedAsAnError() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.failure("Invalid license: expired"));

        ObjectNode args = McpJson.object().put("action", "activate").put("license", "a@b.c,KEY");
        ObjectNode result = tools(runner).call("pdf_license", args);

        assertTrue(result.get("isError").asBoolean());
        assertFalse(result.get("structuredContent").get("ok").asBoolean());
        assertEquals("Invalid license: expired", result.get("structuredContent").get("message").asText());
    }

    @Test
    public void unknownActionIsRejected() throws Exception {
        RecordingRunner runner = new RecordingRunner(CommandResult.success("unused"));

        ObjectNode result = tools(runner).call("pdf_license", McpJson.object().put("action", "renew"));

        assertTrue(result.get("isError").asBoolean());
        assertNull(runner.command);
        assertTrue(result.get("content").toString().contains("Unknown license action"));
    }

    @Test
    public void licenseToolIsListedWithoutPaths() throws Exception {
        ObjectNode list = tools(new RecordingRunner(CommandResult.success("unused"))).listResult();

        ObjectNode definition = null;
        for (var tool : (ArrayNode) list.get("tools")) {
            if ("pdf_license".equals(tool.get("name").asText())) {
                definition = (ObjectNode) tool;
            }
        }

        assertTrue(definition != null, "pdf_license should be listed");
        ObjectNode schema = (ObjectNode) definition.get("inputSchema");
        assertTrue(schema.get("properties").has("action"));
        assertTrue(schema.get("properties").has("license"));
        assertFalse(schema.get("required").toString().contains("paths"));
        assertTrue(definition.get("description").asText().contains("comma-separated"));
    }

    private McpTools tools(CommandRunner runner) throws Exception {
        Path root = tempDir.toRealPath();
        McpMetadata metadata = new McpMetadata(
                McpPaths.forRoot(root),
                new McpOptions(root, false, () -> false),
                runner);
        return new McpTools(metadata);
    }

    private static final class RecordingRunner implements CommandRunner {

        private final CommandResult result;
        private String command;
        private String argument;

        private RecordingRunner(CommandResult result) {
            this.result = result;
        }

        @Override
        public CommandResult execute(String command, String argument) {
            this.command = command;
            this.argument = argument;
            return result;
        }
    }
}
