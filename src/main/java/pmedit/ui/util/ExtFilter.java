package pmedit.ui.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;


public class ExtFilter extends FileFilter {
    String[] extenstions;
    String description;

    public ExtFilter(String extension){
        this(new String[]{extension});
    }

    public ExtFilter(String[] extensions){
        this.extenstions = new String[extensions.length];
        for(int i=0; i < extensions.length; i++){
            this.extenstions[i] = extensions[i].toLowerCase();
        }
        String pre = this.extenstions[0].toUpperCase();
        String post = Arrays.stream(this.extenstions).map(e -> "*."+ e).collect(Collectors.joining(","));
        description = pre + " files(" + post + ")";
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String fn = f.getName();
        String ext = fn.substring(fn.lastIndexOf('.') + 1).toLowerCase();
        for(String e: extenstions){
            if(ext.equals(e)){
                return  true;
            }
        }
        return  false;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
