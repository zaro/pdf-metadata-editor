package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.ext.CommandResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Tool definitions and argument dispatch for the MCP server.
 *
 * <p>Every file operation is a batch operation with CLI parity: callers pass a
 * {@code paths} array containing files and/or directories, and the tool returns
 * one result per PDF. A single file is just {@code paths: ["one.pdf"]}.</p>
 */
public final class McpTools {

    private static final Logger LOG = LoggerFactory.getLogger(McpTools.class);
    private static final String OWNER_PASSWORD_DESCRIPTION =
            "Owner password for an encrypted PDF. If omitted, a protected PDF returns password_required.";
    private static final String PATHS_DESCRIPTION =
            "PDF files and/or directories, relative to the MCP root unless absolute paths inside the "
                    + "root are used. Directories are searched recursively for .pdf files. "
                    + "A single file is [\"one.pdf\"].";
    private static final String OUTPUT_DIR_DESCRIPTION =
            "Optional output directory, relative to the MCP root. The input directory structure is "
                    + "preserved beneath it. When omitted, files are updated in place.";

    private final McpMetadata metadata;
    private final int maxFiles;
    private final List<Tool> tools;

    public McpTools(McpMetadata metadata) {
        this(metadata, McpPaths.MAX_BATCH_FILES);
    }

    public McpTools(McpMetadata metadata, int maxFiles) {
        this.metadata = metadata;
        this.maxFiles = maxFiles;
        this.tools = buildTools();
    }

    private String batchLimit() {
        return maxFiles > 0
                ? "A batch may contain at most " + maxFiles + " files or operations."
                : "No fixed batch limit is configured.";
    }

    public ObjectNode listResult() {
        ObjectNode result = McpJson.object();
        ArrayNode definitions = McpJson.array();

        for (Tool tool : tools) {
            ObjectNode definition = McpJson.object();
            definition.put("name", tool.name());
            definition.put("title", tool.title());
            definition.put("description", tool.description());
            definition.set("inputSchema", tool.inputSchema());
            if (tool.outputSchema() != null) {
                definition.set("outputSchema", tool.outputSchema());
            }
            definition.set("annotations", tool.annotations());
            definitions.add(definition);
        }

        result.set("tools", definitions);
        return result;
    }

    public ObjectNode call(String name, ObjectNode args) throws UnknownToolException {
        Tool tool = find(name);
        if (tool == null) {
            throw new UnknownToolException("Unknown tool: " + name);
        }

        try {
            return tool.handler().call(args);
        } catch (McpToolException e) {
            if (e.code() == null) {
                return McpResult.error(e.getMessage());
            }
            return McpResult.error(e.code(), e.getMessage());
        } catch (Exception e) {
            LOG.error("MCP tool {} failed", name, e);
            return McpResult.error("Unexpected error while executing " + name + ": " + safeMessage(e));
        }
    }

    private Tool find(String name) {
        for (Tool tool : tools) {
            if (tool.name().equals(name)) {
                return tool;
            }
        }
        return null;
    }

    private List<Tool> buildTools() {
        List<Tool> definitions = new ArrayList<>();

        definitions.add(new Tool(
                "pdf_list_fields",
                "List PDF metadata fields",
                "List every metadata field identifier, its data type, and whether it is writable. "
                        + "Call this before pdf_set_metadata to discover valid field names. "
                        + "Read-only and free.",
                emptySchema(),
                listFieldsOutputSchema(),
                McpJson.annotations(true, false, true),
                args -> McpResult.success(metadata.listFields())
        ));

        definitions.add(new Tool(
                "pdf_read_metadata",
                "Read PDF metadata",
                "Read metadata from one or more PDFs or directories, like the CLI's batch input. "
                        + "By default only populated fields are returned; pass fields to request specific "
                        + "field IDs. " + batchLimit() + " Read-only and free.",
                readInputSchema(),
                batchOutputSchema("One entry per PDF file.", true),
                McpJson.annotations(true, false, true),
                args -> McpResult.batch(metadata.readBatch(
                        requiredStringList(args, "paths"),
                        optionalString(args, "ownerPassword"),
                        optionalStringList(args, "fields")))
        ));

        definitions.add(new Tool(
                "pdf_set_metadata",
                "Set PDF metadata",
                "Set one or more metadata fields on every input PDF. Equivalent to the CLI 'edit' command. "
                        + "Only the supplied fields are changed; all other metadata in each document is "
                        + "preserved. Values can use {field.id} templates expanded from each file's own "
                        + "metadata. Writes in place by default, or beneath outputDir when supplied. "
                        + batchLimit() + " May require a license.",
                setInputSchema(),
                batchOutputSchema("One entry per PDF file.", false),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.setBatch(
                        requiredStringList(args, "paths"),
                        requiredObject(args, "metadata"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_clear_metadata",
                "Clear PDF metadata",
                "Clear all metadata or only the selected fields on every input PDF. Equivalent to the CLI "
                        + "'clear' command. Writes in place by default, or beneath outputDir when supplied. "
                        + batchLimit() + " May require a license.",
                clearInputSchema(),
                batchOutputSchema("One entry per PDF file.", false),
                McpJson.annotations(false, true, true),
                args -> McpResult.batch(metadata.clearBatch(
                        requiredStringList(args, "paths"),
                        optionalStringList(args, "fields"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_rename",
                "Rename PDFs from metadata",
                "Rename every input PDF using a metadata template such as "
                        + "'{doc.author} - {doc.title}.pdf'. Equivalent to the CLI 'rename' command. "
                        + "The destination must not exist. When outputDir is omitted the file is moved within "
                        + "its current directory; when supplied the file is copied beneath outputDir. "
                        + batchLimit() + " May require a license.",
                renameInputSchema(),
                batchOutputSchema("One entry per PDF file.", false),
                McpJson.annotations(false, true, false),
                args -> McpResult.batch(metadata.renameBatch(
                        requiredStringList(args, "paths"),
                        requiredString(args, "template"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_export_json",
                "Export PDF metadata as JSON",
                "Export metadata from one or more PDFs or directories to JSON. Equivalent to the CLI "
                        + "'tojson' command. By default one aggregate file is written; set perFileExport "
                        + "to write one file per PDF. " + batchLimit() + " May require a license.",
                exportInputSchema(),
                batchOutputSchema("One entry per input PDF.", false),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.exportBatch(
                        "json",
                        requiredStringList(args, "paths"),
                        optionalStringList(args, "fields"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "outputFile"),
                        optionalBoolean(args, "perFileExport"),
                        optionalBoolean(args, "relativePaths"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_export_yaml",
                "Export PDF metadata as YAML",
                "Export metadata from one or more PDFs or directories to YAML. Equivalent to the CLI "
                        + "'toyaml' command. By default one aggregate file is written; set perFileExport "
                        + "to write one file per PDF. " + batchLimit() + " May require a license.",
                exportInputSchema(),
                batchOutputSchema("One entry per input PDF.", false),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.exportBatch(
                        "yaml",
                        requiredStringList(args, "paths"),
                        optionalStringList(args, "fields"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "outputFile"),
                        optionalBoolean(args, "perFileExport"),
                        optionalBoolean(args, "relativePaths"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_export_csv",
                "Export PDF metadata as CSV",
                "Export metadata from one or more PDFs or directories to CSV. Equivalent to the CLI "
                        + "'tocsv' command. By default one aggregate file is written; set perFileExport "
                        + "to write one file per PDF. " + batchLimit() + " May require a license.",
                exportInputSchema(),
                batchOutputSchema("One entry per input PDF.", false),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.exportBatch(
                        "csv",
                        requiredStringList(args, "paths"),
                        optionalStringList(args, "fields"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "outputFile"),
                        optionalBoolean(args, "perFileExport"),
                        optionalBoolean(args, "relativePaths"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_import_from_filename",
                "Import PDF metadata from file names",
                "Extract metadata from each PDF's file name and write it back. Equivalent to the CLI "
                        + "'fromfilename' command. The template uses {field.id} placeholders, for example "
                        + "'{doc.author} - {doc.title}'. " + batchLimit() + " May require a license.",
                importFromFilenameInputSchema(),
                batchOutputSchema("One entry per input PDF.", false),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.importFromFilenameBatch(
                        requiredStringList(args, "paths"),
                        requiredString(args, "template"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_import_from_json",
                "Import PDF metadata from JSON",
                "Import metadata from JSON files. Equivalent to the CLI 'fromjson' command. Each record "
                        + "must contain file.fullPath. A record's prop.ownerPassword is used to open that "
                        + "PDF when present; otherwise ownerPassword is used. " + batchLimit()
                        + " May require a license.",
                importFileInputSchema(),
                importOutputSchema(),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.importBatch(
                        "json",
                        requiredStringList(args, "importPaths"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_import_from_yaml",
                "Import PDF metadata from YAML",
                "Import metadata from YAML files. Equivalent to the CLI 'fromyaml' command. Each record "
                        + "must contain file.fullPath. A record's prop.ownerPassword is used to open that "
                        + "PDF when present; otherwise ownerPassword is used. " + batchLimit()
                        + " May require a license.",
                importFileInputSchema(),
                importOutputSchema(),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.importBatch(
                        "yaml",
                        requiredStringList(args, "importPaths"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_import_from_csv",
                "Import PDF metadata from CSV",
                "Import metadata from CSV files. Equivalent to the CLI 'fromcsv' command. Each record "
                        + "must contain file.fullPath. A record's prop.ownerPassword is used to open that "
                        + "PDF when present; otherwise ownerPassword is used. " + batchLimit()
                        + " May require a license.",
                importFileInputSchema(),
                importOutputSchema(),
                McpJson.annotations(false, false, true),
                args -> McpResult.batch(metadata.importBatch(
                        "csv",
                        requiredStringList(args, "importPaths"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword")))
        ));

        definitions.add(new Tool(
                "pdf_license",
                "Manage the license",
                "Check, activate, or release the license. Activate needs the license information the "
                        + "user received by email: the agent should ask the user for the email address and "
                        + "the license ID, then pass them as one comma-separated string in the license "
                        + "argument, for example \"name@example.com,ABC-123\". Pass the values unchanged, "
                        + "never invent them, and never repeat the license back. Activation contacts the "
                        + "license server and is bound to this computer. Use status to check the current "
                        + "license, and release before moving it to another computer.",
                licenseInputSchema(),
                licenseOutputSchema(),
                McpJson.annotations(false, false, true),
                this::license
        ));

        return definitions;
    }

    private ObjectNode license(ObjectNode args) throws McpToolException {
        String action = optionalString(args, "action");
        if (action == null || action.isBlank()) {
            action = "status";
        }

        String command;
        String argument = "";
        switch (action) {
            case "status" -> command = "showLicense";
            case "activate" -> {
                String license = optionalString(args, "license");
                if (license == null || license.isBlank()) {
                    throw new McpToolException("invalid_argument",
                            "The license argument is required for activate: pass the email and license ID "
                                    + "as one comma-separated string.");
                }
                if (license.indexOf(',') < 0) {
                    throw new McpToolException("invalid_argument",
                            "The license argument must contain the email and license ID separated by a comma.");
                }
                command = "license";
                argument = license.trim();
            }
            case "release" -> command = "releaseLicense";
            default -> throw new McpToolException("invalid_argument",
                    "Unknown license action: " + action + ". Use status, activate, or release.");
        }

        CommandResult result = metadata.executeCommand(command, argument);

        ObjectNode payload = McpJson.object();
        payload.put("ok", result.ok());
        payload.put("message", result.message());

        ObjectNode response = McpResult.success(payload);
        if (!result.ok() && !"status".equals(action)) {
            response.put("isError", true);
        }
        return response;
    }

    private static ObjectNode licenseInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "action",
                McpJson.enumProperty(
                        "status checks the current license, activate installs one, release removes it.",
                        "status", "activate", "release"),
                false);
        McpJson.addProperty(schema, "license", McpJson.stringProperty(
                "License information passed to the license provider unchanged. For activate, the email "
                        + "address and the license ID from the purchase email, separated by a comma. "
                        + "Ignored for status and release."), false);
        return schema;
    }

    private static ObjectNode licenseOutputSchema() {
        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "ok", McpJson.booleanProperty(
                "True when the command succeeded. For status, whether a license is installed."), true);
        McpJson.addProperty(schema, "message", McpJson.stringProperty(
                "Human-readable status or failure reason."), true);
        return schema;
    }

    // ---------------------------------------------------------------------
    // Input schemas
    // ---------------------------------------------------------------------

    private static ObjectNode emptySchema() {
        return McpJson.objectSchema();
    }

    private static ObjectNode pathsProperty() {
        ObjectNode property = McpJson.stringArrayProperty(PATHS_DESCRIPTION);
        property.put("minItems", 1);
        return property;
    }

    private static ObjectNode readInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "paths", pathsProperty(), true);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty(OWNER_PASSWORD_DESCRIPTION), false);
        McpJson.addProperty(schema, "fields", McpJson.stringArrayProperty("Optional subset of metadata field IDs."), false);
        return schema;
    }

    private static ObjectNode setInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "paths", pathsProperty(), true);
        McpJson.addProperty(schema, "metadata", McpJson.objectProperty("Map of metadata field IDs to values."), true);
        McpJson.addProperty(schema, "outputDir", McpJson.stringProperty(OUTPUT_DIR_DESCRIPTION), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty(OWNER_PASSWORD_DESCRIPTION), false);
        return schema;
    }

    private static ObjectNode clearInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "paths", pathsProperty(), true);
        McpJson.addProperty(schema, "fields", McpJson.stringArrayProperty("Field IDs to clear. Defaults to [\"all\"]."), false);
        McpJson.addProperty(schema, "outputDir", McpJson.stringProperty(OUTPUT_DIR_DESCRIPTION), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty(OWNER_PASSWORD_DESCRIPTION), false);
        return schema;
    }

    private static ObjectNode renameInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "paths", pathsProperty(), true);
        McpJson.addProperty(schema, "template", McpJson.stringProperty("Rename template, e.g. '{doc.author} - {doc.title}.pdf'."), true);
        McpJson.addProperty(schema, "outputDir", McpJson.stringProperty(
                "Optional destination directory. Defaults to moving within each source directory. "
                        + "The input directory structure is preserved beneath it."), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty(OWNER_PASSWORD_DESCRIPTION), false);
        return schema;
    }

    private static ObjectNode exportInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "paths", pathsProperty(), true);
        McpJson.addProperty(schema, "fields", McpJson.stringArrayProperty(
                "Optional field IDs to export. Defaults to all readable, non-secret fields. "
                        + "Use [\"all\"] explicitly or omit. file.fullPath is always included."), false);
        McpJson.addProperty(schema, "outputDir", McpJson.stringProperty(
                "Optional output directory. Defaults to the input directory."), false);
        McpJson.addProperty(schema, "outputFile", McpJson.stringProperty(
                "Base output file name, default 'metadata-export'. The format extension is added. "
                        + "Do not include directories; use outputDir."), false);
        McpJson.addProperty(schema, "perFileExport", McpJson.booleanProperty(
                "When true, write one export file per input PDF. Default false: one aggregate file."), false);
        McpJson.addProperty(schema, "relativePaths", McpJson.booleanProperty(
                "When true (default), file.fullPath values are relative to the input directory. "
                        + "When false, absolute paths are exported."), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty(OWNER_PASSWORD_DESCRIPTION), false);
        return schema;
    }

    private static ObjectNode importFromFilenameInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "paths", pathsProperty(), true);
        McpJson.addProperty(schema, "template", McpJson.stringProperty(
                "Extract template, e.g. '{doc.author} - {doc.title}'."), true);
        McpJson.addProperty(schema, "outputDir", McpJson.stringProperty(OUTPUT_DIR_DESCRIPTION), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty(OWNER_PASSWORD_DESCRIPTION), false);
        return schema;
    }

    private static ObjectNode importFileInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "importPaths", importPathsProperty(), true);
        McpJson.addProperty(schema, "outputDir", McpJson.stringProperty(OUTPUT_DIR_DESCRIPTION), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty(
                OWNER_PASSWORD_DESCRIPTION
                        + " Used when an import record does not contain prop.ownerPassword."), false);
        return schema;
    }

    private static ObjectNode importPathsProperty() {
        ObjectNode property = McpJson.stringArrayProperty(
                "Import files and/or directories, relative to the MCP root. Directories are searched "
                        + "for files matching this tool's format. A single file is [\"one.json\"] for JSON "
                        + "imports; use the matching extension for YAML or CSV.");
        property.put("minItems", 1);
        return property;
    }

    // ---------------------------------------------------------------------
    // Output schemas
    // ---------------------------------------------------------------------

    private static ObjectNode listFieldsOutputSchema() {
        ObjectNode item = McpJson.objectSchema();
        McpJson.addProperty(item, "id", McpJson.stringProperty("Metadata field ID."), true);
        McpJson.addProperty(item, "type", McpJson.stringProperty("Field data type."), true);
        McpJson.addProperty(item, "list", McpJson.booleanProperty("Whether the field is a list."), true);
        McpJson.addProperty(item, "writable", McpJson.booleanProperty("Whether the field can be written."), true);
        McpJson.addProperty(item, "readonly", McpJson.booleanProperty("Whether the field is read-only."), true);
        McpJson.addProperty(item, "enumValues", McpJson.stringArrayProperty("Allowed values for enum fields."), false);

        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "fields", McpJson.arrayProperty("Available metadata fields.", item), true);
        return schema;
    }

    private static ObjectNode batchOutputSchema(String resultDescription, boolean readResults) {
        ObjectNode summary = McpJson.outputObjectSchema();
        McpJson.addProperty(summary, "total", McpJson.integerProperty("Number of PDF files selected."), true);
        McpJson.addProperty(summary, "succeeded", McpJson.integerProperty("Number of files that succeeded."), true);
        McpJson.addProperty(summary, "failed", McpJson.integerProperty("Number of files that failed."), true);
        McpJson.addProperty(summary, "truncated", McpJson.booleanProperty("Whether the result list was capped."), true);

        ObjectNode error = McpJson.outputObjectSchema();
        McpJson.addProperty(error, "code", McpJson.stringProperty("Machine-readable error code."), true);
        McpJson.addProperty(error, "message", McpJson.stringProperty("Human-readable error message."), true);

        ObjectNode item = McpJson.outputObjectSchema();
        McpJson.addProperty(item, "input", McpJson.stringProperty("Input path relative to the MCP root."), true);
        McpJson.addProperty(item, "output", McpJson.stringProperty("Output path, when a new file was written."), false);
        McpJson.addProperty(item, "ok", McpJson.booleanProperty("Whether the file was processed successfully."), true);
        McpJson.addProperty(item, "action", McpJson.stringProperty("Operation performed on the file."), false);
        McpJson.addProperty(item, "fields", readResults
                ? McpJson.objectMapProperty("Metadata field values for each file.")
                : McpJson.arrayProperty("Field IDs written or cleared.", McpJson.stringProperty("Metadata field ID.")), false);
        McpJson.addProperty(item, "file", McpJson.objectProperty("File information for read results."), false);
        McpJson.addProperty(item, "warnings", McpJson.stringArrayProperty("Warnings produced while processing."), false);
        McpJson.addProperty(item, "error", error, false);

        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "summary", summary, true);
        McpJson.addProperty(schema, "outputs", McpJson.stringArrayProperty("Files generated by the batch."), false);
        McpJson.addProperty(schema, "results", McpJson.arrayProperty(resultDescription, item), true);
        return schema;
    }

    private static ObjectNode importOutputSchema() {
        ObjectNode summary = McpJson.outputObjectSchema();
        McpJson.addProperty(summary, "total", McpJson.integerProperty("Number of import operations and parse failures."), true);
        McpJson.addProperty(summary, "succeeded", McpJson.integerProperty("Number of PDFs imported successfully."), true);
        McpJson.addProperty(summary, "failed", McpJson.integerProperty("Number of import operations or parse failures that failed."), true);
        McpJson.addProperty(summary, "truncated", McpJson.booleanProperty("Whether the result list was capped."), true);

        ObjectNode error = McpJson.outputObjectSchema();
        McpJson.addProperty(error, "code", McpJson.stringProperty("Machine-readable error code."), true);
        McpJson.addProperty(error, "message", McpJson.stringProperty("Human-readable error message."), true);

        ObjectNode item = McpJson.outputObjectSchema();
        McpJson.addProperty(item, "source", McpJson.stringProperty("Import file path relative to the MCP root."), true);
        McpJson.addProperty(item, "input", McpJson.stringProperty("Referenced PDF path, when one was resolved."), false);
        McpJson.addProperty(item, "output", McpJson.stringProperty("Output path, when a file was written."), false);
        McpJson.addProperty(item, "ok", McpJson.booleanProperty("Whether the import succeeded."), true);
        McpJson.addProperty(item, "action", McpJson.stringProperty("Operation performed on the file."), false);
        McpJson.addProperty(item, "fields", McpJson.arrayProperty(
                "Field IDs applied.", McpJson.stringProperty("Metadata field ID.")), false);
        McpJson.addProperty(item, "warnings", McpJson.stringArrayProperty("Warnings produced while processing."), false);
        McpJson.addProperty(item, "error", error, false);

        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "summary", summary, true);
        McpJson.addProperty(schema, "results", McpJson.arrayProperty("One entry per import operation or parse failure.", item), true);
        return schema;
    }

    // ---------------------------------------------------------------------
    // Argument helpers
    // ---------------------------------------------------------------------

    private static String requiredString(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || !node.isTextual() || node.asText().isBlank()) {
            throw new McpToolException("invalid_argument", "Missing or invalid '" + name + "' argument");
        }
        return node.asText();
    }

    private static String optionalString(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            throw new McpToolException("invalid_argument", "Invalid '" + name + "' argument: expected a string");
        }
        return node.asText();
    }

    private static List<String> requiredStringList(ObjectNode args, String name) throws McpToolException {
        List<String> values = optionalStringList(args, name);
        if (values == null || values.isEmpty()) {
            throw new McpToolException("invalid_argument", "Missing or empty '" + name + "' argument");
        }
        return values;
    }

    private static List<String> optionalStringList(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray()) {
            throw new McpToolException("invalid_argument", "Invalid '" + name + "' argument: expected an array of strings");
        }

        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isTextual()) {
                throw new McpToolException("invalid_argument", "Invalid '" + name + "' argument: expected an array of strings");
            }
            result.add(item.asText());
        }
        return result;
    }

    private static Boolean optionalBoolean(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isBoolean()) {
            throw new McpToolException("invalid_argument", "Invalid '" + name + "' argument: expected a boolean");
        }
        return node.asBoolean();
    }

    private static ObjectNode requiredObject(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || !node.isObject() || node.size() == 0) {
            throw new McpToolException("invalid_argument", "Missing or invalid '" + name + "' argument: expected a non-empty object");
        }
        return (ObjectNode) node;
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private record Tool(
            String name,
            String title,
            String description,
            ObjectNode inputSchema,
            ObjectNode outputSchema,
            ObjectNode annotations,
            Handler handler) {
    }

    @FunctionalInterface
    private interface Handler {
        ObjectNode call(ObjectNode args) throws Exception;
    }

    public static class UnknownToolException extends Exception {
        public UnknownToolException(String message) {
            super(message);
        }
    }
}
