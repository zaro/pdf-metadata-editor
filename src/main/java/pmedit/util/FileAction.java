package pmedit.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Stack;

public abstract class FileAction {
    File currentInputDir;
    Stack<File> outDirStack = new Stack<>();

    public FileAction(File outDir) {
        outDirStack.push(outDir);
    }

    public File outDir() {
        return outDirStack.peek();
    }

    public File pushOutDirFromInputFile(File input) {
        File toPush = null;
        if (outDir() != null) {
            toPush = new File(outDir(), input.getName());
        }
        return outDirStack.push(toPush);
    }

    public File popOutDir() {
        return outDirStack.pop();
    }

    public File getOutputFile(File inputFile) {
        File outDir = outDir();
        if (outDir == null) {
            return null;
        }
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        return new File(outDir, inputFile.getName());
    }

    public File getOutputFileNonNull(File inputFile) {
        return getOutputFileNonNull(inputFile, null);
    }

    public File getOutputFileNonNull(File inputFile, File fallbackOutDir) {
        File o = getOutputFile(inputFile);
        if (o == null) {
            if (fallbackOutDir == null) {
                return inputFile;
            }
            return new File(fallbackOutDir, inputFile.getName());
        }
        return o;
    }


    public File getNewOutputFile(String name) {
        File outDir = outDir();
        if (outDir == null) {
            outDir = currentInputDir;
        } else {
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
        }
        return new File(outDir, name);
    }

    public String outputFileRelativeName(File file) {
        if (outDir() == null) {
            return file.getName();
        }
        Path basePath = outDir().toPath().toAbsolutePath();
        Path targetPath = file.toPath().toAbsolutePath();
        if (targetPath.startsWith(basePath)) {
            return basePath.relativize(targetPath).toString();
        }
        return file.getName();
    }

    public void setCurrentInputDir(File inputFile) {
        if (inputFile.isDirectory()) {
            currentInputDir = inputFile;
        } else {
            currentInputDir = inputFile.getParentFile();
        }
    }

    public String inputFileRelativeName(File file) {
        if (currentInputDir == null) {
            return file.getName();
        }
        Path basePath = currentInputDir.toPath().toAbsolutePath();
        Path targetPath = file.toPath().toAbsolutePath();
        if (targetPath.startsWith(basePath)) {
            return basePath.relativize(targetPath).toString();
        }
        return file.getName();
    }

    public void beforeAll() {
    }

    public void afterAll() {
    }

    public void beforeEach() {

    }

    public void afterEach() {
    }

    public abstract void apply(File file);

    public abstract void ignore(File file);
}
