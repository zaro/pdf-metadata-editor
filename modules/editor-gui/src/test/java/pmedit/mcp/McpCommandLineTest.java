package pmedit.mcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pmedit.CommandLine;
import pmedit.MainCli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpCommandLineTest {

    @Test
    public void parsesMcpOptions() throws Exception {
        CommandLine cmdLine = CommandLine.parse(new String[]{
                "--mcp", "--mcp-root=/tmp/pdfs", "--mcp-read-only"
        });

        assertTrue(cmdLine.mcp);
        assertEquals("/tmp/pdfs", cmdLine.mcpRoot);
        assertTrue(cmdLine.mcpReadOnly);
    }

    @Test
    public void parsesRootAsSeparateArgument() throws Exception {
        CommandLine cmdLine = CommandLine.parse(new String[]{
                "-mcp", "-mcp-root", "/tmp/pdfs"
        });

        assertTrue(cmdLine.mcp);
        assertEquals("/tmp/pdfs", cmdLine.mcpRoot);
        assertFalse(cmdLine.mcpReadOnly);
    }

    @Test
    public void parsesBatchLimit() throws Exception {
        CommandLine cmdLine = CommandLine.parse(new String[]{
                "--mcp", "--mcp-batch-limit=25"
        });
        assertEquals(25, cmdLine.mcpBatchLimit);

        CommandLine separate = CommandLine.parse(new String[]{
                "--mcp", "--mcp-batch-limit", "0"
        });
        assertEquals(0, separate.mcpBatchLimit);
    }

    @Test
    public void rejectsInvalidBatchLimit() {
        assertThrows(CommandLine.ParseError.class, () -> CommandLine.parse(new String[]{
                "--mcp", "--mcp-batch-limit=-1"
        }));
        assertThrows(CommandLine.ParseError.class, () -> CommandLine.parse(new String[]{
                "--mcp", "--mcp-batch-limit=nope"
        }));
    }

    @Test
    public void optionsCarryTheConfiguredBatchLimit(@TempDir Path root) throws Exception {
        CommandLine cmdLine = CommandLine.parse(new String[]{
                "--mcp", "--mcp-root=" + root, "--mcp-batch-limit=7"
        });

        McpOptions options = McpOptions.from(cmdLine);

        assertEquals(7, options.maxFiles());
    }

    @Test
    public void parsesHelpMcp() throws Exception {
        CommandLine cmdLine = CommandLine.parse(new String[]{"--help-mcp"});
        assertTrue(cmdLine.helpMcp);

        CommandLine singleDash = CommandLine.parse(new String[]{"-help-mcp"});
        assertTrue(singleDash.helpMcp);
    }

    @Test
    public void helpMcpPrintsConfiguration() throws Exception {
        CommandLine cmdLine = CommandLine.parse(new String[]{"--help-mcp"});

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            MainCli.executeCommand(cmdLine);
        } finally {
            System.setOut(previous);
        }

        String output = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("# MCP configuration"));
        assertTrue(output.contains("mcpServers"));
        assertTrue(output.contains("--mcp-root"));
        assertTrue(output.contains("No npm install is required"));
        assertTrue(output.contains("Supported MCP options"));
    }
}
