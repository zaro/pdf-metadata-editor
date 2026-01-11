package pmedit.ui;

import com.formdev.flatlaf.util.SystemFileChooser;
import pmedit.prefs.Preferences;

import java.awt.*;
import java.io.File;

public class FileChooser extends SystemFileChooser {
    public static final int APPROVE_OPTION = SystemFileChooser.APPROVE_OPTION;
    FileChooser(){
        this(new SystemFileChooser.FileNameExtensionFilter("Pdf File", "pdf"));
    }

    FileChooser(String[] extensions){
        this(new SystemFileChooser.FileNameExtensionFilter(extensions[0].toUpperCase() + " File", extensions));
    }

    FileChooser(FileFilter filter){
        addChoosableFileFilter(filter);
        setFileFilter(filter);
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        String dir = Preferences.getInstance().get("LastDir", null);
        if (dir != null) {
            try {
                setCurrentDirectory(new File(dir));
            } catch (Exception e) {
            }
        }
        int retValue =  super.showOpenDialog(parent);

        if( retValue == APPROVE_OPTION) {
            Preferences.getInstance().put("LastDir", getSelectedFile().getParent());

        }
        return retValue;
    }
}
