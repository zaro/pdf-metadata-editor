package pmedit.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogRotatorTest {

    @TempDir
    Path tempDir;

    private Path logFile() {
        return tempDir.resolve("log.txt");
    }

    private void write(String content, Path path) throws IOException {
        Files.writeString(path, content);
    }

    @Test
    void rotatesCurrentLogIntoSlotOne() throws Exception {
        Path log = logFile();
        write("current", log);

        assertTrue(LogRotator.rotate(log));

        assertFalse(Files.exists(log));
        assertEquals("current", Files.readString(LogRotator.numberedLogPath(log, 1)));
    }

    @Test
    void shiftsChainAndDropsOldestBeyondKeepWindow() throws Exception {
        Path log = logFile();
        write("current", log);
        for (int i = 1; i <= LogRotator.KEEP_RUNS; i++) {
            write("run-" + i, LogRotator.numberedLogPath(log, i));
        }

        assertTrue(LogRotator.rotate(log));

        assertEquals("current", Files.readString(LogRotator.numberedLogPath(log, 1)));
        assertEquals("run-1", Files.readString(LogRotator.numberedLogPath(log, 2)));
        assertEquals("run-2", Files.readString(LogRotator.numberedLogPath(log, 3)));
        assertEquals("run-3", Files.readString(LogRotator.numberedLogPath(log, 4)));
        assertEquals("run-4", Files.readString(LogRotator.numberedLogPath(log, 5)));
        // the oldest run (previously slot KEEP_RUNS) is dropped
        assertFalse(Files.exists(LogRotator.numberedLogPath(log, LogRotator.KEEP_RUNS + 1)));
    }

    @Test
    void removesLeftoversBeyondKeepWindow() throws Exception {
        Path log = logFile();
        write("current", log);
        for (int i = LogRotator.KEEP_RUNS + 1; i <= LogRotator.KEEP_RUNS + 3; i++) {
            write("old-" + i, LogRotator.numberedLogPath(log, i));
        }

        assertTrue(LogRotator.rotate(log));

        for (int i = LogRotator.KEEP_RUNS + 1; i <= LogRotator.KEEP_RUNS + 3; i++) {
            assertFalse(Files.exists(LogRotator.numberedLogPath(log, i)));
        }
        assertEquals("current", Files.readString(LogRotator.numberedLogPath(log, 1)));
    }

    @Test
    void missingLogFileIsNoOp() throws Exception {
        assertTrue(LogRotator.rotate(logFile()));
        try (var entries = Files.list(tempDir)) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    void failedMoveLeavesCurrentLogIntact() throws Exception {
        Path log = logFile();
        write("current", log);
        write("old4", LogRotator.numberedLogPath(log, 4));
        // occupy the top slot with a non-empty directory so the first shift
        // move fails (replacing a non-empty directory is not possible)
        Path blocker = LogRotator.numberedLogPath(log, 5);
        Files.createDirectory(blocker);
        Files.writeString(blocker.resolve("keep"), "data");

        assertFalse(LogRotator.rotate(log));

        assertTrue(Files.isRegularFile(log));
        assertEquals("current", Files.readString(log));
        assertEquals("old4", Files.readString(LogRotator.numberedLogPath(log, 4)));
        // the blocker directory is untouched
        assertEquals("data", Files.readString(blocker.resolve("keep")));
    }

    @Test
    void numbersBeforeTheFileExtension() {
        assertEquals(
                tempDir.resolve("application.3.log"),
                LogRotator.numberedLogPath(tempDir.resolve("application.log"), 3));
        assertEquals(
                tempDir.resolve("log.singleInstance.1.txt"),
                LogRotator.numberedLogPath(tempDir.resolve("log.singleInstance.txt"), 1));
        assertEquals(
                tempDir.resolve("noext.2"),
                LogRotator.numberedLogPath(tempDir.resolve("noext"), 2));
    }
}