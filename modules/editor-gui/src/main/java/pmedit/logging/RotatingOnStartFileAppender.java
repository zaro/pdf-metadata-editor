package pmedit.logging;

import ch.qos.logback.core.FileAppender;

import java.io.IOException;
import java.nio.file.Path;

/**
 * {@link FileAppender} that rotates the logs of the previous runs before
 * opening the log file, keeping the last {@link LogRotator#KEEP_RUNS} runs
 * for troubleshooting.
 *
 * <p>The rotation is plugged into the logging configuration by referencing
 * this class from {@code logback-config.xml} (the XML source that
 * {@code TylerConfigurator} is generated from), so the behaviour survives
 * regeneration of the configurator.</p>
 *
 * <p>Rotating before the file is opened also avoids the open-handle problem
 * on Windows: the previous run's log can be renamed while no stream has it
 * open yet, and the freshly created file is already empty when
 * {@code <append>false</append>} is configured.</p>
 */
public class RotatingOnStartFileAppender<E> extends FileAppender<E> {

    @Override
    public void openFile(String file_name) throws IOException {
        // Rotate the previous runs' logs before this file is opened. If the
        // rotation fails (e.g. another process holds the file), logging still
        // proceeds; the next startup retries the rotation.
        LogRotator.rotate(Path.of(file_name));
        super.openFile(file_name);
    }
}