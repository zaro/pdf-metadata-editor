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

<!-- BEGIN MCP CLIENTS -->
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
<!-- END MCP CLIENTS -->

## Other clients

If your client uses the `mcpServers` shape, start from the generic block above.

- `npx` install: `command` is `npx`, `args` is `["-y", "pdf-metadata-editor-mcp@next"]`.
- Global install: `command` is `pdf-metadata-editor-mcp`, `args` is `[]`.

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
| `--mcp-batch-limit N` | Maximum files or operations per batch. Default `100`; `0` means unlimited. |
| `--mcp-walk-timeout N` | Maximum seconds spent expanding a directory input. Default `20`; `0` means unlimited. |
| `--doctor` | Print the selected runtime, JAR path, root, batch limit, and write mode. |
| `--version` | Print the package version. |
| `--help` | Print usage. |

## Tools

Every file operation is a batch operation, matching the CLI. PDF tools take a
`paths` array containing files and/or directories; a single file is
`paths: ["one.pdf"]`. Import tools use `importPaths` for JSON/YAML/CSV files and
directories. Directories are searched recursively for the matching extensions.

The default batch limit is 100 files or operations. Use `--mcp-batch-limit N`
to change it; `0` removes the limit.

| Tool | Purpose |
|---|---|
| `pdf_list_fields` | List valid metadata field IDs, types, and writability. |
| `pdf_read_metadata` | Read metadata from one or more PDFs or directories. |
| `pdf_set_metadata` | Set metadata fields on every input PDF (CLI `edit`). |
| `pdf_clear_metadata` | Clear metadata fields on every input PDF (CLI `clear`). |
| `pdf_rename` | Rename every input PDF using a metadata template (CLI `rename`). |
| `pdf_export_json` | Export metadata to JSON (CLI `tojson`). |
| `pdf_export_yaml` | Export metadata to YAML (CLI `toyaml`). |
| `pdf_export_csv` | Export metadata to CSV (CLI `tocsv`). |
| `pdf_import_from_filename` | Set metadata extracted from PDF file names (CLI `fromfilename`). |
| `pdf_import_from_json` | Import metadata from JSON (CLI `fromjson`). |
| `pdf_import_from_yaml` | Import metadata from YAML (CLI `fromyaml`). |
| `pdf_import_from_csv` | Import metadata from CSV (CLI `fromcsv`). |
| `pdf_license` | Check, activate, or release the license (CLI `--showLicense`, `--license`, `--releaseLicense`). |

`pdf_list_fields`, `pdf_read_metadata`, and `pdf_license` are available without a
license. The CLI-parity batch tools may require a license; the server returns a
clear message when one is needed.

`pdf_license` takes an `action` of `status`, `activate`, or `release`. Activation
takes the license information as one comma-separated string — the email address
and license ID from the purchase email — and passes it to the license provider
unchanged.

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

### Exports

Export tools write files and return their paths in an `outputs` list.

```json
{
  "paths": ["docs"],
  "fields": ["all"],
  "outputDir": "exports",
  "outputFile": "metadata-export",
  "perFileExport": false,
  "relativePaths": true
}
```

- `fields` defaults to all readable, non-secret fields, including read-only
  file properties.
- `perFileExport: false` writes one aggregate file. `true` writes one file per
  PDF.
- `relativePaths: true` writes `file.fullPath` relative to the input directory.
  Set it to `false` to write absolute paths.
- When exporting to a custom `outputDir`, relative `file.fullPath` values are
  still based on the input directory, not the export directory. For round-trip
  imports from a custom export directory, use `relativePaths: false` or keep the
  export file beside the inputs.
- `file.fullPath` is always included so the export can be imported again.
- Aggregate and per-file output write failures are reported as errors.

### Imports

```json
{
  "importPaths": ["exports/metadata-export.json"],
  "outputDir": "imported"
}
```

- JSON/YAML/CSV records must contain `file.fullPath`.
- Relative PDF paths resolve against the import file's directory.
- With `outputDir`, relative paths preserve their subdirectories. Absolute paths
  that are outside the import file's directory are flattened to the PDF file
  name, matching the CLI.
- Referenced PDFs and outputs must stay inside the MCP root.
- Parse failures are reported per import file and do not stop other files.
- A record can contain `prop.ownerPassword`; it opens that PDF and overrides the
  top-level `ownerPassword` argument. `prop.userPassword` is applied only as an
  encryption setting, not as the password used to open the file.
- Import files may contain passwords; treat them as sensitive.

`pdf_import_from_filename` uses a template instead of import files:

```json
{
  "paths": ["docs"],
  "template": "{doc.author} - {doc.title}",
  "outputDir": "imported"
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
- Explicit inputs with the wrong extension are rejected; unrelated files inside
  directories are ignored.
- Import files can contain per-record passwords; treat them as sensitive.
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

### Log file

Each MCP server writes its own log under the application data directory
(`pdf-metadata-editor/mcp/mcp-<timestamp>-<pid>.log`) and prints the path to
stderr on startup. The desktop app keeps using its own `log.txt`, so starting
one no longer rotates the other's log. The ten most recent server logs are kept.

### Startup logs

The wrapper reserves stdout for MCP JSON-RPC messages. Diagnostic messages are
written to stderr.

## License

GPL-3.0. See [LICENSE](LICENSE).
