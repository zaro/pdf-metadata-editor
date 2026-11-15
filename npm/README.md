# PDF Metadata Editor MCP

A [Model Context Protocol](https://modelcontextprotocol.io/) server for reading
and editing PDF metadata.

The npm package starts the PDF Metadata Editor MCP server over stdio. It
selects a platform bundle when one is available, and otherwise falls back to a
system Java installation.

## Requirements

- Node.js 18 or newer.
- No Java installation is required on supported platforms.
- On unsupported platforms, or when optional dependencies are omitted, Java 17+
  is used with the bundled application JAR.

## Usage

### npx

```sh
npx -y pdf-metadata-editor-mcp@next
```

### Global install

```sh
npm install -g pdf-metadata-editor-mcp@next
pdf-metadata-editor-mcp
```

By default, the server is limited to the current working directory. Use
`--mcp-root` only when you want to restrict access to a specific folder.

The examples use the `@next` prerelease tag. After the stable `4.5.0` release,
remove `@next`.

## Choose your client

The configuration shape differs between MCP clients. Use the section for your
client rather than copying a `mcpServers` block into a client that expects
`servers`, `mcp.servers`, or TOML.

| Client | Where to configure | Format |
|---|---|---|
| OpenCode | `opencode.json(c)` or `~/.config/opencode/opencode.json(c)` | `mcp.servers` |
| Claude Code | `.mcp.json` or `claude mcp add` | `mcpServers` |
| Codex CLI | `~/.codex/config.toml` or `.codex/config.toml` | `[mcp_servers.*]` |
| Cursor | `~/.cursor/mcp.json` or `.cursor/mcp.json` | `mcpServers` |
| VS Code / GitHub Copilot | `.vscode/mcp.json` | `servers` |
| Claude Desktop | `claude_desktop_config.json` | `mcpServers` |

## OpenCode

Add the server to the current project:

```sh
opencode mcp add pdf-metadata-editor -- npx -y pdf-metadata-editor-mcp@next
```

Add it globally:

```sh
opencode mcp add pdf-metadata-editor --global -- npx -y pdf-metadata-editor-mcp@next
```

Manual configuration in `opencode.jsonc`:

```jsonc
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

OpenCode uses `mcp.servers`, requires `"type": "local"` for local servers, and
stores the executable and arguments in a single `command` array. Local servers
start in the workspace directory by default, so `--mcp-root` is not needed for
normal use.

Global configuration lives at `~/.config/opencode/opencode.json(c)`. Project
configuration can be `opencode.json(c)` or `.opencode/opencode.json(c)`.

## Claude Code

Add the server with the Claude Code CLI:

```sh
claude mcp add --transport stdio pdf-metadata-editor -- npx -y pdf-metadata-editor-mcp@next
```

For a project-scoped server:

```sh
claude mcp add --scope project --transport stdio pdf-metadata-editor -- npx -y pdf-metadata-editor-mcp@next
```

Or add it to `.mcp.json`:

```json
{
  "mcpServers": {
    "pdf-metadata-editor": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "pdf-metadata-editor-mcp@next"]
    }
  }
}
```

On Windows, if `npx` is not available to Claude Code, wrap the command:

```sh
claude mcp add --transport stdio pdf-metadata-editor -- cmd /c npx -y pdf-metadata-editor-mcp@next
```

## Codex CLI

Add the server to `~/.codex/config.toml`, or to `.codex/config.toml` for a
project-scoped configuration:

```toml
[mcp_servers.pdf-metadata-editor]
command = "npx"
args = ["-y", "pdf-metadata-editor-mcp@next"]
```

## Cursor

Global configuration is `~/.cursor/mcp.json`; project configuration is
`.cursor/mcp.json`.

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

## VS Code / GitHub Copilot

Use `.vscode/mcp.json` in the workspace:

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

VS Code and GitHub Copilot use the `servers` key, not `mcpServers`.

## Claude Desktop

Configuration file locations:

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: use your client’s documented configuration path

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

Restart Claude Desktop after changing the file.

## Other clients

If your client uses the `mcpServers` shape, use `command` and `args`:

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

If the package is installed globally, use the binary directly:

```json
{
  "mcpServers": {
    "pdf-metadata-editor": {
      "command": "pdf-metadata-editor-mcp",
      "args": []
    }
  }
}
```

Follow your client’s documentation if it uses a different key or file format.

## Optional: restrict file access

`--mcp-root` is an additional security measure, not a required setting. The
default root is the process working directory. Use `--mcp-root` when you want a
fixed, narrower scope:

```sh
npx -y pdf-metadata-editor-mcp@next --mcp-root /absolute/path/to/pdfs
```

In a client that uses `mcpServers`:

```json
{
  "mcpServers": {
    "pdf-metadata-editor": {
      "command": "npx",
      "args": [
        "-y",
        "pdf-metadata-editor-mcp@next",
        "--mcp-root",
        "/absolute/path/to/pdfs"
      ]
    }
  }
}
```

In OpenCode:

```jsonc
{
  "$schema": "https://opencode.ai/config.json",
  "mcp": {
    "servers": {
      "pdf-metadata-editor": {
        "type": "local",
        "command": [
          "npx",
          "-y",
          "pdf-metadata-editor-mcp@next",
          "--mcp-root",
          "/absolute/path/to/pdfs"
        ]
      }
    }
  }
}
```

Project-oriented clients such as OpenCode, Claude Code, and Cursor usually start
in the project directory. Desktop clients may use a different working
directory; set `--mcp-root` there if you need a predictable scope.

## Options

| Option | Description |
|---|---|
| `--mcp-root DIR` | Optional. Restrict file access to `DIR`; defaults to the current working directory. |
| `--mcp-read-only` | Disable every mutating tool. |
| `--doctor` | Print the selected runtime, JAR path, root, and write mode. |
| `--version` | Print the package version. |
| `--help` | Print usage. |

## Tools

Every file operation is a batch operation, matching the CLI. Tools take a
`paths` array containing files and/or directories; a single file is
`paths: ["one.pdf"]`. Directories are searched recursively for `.pdf` files.
A batch may contain at most 100 PDF files; larger requests are rejected before
any file is touched.

| Tool | Purpose |
|---|---|
| `pdf_list_fields` | List valid metadata field IDs, types, and writability. |
| `pdf_read_metadata` | Read metadata from one or more PDFs or directories. |
| `pdf_set_metadata` | Set one or more metadata fields on every input PDF (CLI `edit`). |
| `pdf_clear_metadata` | Clear all or selected metadata fields on every input PDF (CLI `clear`). |
| `pdf_rename` | Rename every input PDF using a metadata template (CLI `rename`). |

`pdf_list_fields` and `pdf_read_metadata` are available without a license. The
mutating tools may require a license; the server returns a clear message when
one is needed.

### Batch results

Every batch tool returns a summary plus one entry per file:

```json
{
  "summary": { "total": 2, "succeeded": 1, "failed": 1, "truncated": false },
  "results": [
    { "input": "docs/a.pdf", "output": "out/a.pdf", "ok": true, "action": "updated" },
    {
      "input": "docs/b.pdf",
      "ok": false,
      "error": { "code": "password_required", "message": "This PDF is password-protected. Retry with the ownerPassword argument." }
    }
  ]
}
```

A per-file failure does not abort the batch. The tool result is only marked as
an error when the request itself is invalid or every file failed.

### Output directory

`pdf_set_metadata`, `pdf_clear_metadata`, and `pdf_rename` accept an optional
`outputDir`. The input directory structure is preserved beneath it. When
`outputDir` is omitted, set and clear update files in place, and rename moves
files within their current directory.

```json
{
  "paths": ["docs"],
  "metadata": { "doc.title": "Weekly Report", "doc.author": "Jane Doe" },
  "outputDir": "out"
}
```

`docs/a.pdf` becomes `out/a.pdf`; `docs/sub/b.pdf` becomes `out/sub/b.pdf`.

### Templates

Values can use `{field.id}` templates expanded from each file's own metadata:

```json
{
  "paths": ["docs"],
  "metadata": { "doc.title": "{doc.author} - report" }
}
```

### Password-protected PDFs

Pass `ownerPassword` to the tool. A protected PDF without a password produces a
per-file error entry:

```json
{"code":"password_required","message":"This PDF is password-protected. Retry with the ownerPassword argument."}
```

If the supplied password is rejected:

```json
{"code":"invalid_password","message":"The supplied ownerPassword could not open this PDF."}
```

## Supported platforms

The package includes platform bundles for:

- Linux x64
- Linux arm64
- macOS arm64
- Windows x64
- Windows arm64

On other platforms, the wrapper falls back to system Java 17+ and the bundled
application JAR.

## Security

- The server is limited to the current working directory by default.
- Use `--mcp-root` to narrow access to a specific directory.
- Paths cannot escape the selected root; symlink escapes are rejected.
- Explicit non-PDF inputs are rejected; non-PDF files inside directories are ignored.
- Use `--mcp-read-only` when write access is not needed.

## Troubleshooting

### Inspect the selected runtime

```sh
npx -y pdf-metadata-editor-mcp@next --doctor
```

### Java fallback

If no platform bundle is available, install Java 17 or newer and optionally
point the wrapper at it:

```sh
PDF_METADATA_EDITOR_JAVA=/path/to/java pdf-metadata-editor-mcp
```

### Custom JAR

For development or a custom build, point the wrapper at an application JAR:

```sh
PDF_METADATA_EDITOR_JAR=/path/to/pdf-metadata-editor.jar pdf-metadata-editor-mcp
```

### Startup logs

The wrapper reserves stdout for MCP JSON-RPC messages. Diagnostic messages are
written to stderr.

## License

GPL-3.0. See [LICENSE](LICENSE).
