package pmedit.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RotatingOnStartFileAppenderTest {

    @TempDir
    Path tempDir;

    private void openAndClose(RotatingOnStartFileAppender<ILoggingEvent> appender, String fileName) throws Exception {
        appender.openFile(fileName);
        OutputStream stream = appender.getOutputStream();
        assertNotNull(stream, "openFile should open an output stream");
        stream.close();
    }

    @Test
    void rotatesPreviousLogBeforeOpeningAndStartsFreshFile() throws Exception {
        Path log = tempDir.resolve("log.txt");
        Files.writeString(log, "PREVIOUS-RUN");
        RotatingOnStartFileAppender<ILoggingEvent> appender = new RotatingOnStartFileAppender<>();

        openAndClose(appender, log.toString());

        // previous run's log was shifted away before the new file was opened
        assertEquals("PREVIOUS-RUN", Files.readString(LogRotator.numberedLogPath(log, 1)));
        assertTrue(Files.isRegularFile(log));
    }

    @Test
    void secondRunShiftsChain() throws Exception {
        Path log = tempDir.resolve("log.txt");
        Files.writeString(LogRotator.numberedLogPath(log, 1), "RUN-1");
        Files.writeString(log, "RUN-2");
        RotatingOnStartFileAppender<ILoggingEvent> appender = new RotatingOnStartFileAppender<>();

        openAndClose(appender, log.toString());

        assertEquals("RUN-2", Files.readString(LogRotator.numberedLogPath(log, 1)));
        assertEquals("RUN-1", Files.readString(LogRotator.numberedLogPath(log, 2)));
        assertTrue(Files.isRegularFile(log));
    }

    @Test
    void missingFileIsCreatedWithoutRotation() throws Exception {
        Path log = tempDir.resolve("log.txt");
        RotatingOnStartFileAppender<ILoggingEvent> appender = new RotatingOnStartFileAppender<>();

        openAndClose(appender, log.toString());

        assertTrue(Files.isRegularFile(log));
        assertFalse(Files.exists(LogRotator.numberedLogPath(log, 1)));
    }

    @Test
    void openFileProceedsWhenRotationFails() throws Exception {
        Path log = tempDir.resolve("log.txt");
        Files.writeString(log, "PREVIOUS");
        // block the shift so the rotation fails (replacing a non-empty
        // directory is not possible); openFile must still succeed
        Path blocker = LogRotator.numberedLogPath(log, LogRotator.KEEP_RUNS);
        Files.createDirectory(blocker);
        Files.writeString(blocker.resolve("keep"), "data");
        Files.writeString(LogRotator.numberedLogPath(log, LogRotator.KEEP_RUNS - 1), "old4");
        RotatingOnStartFileAppender<ILoggingEvent> appender = new RotatingOnStartFileAppender<>();

        openAndClose(appender, log.toString());

        assertTrue(Files.isRegularFile(log));
        assertEquals("data", Files.readString(blocker.resolve("keep")));
        assertEquals("old4", Files.readString(LogRotator.numberedLogPath(log, LogRotator.KEEP_RUNS - 1)));
    }
}