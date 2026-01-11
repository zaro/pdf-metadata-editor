package pmedit.ui;

import com.formdev.flatlaf.util.SystemFileChooser;
import pmedit.prefs.Preferences;

import java.awt.*;
import java.io.File;

public class DirChooser extends SystemFileChooser {
    public static final int APPROVE_OPTION = SystemFileChooser.APPROVE_OPTION;
    String designation = "";
    DirChooser(){
        setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);
    }
    DirChooser(String designation){
        this();
        this.designation = designation;
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        String dir = Preferences.getInstance().get("LastDir" + designation, null);
        if (dir != null) {
            try {
                setCurrentDirectory(new File(dir));
            } catch (Exception e) {
            }
        }
        int retValue =  super.showOpenDialog(parent);

        if( retValue == APPROVE_OPTION) {
            Preferences.getInstance().put("LastDir"+ designation, getSelectedFile().getParent());

        }
        return retValue;
    }
}
