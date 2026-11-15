package pmedit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppLauncherTest {

    @TempDir
    Path tempDir;

    @Test
    public void findsCliSiblingOfGuiLauncher() throws Exception {
        Path gui = Files.createFile(tempDir.resolve("Pdf Metadata Editor"));
        Path cli = Files.createFile(tempDir.resolve("pmedit-cli"));

        assertEquals(cli.toAbsolutePath().normalize(), AppLauncher.cliSibling(gui));
    }

    @Test
    public void keepsCliLauncherItself() throws Exception {
        Path cli = Files.createFile(tempDir.resolve("pmedit-cli"));

        assertEquals(cli.toAbsolutePath().normalize(), AppLauncher.cliSibling(cli));
    }

    @Test
    public void findsGuiSiblingOfCliLauncher() throws Exception {
        Path cli = Files.createFile(tempDir.resolve("pmedit-cli"));
        Path gui = Files.createFile(tempDir.resolve(Version.getAppName() + ".exe"));

        assertEquals(gui.toAbsolutePath().normalize(), AppLauncher.guiSibling(cli));
    }

    @Test
    public void returnsNullWhenNoCliSiblingExists() throws Exception {
        Path gui = Files.createFile(tempDir.resolve("Pdf Metadata Editor"));

        assertNull(AppLauncher.cliSibling(gui));
    }

    @Test
    public void identifiesLauncherKinds() {
        assertTrue(AppLauncher.isCliLauncher(Path.of("/opt/app/bin/pmedit-cli")));
        assertTrue(AppLauncher.isCliLauncher(Path.of("C:/app/pmedit-cli.exe")));
        assertFalse(AppLauncher.isCliLauncher(Path.of("/opt/app/bin/Pdf Metadata Editor")));

        assertTrue(AppLauncher.isJavaLauncher(Path.of("/usr/bin/java")));
        assertTrue(AppLauncher.isJavaLauncher(Path.of("C:/Java/bin/javaw.exe")));
        assertFalse(AppLauncher.isJavaLauncher(Path.of("/opt/app/bin/pmedit-cli")));
    }

    @Test
    public void guiExecutableUsesJpackagePath() throws Exception {
        Path gui = Files.createFile(tempDir.resolve("Pdf Metadata Editor.exe"));
        withJpackagePath(gui, () -> assertEquals(
                gui.toAbsolutePath().normalize(),
                AppLauncher.guiExecutable()));
    }

    @Test
    public void guiExecutableResolvesGuiWhenJpackagePathIsCli() throws Exception {
        Path cli = Files.createFile(tempDir.resolve("pmedit-cli"));
        Path gui = Files.createFile(tempDir.resolve(Version.getAppName() + ".exe"));

        withJpackagePath(cli, () -> assertEquals(
                gui.toAbsolutePath().normalize(),
                AppLauncher.guiExecutable()));
    }

    @Test
    public void mcpLaunchUsesCliSiblingOfJpackageApp() throws Exception {
        Assumptions.assumeFalse(OsCheck.isFlatpak(), "Running inside Flatpak");
        Assumptions.assumeFalse(OsCheck.isAppImage(), "Running inside an AppImage");

        Path gui = Files.createFile(tempDir.resolve("Pdf Metadata Editor"));
        Path cli = Files.createFile(tempDir.resolve("pmedit-cli"));

        withJpackagePath(gui, () -> {
            AppLauncher.LaunchCommand launch = AppLauncher.mcpLaunch();
            assertEquals(cli.toAbsolutePath().normalize().toString(), launch.command());
            assertTrue(launch.arguments().isEmpty());
            assertEquals("Installed desktop app", launch.description());
        });
    }

    private static void withJpackagePath(Path path, CheckedRunnable action) throws Exception {
        String previous = System.getProperty("jpackage.app-path");
        System.setProperty("jpackage.app-path", path.toAbsolutePath().toString());
        try {
            action.run();
        } finally {
            if (previous == null) {
                System.clearProperty("jpackage.app-path");
            } else {
                System.setProperty("jpackage.app-path", previous);
            }
        }
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
