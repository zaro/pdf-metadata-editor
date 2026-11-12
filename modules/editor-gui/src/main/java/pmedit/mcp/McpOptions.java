package pmedit.mcp;

import pmedit.CommandLine;
import pmedit.ext.PmeExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;

/**
 * Runtime configuration for the MCP server.
 *
 * @param root        canonical directory that all tool paths must stay inside
 * @param readOnly    when true, all mutating tools are rejected
 * @param licenseGate returns true when licensed operations are allowed
 */
public record McpOptions(Path root, boolean readOnly, BooleanSupplier licenseGate) {

    public McpOptions {
        if (root == null) {
            throw new IllegalArgumentException("MCP root must not be null");
        }
        if (licenseGate == null) {
            licenseGate = () -> false;
        }
    }

    public static McpOptions from(CommandLine cmdLine) throws IOException {
        Path configured;
        try {
            configured = cmdLine.mcpRoot == null || cmdLine.mcpRoot.isBlank()
                    ? Path.of(".")
                    : Path.of(cmdLine.mcpRoot);
        } catch (InvalidPathException e) {
            throw new IOException("Invalid MCP root path: " + cmdLine.mcpRoot, e);
        }

        Path absolute = configured.toAbsolutePath().normalize();
        if (!Files.exists(absolute)) {
            throw new IOException("MCP root does not exist: " + absolute);
        }
        if (!Files.isDirectory(absolute)) {
            throw new IOException("MCP root is not a directory: " + absolute);
        }

        return new McpOptions(
                absolute.toRealPath(),
                cmdLine.mcpReadOnly,
                () -> PmeExtension.get().hasBatch()
        );
    }
}
