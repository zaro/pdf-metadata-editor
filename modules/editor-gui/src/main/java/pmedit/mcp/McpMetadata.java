package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pmedit.DateFormat;
import pmedit.MetadataInfo;
import pmedit.TemplateString;
import pmedit.WebsiteConstants;
import pmedit.ext.PdfReader;
import pmedit.ext.PdfWriter;
import pmedit.ext.PmeExtension;
import pmedit.serdes.SerDeslUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Metadata operations exposed through MCP tools.
 *
 * <p>Writes follow the same rules as the GUI and CLI: only fields whose
 * enabled flag is set are written; everything else in the PDF is preserved by
 * the active {@link PdfWriter}.</p>
 */
public final class McpMetadata {

    private static final Set<String> SECRET_FIELDS = Set.of(
            "prop.ownerPassword",
            "prop.userPassword"
    );

    private final McpPaths paths;
    private final McpOptions options;

    public McpMetadata(McpPaths paths, McpOptions options) {
        this.paths = paths;
        this.options = options;
    }

    public ObjectNode read(String rawPath, String ownerPassword, List<String> requestedFields) throws Exception {
        Path file = paths.input(rawPath);
        MetadataInfo metadata = new MetadataInfo();
        reader().loadFromPDF(file.toFile(), ownerPassword, metadata);

        ObjectNode result = McpJson.object();
        result.put("path", paths.relative(file));
        result.set("file", fileInfo(metadata, file));

        ObjectNode fields = McpJson.object();
        if (requestedFields == null || requestedFields.isEmpty()) {
            for (Map.Entry<String, String> entry : metadata.asFlatStringMap().entrySet()) {
                String id = entry.getKey();
                if (SECRET_FIELDS.contains(id)) {
                    continue;
                }
                String value = entry.getValue();
                if (value == null || value.isEmpty()) {
                    continue;
                }
                if ("file.fullPath".equals(id)) {
                    value = paths.relative(file);
                }
                fields.put(id, value);
            }
        } else {
            for (String id : requestedFields) {
                if (SECRET_FIELDS.contains(id)) {
                    throw new McpToolException("Field " + id + " is not readable");
                }
                requireKnownField(id);
                String value = metadata.getString(id);
                if ("file.fullPath".equals(id)) {
                    value = paths.relative(file);
                }
                fields.put(id, value);
            }
        }

        result.set("fields", fields);
        result.set("warnings", warnings(metadata));
        return result;
    }

    public ObjectNode listFields() {
        ObjectNode result = McpJson.object();
        ArrayNode fields = McpJson.array();

        for (String id : MetadataInfo.keys()) {
            if (SECRET_FIELDS.contains(id)) {
                continue;
            }
            MetadataInfo.FieldDescription description = MetadataInfo.getFieldDescription(id);
            if (description == null) {
                continue;
            }

            ObjectNode field = McpJson.object();
            field.put("id", id);
            field.put("type", description.type == null ? "unknown" : description.type.name());
            field.put("list", description.isList);
            field.put("writable", description.isWritable && !description.isReadonly);
            field.put("readonly", description.isReadonly);

            String[] enumValues = description.getEnumValuesAsStrings();
            if (enumValues != null) {
                ArrayNode values = McpJson.array();
                for (String value : enumValues) {
                    values.add(value);
                }
                field.set("enumValues", values);
            }

            fields.add(field);
        }

        result.set("fields", fields);
        return result;
    }

    public ObjectNode set(String rawPath, JsonNode metadataValues, String rawOutput, String ownerPassword) throws Exception {
        requireWritable();
        if (metadataValues == null || !metadataValues.isObject() || metadataValues.size() == 0) {
            throw new McpToolException("metadata must be a non-empty object");
        }

        Path input = paths.input(rawPath);
        Path output = rawOutput == null || rawOutput.isBlank() ? input : paths.output(rawOutput);

        MetadataInfo changes = new MetadataInfo();
        changes.setEnabled(false);

        List<String> updated = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = metadataValues.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String id = entry.getKey();
            MetadataInfo.FieldDescription description = writableField(id);
            Object value = McpJson.toMetadataValue(description, entry.getValue());
            changes.set(id, value);
            changes.setEnabled(id, true);
            updated.add(id);
        }

        if (ownerPassword != null && !ownerPassword.isEmpty()) {
            changes.prop.ownerPassword = ownerPassword;
        }

        PdfWriter writer = writer();
        boolean unsupported = writer.allFieldsSupported(changes);
        writer.saveAsPDF(changes, input.toFile(), output.toFile());

        ObjectNode result = mutationResult("path", input, "outputPath", output, "updated", updated);
        if (unsupported) {
            addWarning(result, unsupportedFieldsWarning());
        }
        return result;
    }

    public ObjectNode clear(String rawPath, List<String> requestedFields, String rawOutput, String ownerPassword) throws Exception {
        requireWritable();

        Path input = paths.input(rawPath);
        Path output = rawOutput == null || rawOutput.isBlank() ? input : paths.output(rawOutput);

        List<String> requested = requestedFields == null || requestedFields.isEmpty()
                ? List.of("all")
                : requestedFields;

        MetadataInfo params = new MetadataInfo();
        params.setEnabled(false);

        List<String> cleared = new ArrayList<>();
        boolean all = requested.stream().anyMatch("all"::equalsIgnoreCase);
        if (all) {
            params.setEnabled(true);
            for (String id : MetadataInfo.keys()) {
                if (!SECRET_FIELDS.contains(id) && MetadataInfo.keyIsWritable(id)) {
                    cleared.add(id);
                }
            }
        } else {
            for (String id : requested) {
                if ("none".equalsIgnoreCase(id)) {
                    throw new McpToolException("'none' is not a valid clear target");
                }
                writableField(id);
                params.setEnabled(id, true);
                cleared.add(id);
            }
        }

        MetadataInfo changes = new MetadataInfo();
        changes.copyEnabled(params);
        if (ownerPassword != null && !ownerPassword.isEmpty()) {
            changes.prop.ownerPassword = ownerPassword;
        }

        PdfWriter writer = writer();
        boolean unsupported = writer.allFieldsSupported(changes);
        writer.saveAsPDF(changes, input.toFile(), output.toFile());

        ObjectNode result = mutationResult("path", input, "outputPath", output, "cleared", cleared);
        if (unsupported) {
            addWarning(result, unsupportedFieldsWarning());
        }
        return result;
    }

    public ObjectNode rename(String rawPath, String template, String outputDir, String ownerPassword) throws Exception {
        requireWritable();
        if (template == null || template.isBlank()) {
            throw new McpToolException("template is required");
        }

        Path input = paths.input(rawPath);
        MetadataInfo metadata = new MetadataInfo();
        reader().loadFromPDF(input.toFile(), ownerPassword, metadata);

        String targetName = template.toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? template
                : template + ".pdf";

        String evaluated;
        try {
            evaluated = new TemplateString(targetName).process(metadata);
        } catch (RuntimeException e) {
            throw new McpToolException("Failed to evaluate rename template: " + e.getMessage(), e);
        }
        if (evaluated.isBlank()) {
            throw new McpToolException("Rename template produced an empty file name");
        }

        Path target = paths.renameTarget(input, evaluated, outputDir);
        if (Files.exists(target)) {
            throw new McpToolException("Destination file already exists: " + paths.relative(target));
        }

        try {
            if (outputDir == null || outputDir.isBlank()) {
                Files.move(input, target);
            } else {
                Files.copy(input, target, StandardCopyOption.COPY_ATTRIBUTES);
            }
        } catch (IOException e) {
            throw new McpToolException("Rename failed: " + e.getMessage(), e);
        }

        ObjectNode result = McpJson.object();
        result.put("inputPath", paths.relative(input));
        result.put("outputPath", paths.relative(target));
        result.put("renamed", true);
        return result;
    }

    public ObjectNode export(String rawPath, String format, String rawOutput, String ownerPassword) throws Exception {
        Path input = paths.input(rawPath);

        String normalizedFormat;
        if ("json".equalsIgnoreCase(format)) {
            normalizedFormat = "json";
        } else if ("yaml".equalsIgnoreCase(format)) {
            normalizedFormat = "yaml";
        } else {
            throw new McpToolException("format must be 'json' or 'yaml'");
        }

        MetadataInfo metadata = new MetadataInfo();
        reader().loadFromPDF(input.toFile(), ownerPassword, metadata);

        Map<String, Object> values = metadata.asFlatMap(false);
        values.remove("prop.ownerPassword");
        values.remove("prop.userPassword");
        values.put("file.fullPath", paths.relative(input));

        String content = "json".equals(normalizedFormat)
                ? SerDeslUtils.toJSON(true, values)
                : SerDeslUtils.toYAML(values);

        ObjectNode result = McpJson.object();
        result.put("path", paths.relative(input));
        result.put("format", normalizedFormat);

        if (rawOutput == null || rawOutput.isBlank()) {
            result.put("content", content);
        } else {
            Path output = paths.output(rawOutput);
            try {
                Files.writeString(output, content, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new McpToolException("Failed to write export: " + e.getMessage(), e);
            }
            result.put("outputPath", paths.relative(output));
        }

        return result;
    }

    private void requireWritable() throws McpToolException {
        if (options.readOnly()) {
            throw new McpToolException("The MCP server is running in read-only mode.");
        }
        if (!options.licenseGate().getAsBoolean()) {
            throw new McpToolException(
                    "Editing PDF metadata through MCP requires a license. You can get one at "
                            + WebsiteConstants.batchLicenseUrl);
        }
    }

    private static void requireKnownField(String id) throws McpToolException {
        if (!MetadataInfo.validMdNames.contains(id)) {
            throw new McpToolException("Unknown metadata field: " + id);
        }
    }

    private static MetadataInfo.FieldDescription writableField(String id) throws McpToolException {
        if (SECRET_FIELDS.contains(id)) {
            throw new McpToolException("Use the top-level ownerPassword argument for field " + id);
        }
        requireKnownField(id);

        MetadataInfo.FieldDescription description = MetadataInfo.getFieldDescription(id);
        if (description == null) {
            throw new McpToolException("Unknown metadata field: " + id);
        }
        if (!description.isWritable || description.isReadonly) {
            throw new McpToolException("Field is read-only: " + id);
        }
        return description;
    }

    private ObjectNode fileInfo(MetadataInfo metadata, Path file) {
        ObjectNode info = McpJson.object();
        info.put("path", paths.relative(file));
        putNullable(info, "nameWithExt", metadata.file.nameWithExt);
        putNullable(info, "sizeBytes", metadata.file.sizeBytes);
        putNullable(info, "size", metadata.file.size);
        putNullable(info, "createTime", DateFormat.formatDateTime(metadata.file.createTime));
        putNullable(info, "modifyTime", DateFormat.formatDateTime(metadata.file.modifyTime));
        return info;
    }

    private static ArrayNode warnings(MetadataInfo metadata) {
        ArrayNode warnings = McpJson.array();
        if (metadata.warnings != null) {
            for (MetadataInfo.Warning warning : metadata.warnings) {
                if (warning == null || warning.msg() == null || warning.msg().isBlank()) {
                    continue;
                }
                warnings.add(warning.type() + ": " + warning.msg());
            }
        }
        return warnings;
    }

    private ObjectNode mutationResult(
            String pathKey,
            Path input,
            String outputKey,
            Path output,
            String listKey,
            List<String> values) {
        ObjectNode result = McpJson.object();
        result.put(pathKey, input == null ? "" : paths.relative(input));
        result.put(outputKey, output == null ? "" : paths.relative(output));

        ArrayNode array = McpJson.array();
        for (String value : values) {
            array.add(value);
        }
        result.set(listKey, array);
        return result;
    }

    private static void addWarning(ObjectNode result, String message) {
        ArrayNode warnings = (ArrayNode) result.get("warnings");
        if (warnings == null) {
            warnings = McpJson.array();
            result.set("warnings", warnings);
        }
        warnings.add(message);
    }

    private static String unsupportedFieldsWarning() {
        return "The active extension may not support some requested fields "
                + "(encryption, permissions, rights, or file properties). A license may be required.";
    }

    private static void putNullable(ObjectNode node, String name, String value) {
        if (value == null) {
            node.putNull(name);
        } else {
            node.put(name, value);
        }
    }

    private static void putNullable(ObjectNode node, String name, Long value) {
        if (value == null) {
            node.putNull(name);
        } else {
            node.put(name, value);
        }
    }

    private static PdfReader reader() {
        return PmeExtension.get().newPdfReader();
    }

    private static PdfWriter writer() {
        return PmeExtension.get().newPdfWriter();
    }
}
