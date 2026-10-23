package pmedit.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.CommandLine;

import java.io.PrintStream;

/**
 * Process entry point for {@code pmedit-cli --mcp}.
 */
public final class McpMain {

    private static final Logger LOG = LoggerFactory.getLogger(McpMain.class);

    private McpMain() {
    }

    public static void run(CommandLine cmdLine) {
        McpOptions options;
        try {
            options = McpOptions.from(cmdLine);
        } catch (Exception e) {
            System.err.println("MCP server failed to start: " + e.getMessage());
            System.exit(2);
            return;
        }

        PrintStream protocolOut = System.out;
        // Anything that accidentally writes to System.out must not corrupt the
        // JSON-RPC stream. Send it to stderr instead.
        System.setOut(System.err);

        LOG.info("Starting MCP server, root={}, readOnly={}", options.root(), options.readOnly());
        try {
            new McpServer(System.in, protocolOut, options).run();
        } catch (Exception e) {
            LOG.error("MCP server failed", e);
            System.err.println("MCP server failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
