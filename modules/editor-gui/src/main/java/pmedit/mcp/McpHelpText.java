package pmedit.mcp;

import pmedit.AppLauncher;
import pmedit.OsCheck;
import pmedit.Version;
import pmedit.WebsiteConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Generates the MCP configuration guide shown by {@code --help-mcp} and by
 * the GUI Help → MCP dialog.
 *
 * <p>The client sections are shared with {@code npm/README.md} and live in
 * {@code docs/mcp-clients.md}. This class fills the placeholders with the
 * detected launcher; {@code npm/scripts/gen-readme.js} fills the same
 * placeholders with the npm invocation.</p>
 */
public final class McpHelpText {

    public static final String DEFAULT_ROOT = "/path/to/pdfs";

    static final String CLIENTS_RESOURCE = "/pmedit/mcp/mcp-clients.md";

    private McpHelpText() {
    }

    public static String text() {
        return text(AppLauncher.mcpLaunch());
    }

    static String text(AppLauncher.LaunchCommand launch) {
        String root = DEFAULT_ROOT;
        String commandLine = shellJoin(launch.commandLine("--mcp"));

        StringBuilder out = new StringBuilder();
        out.append("# MCP configuration for ")
                .append(Version.getAppName())
                .append(' ')
                .append(Version.get().getAsString())
                .append("\n\n");
        out.append("No npm install is required. The installed desktop app includes the MCP server.\n\n");
        out.append("## Detected launcher\n\n");
        appendCode(out, "text", commandLine);
        out.append('\n');
        out.append(clientsText(launch));
        out.append("\n## Notes\n\n");
        out.append("- Add --mcp-root ").append(root).append(" to restrict file access. Without it, the client's working directory is used.\n");
        out.append("- Add --mcp-read-only to disable mutating tools.\n");
        out.append("- Add --mcp-batch-limit N to change the default limit of 100 (0 = unlimited).\n");
        out.append("- On Windows, use the command syntax required by your shell or client.\n");

        if (isFlatpak(launch)) {
            String flatpakId = flatpakId(launch);
            out.append("\n## Flatpak notes\n\n");
            out.append("- The app runs in a filesystem sandbox. The --mcp-root folder must be visible to it.\n");
            out.append("- If it is not, use a folder the app can access, grant access with Flatseal, or run:\n\n");
            appendCode(out, "sh",
                    "flatpak override --user --filesystem=" + root + " " + flatpakId);
            out.append("\n- If the MCP client itself runs in a sandbox, use flatpak-spawn --host flatpak run ...\n");
        }

        out.append("\n## Documentation\n\n");
        out.append("[MCP help](").append(WebsiteConstants.websiteUrl).append("help/)\n");
        return out.toString();
    }

    /**
     * The shared client sections with this installation's launcher filled in.
     */
    static String clientsText(AppLauncher.LaunchCommand launch) {
        return clientsText(launch, readClientsTemplate());
    }

    static String clientsText(AppLauncher.LaunchCommand launch, String template) {
        List<String> commandLine = launch.commandLine("--mcp");
        String filled = template
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("{{COMMAND_AND_ARGS}}", jsonArray(commandLine))
                .replace("{{COMMAND_LINE}}", shellJoin(commandLine))
                .replace("{{COMMAND}}", McpJson.quote(launch.command()))
                .replace("{{ARGS}}", jsonArray(launch.mcpArguments(null)));
        if (filled.contains("{{")) {
            throw new IllegalStateException("Unresolved placeholder in " + CLIENTS_RESOURCE);
        }
        return filled;
    }

    private static String readClientsTemplate() {
        try (InputStream in = McpHelpText.class.getResourceAsStream(CLIENTS_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + CLIENTS_RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + CLIENTS_RESOURCE, e);
        }
    }

    private static String jsonArray(List<String> values) {
        StringBuilder array = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                array.append(", ");
            }
            array.append(McpJson.quote(values.get(i)));
        }
        return array.append(']').toString();
    }

    public static String optionsText() {
        return "# Supported MCP options\n\n"
                + "## --mcp\n\n"
                + "Start the MCP server on stdin/stdout.\n\n"
                + "## --mcp-root DIR\n\n"
                + "Restrict file access to DIR. Default: the client's working directory.\n"
                + "Also accepted as --mcp-root=DIR.\n\n"
                + "## --mcp-read-only\n\n"
                + "Disable mutating tools. Read and list tools remain available.\n\n"
                + "## --mcp-batch-limit N\n\n"
                + "Maximum files or operations per batch. Default: 100. 0 = unlimited.\n"
                + "Also accepted as --mcp-batch-limit=N.\n\n"
                + "## --help-mcp\n\n"
                + "Print this MCP configuration guide and exit.\n\n"
                + "## Notes\n\n"
                + "- MCP options are CLI options and appear before the command/arguments.\n"
                + "- The server uses stdio; client diagnostics must not share stdout.\n"
                + "- Mutating tools may require a license.\n";
    }

    private static void appendCode(StringBuilder out, String language, String code) {
        out.append("```");
        if (language != null && !language.isBlank()) {
            out.append(language);
        }
        out.append('\n');
        out.append(code);
        if (!code.endsWith("\n")) {
            out.append('\n');
        }
        out.append("```\n");
    }

    private static boolean isFlatpak(AppLauncher.LaunchCommand launch) {
        return "Flatpak".equalsIgnoreCase(launch.description())
                || (launch.arguments().size() >= 2
                && "run".equals(launch.arguments().get(0))
                && "flatpak".equalsIgnoreCase(launch.command()));
    }

    private static String flatpakId(AppLauncher.LaunchCommand launch) {
        List<String> arguments = launch.arguments();
        if (arguments.size() >= 2 && "run".equals(arguments.get(0))) {
            return arguments.get(1);
        }
        return OsCheck.flatpakId();
    }

    private static String shellJoin(List<String> parts) {
        StringBuilder line = new StringBuilder();
        for (String part : parts) {
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(shellQuote(part));
        }
        return line.toString();
    }

    private static String shellQuote(String value) {
        if (value == null || value.isEmpty()) {
            return "''";
        }
        if (OsCheck.isWindows()) {
            if (!value.contains(" ") && !value.contains("\"")) {
                return value;
            }
            return "\"" + value.replace("\"", "\\\"") + "\"";
        }
        if (value.matches("[A-Za-z0-9_./:@%+=,-]+")) {
            return value;
        }
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
