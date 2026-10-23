package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for stdout pollution: the server must write JSON-RPC frames
 * and nothing else to stdout, even when started as a separate process.
 */
public class McpProcessTest {

    @TempDir
    Path tempDir;

    @Test
    public void processKeepsStdoutClean() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty(
                "surefire.test.class.path",
                System.getProperty("java.class.path"));

        ProcessBuilder builder = new ProcessBuilder(
                java,
                "-cp", classpath,
                "pmedit.MainCli",
                "--mcp",
                "--mcp-root", tempDir.toAbsolutePath().toString());
        builder.redirectErrorStream(false);

        Process process = builder.start();
        try (BufferedWriter stdin = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
            stdin.write(initialize());
            stdin.newLine();
            stdin.write("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
            stdin.newLine();
            stdin.write("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}");
            stdin.newLine();
            stdin.flush();
        }

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(process.waitFor(20, TimeUnit.SECONDS), "MCP process did not exit");
        assertEquals(0, process.exitValue(), "MCP process failed: " + stderr);

        List<JsonNode> responses = new ArrayList<>();
        for (String line : stdout.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            JsonNode response = McpJson.parse(line);
            assertTrue(response.isObject(), "stdout contained a non-object line: " + line);
            assertEquals("2.0", response.get("jsonrpc").asText(), "stdout contained a non-JSON-RPC line: " + line);
            responses.add(response);
        }

        assertEquals(2, responses.size(), "Expected exactly two protocol responses, got: " + stdout);
        assertEquals(1, responses.get(0).get("id").asInt());
        assertEquals(2, responses.get(1).get("id").asInt());
        assertFalse(responses.get(1).get("result").get("tools").isEmpty());
    }

    private static String initialize() {
        return "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
                + "\"params\":{\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},"
                + "\"clientInfo\":{\"name\":\"process-test\",\"version\":\"1\"}}}";
    }
}
