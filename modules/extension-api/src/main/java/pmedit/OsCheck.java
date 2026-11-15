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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class OsCheck {
    public static final String DEFAULT_FLATPAK_ID = "me.broken_by.PdfMetadataEditor";

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

    /**
     * True when the current process runs inside a Flatpak sandbox.
     */
    public static boolean isFlatpak() {
        return hasText(System.getenv("FLATPAK_ID")) || Files.exists(Path.of("/.flatpak-info"));
    }

    /**
     * The Flatpak application ID, falling back to this application's ID.
     */
    public static String flatpakId() {
        String environmentId = System.getenv("FLATPAK_ID");
        if (hasText(environmentId)) {
            return environmentId.trim();
        }
        String infoId = flatpakIdFromInfo(Path.of("/.flatpak-info"));
        return hasText(infoId) ? infoId : DEFAULT_FLATPAK_ID;
    }

    static String flatpakIdFromInfo(Path infoFile) {
        if (infoFile == null || !Files.isRegularFile(infoFile)) {
            return null;
        }
        try {
            List<String> lines = Files.readAllLines(infoFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("name=")) {
                    String value = trimmed.substring("name=".length()).trim();
                    return value.isEmpty() ? null : value;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * True when the current process was launched from an AppImage.
     */
    public static boolean isAppImage() {
        return hasText(appImagePath());
    }

    /**
     * The AppImage path from the APPIMAGE environment variable, or null.
     */
    public static String appImagePath() {
        String path = System.getenv("APPIMAGE");
        return hasText(path) ? path.trim() : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String getComputerName() {
        if(computerName == null) {
            // On MacOs try to use scutil
            if(isMacOs()) {
                var result = RunCmd.execCmd(new String[]{"scutil", "--get", "ComputerName"});
                if(result.ok()) {
                    computerName = result.out().trim();
                }
            } else {
                // Try OS-specific environment variables first
                computerName = System.getenv("COMPUTERNAME"); // Windows
                if (computerName == null || computerName.isEmpty()) {
                    computerName = System.getenv("HOSTNAME"); // Linux/Mac
                }
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
