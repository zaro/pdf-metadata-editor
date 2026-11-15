package pmedit.mcp;

import pmedit.BatchException;
import pmedit.BatchPathPolicy;

import java.io.File;

/**
 * Keeps every batch input and output inside the MCP root.
 */
final class McpBatchPathPolicy implements BatchPathPolicy {

    private final McpPaths paths;

    McpBatchPathPolicy(McpPaths paths) {
        this.paths = paths;
    }

    @Override
    public File validateInput(File file) throws BatchException {
        try {
            return paths.resolveExisting(file.getPath()).toFile();
        } catch (McpToolException e) {
            throw wrap(e);
        }
    }

    @Override
    public File validateOutput(File file) throws BatchException {
        try {
            return paths.prepareOutput(file.toPath()).toFile();
        } catch (McpToolException e) {
            throw wrap(e);
        }
    }

    @Override
    public File prepareOutputDirectory(File directory) throws BatchException {
        if (directory == null) {
            return null;
        }
        try {
            return paths.outputDirectory(directory.getPath()).toFile();
        } catch (McpToolException e) {
            throw wrap(e);
        }
    }

    private static BatchException wrap(McpToolException error) {
        String code = error.code() == null ? "invalid_argument" : error.code();
        return new BatchException(code, error.getMessage(), error);
    }
}
