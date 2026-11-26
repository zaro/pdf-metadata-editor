package pmedit.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpLogFilesTest {

    @TempDir
    Path tempDir;

    @Test
    public void namesLogsPerProcess() {
        Path log = McpLogFiles.newLogPath(tempDir);

        assertEquals(tempDir, log.getParent());
        assertTrue(log.getFileName().toString().matches("mcp-\\d{8}-\\d{6}-\\d+\\.log"),
                log.getFileName().toString());
        assertTrue(log.getFileName().toString()
                .contains("-" + ProcessHandle.current().pid() + ".log"));
    }

    @Test
    public void createsTheLogDirectory() {
        Path nested = tempDir.resolve("mcp");
        McpLogFiles.newLogPath(nested);
        assertTrue(Files.isDirectory(nested));
    }

    @Test
    public void keepsTheNewestLogs() throws Exception {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 14; i++) {
            Path log = Files.writeString(tempDir.resolve("mcp-20260101-0000-" + i + ".log"), "x");
            Files.setLastModifiedTime(log, FileTime.fromMillis(now - 3_600_000L - i * 1_000L));
        }

        McpLogFiles.prune(tempDir);

        assertEquals(McpLogFiles.KEEP_FILES - 1, logCount());
    }

    @Test
    public void sparesLogsWrittenInTheLastTenMinutes() throws Exception {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 12; i++) {
            Path log = Files.writeString(tempDir.resolve("mcp-20260101-0000-" + i + ".log"), "x");
            Files.setLastModifiedTime(log, FileTime.fromMillis(now - 20_000L * (i + 1)));
        }

        McpLogFiles.prune(tempDir);

        assertEquals(12, logCount());
    }

    @Test
    public void leavesUnrelatedFilesAlone() throws Exception {
        Path other = Files.writeString(tempDir.resolve("log.txt"), "x");
        Files.setLastModifiedTime(other, FileTime.fromMillis(0));
        for (int i = 0; i < 14; i++) {
            Path log = Files.writeString(tempDir.resolve("mcp-20260101-0000-" + i + ".log"), "x");
            Files.setLastModifiedTime(log, FileTime.fromMillis(0));
        }

        McpLogFiles.prune(tempDir);

        assertTrue(Files.exists(other));
        assertEquals(McpLogFiles.KEEP_FILES - 1, logCount());
    }

    private long logCount() throws Exception {
        try (Stream<Path> entries = Files.list(tempDir)) {
            return entries.filter(path -> path.getFileName().toString().startsWith("mcp-")).count();
        }
    }
}
