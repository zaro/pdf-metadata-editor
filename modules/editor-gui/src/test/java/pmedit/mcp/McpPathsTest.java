package pmedit.mcp;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    @Test
    public void expandCollectsPdfsAndIgnoresOtherFiles() throws Exception {
        file(root, "docs/a.pdf");
        file(root, "docs/sub/b.pdf");
        file(root, "docs/notes.txt");

        var entries = paths().expand(List.of("docs"), null);

        assertEquals(2, entries.size());
        assertEquals("a.pdf", entries.get(0).relative());
        assertEquals("sub/b.pdf", entries.get(1).relative());
    }

    @Test
    public void expandKeepsFileRelativeNameForSingleFile() throws Exception {
        file(root, "docs/a.pdf");

        var entries = paths().expand(List.of("docs/a.pdf"), null);

        assertEquals(1, entries.size());
        assertEquals("a.pdf", entries.get(0).relative());
    }

    @Test
    public void expandDeduplicatesOverlappingRoots() throws Exception {
        file(root, "docs/a.pdf");
        file(root, "docs/sub/b.pdf");

        var entries = paths().expand(List.of("docs", "docs/a.pdf"), null);

        assertEquals(2, entries.size());
    }

    @Test
    public void expandSkipsTheOutputDirectory() throws Exception {
        file(root, "docs/a.pdf");
        file(root, "docs/out/previous.pdf");
        Path outputDir = root.resolve("docs/out").toRealPath();

        var entries = paths().expand(List.of("docs"), outputDir);

        assertEquals(1, entries.size());
        assertEquals("a.pdf", entries.get(0).relative());
    }

    @Test
    public void expandRejectsExplicitNonPdf() throws Exception {
        file(root, "notes.txt");

        McpToolException error = assertThrows(McpToolException.class,
                () -> paths().expand(List.of("notes.txt"), null));
        assertEquals("not_a_pdf", error.code());
    }

    @Test
    public void expandRejectsEmptyInput() throws Exception {
        McpToolException error = assertThrows(McpToolException.class,
                () -> paths().expand(List.of(), null));
        assertEquals("no_input_files", error.code());
    }

    @Test
    public void expandRejectsTooManyFiles() throws Exception {
        Path dir = root.resolve("many");
        Files.createDirectories(dir);
        for (int i = 0; i <= McpPaths.MAX_BATCH_FILES; i++) {
            Files.writeString(dir.resolve("file-" + i + ".pdf"), "test");
        }

        McpToolException error = assertThrows(McpToolException.class,
                () -> paths().expand(List.of("many"), null));
        assertEquals("batch_too_large", error.code());
    }

    @Test
    public void expandRejectsSymlinkedFileThatEscapesRoot() throws Exception {
        Path secret = file(outside, "secret.pdf");
        Files.createDirectories(root.resolve("docs"));
        Path link = root.resolve("docs/link.pdf");
        try {
            Files.createSymbolicLink(link, secret);
        } catch (IOException | UnsupportedOperationException e) {
            Assumptions.assumeTrue(false, "Symbolic links are not supported on this platform");
            return;
        }

        McpToolException error = assertThrows(McpToolException.class,
                () -> paths().expand(List.of("docs"), null));
        assertEquals("path_outside_root", error.code());
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
