package pmedit.logging;

import pmedit.prefs.LocalDataDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Log file naming for the MCP server.
 *
 * <p>The MCP server is long lived and one process is started per MCP client, so
 * it cannot share the GUI's {@code log.txt}: the startup rotation would move or
 * truncate a log another process is still writing. Each server gets its own
 * file under {@code <appdata>/mcp}.</p>
 */
public final class McpLogFiles {

    static final String DIRECTORY = "mcp";
    static final String PREFIX = "mcp-";
    static final String SUFFIX = ".log";
    static final int KEEP_FILES = 10;
    static final long RECENT_MILLIS = 10 * 60 * 1000L;

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private McpLogFiles() {
    }

    /**
     * Creates the MCP log directory, prunes old logs, and returns this
     * process's log file path. The file itself is created by logback.
     */
    public static Path newLogPath() {
        return newLogPath(directory());
    }

    static Path newLogPath(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create MCP log directory: " + directory, e);
        }
        prune(directory);
        String name = PREFIX + LocalDateTime.now().format(STAMP)
                + "-" + ProcessHandle.current().pid() + SUFFIX;
        return directory.resolve(name);
    }

    static Path directory() {
        return Path.of(LocalDataDir.getAppDataDir(), DIRECTORY);
    }

    /**
     * Keeps the newest {@link #KEEP_FILES} logs, counting the file about to be
     * created. Files modified in the last ten minutes are never removed, so a
     * running server cannot lose its log. Delete failures are ignored.
     */
    static void prune(Path directory) {
        if (!Files.isDirectory(directory)) {
            return;
        }
        List<Path> logs;
        try (Stream<Path> entries = Files.list(directory)) {
            logs = entries
                    .filter(McpLogFiles::isLogFile)
                    .sorted(Comparator.comparingLong(McpLogFiles::lastModified).reversed())
                    .toList();
        } catch (IOException e) {
            return;
        }

        long recent = System.currentTimeMillis() - RECENT_MILLIS;
        for (int i = KEEP_FILES - 1; i < logs.size(); i++) {
            Path log = logs.get(i);
            if (lastModified(log) > recent) {
                continue;
            }
            try {
                Files.deleteIfExists(log);
            } catch (IOException ignored) {
                // Another process may still hold the file open on Windows.
            }
        }
    }

    private static boolean isLogFile(Path file) {
        String name = file.getFileName().toString();
        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
    }

    private static long lastModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }
}
