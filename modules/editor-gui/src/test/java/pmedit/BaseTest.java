package pmedit;

import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class BaseTest {
    private static String currentTempTop;

    private static String getCurrentTempTop(){
        if(currentTempTop == null) {
            currentTempTop=  "target" + File.separator + "test-data" + File.separator + "run-" + DateFormat.formatDateTimeForPath(Calendar.getInstance());
        }
        return currentTempTop;
    }

    protected Stack<File> tempDirs = new Stack<>();

    public void pushTempDir(TestInfo testInfo) {
        String className=testInfo.getTestClass().get().getSimpleName();
        pushTempDir(className);
        pushTempDir(testInfo.getTestMethod().get().getName());
    }

    public void popTempDir(TestInfo testInfo) {
        popTempDir();
        popTempDir();
    }

    public void pushTempDir(String name){
        File tempDir = new File(getTempDir(), name);
        if(!tempDir.exists()){
            tempDir.mkdirs();
        }
        tempDirs.push(tempDir);
    }

    public void popTempDir(){
        tempDirs.pop();
    }

    public File getTempDir(){
        if(tempDirs.isEmpty()){
            File tempDir = new File(getCurrentTempTop());
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            tempDirs.push(tempDir);
        }
        return tempDirs.peek();
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
