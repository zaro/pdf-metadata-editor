package pmedit;

/**
 * helper class to check the operating system this Java VM runs in
 * <p>
 * please keep the notes below as a pseudo-license
 * <p>
 * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
 * compare to http://svn.terracotta.org/svn/tc/dso/tags/2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
 * http://www.docjar.com/html/api/org/apache/commons/lang/SystemUtils.java.html
 */

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public final class OsCheck {
    // cached result of OS detection
    private static OSType detectedOS;
    private static String computerName;

    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @returns - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                detectedOS = OSType.MacOS;
            } else if (OS.indexOf("win") >= 0) {
                detectedOS = OSType.Windows;
            } else if (OS.indexOf("nux") >= 0) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }

    public static boolean isWindows() {
        return getOperatingSystemType() == OSType.Windows;
    }
    public static boolean isLinux() {
        return getOperatingSystemType() == OSType.Linux;
    }
    public static boolean isMacOs() {return  getOperatingSystemType() == OSType.MacOS; }

    public static String getComputerName() {
        if(computerName == null) {
            // Method 1: Try OS-specific environment variables first
            computerName = System.getenv("COMPUTERNAME"); // Windows
            if (computerName == null || computerName.isEmpty()) {
                computerName = System.getenv("HOSTNAME"); // Linux/Mac
            }

            if (computerName == null || computerName.isEmpty()) {
                try {
                    // Method 2: Use InetAddress as fallback
                    computerName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    computerName = "Unknown";
                }
            }
        }
        return computerName;
    }

    /**
     * types of Operating Systems
     */
    public enum OSType {
        Windows, MacOS, Linux, Other
    }
}
