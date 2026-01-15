package pmedit;

import java.io.File;
import java.util.Calendar;
import java.util.Stack;

public class TempDirStack {
    private static String currentTempTop;

    private static String getCurrentTempTop(){
        if(currentTempTop == null) {
            currentTempTop=  "target" + File.separator + "test-output" + File.separator + "run-" + DateFormat.formatDateTimeForPath(Calendar.getInstance());
        }
        return currentTempTop;
    }

    protected Stack<File> tempDirs;

    public TempDirStack(){
        tempDirs = new Stack<>();
    }

    private TempDirStack(TempDirStack other){
        tempDirs = (Stack<File>) other.tempDirs.clone();
    }

    public TempDirStack fork(){
        return new TempDirStack(this);
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

}
