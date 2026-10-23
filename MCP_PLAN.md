# MCP Server Implementation Plan

Status: **implemented on branch `mcp`** — MCP tests and the shaded build are passing.
This document remains the design reference and implementation record.

This document describes the design for adding a [Model Context Protocol](https://modelcontextprotocol.io/)
(MCP) server to the PDF Metadata Editor command line tool.

---

## 1. Summary

Add an MCP server to the existing `pmedit-cli` binary:

```sh
pmedit-cli --mcp [--mcp-root=DIR] [--mcp-read-only]
```

The server speaks newline-delimited JSON-RPC 2.0 over stdin/stdout and exposes
tools for reading, writing, clearing, renaming, and exporting PDF metadata.
It is implemented by hand using the Jackson 2.x dependency already present in
the project, so no new runtime dependency, shading rule, or jlink change is
required.

The design reuses the existing extension API (`PdfReader`, `PdfWriter`,
`MetadataInfo`) and the existing metadata field model, so MCP writes behave the
same way as GUI and CLI writes.

---

## 2. Confirmed decisions

| Topic | Decision |
|---|---|
| Protocol | MCP over **stdio** (stdin/stdout), JSON-RPC 2.0, newline-delimited |
| Implementation | **Hand-rolled** JSON-RPC server using Jackson 2.18; no MCP SDK dependency |
| Licensing | Read-only tools are **free**; mutating tools require the existing Pro/batch license (`PmeExtension.hasBatch()`) |
| Write access | **Read-write by default** inside the configured root; `--mcp-read-only` is an optional safety switch |
| Path sandbox | Root defaults to the current working directory; `--mcp-root=DIR` overrides it |
| First scope | MVP: read, list fields, set, clear, rename, export JSON/YAML |
| Deferred | Import, XMP↔Document copy, CSV, batch/directory operations, HTTP transport, MCP resources |

### Assumptions

- "MCP" means the Model Context Protocol, not another acronym.
- The primary consumers are desktop AI clients (Claude Desktop, IDEs, agents)
  that launch the server as a subprocess and speak stdio.
- The server is stateless between calls: every tool call specifies its own file.
- Existing CLI behavior and output must not change when `--mcp` is absent.

---

## 3. Goals and non-goals

### Goals

- Expose PDF metadata operations through a small, predictable MCP tool surface.
- Preserve the existing PDF read/write semantics and field names.
- Keep stdout strictly reserved for protocol traffic.
- Enforce a filesystem sandbox so an AI client cannot escape the configured root.
- Reuse the existing Pro license gate for mutations.
- Require no new third-party dependencies.

### Non-goals (for this version)

- HTTP / Streamable HTTP transport.
- Authentication (stdio is a local subprocess channel).
- Multi-user or concurrent request processing.
- MCP resources, prompts, sampling, or elicitation.
- Batch operations over directories or multiple files per call.
- Importing metadata from JSON/YAML/CSV.
- Editing PDF page content, attachments, or annotations.

---

## 4. User-facing interface

### 4.1 Command line

```text
pmedit-cli --mcp [OPTIONS]

OPTIONS
  --mcp                  start the MCP server on stdin/stdout
  --mcp-root=DIR         root directory for all file access (default: current directory)
  --mcp-read-only        disable all mutating tools
```

Examples:

```sh
# Read/write access to the current directory
pmedit-cli --mcp

# Restrict access to a documents folder
pmedit-cli --mcp --mcp-root=/home/user/Documents/pdfs

# Offer a read-only metadata inspector
pmedit-cli --mcp --mcp-root=/home/user/Documents/pdfs --mcp-read-only
```

`--mcp` is an **option**, not a `CommandDescription`, so it does not appear in
the GUI batch menus and does not interact with the existing batch commands.

### 4.2 Help text

Add a section to `MainCli.helpMessage`:

```text
MCP SERVER

  pmedit-cli --mcp [--mcp-root=DIR] [--mcp-read-only]

  Starts a Model Context Protocol server over stdin/stdout. All paths passed
  to MCP tools are resolved relative to --mcp-root (default: current working
  directory) and may not escape it. Mutating tools require a Pro license.
```

### 4.3 Client configuration example

Document this in `README.md`:

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

Windows users point `command` at the installed `pmedit-cli.exe`.

---

## 5. Architecture

### 5.1 New package

All new code lives in `modules/editor-gui/src/main/java/pmedit/mcp/`.

| Class | Responsibility |
|---|---|
| `McpMain` | Process bootstrap: validate options, resolve the root, redirect `System.out`, create and run the server. |
| `McpServer` | Read/write JSON-RPC frames, track initialization state, dispatch methods, own the tool registry. |
| `McpProtocol` | Supported protocol versions, capability object, JSON-RPC error codes, request/notification detection. |
| `McpTools` | Tool definitions (name, description, input schema, output schema, annotations) and handler dispatch. |
| `McpMetadata` | Read/write/clear/rename/export implementations built on `MetadataInfo`, `PdfReader`, `PdfWriter`, `TemplateString`, `SerDeslUtils`. |
| `McpJson` | Jackson helpers: parse frames, build result objects, convert JSON values to typed metadata values, stable ordering. |
| `McpPaths` | Root resolution and sandbox checks, PDF extension checks, output path preparation. |
| `McpResult` | Helpers for successful text/structured results and `isError` tool results. |

The classes are intentionally separate so the protocol layer can be tested
without touching the filesystem, and the metadata layer can be tested without
speaking JSON-RPC.

### 5.2 Startup flow

```text
pmedit-cli --mcp ...
   │
   ├─ MainCli.main(String[])
   │    ├─ Main.setupLogging(false, true)
   │    ├─ CommandLine.parse(args)
   │    └─ MainCli.main(CommandLine)
   │         └─ if (cmdLine.mcp) McpMain.run(cmdLine)
   │
   ├─ McpMain.run
   │    ├─ resolve/validate --mcp-root
   │    ├─ remember real stdout
   │    ├─ System.setOut(System.err)          // protect the protocol stream
   │    ├─ create McpServer(stdin, realStdout, options)
   │    └─ server.run() until EOF
   │
   └─ process exits 0
```

`Main.main(String[])` (the uberjar entry point) gets a pre-scan before the
Windows single-instance logic:

```java
if (containsMcpFlag(args)) {
    MainCli.main(args);   // CLI logging, parse, MCP dispatch
    return;               // never start the GUI
}
```

This is important on Windows: otherwise an already running GUI instance would
receive `--mcp` through the named pipe instead of starting a server. It also
prevents the `Logfile location: ...` line from polluting stdout when the
uberjar is used instead of the `pmedit-cli` launcher.

---

## 6. CLI integration

### 6.1 `CommandLine`

Add fields:

```java
public boolean mcp = false;
public String mcpRoot = null;      // null => current working directory
public boolean mcpReadOnly = false;
```

Parse in `processOptions`:

```java
} else if (arg.equalsIgnoreCase("mcp")) {
    cmdLine.mcp = true;
} else if (arg.startsWith("mcp-root")) {
    OptionArgument oa = getOptionArgument(arg, i, args);
    cmdLine.mcpRoot = oa.arg.trim();
    i += oa.advance;
} else if (arg.equalsIgnoreCase("mcp-read-only")) {
    cmdLine.mcpReadOnly = true;
}
```

Notes:

- `arg.equalsIgnoreCase("mcp")` must be checked before `startsWith("mcp-root")`.
- `getOptionArgument` already supports both `--mcp-root DIR` and
  `--mcp-root=DIR`.
- Add the new fields to `CommandLine.toString()` for diagnostics.

### 6.2 `MainCli`

At the top of `executeCommand`:

```java
if (cmdLine.mcp) {
    McpMain.run(cmdLine);
    return;
}
```

The MCP server must be dispatched **before** `cmdLine.hasCommand()` and before
the batch-license check, because it is long-running and owns its own tool
licensing.

### 6.3 `Main`

Add the pre-scan described in section 5.2 as the very first statement of
`Main.main(final String[] args)`.

### 6.4 stdout discipline

In `McpMain`:

```java
PrintStream protocolOut = System.out;
System.setOut(System.err);
try (McpServer server = new McpServer(System.in, protocolOut, options)) {
    server.run();
}
```

Consequences:

- Accidental `System.out.println` calls go to stderr and cannot corrupt the
  protocol.
- The protocol writer uses the original stdout only.
- All log output continues to go to the existing log file. `Main.setupLogging`
  already forces CLI mode and suppresses the console appender.

---

## 7. Protocol layer

### 7.1 Transport

- UTF-8.
- One JSON-RPC message per line (`\n` terminated).
- Requests and notifications are read from stdin.
- Responses are written to stdout and flushed after every message.
- Embedded newlines inside JSON strings are forbidden by the transport;
  Jackson's writer emits compact JSON without raw newlines.
- EOF on stdin terminates the server with exit code 0.
- A maximum frame size (e.g. 16 MiB) is enforced to avoid unbounded buffering.

### 7.2 Initialization state

```java
enum State { NEW, INITIALIZED }
```

- `NEW`: only `initialize` and `ping` are accepted.
- `INITIALIZED`: all advertised methods are accepted.
- Other requests before initialization return JSON-RPC error `-32002`
  ("Server not initialized").

`initialize` response:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2025-11-25",
    "capabilities": {
      "tools": { "listChanged": false }
    },
    "serverInfo": {
      "name": "pdf-metadata-editor",
      "version": "4.4.2"
    },
    "instructions": "Tools operate on PDF files inside the configured root. Call pdf_list_fields before writing to discover valid field IDs. Mutating tools require a Pro license."
  }
}
```

- The version is taken from `Version.get().getAsString()`.
- The instructions string is short and does not leak absolute filesystem paths.

### 7.3 Version negotiation

Supported versions, newest first:

```text
2025-11-25
2025-06-18
2025-03-26
2024-11-05
```

Behavior:

- If the client's `params.protocolVersion` is supported, echo it back.
- Otherwise return the newest supported version (`2025-11-25`).
- The client decides whether it can continue; the server does not disconnect.
- Optional `params.capabilities` and `params.clientInfo` are ignored but
  validated to be objects when present.

### 7.4 Method dispatch

| Method | Type | Behavior |
|---|---|---|
| `initialize` | request | Negotiate version, return capabilities/serverInfo/instructions, set state |
| `notifications/initialized` | notification | Ignore, no response |
| `ping` | request | Return `{}` |
| `tools/list` | request | Return all MVP tools; no pagination cursor |
| `tools/call` | request | Validate tool name and arguments, execute, return result |
| `notifications/cancelled` | notification | Ignore (calls are synchronous) |
| anything else | request | `-32601 Method not found` |
| anything else | notification | Ignore |

JSON-RPC rules:

- A message without `id` is a notification and **must not** receive a response.
- `id` may be a string, number, or null; the response echoes the same JSON value.
- Missing/invalid `jsonrpc` → `-32600 Invalid Request`.
- Malformed JSON → `-32700 Parse error` with `id: null`.
- Tool execution failures are **not** JSON-RPC errors. They are returned as
  `result.isError = true` so the model can read the message and retry.

### 7.5 Error codes

| Code | Use |
|---|---|
| `-32700` | Invalid JSON |
| `-32600` | Invalid JSON-RPC request |
| `-32601` | Unknown method |
| `-32602` | Invalid method parameters (missing/wrong-typed fields) |
| `-32603` | Unexpected server error |
| `-32002` | Server not initialized |

Unexpected exceptions are logged with the full stack trace to the log file;
the client receives only a safe message.

---

## 8. Tool surface

### 8.1 Common conventions

- All tools are prefixed with `pdf_`.
- Every tool accepts a `path` string. Paths are resolved by `McpPaths`.
- Input schemas set `"additionalProperties": false`.
- Descriptions state whether the tool is read-only and whether it requires a
  Pro license.
- Results include:
  - `content`: a single `text` block with human-readable JSON.
  - `structuredContent`: the same data as a JSON object (where applicable).
  - `isError` only on failure.
- Tool errors use `McpResult.error(message)` and never include stack traces,
  passwords, or absolute paths outside the root.

### 8.2 Tool list

#### `pdf_list_fields`

- **Mutating:** no
- **License:** free
- **Args:** none
- **Returns:** `{ "fields": [ ... ] }`

Each field entry:

```json
{
  "id": "doc.title",
  "type": "StringField",
  "list": false,
  "writable": true,
  "readonly": false,
  "enumValues": ["UseNone", "UseOutlines", "..."]
}
```

Built from `MetadataInfo.keys()`, `MetadataInfo.getFieldDescription(id)`, and
`FieldDescription.getEnumValuesAsStrings()` when the field is an enum.
`enumValues` is omitted for non-enum fields.

#### `pdf_read_metadata`

- **Mutating:** no
- **License:** free
- **Args:**
  - `path` (string, required)
  - `ownerPassword` (string, optional) — used only to open encrypted PDFs
  - `fields` (string[], optional) — subset of field IDs; default all
- **Returns:**

```json
{
  "path": "report.pdf",
  "file": {
    "path": "report.pdf",
    "nameWithExt": "report.pdf",
    "sizeBytes": 12345,
    "createTime": "2026-10-23T10:00:00",
    "modifyTime": "2026-10-23T10:05:00"
  },
  "fields": {
    "doc.title": "Weekly Report",
    "doc.author": "Jane Doe",
    "dc.creators": "Jane Doe",
    "prop.version": "1.7"
  },
  "warnings": []
}
```

Implementation:

- `PmeExtension.get().newPdfReader().loadFromPDF(file, ownerPassword, md)`.
- `md.asFlatStringMap()` for values (dates formatted, lists newline-joined).
- By default only populated fields are returned, which keeps the model context
  small. When `fields` is supplied, exactly those fields are returned, even if
  unset (as an empty string).
- `file.fullPath` is replaced with the root-relative path; absolute paths are
  not exposed to the client.
- Filter `prop.ownerPassword` and `prop.userPassword` from `fields`.
- When `fields` is supplied, validate every ID against `MetadataInfo.validMdNames`.

#### `pdf_set_metadata`

- **Mutating:** yes
- **License:** Pro
- **Args:**
  - `path` (string, required)
  - `metadata` (object, required, min 1 property)
  - `outputPath` (string, optional) — default: overwrite `path` in place
  - `ownerPassword` (string, optional)
- **Returns:**

```json
{
  "path": "report.pdf",
  "outputPath": "report.pdf",
  "updated": ["doc.title", "doc.author"]
}
```

Implementation:

1. Validate `--mcp-read-only` and the license gate.
2. Resolve `path` and `outputPath` through `McpPaths`.
3. Create `MetadataInfo changes = new MetadataInfo()`.
4. **Disable all fields first** (`changes.setEnabled(false)`), because enabled
   flags default to `true`; otherwise untouched fields would be written as
   null and wipe existing metadata.
5. For each entry in `metadata`:
   - reject unknown IDs;
   - reject read-only IDs (`file.*`, `FieldDescription.isReadonly`);
   - normalize the JSON value (section 8.3);
   - set the value and enable the field.
6. If `ownerPassword` is supplied, assign `changes.prop.ownerPassword`
   **without enabling** any `prop.*` field. `BasicPdfWriter` uses it to open the
   document.
7. Call `PmeExtension.get().newPdfWriter().saveAsPDF(changes, input, output)`.
8. Return the list of updated field IDs.

#### `pdf_clear_metadata`

- **Mutating:** yes
- **License:** Pro
- **Args:**
  - `path` (string, required)
  - `fields` (string[], optional) — default `["all"]`
  - `outputPath` (string, optional)
  - `ownerPassword` (string, optional)
- **Returns:** `{ "path": ..., "outputPath": ..., "cleared": [...] }`

Implementation:

- `"all"` expands to every writable field.
- `"none"` is rejected because it is a no-op.
- Build a `MetadataInfo` whose enabled flags are all `false` for a subset, or
  leave the default all-enabled flags for `all`.
- Set the selected values to `null`.
- Save with `saveAsPDF`.

Important: a `MetadataInfo` with all fields disabled causes the writer to copy
the source unchanged, so "clear all" must keep the flags enabled and set values
to null instead.

#### `pdf_rename`

- **Mutating:** yes
- **License:** Pro
- **Args:**
  - `path` (string, required)
  - `template` (string, required) — e.g. `"{doc.author} - {doc.title}.pdf"`
  - `outputDir` (string, optional) — default: source file's directory
  - `ownerPassword` (string, optional)
- **Returns:** `{ "inputPath": ..., "outputPath": ..., "renamed": true }`

Implementation:

- Append `.pdf` to the template if missing, matching the CLI.
- Load metadata with `PdfReader`.
- Evaluate with `TemplateString.process(md)`.
- Resolve the destination through `McpPaths`.
- Refuse if the destination already exists (do not overwrite).
- If `outputDir` is supplied, copy with `COPY_ATTRIBUTES`; otherwise move.

#### `pdf_export_metadata`

- **Mutating:** no
- **License:** free
- **Args:**
  - `path` (string, required)
  - `format` (enum: `json`, `yaml`, required)
  - `outputPath` (string, optional)
  - `ownerPassword` (string, optional)
- **Returns:**
  - Without `outputPath`: `{ "path": ..., "format": ..., "content": "..." }`
  - With `outputPath`: `{ "path": ..., "format": ..., "outputPath": "..." }`

Implementation:

- Load metadata and build a filtered flat map (passwords removed).
- JSON: `SerDeslUtils.toJSON(true, map)`.
- YAML: `SerDeslUtils.toYAML(map)`.
- If `outputPath` is given, write the text with UTF-8 and return the path.
- Otherwise return the text inline.

### 8.3 JSON value conversion

`MetadataInfo` values are Java types (`String`, `Boolean`, `Integer`, `Long`,
`Float`, `Calendar`, `List<...>`). MCP arguments arrive as Jackson `JsonNode`.
Conversion algorithm:

1. Convert the JSON node to plain Java values:
   - text → `String`
   - boolean → `Boolean`
   - integer → `Integer` or `Long`
   - floating point → `Double`
   - array → `List<Object>` (recursively converted)
   - null → `null`
2. Look up `FieldDescription fd = MetadataInfo.getFieldDescription(id)`.
3. If the field is a list and the value is not a list, wrap it in a singleton list.
4. Pass the value through `fd.postProcessDeserializedValue(...)` to handle:
   - date strings and date lists,
   - `java.util.Date` → `Calendar`,
   - numeric `Float` conversion.
5. For enum fields, validate the string with `fd.enumValueFromString(...)` and
   keep the string form for assignment; invalid values produce a clear error
   listing the allowed values from `fd.getEnumValuesAsStrings()`.
6. Assign with `md.set(id, normalized)` and `md.setEnabled(id, true)`.

Rejected values:

- Unknown field IDs.
- `file.*` fields (read-only).
- `FieldDescription.isReadonly` fields.
- Invalid dates, enums, numbers, or booleans.
- JSON objects as field values (not supported for any field type).

### 8.4 Passwords

- `prop.ownerPassword` and `prop.userPassword` are removed from all read and
  export results.
- They are never written to the log.
- They are accepted only as the top-level `ownerPassword` argument, not as
  metadata map entries (a metadata entry for either field is rejected with a
  message telling the caller to use `ownerPassword`).

---

## 9. Licensing

Mutations check the existing Pro/batch capability:

```java
PmeExtension.get().hasBatch()
```

To keep unit tests independent of the Pro extension, `McpServer` receives a
`BooleanSupplier licenseGate` (default: `() -> PmeExtension.get().hasBatch()`).

Behavior:

- Read-only tools (`pdf_read_metadata`, `pdf_list_fields`,
  `pdf_export_metadata`) always run.
- Mutating tools (`pdf_set_metadata`, `pdf_clear_metadata`, `pdf_rename`)
  return a tool error when the gate is false:

```text
Editing PDF metadata through MCP requires a Pro license.
You can get one at https://pdf.metadata.care/buy/
```

- The URL comes from `WebsiteConstants.batchLicenseUrl`.
- No JSON-RPC-level error is used, so the client can display the message.

---

## 10. Filesystem sandbox

### 10.1 Root

- `--mcp-root` defaults to the current working directory (`.`).
- The root is canonicalized with `Path.toRealPath()`.
- If the root does not exist, is not a directory, or cannot be resolved, the
  server exits with a clear stderr message and a non-zero exit code.

### 10.2 Input paths

- Relative paths are resolved against the root.
- Absolute paths are accepted only if their real path is inside the root.
- The file must exist, be a regular file, and have a `.pdf` extension
  (case-insensitive).
- Symlinks are resolved before the containment check, preventing escapes.

### 10.3 Output paths

- Relative paths are resolved against the root.
- The parent directory is resolved with `toRealPath()` and must be inside the
  root.
- Missing parent directories may be created inside the root.
- The final normalized path must start with the root.
- Existing output files are overwritten only when the caller explicitly asks
  for that path (the default in-place write is the input file itself).
- `pdf_rename` never overwrites an existing destination.

### 10.4 Helper API

```java
final class McpPaths {
    static McpPaths forRoot(String configuredRoot) throws IOException;
    Path input(String path);                  // exists, regular file, .pdf, inside root
    Path output(String path);                 // inside root, parents creatable
    Path sibling(Path input, String name);    // rename destination
}
```

Containment check:

```java
Path realRoot = root.toRealPath();
Path realTarget = target.toRealPath(); // for existing paths
if (!realTarget.startsWith(realRoot)) {
    throw new McpToolException("Path is outside the configured root");
}
```

For non-existing outputs, resolve and check the parent, then append the file
name and normalize.

---

## 11. Error handling and logging

### 11.1 Protocol errors

- Malformed JSON → `-32700`, `id: null`.
- Non-object or missing `jsonrpc` → `-32600`.
- Unknown method → `-32601`.
- Missing/invalid `params` → `-32602`.
- Uncaught server exception → `-32603` with a generic message.

### 11.2 Tool errors

Returned inside a successful JSON-RPC response:

```json
{
  "jsonrpc": "2.0",
  "id": 7,
  "result": {
    "content": [{ "type": "text", "text": "Error: file is outside the configured root" }],
    "isError": true
  }
}
```

Cases:

- Read-only server rejects a mutation.
- Pro license missing.
- Path outside root.
- File not found or not a PDF.
- Destination already exists.
- Invalid field ID, date, enum, or number.
- PDF parsing/writing failure (including encrypted-file errors).

### 11.3 Logging

- Use SLF4J with the existing file appender.
- Log tool invocations at `INFO` with field names but **never** values or
  passwords.
- Log exceptions at `ERROR` with stack traces to the file only.
- stderr is used for startup failures and optional diagnostics; it is safe to
  use because MCP treats it as a log channel.

---

## 12. Files to add and modify

### New files

```text
modules/editor-gui/src/main/java/pmedit/mcp/McpMain.java
modules/editor-gui/src/main/java/pmedit/mcp/McpOptions.java
modules/editor-gui/src/main/java/pmedit/mcp/McpServer.java
modules/editor-gui/src/main/java/pmedit/mcp/McpProtocol.java
modules/editor-gui/src/main/java/pmedit/mcp/McpTools.java
modules/editor-gui/src/main/java/pmedit/mcp/McpMetadata.java
modules/editor-gui/src/main/java/pmedit/mcp/McpJson.java
modules/editor-gui/src/main/java/pmedit/mcp/McpPaths.java
modules/editor-gui/src/main/java/pmedit/mcp/McpResult.java
modules/editor-gui/src/main/java/pmedit/mcp/McpToolException.java
```

### Modified files

```text
modules/editor-gui/src/main/java/pmedit/CommandLine.java   # new options and fields
modules/editor-gui/src/main/java/pmedit/MainCli.java       # dispatch + help text
modules/editor-gui/src/main/java/pmedit/Main.java          # --mcp pre-scan
README.md                                                  # usage + client config
MCP_PLAN.md                                                # this document
```

### No changes

- `pom.xml` / module POMs — no new dependencies.
- `maven-shade-plugin` configuration — no new artifacts.
- `jpackage/cli.properties` — the existing `pmedit-cli` launcher is reused.
- GUI code and batch command definitions.

---

## 13. Testing plan

### 13.1 Protocol tests (`McpServerTest`)

Use `ByteArrayInputStream`/`ByteArrayOutputStream` and the `McpServer`
constructor; do not hijack `System.out`.

- `initialize` returns the negotiated version and tool capability.
- Client version `2025-06-18` is echoed back.
- Unknown client version falls back to `2025-11-25`.
- `ping` returns `{}`.
- `notifications/initialized` produces no output.
- Requests before initialization return `-32002`.
- Unknown method returns `-32601`.
- Malformed JSON returns `-32700` with `id: null`.
- Invalid params return `-32602`.
- Unknown notifications are silently ignored.
- String, numeric, and null IDs are echoed exactly.
- Multiple sequential requests are answered in order.
- EOF ends the loop cleanly.

### 13.2 Tool tests (`McpMetadataTest`)

Use `FilesTestHelper` with temporary directories.

- `pdf_list_fields` includes known fields and their writable flags.
- `pdf_read_metadata` returns expected values for a generated PDF.
- `pdf_read_metadata` with a `fields` subset returns only those fields.
- `pdf_set_metadata` updates fields in place and preserves untouched fields.
- `pdf_set_metadata` with `outputPath` leaves the source unchanged.
- Dates, enums, booleans, numbers, and list fields convert correctly.
- Invalid field IDs, read-only fields, invalid dates/enums, and object values
  produce `isError` results.
- `pdf_clear_metadata` with `all` clears the metadata.
- `pdf_clear_metadata` with a subset clears only those fields.
- `pdf_rename` moves the file and reports the new path.
- `pdf_rename` refuses an existing destination.
- `pdf_export_metadata` returns valid JSON and YAML.
- `pdf_export_metadata` writes the file when `outputPath` is supplied.
- Passwords never appear in read/export results.
- License gate:
  - gate `true` → mutations succeed;
  - gate `false` → mutations return the Pro-license message;
  - read-only tools work with gate `false`.
- `--mcp-read-only` rejects every mutating tool.

### 13.3 Path tests (`McpPathsTest`)

- Relative path resolves against the root.
- Absolute path inside the root is accepted.
- Absolute path outside the root is rejected.
- `..` traversal is rejected.
- Symlink pointing outside the root is rejected (create the symlink with
  `Files.createSymbolicLink`; skip the test on platforms where symlinks are not
  supported).
- Non-PDF input is rejected.
- Directory input is rejected.
- Output parent creation works inside the root.
- Output outside the root is rejected.

### 13.4 Process-level test (`McpProcessTest`)

Implemented: the test launches `pmedit.MainCli` in a subprocess with the
surefire classpath and exchanges real JSON-RPC lines.

- Send `initialize`, `notifications/initialized`, `tools/list`.
- Assert:
  - each stdout line is valid JSON-RPC;
  - there are no log lines or banners on stdout;
  - responses match the request IDs;
  - the process exits 0 after stdin is closed.
- This is the regression test for stdout pollution.

### 13.5 CLI option tests (`McpCommandLineTest`)

- `--mcp`, `--mcp-root=DIR`, and `--mcp-read-only` are parsed.
- `--mcp-root DIR` as a separate argument is parsed.
- `-mcp` (single dash) is accepted by the existing option parser.

### 13.6 Commands

```sh
mvn test -pl modules/editor-gui
mvn test -pl modules/editor-gui -am -Dtest='Mcp*Test' -Dsurefire.failIfNoSpecifiedTests=false
mvn clean package -DskipTests -Dpackages.skip=true
```

A manual smoke test:

```sh
printf '%s\n' \
  '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"smoke","version":"1"}}}' \
  '{"jsonrpc":"2.0","method":"notifications/initialized"}' \
  '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' \
  | java -DnoGui -jar modules/editor-gui/target/jar/pdf-metadata-editor-4.4.2.jar --mcp
```

---

## 14. Implementation order

1. **CLI plumbing**
   - Add fields and option parsing to `CommandLine`.
   - Dispatch from `MainCli`.
   - Add the `Main` pre-scan.
   - Add the help text.
2. **Protocol skeleton**
   - `McpServer`, `McpProtocol`, `McpJson`, `McpResult`.
   - `initialize`, `ping`, `tools/list` with an empty/placeholder tool list.
   - Protocol unit tests.
3. **Sandbox and read path**
   - `McpPaths`.
   - `McpMetadata` read + list-fields + export.
   - Tool registration and result schemas.
   - Path and read tool tests.
4. **Mutations**
   - set/clear/rename.
   - License gate injection.
   - Read-only mode.
   - Mutation tests.
5. **Process-level integration**
   - `McpProcessTest`.
6. **Documentation**
   - README section and client configuration.
7. **Full verification**
   - Module tests and full shaded build.

---

## 15. Documentation

### README

Add a "Command line MCP server" section covering:

- what MCP is and why it is useful,
- the `--mcp` flags,
- the root/write/read-only semantics,
- the Pro license requirement for mutations,
- a client configuration example,
- a short tool list.

### Help output

Add the `MCP SERVER` section described in section 4.2.

### Code documentation

- Class-level Javadoc on `McpServer` explaining the line-delimited transport.
- Javadoc on `McpPaths` explaining the containment rules.
- Javadoc on `McpMetadata` explaining that only enabled fields are written.

---

## 16. Packaging and verification

No packaging changes are expected because no dependency is added.

Verify:

1. `mvn clean package -DskipTests -Dpackages.skip=true` produces the shaded jar.
2. `java -DnoGui -jar ... --mcp` starts the server and does not print a log
   location or GUI banner to stdout.
3. The jpackage `pmedit-cli` launcher starts the server on Linux, Windows, and
   macOS.
4. The shaded jar does not contain new duplicate classes or service files.
5. jdeps/jlink packaging still succeeds (`mvn clean package -DskipTests` on a
   machine with the packaging tools installed).

---

## 17. Known limitations and future work

### Current limitations

- Single-file operations only; no directory or multi-file batch tools.
- No import from JSON/YAML/CSV.
- No XMP↔Document copy tools.
- No MCP resources, prompts, sampling, or elicitation.
- Stdio only.
- `BasicPdfWriter` creates its temporary output file in the source PDF's
  directory. Writing to a different `outputPath` still requires the source
  directory to be writable.
- Encryption and permission fields (`prop.*`) may require the Pro writer.
- The server processes requests sequentially.
- No cancellation support beyond ignoring `notifications/cancelled`.

### Phase 2 candidates

- `pdf_import_metadata` (JSON/YAML/CSV).
- `pdf_copy_metadata` (`xmp_to_doc`, `doc_to_xmp`).
- `pdf_batch` for directories, gated behind Pro.
- MCP resources exposing PDF files and their metadata.
- A `pmedit-mcp` dedicated jpackage launcher.
- Streamable HTTP transport for remote/team use.
- Optional in-memory operation for content passed as base64.

---

## 18. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Accidental stdout writes corrupt the protocol | Redirect `System.out` to stderr; process-level test asserts stdout is JSON-only |
| Enabled flags default to true and wipe metadata | Always disable all fields before applying a partial update; dedicated round-trip tests |
| Symlink escape from the root | Resolve real paths and compare with the canonical root; symlink tests |
| Pro license bypass | Route all mutations through the injected license gate; test both states |
| Passwords leak into model context or logs | Strip `prop.ownerPassword`/`prop.userPassword` from outputs; never log values |
| Windows single-instance logic swallows `--mcp` | Pre-scan before `WindowsSingletonApplication.isAlreadyRunning()` |
| Protocol version drift | Support a small explicit version list with a documented fallback |
| Jackson schema drift | Build schemas with plain `LinkedHashMap`s and the existing Jackson 2.x mapper; no new dependency |
| Temp-file write failure on read-only source directories | Document the limitation; consider a future writer improvement |

---

## 19. Acceptance criteria

The feature is complete when:

- `pmedit-cli --mcp` starts a server that completes the MCP initialization
  handshake over stdin/stdout.
- `tools/list` returns the six MVP tools with valid input schemas.
- A generated PDF can be read, modified, cleared, renamed, and exported through
  MCP tool calls.
- Untouched metadata survives a partial write.
- Mutating tools fail with a clear message without a Pro license.
- `--mcp-read-only` rejects all mutations.
- Paths outside `--mcp-root` are rejected, including symlink escapes.
- stdout contains only JSON-RPC frames during a full session.
- All new unit tests and the existing test suite pass.
- The shaded jar builds and the `pmedit-cli` launcher works without packaging
  changes.
- README and `--help` document the feature.
