package pmedit.prefs;

import java.io.File;
import java.util.Locale;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.PointerByReference;

import pmedit.OsCheck;

public class LocalDataDir {
	
	public static String get(){
		String dir;
		switch (OsCheck.getOperatingSystemType()) {
		    case Windows:
		    	dir =getWindows();
		    	break;
		    case MacOS:
		    	dir = getMacos();
		    	break;
		    case Linux:
		    	dir = getLinux();
		    	break;
		    default:
		    	dir = "";
		    	break;
		}
		
		if( dir.length() > 0 && !dir.endsWith(File.separator) ){
			dir = dir + File.separator;
		}
		return dir;
	}
	
	// according to : https://msdn.microsoft.com/en-us/library/windows/desktop/dd378457(v=vs.85).aspx
    public static final String  FOLDERID_APPDATA = "{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}";
	private static String getWindows() {
		String dir = "";
		try {
		 dir = Shell32Util.getKnownFolderPath(GUID.fromString(FOLDERID_APPDATA));
		} catch(Win32Exception e){
			try {
				dir = Shell32Util.getFolderPath(ShlObj.CSIDL_APPDATA);
			}  catch (Win32Exception e1) {
			}
		}
		return dir;
	}

	// according to : https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
	private static String getLinux() {
		String dir=System.getenv("XDG_CONFIG_HOME");
		if(dir == null || dir.length() == 0) {
			String home=System.getenv("HOME");
			if( home == null || home.length() ==0){
				return "";
			}
			return  home + File.separator + ".config";
		}
		return dir;
	}
	
	// according to: https://developer.apple.com/library/mac/qa/qa1170/_index.html
	private static String getMacos() {
		return System.getProperty("user.home") + "/Library/Preferences/";
	}
}
