## Generic MCP client configuration

This configuration applies to clients that use the *mcpServers* JSON shape:

- **Claude Desktop**: *claude_desktop_config.json* in *~/Library/Application Support/Claude* (macOS), *%APPDATA%\Claude* (Windows), or *~/.config/Claude* (Linux)
- **Claude Code**: *.mcp.json* in the project, or claude mcp add
- **Cursor**: *~/.cursor/mcp.json*, or *.cursor/mcp.json* in the project
- **Windsurf**: *~/.codeium/windsurf/mcp_config.json*
- **Cline**: *cline_mcp_settings.json* in the VS Code Cline extension settings

Other clients may use a different shape; OpenCode, VS Code/Copilot, and Codex are covered separately below.

Directories in *paths* are walked recursively. A walk stops after the batch limit
(default 100 files) or after the walk timeout (default 20 seconds), and the call
fails instead of returning partial results.

```json
{
  "mcpServers": {
    "pdf-metadata-editor": {
      "command": {{COMMAND}},
      "args": {{ARGS}}
    }
  }
}
```

## OpenCode (mcp.servers)

Add it to the current project:

```sh
opencode mcp add pdf-metadata-editor -- {{COMMAND_LINE}}
```

Add it globally:

```sh
opencode mcp add pdf-metadata-editor --global -- {{COMMAND_LINE}}
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
        "command": {{COMMAND_AND_ARGS}}
      }
    }
  }
}
```

## Claude Code

Add the server with the Claude Code CLI:

```sh
claude mcp add --transport stdio pdf-metadata-editor -- {{COMMAND_LINE}}
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
      "command": {{COMMAND}},
      "args": {{ARGS}}
    }
  }
}
```

## Codex (mcp_servers)

Add this to *~/.codex/config.toml*, or *.codex/config.toml* for a project-scoped configuration.

```toml
[mcp_servers.pdf-metadata-editor]
command = {{COMMAND}}
args = {{ARGS}}
```
