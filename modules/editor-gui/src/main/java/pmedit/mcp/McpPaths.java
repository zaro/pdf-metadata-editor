package pmedit.mcp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Resolves input and output paths against a canonical root directory.
 *
 * <p>Relative paths are resolved against the root. Absolute paths are allowed
 * only when their real path stays inside the root. Symlinks are resolved before
 * the containment check, so a link cannot be used to escape the sandbox.</p>
 */
public final class McpPaths {

    public static final int MAX_BATCH_FILES = 100;

    private final Path root;

    private McpPaths(Path root) {
        this.root = root;
    }

    public static McpPaths forRoot(Path root) throws IOException {
        if (root == null) {
            throw new IOException("MCP root is not configured");
        }
        Path real = root.toRealPath();
        if (!Files.isDirectory(real)) {
            throw new IOException("MCP root is not a directory: " + real);
        }
        return new McpPaths(real);
    }

    public Path root() {
        return root;
    }

    /**
     * Return a root-relative, forward-slash path suitable for tool results.
     * The absolute path is never exposed to the client.
     */
    public String relative(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(root)) {
            return normalized.toString();
        }
        return root.relativize(normalized).toString().replace(File.separatorChar, '/');
    }

    /**
     * Resolve a path that must already exist, be a regular file, and be a PDF.
     */
    public Path input(String raw) throws McpToolException {
        Path resolved = resolve(raw);
        if (!Files.exists(resolved)) {
            throw new McpToolException("File not found: " + raw);
        }

        Path real;
        try {
            real = resolved.toRealPath();
        } catch (IOException e) {
            throw new McpToolException("Cannot access file: " + raw, e);
        }

        ensureInside(real, raw);
        if (!Files.isRegularFile(real)) {
            throw new McpToolException("Not a regular file: " + raw);
        }
        if (!isPdf(real)) {
            throw new McpToolException("Not a PDF file: " + raw);
        }
        return real;
    }

    /**
     * Resolve an output path, creating missing parent directories inside the
     * root. The returned path is canonical when the target already exists.
     */
    public Path output(String raw) throws McpToolException {
        return prepareTarget(resolve(raw), raw);
    }

    /**
     * Resolve a path that must already exist. The path may be a regular file
     * or a directory. The returned path is canonical and inside the root.
     */
    public Path resolveExisting(String raw) throws McpToolException {
        Path resolved = resolve(raw);
        if (!Files.exists(resolved)) {
            throw new McpToolException("file_not_found", "File not found: " + raw);
        }

        Path real;
        try {
            real = resolved.toRealPath();
        } catch (IOException e) {
            throw new McpToolException("file_not_found", "Cannot access file: " + raw, e);
        }

        ensureInside(real, raw);
        return real;
    }

    /**
     * Resolve an output directory without creating it. This lets a batch
     * enforce its size limit before touching the filesystem.
     */
    public Path outputDirectoryPath(String raw) throws McpToolException {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        Path resolved = resolve(raw);
        ensureInsideLexically(resolved, raw);
        if (!Files.exists(resolved)) {
            return resolved;
        }

        Path real;
        try {
            real = resolved.toRealPath();
        } catch (IOException e) {
            throw new McpToolException("output_not_writable", "Cannot access output directory: " + raw, e);
        }
        ensureInside(real, raw);
        if (!Files.isDirectory(real)) {
            throw new McpToolException("output_not_writable", "Not a directory: " + raw);
        }
        return real;
    }

    /**
     * Resolve an output directory, creating it inside the root when needed.
     */
    public Path outputDirectory(String raw) throws McpToolException {
        Path path = outputDirectoryPath(raw);
        if (path == null) {
            return null;
        }
        if (!Files.exists(path)) {
            createDirectoriesInsideRoot(path, raw);
            try {
                path = path.toRealPath();
            } catch (IOException e) {
                throw new McpToolException("output_not_writable", "Cannot access output directory: " + raw, e);
            }
            ensureInside(path, raw);
        }
        return path;
    }

    /**
     * Validate an already-resolved output path and create its parent.
     */
    public Path prepareOutput(Path target) throws McpToolException {
        return prepareTarget(target, target.toString());
    }

    private Path resolve(String raw) throws McpToolException {
        if (raw == null || raw.isBlank()) {
            throw new McpToolException("A file path is required");
        }
        try {
            Path path = Path.of(raw);
            return path.isAbsolute() ? path.normalize() : root.resolve(path).normalize();
        } catch (InvalidPathException e) {
            throw new McpToolException("Invalid path: " + raw, e);
        }
    }

    private Path prepareTarget(Path target, String display) throws McpToolException {
        ensureInsideLexically(target, display);

        Path parent = target.getParent();
        if (parent == null) {
            throw new McpToolException("output_not_writable", "Invalid target path: " + display);
        }

        createDirectoriesInsideRoot(parent, display);

        Path realParent;
        try {
            realParent = parent.toRealPath();
        } catch (IOException e) {
            throw new McpToolException(
                    "output_not_writable",
                    "Cannot access parent directory for: " + display, e);
        }
        ensureInside(realParent, display);

        Path finalTarget = realParent.resolve(target.getFileName()).normalize();
        ensureInside(finalTarget, display);

        if (Files.exists(finalTarget)) {
            Path realTarget;
            try {
                realTarget = finalTarget.toRealPath();
            } catch (IOException e) {
                throw new McpToolException(
                        "output_not_writable",
                        "Cannot access target: " + display, e);
            }
            ensureInside(realTarget, display);
            if (Files.isDirectory(realTarget)) {
                throw new McpToolException("output_not_writable", "Target is a directory: " + display);
            }
            return realTarget;
        }

        return finalTarget;
    }

    private void createDirectoriesInsideRoot(Path directory, String display) throws McpToolException {
        Path existing = directory;
        while (existing != null && !Files.exists(existing)) {
            existing = existing.getParent();
        }
        if (existing == null) {
            throw new McpToolException(
                    "output_not_writable",
                    "Cannot create directory outside the configured root: " + display);
        }

        Path realExisting;
        try {
            realExisting = existing.toRealPath();
        } catch (IOException e) {
            throw new McpToolException(
                    "output_not_writable",
                    "Cannot access directory for: " + display, e);
        }
        ensureInside(realExisting, display);

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new McpToolException(
                    "output_not_writable",
                    "Cannot create directory for: " + display, e);
        }

        Path realDirectory;
        try {
            realDirectory = directory.toRealPath();
        } catch (IOException e) {
            throw new McpToolException(
                    "output_not_writable",
                    "Cannot access directory for: " + display, e);
        }
        ensureInside(realDirectory, display);
    }

    private void ensureInsideLexically(Path path, String display) throws McpToolException {
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(root)) {
            throw new McpToolException("path_outside_root", "Path is outside the configured root: " + display);
        }
    }

    private void ensureInside(Path realPath, String display) throws McpToolException {
        if (!realPath.startsWith(root)) {
            throw new McpToolException("path_outside_root", "Path is outside the configured root: " + display);
        }
    }

    private static boolean isPdf(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        return fileName.toString().toLowerCase(Locale.ROOT).endsWith(".pdf");
    }
}
