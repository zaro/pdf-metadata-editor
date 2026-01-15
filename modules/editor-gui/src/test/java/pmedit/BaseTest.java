package pmedit;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class BaseTest {
    static TempDirStack rootTempDir = new TempDirStack();

    public static void pushTempDirRoot(String name){
        rootTempDir.pushTempDir(name);
    }

    public static void popTempDirRoot(){
        rootTempDir.popTempDir();
    }

    public static File getTempDirRoot(){
        return rootTempDir.getTempDir();
    }

    TempDirStack _tempDir;
    private TempDirStack tempDir(){
        if(_tempDir == null){
            _tempDir = rootTempDir.fork();
        }
        return _tempDir;
    }

    public void pushTempDir(String name){
        tempDir().pushTempDir(name);
    }

    public void popTempDir(){
        tempDir().popTempDir();
    }

    public File getTempDir(){
        return tempDir().getTempDir();
    }

    //
    public List<FilesTestHelper.PMTuple> randomFiles(int numFiles) throws Exception {
        return randomFiles(numFiles, true, null);
    }
    public List<FilesTestHelper.PMTuple> randomFiles(int numFiles, Consumer<MetadataInfo> beforeSave) throws Exception {
        return randomFiles(numFiles,  true, beforeSave);
    }

    public List<FilesTestHelper.PMTuple> randomFiles(int numFiles, boolean allFields) throws Exception {
        return randomFiles(numFiles, allFields, null);
    }

    public List<FilesTestHelper.PMTuple> randomFiles(int numFiles, boolean allFields, Consumer<MetadataInfo> beforeSave) throws Exception {
        return FilesTestHelper.randomFiles(numFiles, allFields, beforeSave, getTempDir());
    }
}
