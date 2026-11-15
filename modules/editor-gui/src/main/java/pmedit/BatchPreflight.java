package pmedit;

import java.io.File;
import java.util.List;

/**
 * The result of validating and expanding a batch request.
 *
 * @param roots     validated input roots, preserving directory structure
 * @param files     the selected PDF files, deduplicated when requested
 * @param outputDir validated output directory, or null
 */
public record BatchPreflight(List<File> roots, List<File> files, File outputDir) {

    public BatchPreflight {
        roots = roots == null ? List.of() : List.copyOf(roots);
        files = files == null ? List.of() : List.copyOf(files);
    }
}
