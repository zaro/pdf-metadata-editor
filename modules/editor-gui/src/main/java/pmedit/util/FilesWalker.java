package pmedit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.WalkLimits;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongSupplier;

/**
 * Expands files and directories into a flat file list.
 *
 * <p>Traversal is unlimited by default. Callers can pass {@link WalkLimits} and
 * a file cap to stop early; the MCP server does this so a large directory
 * cannot block a tool call indefinitely.</p>
 */
public class FilesWalker {

    private static final Logger LOG = LoggerFactory.getLogger(FilesWalker.class);

    protected FileFilter fileFilter;
    protected String[] allowedExtensions;
    public List<File> files;
    File outDir;
    final boolean deduplicate;
    final Set<Path> seen = new HashSet<>();
    final WalkLimits limits;
    final int maxFiles;
    final LongSupplier nanoClock;

    long deadlineNanos;
    int matched;
    WalkAborted aborted;

    public FilesWalker(String[] extensions, List<File> files, File outDir) {
        this(extensions, files, outDir, false);
    }

    public FilesWalker(String[] extensions, List<File> files, File outDir, boolean deduplicate) {
        this(extensions, files, outDir, deduplicate, WalkLimits.unlimited(), 0);
    }

    public FilesWalker(String[] extensions, List<File> files, File outDir, boolean deduplicate,
                       WalkLimits limits, int maxFiles) {
        this(extensions, files, outDir, deduplicate, limits, maxFiles, System::nanoTime);
    }

    FilesWalker(String[] extensions, List<File> files, File outDir, boolean deduplicate,
                WalkLimits limits, int maxFiles, LongSupplier nanoClock) {
        this.files = files;
        this.outDir = outDir;
        this.deduplicate = deduplicate;
        this.limits = limits == null ? WalkLimits.unlimited() : limits;
        this.maxFiles = Math.max(0, maxFiles);
        this.nanoClock = nanoClock == null ? System::nanoTime : nanoClock;
        allowedExtensions = new String[extensions.length];

        for (int i = 0; i < extensions.length; i++) {
            allowedExtensions[i] = "." + extensions[i].toLowerCase();
        }

        fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                String ln = pathname.getName().toLowerCase();
                for (String ext : allowedExtensions) {
                    if (ln.endsWith(ext)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    protected void _forFiles(File file, FileFilter filter, FileAction action, int level) {
        checkDeadline(file);
        if (file.isFile()) {
            if (filter.accept(file)) {
                if (alreadySeen(file)) {
                    return;
                }
                countFile(file);
                action.beforeEach();
                action.apply(file);
                action.afterEach();
            } else {
                action.ignore(file);
            }
        } else if (file.isDirectory()) {
            // If directory is the output directory, ignore
            if(file.equals(outDir)){
                return;
            }
            if (level > 0 && isPruned(file)) {
                return;
            }
            File[]  files = file.listFiles(filter);
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
            }

            // Don't push outputDirs for directories from the original input list
            // as it creates additional directory level
            if(level > 0) {
                action.pushOutDirFromInputFile(file);
            }
            if(files != null) {
                for (File dirEntry : files) {
                    _forFiles(dirEntry, filter, action, level + 1);
                }
            }
            if(level >0) {
                action.popOutDir();
            }
        } else {
            action.ignore(file);
        }
    }

    private void checkDeadline(File file) {
        if (deadlineNanos == 0 || nanoClock.getAsLong() <= deadlineNanos) {
            return;
        }
        throw new WalkAborted(
                "walk_timeout",
                "Stopped walking " + file.getPath() + " after " + limits.timeoutSeconds()
                        + " seconds. Narrow the paths or raise --mcp-walk-timeout.");
    }

    private void countFile(File file) {
        matched++;
        if (maxFiles > 0 && matched > maxFiles) {
            File parent = file.getParentFile();
            throw new WalkAborted(
                    "batch_too_large",
                    "Found more than " + maxFiles + " files under "
                            + (parent == null ? file.getPath() : parent.getPath())
                            + "; the maximum is " + maxFiles
                            + ". Narrow the paths or split the request into smaller batches.");
        }
    }

    private boolean isPruned(File directory) {
        if (limits.skipHiddenDirectories() && directory.getName().startsWith(".")) {
            return true;
        }
        return limits.skipSymlinkedDirectories() && Files.isSymbolicLink(directory.toPath());
    }

    public void forFiles(FileAction action) {
        if(files == null || files.isEmpty()){
            return;
        }
        seen.clear();
        matched = 0;
        aborted = null;
        deadlineNanos = limits.timeoutSeconds() > 0
                ? nanoClock.getAsLong() + limits.timeoutSeconds() * 1_000_000_000L
                : 0;
        LOG.debug("walk start roots={} timeout={}s maxFiles={}",
                files.size(), limits.timeoutSeconds(), maxFiles);
        action.setCurrentInputDir(files.get(0));
        action.beforeAll();
        for (File file : files) {
            action.setCurrentInputDir(file);
            _forFiles(file, fileFilter, action, 0);
        }
        action.afterAll();
    }

    private boolean alreadySeen(File file) {
        if (!deduplicate) {
            return false;
        }
        Path path;
        try {
            path = file.toPath().toRealPath();
        } catch (IOException e) {
            path = file.getAbsoluteFile().toPath().normalize();
        }
        return !seen.add(path);
    }

    public List<File> list(){
        List<File> out = new ArrayList<>();
        try {
            forFiles(new FileAction(null, null) {
                @Override
                public void apply(File file) {
                    out.add(file);
                }

                @Override
                public void ignore(File file) {

                }
            });
        } catch (WalkAborted e) {
            aborted = e;
            LOG.info("Directory walk stopped ({}): {}", e.code(), e.getMessage());
        }
        if (aborted == null) {
            LOG.debug("walk done files={}", out.size());
        }
        return out;
    }

    /**
     * Why the last walk stopped early, or {@code null} when it completed.
     */
    public WalkAborted aborted() {
        return aborted;
    }

    /**
     * Thrown when a walk hits the file cap or the deadline.
     */
    public static final class WalkAborted extends RuntimeException {

        private final String code;

        WalkAborted(String code, String message) {
            super(message, null, false, false);
            this.code = code;
        }

        public String code() {
            return code;
        }
    }

    public File outputDir(){
        if(outDir != null){
            if(!outDir.isDirectory()){
                outDir =outDir.getParentFile();
            }
            if(!outDir.exists()){
                outDir.mkdirs();
            }
            return outDir;
        }
        try {
            forFiles(new FileAction(null, null) {
                @Override
                public void apply(File file){
                    throw new FileFound(file);
                }

                @Override
                public void ignore(File file) {

                }
            });
        } catch (FileFound e) {
            return e.file.isDirectory() ?e.file : e.file.getParentFile();
        }
        return null;
    }

    static class FileFound extends RuntimeException{
        File file;
        public FileFound(File found) {
            super("Found!");
            file= found;
        }

    }
}
