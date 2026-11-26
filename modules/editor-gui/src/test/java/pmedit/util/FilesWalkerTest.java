package pmedit.util;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pmedit.WalkLimits;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesWalkerTest {

    @TempDir
    Path tempDir;

    @Test
    public void unlimitedWalkCollectsEverything() throws Exception {
        Path dir = pdfTree("all", 5);

        FilesWalker walker = new FilesWalker(new String[]{"pdf"}, List.of(dir.toFile()), null);
        List<File> files = walker.list();

        assertEquals(5, files.size());
        assertNull(walker.aborted());
    }

    @Test
    public void stopsAtTheFileCap() throws Exception {
        Path dir = pdfTree("many", 5);

        FilesWalker walker = new FilesWalker(
                new String[]{"pdf"}, List.of(dir.toFile()), null, false,
                new WalkLimits(0, false, false), 3);

        List<File> files = walker.list();

        assertEquals(3, files.size());
        assertNotNull(walker.aborted());
        assertEquals("batch_too_large", walker.aborted().code());
        assertTrue(walker.aborted().getMessage().contains("3"));
    }

    @Test
    public void stopsAtTheDeadline() throws Exception {
        Path dir = pdfTree("slow", 10);
        LongSupplier clock = new LongSupplier() {
            private long now;

            @Override
            public long getAsLong() {
                now += 1_000_000_000L;
                return now;
            }
        };

        FilesWalker walker = new FilesWalker(
                new String[]{"pdf"}, List.of(dir.toFile()), null, false,
                new WalkLimits(2, false, false), 0, clock);

        List<File> files = walker.list();

        assertTrue(files.size() < 10);
        assertNotNull(walker.aborted());
        assertEquals("walk_timeout", walker.aborted().code());
    }

    @Test
    public void skipsHiddenDirectories() throws Exception {
        Path dir = pdfTree("root", 1);
        Path hidden = Files.createDirectories(dir.resolve(".git"));
        Files.writeString(hidden.resolve("hidden.pdf"), "x");

        FilesWalker walker = new FilesWalker(
                new String[]{"pdf"}, List.of(dir.toFile()), null, false,
                new WalkLimits(0, true, false), 0);

        assertEquals(1, walker.list().size());
    }

    @Test
    public void skipsSymlinkedDirectories() throws Exception {
        Assumptions.assumeFalse(isWindows());

        Path dir = pdfTree("root", 1);
        Path outside = pdfTree("outside", 1);
        Files.createSymbolicLink(dir.resolve("link"), outside);

        FilesWalker walker = new FilesWalker(
                new String[]{"pdf"}, List.of(dir.toFile()), null, false,
                new WalkLimits(0, false, true), 0);

        assertEquals(1, walker.list().size());
    }

    @Test
    public void walksAnExplicitSymlinkedRoot() throws Exception {
        Assumptions.assumeFalse(isWindows());

        Path outside = pdfTree("outside", 1);
        Path link = tempDir.resolve("link-root");
        Files.createSymbolicLink(link, outside);

        FilesWalker walker = new FilesWalker(
                new String[]{"pdf"}, List.of(link.toFile()), null, false,
                new WalkLimits(0, false, true), 0);

        assertEquals(1, walker.list().size());
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private Path pdfTree(String name, int count) throws IOException {
        Path dir = Files.createDirectories(tempDir.resolve(name));
        for (int i = 0; i < count; i++) {
            Files.writeString(dir.resolve("file-" + i + ".pdf"), "x");
        }
        return dir;
    }
}
