package pmedit;

import javax.swing.filechooser.FileFilter;
import java.io.File;


public class PdfFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String fn = f.getName();
        String ext = fn.substring(fn.lastIndexOf('.') + 1);
        //System.out.println(ext);
        return ext.equalsIgnoreCase("pdf");
    }

    @Override
    public String getDescription() {
        return "PDF files(*.pdf)";
    }

}
