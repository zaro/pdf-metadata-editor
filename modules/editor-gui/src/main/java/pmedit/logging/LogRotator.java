package pmedit.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Rotates the application log file at startup so the logs of the last
 * {@value #KEEP_RUNS} runs are kept for troubleshooting.
 *
 * Works by shifting: {@code log.txt -> log.1.txt} (most recent previous run),
 * {@code log.1.txt -> log.2.txt}, ... and deleting anything beyond
 * {@code log.KRUN.txt}. Renames are metadata-only operations on the same
 * volume, so the cost at startup is negligible regardless of log file size.
 *
 * The rotation must happen before the logback file appender opens the file
 * (i.e. at the start of {@code TylerConfigurator.configure()}), otherwise the
 * open handle prevents the rename on Windows.
 */
public final class LogRotator {
    /** Number of previous runs' logs to keep (approximate). */
    public static final int KEEP_RUNS = 5;

    private LogRotator() {
    }

    /**
     * Shifts the previous runs' logs one slot down and moves the current log
     * into slot 1. If {@code logFile} does not exist nothing is done.
     *
     * @return {@code true} on success or when there was nothing to rotate;
     *         {@code false} if a move failed (e.g. the previous instance still
     *         holds the file open on Windows) — the current log is then left
     *         intact and the next startup retries the rotation
     */
    public static boolean rotate(Path logFile) {
        if (logFile == null || !Files.isRegularFile(logFile)) {
            return true;
        }
        try {
            // remove any leftovers beyond the keep window
            int i = KEEP_RUNS + 1;
            while (Files.deleteIfExists(numberedLogPath(logFile, i))) {
                i++;
            }
            // shift existing numbered logs one slot down
            for (i = KEEP_RUNS; i >= 2; i--) {
                Path from = numberedLogPath(logFile, i - 1);
                if (Files.exists(from)) {
                    Files.move(from, numberedLogPath(logFile, i), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            // move the current log into slot 1
            Files.move(logFile, numberedLogPath(logFile, 1), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("WARNING: Failed to rotate log file " + logFile + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Path of the n-th previous run's log, with the number inserted before the
     * file extension: {@code log.txt -> log.1.txt},
     * {@code log.singleInstance.txt -> log.singleInstance.1.txt},
     * {@code application.log -> application.1.log}.
     */
    static Path numberedLogPath(Path logFile, int n) {
        String fileName = logFile.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String numbered = dot > 0
                ? fileName.substring(0, dot) + "." + n + fileName.substring(dot)
                : fileName + "." + n;
        return logFile.resolveSibling(numbered);
    }
}