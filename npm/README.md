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
npx -y pdf-metadata-editor-mcp@next --mcp-root /path/to/pdfs
```

### Global install

```sh
npm install -g pdf-metadata-editor-mcp@next
pdf-metadata-editor-mcp --mcp-root /path/to/pdfs
```

`--mcp-root` should be set explicitly. It is the only directory tree the server
may read or write.

## MCP client configuration

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

Alternatively, use the globally installed command:

```json
{
  "mcpServers": {
    "pdf-metadata-editor": {
      "command": "pdf-metadata-editor-mcp",
      "args": ["--mcp-root", "/absolute/path/to/pdfs"]
    }
  }
}
```

## Options

| Option | Description |
|---|---|
| `--mcp-root DIR` | Restrict all file access to `DIR` (default: current directory). |
| `--mcp-read-only` | Disable every mutating tool. |
| `--doctor` | Print the selected runtime, JAR path, root, and write mode. |
| `--version` | Print the package version. |
| `--help` | Print usage. |

## Tools

| Tool | Purpose |
|---|---|
| `pdf_list_fields` | List valid metadata field IDs, types, and writability. |
| `pdf_read_metadata` | Read metadata from a PDF. |
| `pdf_set_metadata` | Set one or more metadata fields. |
| `pdf_clear_metadata` | Clear all or selected metadata fields. |
| `pdf_rename` | Rename a PDF using a metadata template. |
| `pdf_export_metadata` | Export metadata as JSON or YAML. |

Read-only tools are available without a license. Some write operations may
require a license; the server returns a clear message when one is needed.

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

- All paths are resolved against `--mcp-root` and may not escape it.
- Symlink escapes are rejected.
- Only PDF files are accepted by the metadata tools.
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
PDF_METADATA_EDITOR_JAVA=/path/to/java pdf-metadata-editor-mcp --mcp-root /path/to/pdfs
```

### Custom JAR

For development or a custom build, point the wrapper at an application JAR:

```sh
PDF_METADATA_EDITOR_JAR=/path/to/pdf-metadata-editor.jar pdf-metadata-editor-mcp --mcp-root /path/to/pdfs
```

### Startup logs

The wrapper reserves stdout for MCP JSON-RPC messages. Diagnostic messages are
written to stderr.

## License

GPL-3.0. See [LICENSE](LICENSE).
