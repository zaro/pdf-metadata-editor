package pmedit.ui;

import pmedit.prefs.Preferences;

import java.awt.*;
import java.io.File;

public class PdfFileChooser extends FileChooser {
    PdfFileChooser(){
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
