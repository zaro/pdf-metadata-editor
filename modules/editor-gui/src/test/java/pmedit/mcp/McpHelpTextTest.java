package pmedit.mcp;

import org.junit.jupiter.api.Test;
import pmedit.AppLauncher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpHelpTextTest {

    @Test
    public void describesInstalledDesktopLauncher() {
        AppLauncher.LaunchCommand launch = new AppLauncher.LaunchCommand(
                "/opt/Pdf Metadata Editor/bin/pmedit-cli",
                List.of(),
                "Installed desktop app");

        String text = McpHelpText.text(launch);

        assertTrue(text.startsWith("# MCP configuration"));
        assertTrue(text.contains("No npm install is required"));
        assertTrue(text.contains("/opt/Pdf Metadata Editor/bin/pmedit-cli"));
        assertTrue(text.contains("mcpServers"));
        assertTrue(text.contains("mcp.servers"));
        assertTrue(text.contains("claude mcp add"));
        assertTrue(text.contains("```json"));
        assertTrue(text.contains("```sh"));
        assertTrue(text.contains("[MCP help](https://pdf.metadata.care/help/)"));
        assertTrue(text.contains("--mcp-root"));
        assertTrue(text.contains("Add --mcp-root"));
        assertFalse(text.contains("\"--mcp-root\""));
        assertTrue(text.contains("--mcp-read-only"));
        assertTrue(text.contains("--mcp-batch-limit"));
        assertFalse(text.contains("npx"));
        assertFalse(text.contains("npm install -g"));
        assertFalse(text.contains("Flatseal"));
    }

    @Test
    public void listsSupportedOptions() {
        String text = McpHelpText.optionsText();

        assertTrue(text.startsWith("# Supported MCP options"));
        assertTrue(text.contains("--mcp"));
        assertTrue(text.contains("--mcp-root DIR"));
        assertTrue(text.contains("--mcp-read-only"));
        assertTrue(text.contains("--mcp-batch-limit N"));
        assertTrue(text.contains("--help-mcp"));
        assertTrue(text.contains("0 = unlimited"));
        assertTrue(text.contains("Require a license") || text.contains("may require a license"));
    }

    @Test
    public void describesFlatpakWithSandboxNotes() {
        AppLauncher.LaunchCommand launch = new AppLauncher.LaunchCommand(
                "flatpak",
                List.of("run", "me.broken_by.PdfMetadataEditor"),
                "Flatpak");

        String text = McpHelpText.text(launch);

        assertTrue(text.contains("flatpak run me.broken_by.PdfMetadataEditor"));
        assertTrue(text.contains("--mcp"));
        assertTrue(text.contains("Flatseal"));
        assertTrue(text.contains("flatpak override --user --filesystem="));
        assertTrue(text.contains("flatpak-spawn --host"));
    }

    @Test
    public void describesJarLauncher() {
        AppLauncher.LaunchCommand launch = new AppLauncher.LaunchCommand(
                "/usr/bin/java",
                List.of("-jar", "/opt/app/pdf-metadata-editor.jar"),
                "Application JAR");

        String text = McpHelpText.text(launch);

        assertTrue(text.contains("-jar"));
        assertTrue(text.contains("/opt/app/pdf-metadata-editor.jar"));
        assertTrue(text.contains("\"command\""));
    }

    @Test
    public void fillsSharedClientSections() {
        AppLauncher.LaunchCommand launch = new AppLauncher.LaunchCommand(
                "/usr/bin/pmedit-cli",
                List.of(),
                "Installed desktop app");

        String text = McpHelpText.text(launch);

        assertFalse(text.contains("{{"));
        assertTrue(text.contains("opencode mcp add pdf-metadata-editor -- /usr/bin/pmedit-cli --mcp"));
        assertTrue(text.contains("opencode mcp add pdf-metadata-editor --global -- "));
        assertTrue(text.contains("claude mcp add --transport stdio pdf-metadata-editor -- "));
        assertTrue(text.contains("[mcp_servers.pdf-metadata-editor]"));
        assertTrue(text.contains("\"servers\""));
    }

    @Test
    public void sharedJsonBlocksAreValid() throws Exception {
        AppLauncher.LaunchCommand launch = new AppLauncher.LaunchCommand(
                "/usr/bin/java",
                List.of("-jar", "/opt/app/pdf-metadata-editor.jar"),
                "Application JAR");

        List<String> blocks = jsonBlocks(McpHelpText.text(launch));

        assertEquals(3, blocks.size());
        for (String block : blocks) {
            McpJson.parse(block);
        }
    }

    @Test
    public void codexBlockReusesTheJsonArgsArray() {
        AppLauncher.LaunchCommand launch = new AppLauncher.LaunchCommand(
                "/usr/bin/java",
                List.of("-jar", "/opt/app/pdf-metadata-editor.jar"),
                "Application JAR");

        String text = McpHelpText.text(launch);

        assertTrue(text.contains("args = [\"-jar\", \"/opt/app/pdf-metadata-editor.jar\", \"--mcp\"]"));
    }

    @Test
    public void escapesWindowsPathsInSharedSections() {
        AppLauncher.LaunchCommand launch = new AppLauncher.LaunchCommand(
                "C:\\Program Files\\Pdf Metadata Editor\\pmedit-cli.exe",
                List.of(),
                "Installed desktop app");

        String text = McpHelpText.text(launch);

        assertTrue(text.contains("command = \"C:\\\\Program Files\\\\Pdf Metadata Editor\\\\pmedit-cli.exe\""));
    }

    private static List<String> jsonBlocks(String text) {
        List<String> blocks = new ArrayList<>();
        int index = 0;
        while (true) {
            int start = text.indexOf("```json\n", index);
            if (start < 0) {
                return blocks;
            }
            int end = text.indexOf("\n```", start);
            blocks.add(text.substring(start + "```json\n".length(), end));
            index = end + 1;
        }
    }
}
