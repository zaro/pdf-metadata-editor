package pmedit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Detects how the currently running application can be launched again.
 *
 * <p>MCP configuration needs a command that a client can execute from outside
 * the current process. That is usually the {@code pmedit-cli} launcher, an
 * AppImage, a Flatpak invocation, or a Java command for a JAR build.</p>
 */
public final class AppLauncher {

    public record LaunchCommand(String command, List<String> arguments, String description) {

        public LaunchCommand {
            command = command == null || command.isBlank() ? "pmedit-cli" : command;
            arguments = arguments == null ? List.of() : List.copyOf(arguments);
            description = description == null || description.isBlank() ? "Application" : description;
        }

        public List<String> mcpArguments(String root) {
            List<String> args = new ArrayList<>(arguments);
            args.add("--mcp");
            if (root != null && !root.isBlank()) {
                args.add("--mcp-root");
                args.add(root);
            }
            return args;
        }

        public List<String> commandLine(String... extra) {
            List<String> line = new ArrayList<>();
            line.add(command);
            line.addAll(arguments);
            if (extra != null) {
                line.addAll(List.of(extra));
            }
            return line;
        }
    }

    private AppLauncher() {
    }

    /**
     * The command an MCP client should run for this installation.
     */
    public static LaunchCommand mcpLaunch() {
        if (OsCheck.isFlatpak()) {
            return new LaunchCommand(
                    "flatpak",
                    List.of("run", OsCheck.flatpakId()),
                    "Flatpak");
        }

        if (OsCheck.isAppImage()) {
            Path appImage = absolute(OsCheck.appImagePath());
            if (appImage != null && Files.isRegularFile(appImage)) {
                Path cli = cliSibling(appImage);
                return new LaunchCommand(
                        (cli != null ? cli : appImage).toString(),
                        List.of(),
                        "AppImage");
            }
        }

        String jpackagePath = System.getProperty("jpackage.app-path");
        if (hasText(jpackagePath)) {
            Path app = absolute(jpackagePath);
            if (app != null && Files.isRegularFile(app)) {
                Path cli = cliSibling(app);
                if (cli != null) {
                    return new LaunchCommand(cli.toString(), List.of(), "Installed desktop app");
                }
                if (!isCliLauncher(app)) {
                    return new LaunchCommand(app.toString(), List.of(), "Installed desktop app");
                }
            }
        }

        Path pathCli = findOnPath(OsCheck.isWindows() ? "pmedit-cli.exe" : "pmedit-cli");
        if (pathCli != null) {
            return new LaunchCommand(pathCli.toString(), List.of(), "pmedit-cli");
        }

        Path process = processCommand();
        if (process != null && !isJavaLauncher(process)) {
            Path cli = cliSibling(process);
            if (cli != null) {
                return new LaunchCommand(cli.toString(), List.of(), "Application launcher");
            }
            if (!isCliLauncher(process)) {
                return new LaunchCommand(process.toString(), List.of(), "Application launcher");
            }
        }

        Path source = codeSource(AppLauncher.class);
        if (source != null && source.toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
            return new LaunchCommand(
                    javaExecutable(),
                    List.of("-jar", source.toString()),
                    "Application JAR");
        }
        if (source != null && Files.isDirectory(source)) {
            String classpath = System.getProperty("java.class.path");
            if (hasText(classpath)) {
                return new LaunchCommand(
                        javaExecutable(),
                        List.of("-cp", classpath, "pmedit.MainCli"),
                        "Development classpath");
            }
        }

        return new LaunchCommand("pmedit-cli", List.of(), "pmedit-cli");
    }

    /**
     * The GUI executable for this installation, or null when it cannot be
     * resolved. Used by Windows registry registration.
     */
    public static Path guiExecutable() {
        String jpackagePath = System.getProperty("jpackage.app-path");
        if (hasText(jpackagePath)) {
            Path app = absolute(jpackagePath);
            if (app != null && Files.isRegularFile(app)) {
                Path gui = isCliLauncher(app) ? guiSibling(app) : app;
                if (gui != null && Files.isRegularFile(gui)) {
                    return gui;
                }
            }
        }

        Path process = processCommand();
        if (process != null && !isJavaLauncher(process)) {
            Path gui = isCliLauncher(process) ? guiSibling(process) : process;
            if (gui != null && Files.isRegularFile(gui)) {
                return gui;
            }
        }

        Path source = codeSource(AppLauncher.class);
        if (source != null) {
            Path directory = Files.isDirectory(source) ? source : source.getParent();
            if (directory != null) {
                Path gui = existing(directory, Version.getAppName() + ".exe");
                if (gui != null) {
                    return gui;
                }
                gui = existing(directory, Version.getAppName());
                if (gui != null) {
                    return gui;
                }
                Path parent = directory.getParent();
                if (parent != null) {
                    gui = existing(parent, Version.getAppName() + ".exe");
                    if (gui != null) {
                        return gui;
                    }
                    gui = existing(parent, Version.getAppName());
                    if (gui != null) {
                        return gui;
                    }
                }
            }
        }

        return null;
    }

    static Path cliSibling(Path app) {
        if (app == null) {
            return null;
        }
        if (isCliLauncher(app)) {
            return app;
        }
        Path directory = app.getParent();
        if (directory == null) {
            return null;
        }
        for (String name : List.of("pmedit-cli", "pmedit-cli.exe")) {
            Path candidate = existing(directory, name);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    static Path guiSibling(Path cli) {
        if (cli == null || !isCliLauncher(cli)) {
            return cli;
        }
        Path directory = cli.getParent();
        if (directory == null) {
            return null;
        }
        for (String name : List.of(Version.getAppName() + ".exe", Version.getAppName())) {
            Path candidate = existing(directory, name);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    static Path findOnPath(String name) {
        String path = System.getenv("PATH");
        if (!hasText(path)) {
            return null;
        }
        for (String entry : path.split(Pattern.quote(File.pathSeparator))) {
            String directory = entry.trim();
            if (directory.isEmpty()) {
                continue;
            }
            if (directory.length() > 1
                    && directory.startsWith("\"")
                    && directory.endsWith("\"")) {
                directory = directory.substring(1, directory.length() - 1);
            }
            try {
                Path candidate = existing(Path.of(directory), name);
                if (candidate != null) {
                    return candidate;
                }
            } catch (RuntimeException e) {
                // Ignore malformed PATH entries.
            }
        }
        return null;
    }

    static boolean isCliLauncher(Path path) {
        String name = fileName(path);
        return name.equals("pmedit-cli") || name.equals("pmedit-cli.exe");
    }

    static boolean isJavaLauncher(Path path) {
        String name = fileName(path);
        return name.equals("java")
                || name.equals("java.exe")
                || name.equals("javaw")
                || name.equals("javaw.exe");
    }

    private static Path processCommand() {
        try {
            return ProcessHandle.current().info().command()
                    .map(Path::of)
                    .map(path -> path.toAbsolutePath().normalize())
                    .orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Path codeSource(Class<?> type) {
        try {
            var location = type.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            return Path.of(location.toURI()).toAbsolutePath().normalize();
        } catch (Exception e) {
            return null;
        }
    }

    private static String javaExecutable() {
        Path java = Path.of(
                System.getProperty("java.home"),
                "bin",
                OsCheck.isWindows() ? "java.exe" : "java");
        return java.toString();
    }

    private static Path existing(Path directory, String name) {
        Path candidate = directory.resolve(name);
        if (!Files.isRegularFile(candidate)) {
            return null;
        }
        return candidate.toAbsolutePath().normalize();
    }

    private static Path absolute(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Path.of(value).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String fileName(Path path) {
        if (path == null || path.getFileName() == null) {
            return "";
        }
        return path.getFileName().toString().toLowerCase(Locale.ROOT);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
