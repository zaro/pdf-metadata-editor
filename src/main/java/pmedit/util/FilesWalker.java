package pmedit.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FilesWalker {
    protected FileFilter fileFilter;
    protected String[] allowedExtensions;
    public List<File> files;

    public FilesWalker(String[] extensions, List<File> files){
        this.files = files;
        allowedExtensions = new String[extensions.length];

        for(int i=0; i < extensions.length; i++){
            allowedExtensions[i] = "." + extensions[i].toLowerCase();
        }

        fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(pathname.isDirectory()){
                    return  true;
                }
                String ln = pathname.getName().toLowerCase();
                for(String ext: allowedExtensions){
                    if(ln.endsWith(ext)){
                        return  true;
                    }
                }
                return false;
            }
        };
    }

    protected void _forFiles(File file, FileFilter filter, FileAction action) {
        if (file.isFile()) {
            if (filter.accept(file)) {
                action.beforeEach();
                action.apply(file);
                action.afterEach();
            } else {
                action.ignore(file);
            }
        } else if (file.isDirectory()) {
            File[]  files = file.listFiles(filter);
            action.pushOutDirFromInputFile(file);
            if(files != null) {
                for (File dirEntry : files) {
                    _forFiles(dirEntry, filter, action);
                }
            }
            action.popOutDir();
        } else {
            action.ignore(file);
        }
    }


    public void forFiles(FileAction action) {
        if(files == null || files.isEmpty()){
            return;
        }
        action.setCurrentInputDir(files.get(0));
        action.beforeAll();
        for (File file : files) {
            action.setCurrentInputDir(file);
            _forFiles(file, fileFilter, action);
        }
        action.afterAll();
    }

    public List<File> list(){
        List<File> out = new ArrayList<>();
        forFiles(new FileAction(null, null) {
            @Override
            public void apply(File file) {
                out.add(file);
            }

            @Override
            public void ignore(File file) {

            }
        });
        return out;
    }
}
