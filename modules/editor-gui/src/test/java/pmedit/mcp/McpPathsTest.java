package pmedit.mcp;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpPathsTest {

    @TempDir
    Path root;

    @TempDir
    Path outside;

    @Test
    public void resolvesRelativeInputPath() throws Exception {
        Path file = file(root, "document.pdf");

        assertEquals(file.toRealPath(), paths().input("document.pdf"));
    }

    @Test
    public void acceptsAbsolutePathInsideRoot() throws Exception {
        Path file = file(root, "document.pdf");

        assertEquals(file.toRealPath(), paths().input(file.toAbsolutePath().toString()));
    }

    @Test
    public void rejectsAbsolutePathOutsideRoot() throws Exception {
        Path file = file(outside, "document.pdf");

        assertThrows(McpToolException.class,
                () -> paths().input(file.toAbsolutePath().toString()));
    }

    @Test
    public void rejectsTraversalOutsideRoot() throws Exception {
        file(root.getParent(), "escape.pdf");

        assertThrows(McpToolException.class, () -> paths().input("../escape.pdf"));
    }

    @Test
    public void rejectsNonPdfInput() throws Exception {
        file(root, "document.txt");

        assertThrows(McpToolException.class, () -> paths().input("document.txt"));
    }

    @Test
    public void rejectsDirectoryInput() throws Exception {
        Files.createDirectories(root.resolve("directory"));

        assertThrows(McpToolException.class, () -> paths().input("directory"));
    }

    @Test
    public void createsMissingParentsForOutput() throws Exception {
        Path output = paths().output("nested/dir/document.pdf");

        assertTrue(Files.isDirectory(root.resolve("nested/dir")));
        assertEquals(root.toRealPath().resolve("nested/dir/document.pdf"), output);
    }

    @Test
    public void rejectsOutputOutsideRoot() {
        Path target = outside.resolve("document.pdf");

        assertThrows(McpToolException.class,
                () -> paths().output(target.toAbsolutePath().toString()));
    }

    @Test
    public void rejectsSymlinkedInputThatEscapesRoot() throws Exception {
        Path secret = file(outside, "secret.pdf");
        Path link = root.resolve("link.pdf");
        try {
            Files.createSymbolicLink(link, secret);
        } catch (IOException | UnsupportedOperationException e) {
            Assumptions.assumeTrue(false, "Symbolic links are not supported on this platform");
            return;
        }

        assertThrows(McpToolException.class, () -> paths().input("link.pdf"));
    }

    @Test
    public void rejectsOutputThroughSymlinkedDirectoryThatEscapesRoot() throws Exception {
        Path link = root.resolve("outside-dir");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (IOException | UnsupportedOperationException e) {
            Assumptions.assumeTrue(false, "Symbolic links are not supported on this platform");
            return;
        }

        assertThrows(McpToolException.class, () -> paths().output("outside-dir/document.pdf"));
    }

    private McpPaths paths() throws IOException {
        return McpPaths.forRoot(root.toRealPath());
    }

    private static Path file(Path dir, String name) throws IOException {
        Path file = dir.resolve(name);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "test");
        return file;
    }
}
