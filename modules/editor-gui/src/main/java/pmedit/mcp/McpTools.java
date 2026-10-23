package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Tool definitions and argument dispatch for the MCP server.
 */
public final class McpTools {

    private static final Logger LOG = LoggerFactory.getLogger(McpTools.class);

    private final McpMetadata metadata;
    private final List<Tool> tools;

    public McpTools(McpMetadata metadata) {
        this.metadata = metadata;
        this.tools = buildTools();
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
            return McpResult.error(e.getMessage());
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
                        + "Read-only; does not require a Pro license.",
                emptySchema(),
                listFieldsOutputSchema(),
                McpJson.annotations(true, false, true),
                args -> metadata.listFields()
        ));

        definitions.add(new Tool(
                "pdf_read_metadata",
                "Read PDF metadata",
                "Read metadata from a PDF. Paths are relative to the configured MCP root unless absolute "
                        + "paths inside that root are used. By default only populated fields are returned; "
                        + "pass fields to request specific field IDs. Read-only; does not require a Pro license.",
                readInputSchema(),
                readOutputSchema(),
                McpJson.annotations(true, false, true),
                args -> metadata.read(
                        requiredString(args, "path"),
                        optionalString(args, "ownerPassword"),
                        optionalStringList(args, "fields"))
        ));

        definitions.add(new Tool(
                "pdf_set_metadata",
                "Set PDF metadata",
                "Set one or more metadata fields on a PDF. Only the supplied fields are changed; all other "
                        + "metadata in the document is preserved. Writes in place by default, or to outputPath "
                        + "when supplied. Requires a Pro license.",
                setInputSchema(),
                mutationOutputSchema("updated", "The field IDs that were written."),
                McpJson.annotations(false, false, true),
                args -> metadata.set(
                        requiredString(args, "path"),
                        requiredObject(args, "metadata"),
                        optionalString(args, "outputPath"),
                        optionalString(args, "ownerPassword"))
        ));

        definitions.add(new Tool(
                "pdf_clear_metadata",
                "Clear PDF metadata",
                "Clear all metadata or only the selected fields. Writes in place by default, or to outputPath "
                        + "when supplied. Requires a Pro license.",
                clearInputSchema(),
                mutationOutputSchema("cleared", "The field IDs that were cleared."),
                McpJson.annotations(false, true, true),
                args -> metadata.clear(
                        requiredString(args, "path"),
                        optionalStringList(args, "fields"),
                        optionalString(args, "outputPath"),
                        optionalString(args, "ownerPassword"))
        ));

        definitions.add(new Tool(
                "pdf_rename",
                "Rename PDF from metadata",
                "Rename a PDF using a metadata template such as '{doc.author} - {doc.title}.pdf'. "
                        + "The destination must not exist. When outputDir is omitted the file is moved within "
                        + "its current directory; when supplied the file is copied there. Requires a Pro license.",
                renameInputSchema(),
                renameOutputSchema(),
                McpJson.annotations(false, false, false),
                args -> metadata.rename(
                        requiredString(args, "path"),
                        requiredString(args, "template"),
                        optionalString(args, "outputDir"),
                        optionalString(args, "ownerPassword"))
        ));

        definitions.add(new Tool(
                "pdf_export_metadata",
                "Export PDF metadata",
                "Export metadata as JSON or YAML. When outputPath is omitted the serialized document is "
                        + "returned inline. Read-only; does not require a Pro license.",
                exportInputSchema(),
                exportOutputSchema(),
                McpJson.annotations(true, false, true),
                args -> metadata.export(
                        requiredString(args, "path"),
                        requiredString(args, "format"),
                        optionalString(args, "outputPath"),
                        optionalString(args, "ownerPassword"))
        ));

        return definitions;
    }

    // ---------------------------------------------------------------------
    // Input schemas
    // ---------------------------------------------------------------------

    private static ObjectNode emptySchema() {
        return McpJson.objectSchema();
    }

    private static ObjectNode readInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("PDF path relative to the MCP root."), true);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty("Owner password for an encrypted PDF."), false);
        McpJson.addProperty(schema, "fields", McpJson.stringArrayProperty("Optional subset of metadata field IDs."), false);
        return schema;
    }

    private static ObjectNode setInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("PDF path relative to the MCP root."), true);
        McpJson.addProperty(schema, "metadata", McpJson.objectProperty("Map of metadata field IDs to values."), true);
        McpJson.addProperty(schema, "outputPath", McpJson.stringProperty("Optional output path. Defaults to in-place update."), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty("Owner password for an encrypted PDF."), false);
        return schema;
    }

    private static ObjectNode clearInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("PDF path relative to the MCP root."), true);
        McpJson.addProperty(schema, "fields", McpJson.stringArrayProperty("Field IDs to clear. Defaults to [\"all\"]."), false);
        McpJson.addProperty(schema, "outputPath", McpJson.stringProperty("Optional output path. Defaults to in-place update."), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty("Owner password for an encrypted PDF."), false);
        return schema;
    }

    private static ObjectNode renameInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("PDF path relative to the MCP root."), true);
        McpJson.addProperty(schema, "template", McpJson.stringProperty("Rename template, e.g. '{doc.author} - {doc.title}.pdf'."), true);
        McpJson.addProperty(schema, "outputDir", McpJson.stringProperty("Optional destination directory. Defaults to the source directory."), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty("Owner password for an encrypted PDF."), false);
        return schema;
    }

    private static ObjectNode exportInputSchema() {
        ObjectNode schema = McpJson.objectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("PDF path relative to the MCP root."), true);
        McpJson.addProperty(schema, "format", McpJson.enumProperty("Export format.", "json", "yaml"), true);
        McpJson.addProperty(schema, "outputPath", McpJson.stringProperty("Optional output file path. Omit to return the content inline."), false);
        McpJson.addProperty(schema, "ownerPassword", McpJson.stringProperty("Owner password for an encrypted PDF."), false);
        return schema;
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

    private static ObjectNode readOutputSchema() {
        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("Path relative to the MCP root."), true);
        McpJson.addProperty(schema, "file", McpJson.objectProperty("File information."), true);
        McpJson.addProperty(schema, "fields", McpJson.objectMapProperty("Metadata field values."), true);
        McpJson.addProperty(schema, "warnings", McpJson.stringArrayProperty("Parser warnings."), false);
        return schema;
    }

    private static ObjectNode mutationOutputSchema(String listKey, String listDescription) {
        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("Input path relative to the MCP root."), true);
        McpJson.addProperty(schema, "outputPath", McpJson.stringProperty("Output path relative to the MCP root."), true);
        McpJson.addProperty(schema, listKey, McpJson.stringArrayProperty(listDescription), true);
        McpJson.addProperty(schema, "warnings", McpJson.stringArrayProperty("Warnings produced while writing."), false);
        return schema;
    }

    private static ObjectNode renameOutputSchema() {
        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "inputPath", McpJson.stringProperty("Original path relative to the MCP root."), true);
        McpJson.addProperty(schema, "outputPath", McpJson.stringProperty("New path relative to the MCP root."), true);
        McpJson.addProperty(schema, "renamed", McpJson.booleanProperty("Whether the rename succeeded."), true);
        return schema;
    }

    private static ObjectNode exportOutputSchema() {
        ObjectNode schema = McpJson.outputObjectSchema();
        McpJson.addProperty(schema, "path", McpJson.stringProperty("Path relative to the MCP root."), true);
        McpJson.addProperty(schema, "format", McpJson.enumProperty("Export format.", "json", "yaml"), true);
        McpJson.addProperty(schema, "content", McpJson.stringProperty("Serialized metadata when no outputPath was supplied."), false);
        McpJson.addProperty(schema, "outputPath", McpJson.stringProperty("Written file path when outputPath was supplied."), false);
        return schema;
    }

    // ---------------------------------------------------------------------
    // Argument helpers
    // ---------------------------------------------------------------------

    private static String requiredString(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || !node.isTextual() || node.asText().isBlank()) {
            throw new McpToolException("Missing or invalid '" + name + "' argument");
        }
        return node.asText();
    }

    private static String optionalString(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            throw new McpToolException("Invalid '" + name + "' argument: expected a string");
        }
        return node.asText();
    }

    private static List<String> optionalStringList(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray()) {
            throw new McpToolException("Invalid '" + name + "' argument: expected an array of strings");
        }

        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isTextual()) {
                throw new McpToolException("Invalid '" + name + "' argument: expected an array of strings");
            }
            result.add(item.asText());
        }
        return result;
    }

    private static ObjectNode requiredObject(ObjectNode args, String name) throws McpToolException {
        JsonNode node = args.get(name);
        if (node == null || !node.isObject() || node.size() == 0) {
            throw new McpToolException("Missing or invalid '" + name + "' argument: expected a non-empty object");
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
