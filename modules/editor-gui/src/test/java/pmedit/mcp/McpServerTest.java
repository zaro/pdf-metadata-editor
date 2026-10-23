package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpServerTest {

    @TempDir
    Path tempDir;

    @Test
    public void initializeReturnsServerInfoAndToolCapability() throws Exception {
        JsonNode response = responses(run(initialize("2025-06-18"))).get(0);

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(1, response.get("id").asInt());
        assertEquals("2025-06-18", response.get("result").get("protocolVersion").asText());
        assertEquals("pdf-metadata-editor", response.get("result").get("serverInfo").get("name").asText());
        assertFalse(response.get("result").get("serverInfo").get("version").asText().isBlank());
        assertTrue(response.get("result").get("capabilities").get("tools").has("listChanged"));
        assertFalse(response.get("result").get("instructions").asText().isBlank());
    }

    @Test
    public void unknownProtocolVersionFallsBackToLatest() throws Exception {
        JsonNode response = responses(run(initialize("1999-01-01"))).get(0);
        assertEquals(McpProtocol.LATEST_VERSION, response.get("result").get("protocolVersion").asText());
    }

    @Test
    public void pingReturnsEmptyResult() throws Exception {
        List<JsonNode> responses = responses(run(
                initialize(McpProtocol.LATEST_VERSION),
                McpJson.write(request("ping"))));
        assertEquals(2, responses.size());
        assertEquals(0, responses.get(1).get("result").size());
    }

    @Test
    public void notificationDoesNotProduceAResponse() throws Exception {
        String output = run(
                initialize(McpProtocol.LATEST_VERSION),
                notification("notifications/initialized"),
                McpJson.write(request("ping")));

        List<JsonNode> responses = responses(output);
        assertEquals(2, responses.size());
        assertTrue(responses.get(0).get("result").has("serverInfo"));
        assertTrue(responses.get(1).get("result").isObject());
    }

    @Test
    public void toolsListContainsAllMvpTools() throws Exception {
        List<JsonNode> responses = responses(run(
                initialize(McpProtocol.LATEST_VERSION),
                toolsList()));

        JsonNode tools = responses.get(1).get("result").get("tools");
        assertEquals(6, tools.size());

        Set<String> names = new LinkedHashSet<>();
        for (JsonNode tool : tools) {
            names.add(tool.get("name").asText());
            assertTrue(tool.get("inputSchema").isObject());
            assertTrue(tool.get("inputSchema").get("type").asText().equals("object"));
            assertTrue(tool.get("annotations").isObject());
            assertFalse(tool.get("description").asText().isBlank());
        }

        assertEquals(Set.of(
                "pdf_list_fields",
                "pdf_read_metadata",
                "pdf_set_metadata",
                "pdf_clear_metadata",
                "pdf_rename",
                "pdf_export_metadata"), names);
    }

    @Test
    public void requestBeforeInitializeIsRejected() throws Exception {
        JsonNode response = responses(run(toolsList())).get(0);
        assertEquals(McpProtocol.SERVER_NOT_INITIALIZED, response.get("error").get("code").asInt());
    }

    @Test
    public void unknownMethodReturnsMethodNotFound() throws Exception {
        JsonNode response = responses(run(
                initialize(McpProtocol.LATEST_VERSION),
                McpJson.write(request("does/not/exist")))).get(1);
        assertEquals(McpProtocol.METHOD_NOT_FOUND, response.get("error").get("code").asInt());
    }

    @Test
    public void unknownToolReturnsInvalidParams() throws Exception {
        JsonNode response = responses(run(
                initialize(McpProtocol.LATEST_VERSION),
                callTool("pdf_does_not_exist", McpJson.object()))).get(1);
        assertEquals(McpProtocol.INVALID_PARAMS, response.get("error").get("code").asInt());
    }

    @Test
    public void malformedJsonReturnsParseErrorWithNullId() throws Exception {
        JsonNode response = responses(run("{not-json")).get(0);
        assertEquals(McpProtocol.PARSE_ERROR, response.get("error").get("code").asInt());
        assertTrue(response.get("id").isNull());
    }

    @Test
    public void stringAndNumberIdsAreEchoed() throws Exception {
        ObjectNode stringId = request("ping");
        stringId.put("id", "abc-123");

        ObjectNode numberId = request("ping");
        numberId.put("id", 42);

        List<JsonNode> responses = responses(run(
                initialize(McpProtocol.LATEST_VERSION),
                McpJson.write(stringId),
                McpJson.write(numberId)));

        assertEquals("abc-123", responses.get(1).get("id").asText());
        assertEquals(42, responses.get(2).get("id").asInt());
    }

    @Test
    public void nullIdIsTreatedAsARequest() throws Exception {
        ObjectNode request = request("ping");
        request.putNull("id");

        List<JsonNode> responses = responses(run(
                initialize(McpProtocol.LATEST_VERSION),
                McpJson.write(request)));

        assertEquals(2, responses.size());
        assertTrue(responses.get(1).get("id").isNull());
    }

    @Test
    public void toolErrorsAreReturnedAsResultsNotProtocolErrors() throws Exception {
        ObjectNode args = McpJson.object();
        args.put("path", "missing.pdf");

        JsonNode response = responses(run(
                initialize(McpProtocol.LATEST_VERSION),
                callTool("pdf_read_metadata", args))).get(1);

        assertFalse(response.has("error"));
        assertEquals(true, response.get("result").get("isError").asBoolean());
        assertTrue(response.get("result").get("content").get(0).get("text").asText().contains("File not found"));
    }

    private String run(String... messages) throws Exception {
        String input = String.join("\n", messages) + "\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        McpOptions options = new McpOptions(tempDir.toRealPath(), false, () -> true);
        new McpServer(in, out, options).run();
        return out.toString(StandardCharsets.UTF_8);
    }

    private List<JsonNode> responses(String output) throws Exception {
        List<JsonNode> result = new ArrayList<>();
        for (String line : output.split("\n")) {
            if (!line.isBlank()) {
                result.add(McpJson.parse(line));
            }
        }
        return result;
    }

    private static String initialize(String version) {
        ObjectNode params = McpJson.object();
        params.put("protocolVersion", version);
        params.set("capabilities", McpJson.object());
        ObjectNode clientInfo = McpJson.object();
        clientInfo.put("name", "test-client");
        clientInfo.put("version", "1.0");
        params.set("clientInfo", clientInfo);

        ObjectNode request = request("initialize");
        request.set("params", params);
        return McpJson.write(request);
    }

    private static String toolsList() {
        return McpJson.write(request("tools/list"));
    }

    private static String notification(String method) {
        ObjectNode request = McpJson.object();
        request.put("jsonrpc", "2.0");
        request.put("method", method);
        return McpJson.write(request);
    }

    private static String callTool(String name, ObjectNode arguments) {
        ObjectNode params = McpJson.object();
        params.put("name", name);
        params.set("arguments", arguments);

        ObjectNode request = request("tools/call");
        request.set("params", params);
        return McpJson.write(request);
    }

    private static ObjectNode request(String method) {
        ObjectNode request = McpJson.object();
        request.put("jsonrpc", "2.0");
        request.put("id", 1);
        request.put("method", method);
        return request;
    }
}
