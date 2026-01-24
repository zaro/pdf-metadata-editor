package pmedit.prefs;

import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.Win32Exception;
import pmedit.OsCheck;

import java.io.*;
import java.util.stream.Collectors;

public class LocalDataDir {
    final static  boolean isTesting = System.getProperty("junitTest", "").equals("true");

    // according to : https://msdn.microsoft.com/en-us/library/windows/desktop/dd378457(v=vs.85).aspx
    public static final String FOLDERID_APPDATA = "{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}";

    public static String getAppDataDir(){
        if(isTesting){
            File tempDir = new File("target/test-output/preferences/");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            return tempDir.getAbsolutePath();
        }
        return get() + File.separator + "pdf-metadata-editor" + File.separator;
    }

    public static String get() {
        String dir;
        switch (OsCheck.getOperatingSystemType()) {
            case Windows:
                dir = getWindows();
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

        if (!dir.isEmpty() && !dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        return dir;
    }

    private static String getWindows() {
        String dir = "";
        try {
            dir = Shell32Util.getKnownFolderPath(GUID.fromString(FOLDERID_APPDATA));
        } catch (Win32Exception e) {
            try {
                dir = Shell32Util.getFolderPath(ShlObj.CSIDL_APPDATA);
            } catch (Exception e1) {
            }
        } catch (UnsatisfiedLinkError e) {
            try {
                dir = Shell32Util.getFolderPath(ShlObj.CSIDL_APPDATA);
            } catch (Exception e1) {
            }
        }
        return dir;
    }

    // according to : https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
    private static String getLinux() {
        String dir = System.getenv("XDG_CONFIG_HOME");
        if (dir == null || dir.length() == 0) {
            String home = System.getenv("HOME");
            if (home == null || home.length() == 0) {
                return "";
            }
            return home + File.separator + ".config";
        }
        return dir;
    }

    // according to: https://developer.apple.com/library/mac/qa/qa1170/_index.html
    private static String getMacos() {
        return System.getProperty("user.home") + "/Library/Preferences/";
    }

    public static File getResourceFileAsLocalFile(String fileName) throws IOException {
        ClassLoader classLoader = OsCheck.class.getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            File out = new File(getAppDataDir() + File.separator + fileName);
            out.getParentFile().mkdirs();
            is.transferTo(new FileOutputStream(out));
            return  out;
        }
    }

    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = OsCheck.class.getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

}
