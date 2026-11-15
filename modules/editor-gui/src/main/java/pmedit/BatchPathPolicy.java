package pmedit;

import java.io.File;

/**
 * Resolves and validates input and output paths for a batch.
 *
 * <p>The CLI and GUI use a permissive policy. MCP uses a policy that keeps
 * every path inside its configured root.</p>
 */
public interface BatchPathPolicy {

    File validateInput(File file) throws BatchException;

    File validateOutput(File file) throws BatchException;

    File prepareOutputDirectory(File directory) throws BatchException;

    static BatchPathPolicy permissive() {
        return new BatchPathPolicy() {
            @Override
            public File validateInput(File file) {
                return file;
            }

            @Override
            public File validateOutput(File file) {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                return file;
            }

            @Override
            public File prepareOutputDirectory(File directory) {
                return directory;
            }
        };
    }
}
