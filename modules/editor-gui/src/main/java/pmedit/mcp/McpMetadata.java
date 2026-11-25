package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pmedit.BatchEngine;
import pmedit.BatchException;
import pmedit.BatchFailure;
import pmedit.BatchFailures;
import pmedit.BatchOperationParameters;
import pmedit.BatchOptions;
import pmedit.BatchPreflight;
import pmedit.BatchRequest;
import pmedit.CommandDescription;
import pmedit.DateFormat;
import pmedit.MetadataInfo;
import pmedit.WebsiteConstants;
import pmedit.ext.CommandResult;
import pmedit.ext.PdfReader;
import pmedit.ext.PmeExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Metadata operations exposed through MCP tools.
 *
 * <p>File tools use the same {@link BatchEngine} as the CLI and GUI. The MCP
 * layer is responsible for JSON conversion, schemas, the result envelope, and
 * the read-only/root policy.</p>
 */
public final class McpMetadata {

    static final Set<String> SECRET_FIELDS = Set.of(
            "prop.ownerPassword",
            "prop.userPassword"
    );

    static final String LICENSE_MESSAGE =
            "Editing PDF metadata through MCP requires a license. You can get one at "
                    + WebsiteConstants.batchLicenseUrl;

    private final McpPaths paths;
    private final McpOptions options;
    private final CommandRunner commands;

    public McpMetadata(McpPaths paths, McpOptions options) {
        this(paths, options, (command, argument) -> PmeExtension.get().executeCommand(command, argument));
    }

    public McpMetadata(McpPaths paths, McpOptions options, CommandRunner commands) {
        this.paths = paths;
        this.options = options;
        this.commands = commands == null
                ? (command, argument) -> PmeExtension.get().executeCommand(command, argument)
                : commands;
    }

    /**
     * Runs an extension command such as license activation. The command name
     * and argument are passed through unchanged.
     */
    public CommandResult executeCommand(String command, String argument) {
        return commands.execute(command, argument);
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
        validateRequestedFields(requestedFields);

        BatchRequest request = new BatchRequest(toFiles(rawPaths), null, readOptions(ownerPassword));
        BatchPreflight preflight;
        try {
            preflight = new BatchEngine().preflight(BatchEngine.PDF_EXTENSIONS, request);
        } catch (BatchException e) {
            throw new McpToolException(e.code(), e.getMessage(), e);
        }

        McpBatchResult batch = new McpBatchResult();
        for (File file : preflight.files()) {
            Path path = file.toPath();
            try {
                MetadataInfo metadata = load(path, ownerPassword);
                batch.addReadSuccess(path, readPayload(path, metadata, requestedFields), paths);
            } catch (Exception e) {
                BatchFailure failure = BatchFailures.classify(e, ownerPassword, "read_failed");
                batch.addError(paths.relative(path), failure.code(), failure.message());
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

        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);
        Iterator<Map.Entry<String, JsonNode>> iterator = metadataValues.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String id = entry.getKey();
            MetadataInfo.FieldDescription description = writableField(id);
            metadata.set(id, McpJson.toMetadataValue(description, entry.getValue()));
            metadata.setEnabled(id, true);
        }

        BatchOperationParameters params = new BatchOperationParameters();
        params.metadata = metadata;
        return run(CommandDescription.EDIT, params, rawPaths, rawOutputDir, ownerPassword);
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

        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);

        boolean all = requested.stream().anyMatch("all"::equalsIgnoreCase);
        if (all) {
            metadata.setEnabled(true);
        } else {
            for (String id : requested) {
                if ("none".equalsIgnoreCase(id)) {
                    throw new McpToolException("invalid_argument", "'none' is not a valid clear target");
                }
                writableField(id);
                metadata.setEnabled(id, true);
            }
        }

        BatchOperationParameters params = new BatchOperationParameters();
        params.metadata = metadata;
        return run(CommandDescription.CLEAR, params, rawPaths, rawOutputDir, ownerPassword);
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

        BatchOperationParameters params = new BatchOperationParameters();
        params.renameTemplate = template;
        return run(CommandDescription.RENAME, params, rawPaths, rawOutputDir, ownerPassword);
    }

    public McpBatchResult exportBatch(
            String format,
            List<String> rawPaths,
            List<String> fields,
            String rawOutputDir,
            String rawOutputFile,
            Boolean perFileExport,
            Boolean relativePaths,
            String ownerPassword) throws Exception {
        requireWritable();

        BatchOperationParameters params = new BatchOperationParameters();
        params.metadata = exportFields(fields);
        params.outputFile = outputFile(rawOutputFile);
        params.perFileExport = perFileExport;
        params.useRelativePaths = relativePaths;
        return run(exportCommand(format), params, rawPaths, rawOutputDir, ownerPassword);
    }

    public McpBatchResult importBatch(
            String format,
            List<String> rawImportPaths,
            String rawOutputDir,
            String ownerPassword) throws Exception {
        requireWritable();

        BatchOperationParameters params = new BatchOperationParameters();
        return run(
                importCommand(format),
                params,
                toImportRoots(rawImportPaths),
                outputDir(rawOutputDir),
                ownerPassword);
    }

    public McpBatchResult importFromFilenameBatch(
            List<String> rawPaths,
            String template,
            String rawOutputDir,
            String ownerPassword) throws Exception {
        requireWritable();
        if (template == null || template.isBlank()) {
            throw new McpToolException("invalid_argument", "template is required");
        }

        BatchOperationParameters params = new BatchOperationParameters();
        params.extractTemplate = template;
        return run(CommandDescription.FROM_FILE_NAME, params, rawPaths, rawOutputDir, ownerPassword);
    }

    private static CommandDescription importCommand(String format) throws McpToolException {
        if ("json".equalsIgnoreCase(format)) {
            return CommandDescription.FROM_JSON;
        }
        if ("yaml".equalsIgnoreCase(format)) {
            return CommandDescription.FROM_YAML;
        }
        if ("csv".equalsIgnoreCase(format)) {
            return CommandDescription.FROM_CSV;
        }
        throw new McpToolException("invalid_argument", "format must be json, yaml, or csv");
    }

    private static CommandDescription exportCommand(String format) throws McpToolException {
        if ("json".equalsIgnoreCase(format)) {
            return CommandDescription.TO_JSON;
        }
        if ("yaml".equalsIgnoreCase(format)) {
            return CommandDescription.TO_YAML;
        }
        if ("csv".equalsIgnoreCase(format)) {
            return CommandDescription.TO_CSV;
        }
        throw new McpToolException("invalid_argument", "format must be json, yaml, or csv");
    }

    private static String outputFile(String raw) throws McpToolException {
        if (raw == null || raw.isBlank()) {
            return BatchOperationParameters.DEFAULT_OUTPUT_FILENAME;
        }
        String name = raw.trim();
        if (".".equals(name) || "..".equals(name)
                || name.contains("/") || name.contains("\\")) {
            throw new McpToolException(
                    "invalid_argument",
                    "outputFile must be a base file name; use outputDir for directories");
        }
        return name;
    }

    private static MetadataInfo exportFields(List<String> requestedFields) throws McpToolException {
        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);

        if (requestedFields == null) {
            enableAllExportFields(metadata);
            return metadata;
        }
        if (requestedFields.isEmpty()) {
            throw new McpToolException("invalid_argument", "fields must not be empty; omit it to export all fields");
        }

        boolean all = requestedFields.stream().anyMatch("all"::equalsIgnoreCase);
        if (all) {
            if (requestedFields.size() != 1) {
                throw new McpToolException("invalid_argument", "'all' must be used alone");
            }
            enableAllExportFields(metadata);
            return metadata;
        }

        for (String id : requestedFields) {
            if ("none".equalsIgnoreCase(id)) {
                throw new McpToolException("invalid_argument", "'none' is not a valid export field selection");
            }
            exportField(id);
            metadata.setEnabled(id, true);
        }
        return metadata;
    }

    private static void enableAllExportFields(MetadataInfo metadata) {
        metadata.setEnabled(true);
        for (String secret : SECRET_FIELDS) {
            metadata.setEnabled(secret, false);
        }
    }

    private static void exportField(String id) throws McpToolException {
        if (SECRET_FIELDS.contains(id)) {
            throw new McpToolException("invalid_argument", "Field " + id + " is not exportable");
        }
        requireKnownField(id);
        if (MetadataInfo.getFieldDescription(id) == null) {
            throw new McpToolException("invalid_argument", "Unknown metadata field: " + id);
        }
    }

    private McpBatchResult run(
            CommandDescription command,
            BatchOperationParameters params,
            List<String> rawPaths,
            String rawOutputDir,
            String ownerPassword) throws Exception {
        return run(command, params, toFiles(rawPaths), outputDir(rawOutputDir), ownerPassword);
    }

    private McpBatchResult run(
            CommandDescription command,
            BatchOperationParameters params,
            List<File> roots,
            File outputDir,
            String ownerPassword) throws Exception {
        BatchRequest request = new BatchRequest(
                roots,
                outputDir,
                mutationOptions(ownerPassword));
        McpBatchListener listener = new McpBatchListener(paths);
        new BatchEngine().run(command, params, request, listener);
        return listener.finish();
    }

    private List<File> toFiles(List<String> rawPaths) throws McpToolException {
        List<File> files = new ArrayList<>();
        for (String raw : rawPaths) {
            Path resolved = paths.resolveExisting(raw);
            if (Files.isRegularFile(resolved) && !isPdf(resolved)) {
                throw new McpToolException("not_a_pdf", "Not a PDF file: " + raw);
            }
            files.add(resolved.toFile());
        }
        return files;
    }

    private List<File> toImportRoots(List<String> rawPaths) throws McpToolException {
        List<File> roots = new ArrayList<>();
        for (String raw : rawPaths) {
            roots.add(paths.resolveExisting(raw).toFile());
        }
        return roots;
    }

    private static boolean isPdf(Path path) {
        Path name = path.getFileName();
        return name != null && name.toString().toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private File outputDir(String raw) throws McpToolException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return paths.outputDirectoryPath(raw).toFile();
    }

    private BatchOptions readOptions(String ownerPassword) {
        return new BatchOptions(
                false,
                options.licenseGate(),
                options.maxFiles(),
                true,
                true,
                false,
                ownerPassword,
                new McpBatchPathPolicy(paths));
    }

    private BatchOptions mutationOptions(String ownerPassword) {
        return new BatchOptions(
                true,
                options.licenseGate(),
                options.maxFiles(),
                true,
                true,
                false,
                ownerPassword,
                new McpBatchPathPolicy(paths));
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

    private static void validateRequestedFields(List<String> requestedFields) throws McpToolException {
        if (requestedFields == null) {
            return;
        }
        for (String id : requestedFields) {
            if (SECRET_FIELDS.contains(id)) {
                throw new McpToolException("invalid_argument", "Field " + id + " is not readable");
            }
            requireKnownField(id);
        }
    }

    private void requireWritable() throws McpToolException {
        if (options.readOnly()) {
            throw new McpToolException(
                    "read_only",
                    "The MCP server is running in read-only mode.");
        }
    }

    private static MetadataInfo load(Path input, String ownerPassword) throws Exception {
        MetadataInfo metadata = new MetadataInfo();
        reader().loadFromPDF(input.toFile(), ownerPassword, metadata);
        return metadata;
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
}
