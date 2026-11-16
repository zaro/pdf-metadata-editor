## Generic MCP client configuration

This configuration applies to clients that use the *mcpServers* JSON shape:

- **Claude Desktop**: *claude_desktop_config.json* in *~/Library/Application Support/Claude* (macOS), *%APPDATA%\Claude* (Windows), or *~/.config/Claude* (Linux)
- **Claude Code**: *.mcp.json* in the project, or claude mcp add
- **Cursor**: *~/.cursor/mcp.json*, or *.cursor/mcp.json* in the project
- **Windsurf**: *~/.codeium/windsurf/mcp_config.json*
- **Cline**: *cline_mcp_settings.json* in the VS Code Cline extension settings

Other clients may use a different shape; OpenCode, VS Code/Copilot, and Codex are covered separately below.

```json
{
  "mcpServers": {
    "pdf-metadata-editor": {
      "command": "npx",
      "args": ["-y", "pdf-metadata-editor-mcp@next"]
    }
  }
}
```

## OpenCode (mcp.servers)

Add it to the current project:

```sh
opencode mcp add pdf-metadata-editor -- npx -y pdf-metadata-editor-mcp@next
```

Add it globally:

```sh
opencode mcp add pdf-metadata-editor --global -- npx -y pdf-metadata-editor-mcp@next
```

Or configure *opencode.json* (or *opencode.jsonc*) manually. The project file is
*opencode.json* in the project root; the global file is
*~/.config/opencode/opencode.json*.

```json
{
  "$schema": "https://opencode.ai/config.json",
  "mcp": {
    "servers": {
      "pdf-metadata-editor": {
        "type": "local",
        "command": ["npx", "-y", "pdf-metadata-editor-mcp@next"]
      }
    }
  }
}
```

## Claude Code

Add the server with the Claude Code CLI:

```sh
claude mcp add --transport stdio pdf-metadata-editor -- npx -y pdf-metadata-editor-mcp@next
```

For a project-scoped server, add --scope project. Or add it to *.mcp.json*, which
uses the *mcpServers* shape shown above.

## VS Code / Copilot (servers)

Use *.vscode/mcp.json* in the workspace. VS Code and Copilot use *servers*, not *mcpServers*.

```json
{
  "servers": {
    "pdf-metadata-editor": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "pdf-metadata-editor-mcp@next"]
    }
  }
}
```

## Codex (mcp_servers)

Add this to *~/.codex/config.toml*, or *.codex/config.toml* for a project-scoped configuration.

```toml
[mcp_servers.pdf-metadata-editor]
command = "npx"
args = ["-y", "pdf-metadata-editor-mcp@next"]
```
