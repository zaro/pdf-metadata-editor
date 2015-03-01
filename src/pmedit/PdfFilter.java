package pmedit;
import java.io.File;

import javax.swing.filechooser.FileFilter;


public class PdfFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if(f.isDirectory()){
			return true;
		}
		String fn = f.getName();
		String ext  = fn.substring(fn.lastIndexOf('.') + 1);
		//System.out.println(ext);
		if(ext.equalsIgnoreCase("pdf")){
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "PDF files(*.pdf)";
	}

}
