package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import pmedit.DateFormat;
import pmedit.MetadataInfo;
import pmedit.TemplateString;
import pmedit.WebsiteConstants;
import pmedit.ext.PdfReader;
import pmedit.ext.PdfWriter;
import pmedit.ext.PmeExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Metadata operations exposed through MCP tools.
 *
 * <p>All file operations are batch operations, mirroring the CLI: they take a
 * list of files or directories and return one result per file. Writes follow
 * the same rules as the GUI and CLI: only fields whose enabled flag is set are
 * written; everything else in the PDF is preserved by the active
 * {@link PdfWriter}.</p>
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

    public McpBatchResult readBatch(
            List<String> rawPaths,
            String ownerPassword,
            List<String> requestedFields) throws Exception {
        if (requestedFields != null) {
            for (String id : requestedFields) {
                if (SECRET_FIELDS.contains(id)) {
                    throw new McpToolException("invalid_argument", "Field " + id + " is not readable");
                }
                requireKnownField(id);
            }
        }

        List<McpPaths.BatchEntry> entries = paths.expand(rawPaths, null);
        McpBatchResult batch = new McpBatchResult();
        for (McpPaths.BatchEntry entry : entries) {
            try {
                MetadataInfo metadata = load(entry.input(), ownerPassword);
                batch.addReadSuccess(entry.input(), readPayload(entry.input(), metadata, requestedFields), paths);
            } catch (Exception e) {
                batch.addError(entry.input(), paths, classifyReadError(e, ownerPassword));
            }
        }
        return batch;
    }

    public McpBatchResult setBatch(
            List<String> rawPaths,
            JsonNode metadataValues,
            String rawOutputDir,
            String ownerPassword) throws Exception {
        requireWritable();
        if (metadataValues == null || !metadataValues.isObject() || metadataValues.size() == 0) {
            throw new McpToolException("invalid_argument", "metadata must be a non-empty object");
        }

        Map<String, Object> values = prepareChanges(metadataValues);
        List<String> updated = new ArrayList<>(values.keySet());

        Path outputDir = paths.outputDirectoryPath(rawOutputDir);
        List<McpPaths.BatchEntry> entries = paths.expand(rawPaths, outputDir);
        outputDir = paths.outputDirectory(rawOutputDir);

        PdfWriter writer = writer();
        McpBatchResult batch = new McpBatchResult();
        for (McpPaths.BatchEntry entry : entries) {
            try {
                MetadataInfo fileMetadata = load(entry.input(), ownerPassword);
                MetadataInfo changes = buildChanges(values, fileMetadata, ownerPassword);
                Path output = outputPath(entry, outputDir);
                boolean unsupported = writer.allFieldsSupported(changes);
                writer.saveAsPDF(changes, entry.input().toFile(), output.toFile());
                batch.addMutationSuccess(
                        entry.input(),
                        output,
                        paths,
                        "updated",
                        updated,
                        unsupported ? List.of(unsupportedFieldsWarning()) : List.of());
            } catch (Exception e) {
                batch.addError(entry.input(), paths, classifyWriteError(e, ownerPassword));
            }
        }
        return batch;
    }

    public McpBatchResult clearBatch(
            List<String> rawPaths,
            List<String> requestedFields,
            String rawOutputDir,
            String ownerPassword) throws Exception {
        requireWritable();

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
                    throw new McpToolException("invalid_argument", "'none' is not a valid clear target");
                }
                writableField(id);
                params.setEnabled(id, true);
                cleared.add(id);
            }
        }

        Path outputDir = paths.outputDirectoryPath(rawOutputDir);
        List<McpPaths.BatchEntry> entries = paths.expand(rawPaths, outputDir);
        outputDir = paths.outputDirectory(rawOutputDir);

        PdfWriter writer = writer();
        McpBatchResult batch = new McpBatchResult();
        for (McpPaths.BatchEntry entry : entries) {
            try {
                MetadataInfo changes = new MetadataInfo();
                changes.copyEnabled(params);
                if (ownerPassword != null && !ownerPassword.isEmpty()) {
                    changes.prop.ownerPassword = ownerPassword;
                }
                Path output = outputPath(entry, outputDir);
                boolean unsupported = writer.allFieldsSupported(changes);
                writer.saveAsPDF(changes, entry.input().toFile(), output.toFile());
                batch.addMutationSuccess(
                        entry.input(),
                        output,
                        paths,
                        "cleared",
                        cleared,
                        unsupported ? List.of(unsupportedFieldsWarning()) : List.of());
            } catch (Exception e) {
                batch.addError(entry.input(), paths, classifyWriteError(e, ownerPassword));
            }
        }
        return batch;
    }

    public McpBatchResult renameBatch(
            List<String> rawPaths,
            String template,
            String rawOutputDir,
            String ownerPassword) throws Exception {
        requireWritable();
        if (template == null || template.isBlank()) {
            throw new McpToolException("invalid_argument", "template is required");
        }

        Path outputDir = paths.outputDirectoryPath(rawOutputDir);
        List<McpPaths.BatchEntry> entries = paths.expand(rawPaths, outputDir);
        outputDir = paths.outputDirectory(rawOutputDir);

        String targetTemplate = template.toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? template
                : template + ".pdf";

        McpBatchResult batch = new McpBatchResult();
        for (McpPaths.BatchEntry entry : entries) {
            try {
                MetadataInfo metadata = load(entry.input(), ownerPassword);

                String evaluated;
                try {
                    evaluated = new TemplateString(targetTemplate).process(metadata);
                } catch (RuntimeException e) {
                    throw new McpToolException(
                            "invalid_template",
                            "Failed to evaluate rename template: " + e.getMessage(),
                            e);
                }
                if (evaluated.isBlank()) {
                    throw new McpToolException(
                            "invalid_template",
                            "Rename template produced an empty file name");
                }

                Path base = outputDir == null
                        ? entry.input().getParent()
                        : outputDir.resolve(entry.relative()).normalize().getParent();
                Path target = paths.prepareOutput(base.resolve(evaluated).normalize());
                if (Files.exists(target)) {
                    throw new McpToolException(
                            "destination_exists",
                            "Destination file already exists: " + paths.relative(target));
                }

                if (outputDir == null) {
                    Files.move(entry.input(), target);
                } else {
                    Files.copy(entry.input(), target, StandardCopyOption.COPY_ATTRIBUTES);
                }

                batch.addMutationSuccess(
                        entry.input(), target, paths, "renamed", List.of(), List.of());
            } catch (Exception e) {
                batch.addError(entry.input(), paths, classifyWriteError(e, ownerPassword));
            }
        }
        return batch;
    }

    private ObjectNode readPayload(Path file, MetadataInfo metadata, List<String> requestedFields) {
        ObjectNode payload = McpJson.object();
        payload.set("file", fileInfo(metadata, file));

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
                String value = metadata.getString(id);
                if ("file.fullPath".equals(id)) {
                    value = paths.relative(file);
                }
                fields.put(id, value);
            }
        }

        payload.set("fields", fields);
        payload.set("warnings", warnings(metadata));
        return payload;
    }

    private Map<String, Object> prepareChanges(JsonNode metadataValues) throws McpToolException {
        Map<String, Object> values = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = metadataValues.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String id = entry.getKey();
            MetadataInfo.FieldDescription description = writableField(id);
            values.put(id, McpJson.toMetadataValue(description, entry.getValue()));
        }
        return values;
    }

    private static MetadataInfo buildChanges(
            Map<String, Object> values,
            MetadataInfo expandInfo,
            String ownerPassword) throws McpToolException {
        MetadataInfo changes = new MetadataInfo();
        changes.setEnabled(false);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            changes.set(entry.getKey(), expandValue(entry.getValue(), expandInfo));
            changes.setEnabled(entry.getKey(), true);
        }
        if (ownerPassword != null && !ownerPassword.isEmpty()) {
            changes.prop.ownerPassword = ownerPassword;
        }
        return changes;
    }

    private static Object expandValue(Object value, MetadataInfo expandInfo) throws McpToolException {
        try {
            if (value instanceof String stringValue) {
                return new TemplateString(stringValue).process(expandInfo);
            }
            if (value instanceof List<?> list) {
                List<Object> expanded = new ArrayList<>();
                for (Object item : list) {
                    expanded.add(item instanceof String stringItem
                            ? new TemplateString(stringItem).process(expandInfo)
                            : item);
                }
                return expanded;
            }
            return value;
        } catch (RuntimeException e) {
            throw new McpToolException(
                    "invalid_template",
                    "Failed to expand metadata template: " + e.getMessage(),
                    e);
        }
    }

    private Path outputPath(McpPaths.BatchEntry entry, Path outputDir) throws McpToolException {
        if (outputDir == null) {
            return entry.input();
        }
        return paths.prepareOutput(outputDir.resolve(entry.relative()).normalize());
    }

    private void requireWritable() throws McpToolException {
        if (options.readOnly()) {
            throw new McpToolException(
                    "read_only",
                    "The MCP server is running in read-only mode.");
        }
        if (!options.licenseGate().getAsBoolean()) {
            throw new McpToolException(
                    "license_required",
                    "Editing PDF metadata through MCP requires a license. You can get one at "
                            + WebsiteConstants.batchLicenseUrl);
        }
    }

    private MetadataInfo load(Path input, String ownerPassword) throws Exception {
        MetadataInfo metadata = new MetadataInfo();
        try {
            reader().loadFromPDF(input.toFile(), ownerPassword, metadata);
        } catch (InvalidPasswordException e) {
            throw passwordException(ownerPassword, e);
        } catch (IOException e) {
            throw new McpToolException("read_failed", "Failed to read " + paths.relative(input) + ": " + e.getMessage(), e);
        }
        return metadata;
    }

    private static McpToolException classifyReadError(Exception e, String ownerPassword) {
        if (e instanceof McpToolException toolError) {
            return toolError;
        }
        InvalidPasswordException passwordError = findInvalidPassword(e);
        if (passwordError != null) {
            return passwordException(ownerPassword, passwordError);
        }
        return new McpToolException("read_failed", safeMessage(e), e);
    }

    private McpToolException classifyWriteError(Exception e, String ownerPassword) {
        if (e instanceof McpToolException toolError) {
            return toolError;
        }
        InvalidPasswordException passwordError = findInvalidPassword(e);
        if (passwordError != null) {
            return passwordException(ownerPassword, passwordError);
        }
        return new McpToolException("write_failed", safeMessage(e), e);
    }

    private static McpToolException passwordException(String suppliedPassword, InvalidPasswordException cause) {
        if (suppliedPassword == null || suppliedPassword.isEmpty()) {
            return new McpToolException(
                    "password_required",
                    "This PDF is password-protected. Retry with the ownerPassword argument.",
                    cause);
        }
        return new McpToolException(
                "invalid_password",
                "The supplied ownerPassword could not open this PDF.",
                cause);
    }

    private static InvalidPasswordException findInvalidPassword(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof InvalidPasswordException passwordException) {
                return passwordException;
            }
            current = current.getCause();
        }
        return null;
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private static void requireKnownField(String id) throws McpToolException {
        if (!MetadataInfo.validMdNames.contains(id)) {
            throw new McpToolException("invalid_argument", "Unknown metadata field: " + id);
        }
    }

    private static MetadataInfo.FieldDescription writableField(String id) throws McpToolException {
        if (SECRET_FIELDS.contains(id)) {
            throw new McpToolException(
                    "invalid_argument",
                    "Use the top-level ownerPassword argument for field " + id);
        }
        requireKnownField(id);

        MetadataInfo.FieldDescription description = MetadataInfo.getFieldDescription(id);
        if (description == null) {
            throw new McpToolException("invalid_argument", "Unknown metadata field: " + id);
        }
        if (!description.isWritable || description.isReadonly) {
            throw new McpToolException("invalid_argument", "Field is read-only: " + id);
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
