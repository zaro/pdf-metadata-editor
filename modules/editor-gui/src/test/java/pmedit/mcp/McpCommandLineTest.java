package pmedit.mcp;

import org.junit.jupiter.api.Test;
import pmedit.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
