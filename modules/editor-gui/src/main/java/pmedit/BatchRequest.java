package pmedit;

import java.io.File;
import java.util.List;

/**
 * Inputs, output directory, and policy for a shared batch run.
 */
public record BatchRequest(List<File> roots, File outputDir, BatchOptions options) {

    public BatchRequest {
        roots = roots == null ? List.of() : List.copyOf(roots);
        if (options == null) {
            options = BatchOptions.cli();
        }
    }
}
