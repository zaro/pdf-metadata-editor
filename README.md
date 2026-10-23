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
pmedit-cli --mcp [--mcp-root=DIR] [--mcp-read-only]
```

- `--mcp-root=DIR` restricts all file access to `DIR` (default: current directory).
- `--mcp-read-only` disables every mutating tool.
- Read-only tools (`pdf_read_metadata`, `pdf_list_fields`, `pdf_export_metadata`)
  work without a Pro license.
- Mutating tools (`pdf_set_metadata`, `pdf_clear_metadata`, `pdf_rename`)
  require a Pro license.

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
| `pdf_read_metadata` | Read metadata from a PDF |
| `pdf_set_metadata` | Set one or more metadata fields |
| `pdf_clear_metadata` | Clear all or selected metadata fields |
| `pdf_rename` | Rename a PDF using a metadata template |
| `pdf_export_metadata` | Export metadata as JSON or YAML |

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