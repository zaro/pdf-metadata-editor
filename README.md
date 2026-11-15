# Pdf Metadata Editor

An advanced GUI editor for PDF Metadata. It is a Java-based graphical user interface (GUI) application that allows users to edit metadata in PDF files. 
The application utilizes [Apache PDFBox](https://pdfbox.apache.org/) as its core library for PDF processing and metadata manipulation.

## Installation

### Prebuild installers

Prebuilt installers for all supported operating systems  can be found from the [official website](https://pdf.metadata.care/) 
or from the releases section in this repository.

### Flathub

```sh
flatpak install flathub me.broken_by.PdfMetadataEditor
```
    

### AppImage (Linux)

Download the AppImage for your architecture (`x86_64` or `aarch64`) from the
[download page](https://pdf.metadata.care/download/), then:

```sh
chmod +x Pdf_Metadata_Editor-*.AppImage
./Pdf_Metadata_Editor-*.AppImage
```

The command line interface (and the batch launcher) are reachable through
symlinks with the respective name:

```sh
ln -s Pdf_Metadata_Editor-*.AppImage pmedit-cli
./pmedit-cli --help
```

### Arch Linux (AUR)

Arch Linux users can install the package from the AUR.
```sh
yay -S pdf-metadata-editor-bin
```

## MCP server

`pmedit-cli` can run as a [Model Context Protocol](https://modelcontextprotocol.io/)
server, letting AI clients read and edit PDF metadata through tools.

```sh
pmedit-cli --mcp [--mcp-root=DIR] [--mcp-read-only] [--mcp-batch-limit=N]
```

- `--mcp-root=DIR` restricts all file access to `DIR` (default: current directory).
- `--mcp-read-only` disables every mutating tool.
- `--mcp-batch-limit=N` sets the maximum files or operations per batch
  (default: 100, `0` = unlimited).
- `pdf_list_fields` and `pdf_read_metadata` are available without a license.
- The CLI-parity batch tools (`pdf_set_metadata`, `pdf_clear_metadata`,
  `pdf_rename`, the export tools, and the import tools) may require a license.

PDF tools take a `paths` array of files and/or directories, matching the CLI.
A single file is `paths: ["one.pdf"]`. Import tools use `importPaths` for the
JSON/YAML/CSV files and directories. Directories are searched recursively.

The server speaks newline-delimited JSON-RPC over stdin/stdout. Configure it in
your MCP client, for example:

```json
{
  "mcpServers": {
    "pdf-metadata-editor": {
      "command": "pmedit-cli",
      "args": ["--mcp", "--mcp-root", "/path/to/pdfs"]
    }
  }
}
```

Available tools:

| Tool | Purpose |
|---|---|
| `pdf_list_fields` | List valid metadata field IDs, types, and writability |
| `pdf_read_metadata` | Read metadata from one or more PDFs or directories |
| `pdf_set_metadata` | Set metadata fields on every input PDF (CLI `edit`) |
| `pdf_clear_metadata` | Clear metadata fields on every input PDF (CLI `clear`) |
| `pdf_rename` | Rename every input PDF using a metadata template (CLI `rename`) |
| `pdf_export_json` | Export metadata to JSON (CLI `tojson`) |
| `pdf_export_yaml` | Export metadata to YAML (CLI `toyaml`) |
| `pdf_export_csv` | Export metadata to CSV (CLI `tocsv`) |
| `pdf_import_from_filename` | Set metadata extracted from PDF file names (CLI `fromfilename`) |
| `pdf_import_from_json` | Import metadata from JSON (CLI `fromjson`) |
| `pdf_import_from_yaml` | Import metadata from YAML (CLI `fromyaml`) |
| `pdf_import_from_csv` | Import metadata from CSV (CLI `fromcsv`) |

Each batch tool returns a `summary` plus per-file `results`; a per-file failure
does not abort the batch. Exports additionally return an `outputs` list, and
import results include both the import-file `source` and the target PDF `input`.

The MCP server is also available as an npm package:

```sh
npx -y pdf-metadata-editor-mcp@next
```

See [npm/README.md](npm/README.md) for client configuration, platform details,
and troubleshooting.

## Building from source

### Build JAR 

```sh
mvn clean package -DskipTests -Dpackages.skip=true
```

You will find the app JAR in `modules/editor-gui/target/jar/`

### Build OS specific installer/app

```sh
mvn clean package -DskipTests
```

This will build all supported installers for your current OS and 
place them in `modules/editor-gui/target/packages/`

## Help

Documentation is available at the [Help page](https://pdf.metadata.care/help/) 

## License

Distributed under the GPL v3 License. See the [LICENSE](LICENSE) file for more information.