package pmedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.ext.PdfReader;
import pmedit.ext.PdfWriter;
import pmedit.ext.PmeExtension;
import pmedit.serdes.CsvMetadata;
import pmedit.serdes.SerDeslUtils;
import pmedit.util.FileAction;
import pmedit.util.FilesWalker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PDFMetadataEditBatch {
    public static final String BATCH_OUTPUT_LOG = "batch-log.txt";
    Logger logger = LoggerFactory.getLogger(PDFMetadataEditBatch.class);

    BatchOperationParameters params;
    final BatchOptions options;

    public PDFMetadataEditBatch() {
        this(null);
    }

    public PDFMetadataEditBatch(BatchOperationParameters params) {
        this(params, BatchOptions.cli());
    }

    public PDFMetadataEditBatch(BatchOperationParameters params, BatchOptions options) {
        this.params = params;
        this.options = options == null ? BatchOptions.cli() : options;
    }

    public void edit(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        if (params == null) {
            batchError(listener, "invalid_argument", "No metadata defined", null);
            return;
        }
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();
        filesWalker.forFiles(new FileAction(outDir, listener, options.reportIgnoredFiles()) {

            @Override
            public void apply(File file) {
                MetadataInfo mdParams = params.metadata;
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), mdFile);
                    mdFile.copyFromWithExpand(mdParams, mdFile);
                    File output = getOutputFile(file);
                    if (output != null) {
                        output = options.pathPolicy().validateOutput(output);
                    }
                    boolean allSupported = writer.allFieldsSupported(mdParams);
                    writer.saveAsPDF(mdFile, file, output);
                    success(listener, file, output, "updated",
                            enabledFields(mdParams),
                            allSupported ? List.of() : List.of(BatchWarnings.unsupportedFields()),
                            outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("edit", e);
                    failure(listener, file, inputFileRelativeName(file), e, "write_failed");
                }
            }
        });
    }

    public void clear(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();

        filesWalker.forFiles(new FileAction(outDir, listener, options.reportIgnoredFiles()) {

            @Override
            public void apply(File file) {
                MetadataInfo md = new MetadataInfo();
                if (params != null) {
                    md.copyEnabled(params.metadata);
                }
                if (options.ownerPassword() != null && !options.ownerPassword().isEmpty()) {
                    md.prop.ownerPassword = options.ownerPassword();
                }
                try {
                    File output = getOutputFile(file);
                    if (output != null) {
                        output = options.pathPolicy().validateOutput(output);
                    }
                    boolean allSupported = writer.allFieldsSupported(md);
                    writer.saveAsPDF(md, file, output);
                    success(listener, file, output, "cleared",
                            enabledFields(params != null ? params.metadata : null),
                            allSupported ? List.of() : List.of(BatchWarnings.unsupportedFields()),
                            outputFileRelativeName(file), "Cleared");
                } catch (Exception e) {
                    logger.error("clear", e);
                    failure(listener, file, inputFileRelativeName(file), e, "write_failed");
                }
            }
        });
    }

    public void rename(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        String template = null;
        if (params != null) {
            template = params.renameTemplate;
            if (template != null && !template.toLowerCase().endsWith(".pdf")) {
                template += ".pdf";
            }
        }
        if (template == null) {
            batchError(listener, "invalid_argument", "Rename template not configured", null);
            return;
        }
        final TemplateString ts = new TemplateString(template);
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir, listener, options.reportIgnoredFiles()) {

            @Override
            public void apply(File file) {
                File outFile = getOutputFile(file);
                File to;
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), md);
                    String toName;
                    try {
                        toName = ts.process(md);
                    } catch (RuntimeException e) {
                        failure(listener, file, inputFileRelativeName(file),
                                new BatchException(
                                        "invalid_template",
                                        "Failed to evaluate rename template: " + e.getMessage(),
                                        e),
                                "invalid_template");
                        return;
                    }
                    if (toName.isBlank()) {
                        failure(listener, file, inputFileRelativeName(file),
                                new BatchException(
                                        "invalid_template",
                                        "Rename template produced an empty file name"),
                                "invalid_template");
                        return;
                    }
                    to = outFile != null
                            ? new File(outFile.getParentFile(), toName)
                            : new File(file.getParentFile(), toName);
                } catch (Exception e) {
                    logger.error("rename", e);
                    failure(listener, file, inputFileRelativeName(file), e, "write_failed");
                    return;
                }

                try {
                    to = options.pathPolicy().validateOutput(to);
                    if (to.exists()) {
                        throw new BatchException(
                                "destination_exists",
                                "Destination file already exists:  " + to.getName());
                    }
                } catch (Exception e) {
                    logger.error("rename", e);
                    failure(listener, file, outputFileRelativeName(file), e, "write_failed");
                    return;
                }

                try {
                    if (outFile != null) {
                        Files.copy(file.toPath(), to.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                    } else {
                        Files.move(file.toPath(), to.toPath());
                    }
                    success(listener, file, to, "renamed", List.of(), List.of(),
                            outputFileRelativeName(file), to.getName());
                } catch (IOException e) {
                    logger.error("rename", e);
                    failure(listener, file, outputFileRelativeName(file),
                            new BatchException("write_failed", "Rename failed", e), "write_failed");
                }
            }
        });
    }

    public void fromFilename(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        if (params == null || params.extractTemplate == null || params.extractTemplate.isEmpty()) {
            batchError(listener, "invalid_argument", "Extract template not configured", null);
            return;
        }
        TemplateString extractor = new TemplateString(params.extractTemplate);
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir, listener, options.reportIgnoredFiles()) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), mdFile);
                    MetadataInfo mdExtracted = extractor.extract(mdFile.file.name);
                    mdFile.copyOnlyEnabled(mdExtracted);
                    File output = getOutputFile(file);
                    if (output != null) {
                        output = options.pathPolicy().validateOutput(output);
                    }
                    boolean allSupported = writer.allFieldsSupported(mdExtracted);
                    writer.saveAsPDF(mdFile, file, output);
                    success(listener, file, output, "imported",
                            enabledFields(mdExtracted),
                            allSupported ? List.of() : List.of(BatchWarnings.unsupportedFields()),
                            outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("fromFileName", e);
                    failure(listener, file, inputFileRelativeName(file), e, "write_failed");
                }
            }
        });
    }

    public void fromjson(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        List<ImportRecord> records = new ArrayList<>();
        List<ImportParseFailure> failures = new ArrayList<>();
        for (File jsonFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromJSONFileAsList(jsonFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return md;
                }).toList();
                for (MetadataInfo md : actionList) {
                    records.add(new ImportRecord(jsonFile, md));
                }
            } catch (Exception e) {
                logger.error("fromjson", e);
                failures.add(parseFailure(jsonFile, "Failed to parse: " + e, e, "fromjson"));
            }
        }
        applyImportPlan(new ImportPlan(records, failures), outDir, listener);
    }

    protected String makeExportFilename(File f, String extension) {
        String outFile = f.getAbsolutePath().replaceFirst("\\.[Pp][Dd][Ff]$", extension);
        if (!outFile.endsWith(extension)) {
            outFile += extension;
        }
        return outFile;
    }

    public void tojson(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        PdfReader reader = PmeExtension.get().newPdfReader();
        filesWalker.forFiles(new ExportFileAction(outDir, params.isSingleFileExport(), listener, options.reportIgnoredFiles()) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                ExportedObject firstRecord = records.get(0);
                File outFile;
                if (singleFile) {
                    outFile = new File(makeExportFilename(
                            getOutputFileNonNull(new File(params.outputFile), firstRecord.file.getParentFile()), ".json"));
                } else {
                    outFile = new File(makeExportFilename(getOutputFileNonNull(firstRecord.file), ".json"));
                }
                try {
                    outFile = options.pathPolicy().validateOutput(outFile);
                    if (singleFile) {
                        SerDeslUtils.toJSONFile(outFile, records.stream().map(e -> e.data).toList());
                    } else {
                        SerDeslUtils.toJSONFile(outFile, firstRecord.data);
                    }
                    for (int index = 0; index < records.size(); index++) {
                        ExportedObject exported = records.get(index);
                        listener.onFile(BatchFileOutcome.success(
                                exported.file,
                                outFile,
                                "exported",
                                List.of(),
                                List.of(),
                                index == 0 ? inputFileRelativeName(outFile) : null,
                                index == 0 ? "Wrote: " + outputFileRelativeName(outFile) : null));
                    }
                } catch (Exception e) {
                    logger.error("tojson", e);
                    if (singleFile) {
                        BatchFailure classified = BatchFailures.classify(e, options.ownerPassword(), "write_failed");
                        throw new BatchAbortException(new BatchError(
                                "*", classified.code(), classified.message(), e));
                    }
                    failure(listener, firstRecord.file, inputFileRelativeName(outFile), e, "write_failed");
                }
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), md);
                    md.copyEnabled(params.metadata);
                    md.fileEnabled.fullPath = true;
                    if (params.shouldUseRelativePaths()) {
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, md.asFlatMap(true)));
                } catch (Exception e) {
                    logger.error("tojson", e);
                    failure(listener, file, inputFileRelativeName(file), e, "read_failed");
                }
            }

            @Override
            public void ignore(File file) {
                ignoreInvalidFile(file, listener);
            }
        });
    }

    public void fromyaml(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        List<ImportRecord> records = new ArrayList<>();
        List<ImportParseFailure> failures = new ArrayList<>();
        for (File yamlFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromYAMLFileAsList(yamlFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return md;
                }).toList();
                for (MetadataInfo md : actionList) {
                    records.add(new ImportRecord(yamlFile, md));
                }
            } catch (Exception e) {
                logger.error("fromyaml", e);
                failures.add(parseFailure(yamlFile, "Failed to parse YAML: " + e, e, "fromyaml"));
            }
        }
        applyImportPlan(new ImportPlan(records, failures), outDir, listener);
    }

    public void toyaml(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new ExportFileAction(outDir, params.isSingleFileExport(), listener, options.reportIgnoredFiles()) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                ExportedObject firstRecord = records.get(0);
                File outFile;
                if (singleFile) {
                    outFile = new File(makeExportFilename(
                            getOutputFileNonNull(new File(params.outputFile), firstRecord.file.getParentFile()), ".yaml"));
                } else {
                    outFile = new File(makeExportFilename(getOutputFileNonNull(firstRecord.file), ".yaml"));
                }
                try {
                    outFile = options.pathPolicy().validateOutput(outFile);
                    if (singleFile) {
                        SerDeslUtils.toYamlFile(outFile, records.stream().map(e -> e.data).toList());
                    } else {
                        SerDeslUtils.toYamlFile(outFile, firstRecord.data);
                    }
                    for (int index = 0; index < records.size(); index++) {
                        ExportedObject exported = records.get(index);
                        listener.onFile(BatchFileOutcome.success(
                                exported.file,
                                outFile,
                                "exported",
                                List.of(),
                                List.of(),
                                index == 0 ? inputFileRelativeName(outFile) : null,
                                index == 0 ? "Wrote: " + outputFileRelativeName(outFile) : null));
                    }
                } catch (Exception e) {
                    logger.error("toyaml", e);
                    if (singleFile) {
                        BatchFailure classified = BatchFailures.classify(e, options.ownerPassword(), "write_failed");
                        throw new BatchAbortException(new BatchError(
                                "*", classified.code(), classified.message(), e));
                    }
                    failure(listener, firstRecord.file, inputFileRelativeName(outFile), e, "write_failed");
                }
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), md);
                    md.copyEnabled(params.metadata);
                    md.fileEnabled.fullPath = true;
                    if (params.shouldUseRelativePaths()) {
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, md.asFlatMap(true)));
                } catch (Exception e) {
                    logger.error("toyaml", e);
                    failure(listener, file, inputFileRelativeName(file), e, "read_failed");
                }
            }

            @Override
            public void ignore(File file) {
                ignoreInvalidFile(file, listener);
            }
        });
    }

    public void fromcsv(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        List<ImportRecord> records = new ArrayList<>();
        List<ImportParseFailure> failures = new ArrayList<>();
        for (File csvFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = CsvMetadata.readFile(csvFile);
                for (MetadataInfo md : actionList) {
                    records.add(new ImportRecord(csvFile, md));
                }
            } catch (Exception e) {
                logger.error("fromcsv", e);
                failures.add(parseFailure(csvFile, "Failed to parse CSV: " + e, e, "fromcsv"));
            }
        }
        applyImportPlan(new ImportPlan(records, failures), outDir, listener);
    }

    public void tocsv(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new ExportFileAction(outDir, params.isSingleFileExport(), listener, options.reportIgnoredFiles()) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                ExportedObject firstRecord = records.get(0);
                File outFile;
                if (singleFile) {
                    outFile = new File(makeExportFilename(
                            getOutputFileNonNull(new File(params.outputFile), firstRecord.file.getParentFile()), ".csv"));
                } else {
                    outFile = new File(makeExportFilename(getOutputFileNonNull(firstRecord.file), ".csv"));
                }
                try {
                    outFile = options.pathPolicy().validateOutput(outFile);
                    CsvMetadata.writeFile(outFile, records.stream().map(e -> (Map) e.data()).toList());
                    for (int index = 0; index < records.size(); index++) {
                        ExportedObject exported = records.get(index);
                        listener.onFile(BatchFileOutcome.success(
                                exported.file,
                                outFile,
                                "exported",
                                List.of(),
                                List.of(),
                                index == 0 ? inputFileRelativeName(outFile) : null,
                                index == 0 ? "Wrote: " + outputFileRelativeName(outFile) : null));
                    }
                } catch (Exception e) {
                    logger.error("tocsv", e);
                    if (singleFile) {
                        BatchFailure classified = BatchFailures.classify(e, options.ownerPassword(), "write_failed");
                        throw new BatchAbortException(new BatchError(
                                "*", classified.code(), classified.message(), e));
                    }
                    failure(listener, firstRecord.file, inputFileRelativeName(outFile), e, "write_failed");
                }
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), md);
                    md.copyEnabled(params.metadata);
                    md.fileEnabled.fullPath = true;
                    if (params.shouldUseRelativePaths()) {
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, (Map<String, Object>) (Object) md.asFlatStringMap(true)));
                } catch (Exception e) {
                    logger.error("tocsv", e);
                    failure(listener, file, inputFileRelativeName(file), e, "read_failed");
                }
            }

            @Override
            public void ignore(File file) {
                ignoreInvalidFile(file, listener);
            }
        });
    }

    private ImportParseFailure parseFailure(File source, String message, Exception cause, String commandName) {
        logger.error(commandName, cause);
        BatchFailure failure = BatchFailures.classify(
                new BatchException("parse_failed", message, cause),
                options.ownerPassword(),
                "parse_failed");
        return new ImportParseFailure(source, message, failure);
    }

    private void applyImportPlan(ImportPlan plan, File outDir, BatchListener listener) {
        if (plan.records().isEmpty() && plan.parseFailures().isEmpty()) {
            throw new BatchAbortException(new BatchError(
                    "*", "no_input_files", "No import records were found", null));
        }
        if (options.maxFiles() > 0 && plan.records().size() > options.maxFiles()) {
            throw new BatchAbortException(new BatchError(
                    "*",
                    "batch_too_large",
                    "The batch contains " + plan.records().size()
                            + " imported PDFs; the maximum is " + options.maxFiles()
                            + ". Split the request into smaller batches.",
                    null));
        }

        for (ImportParseFailure parseFailure : plan.parseFailures()) {
            listener.onFile(BatchFileOutcome.importFailed(
                    parseFailure.source(),
                    null,
                    parseFailure.source().getName(),
                    parseFailure.displayMessage(),
                    parseFailure.failure()));
        }

        File preparedOutDir;
        try {
            preparedOutDir = plan.records().isEmpty()
                    ? outDir
                    : options.pathPolicy().prepareOutputDirectory(outDir);
        } catch (BatchException e) {
            throw new BatchAbortException(new BatchError("*", e.code(), e.getMessage(), e));
        }

        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        for (ImportRecord record : plan.records()) {
            MetadataInfo mdParams = record.metadata();
            if (mdParams.file.fullPath == null || mdParams.file.fullPath.isBlank()) {
                String message = "Import record is missing file.fullPath";
                listener.onFile(BatchFileOutcome.importFailed(
                        record.source(),
                        null,
                        record.source().getName(),
                        message,
                        new BatchFailure("invalid_argument", message, null)));
                continue;
            }

            FilePathProperty fileProperty = new FilePathProperty(
                    mdParams.file.fullPath, preparedOutDir, record.source());

            File inputFile;
            try {
                inputFile = options.pathPolicy().validateInput(fileProperty.getInputFile());
            } catch (Exception e) {
                BatchFailure failure = BatchFailures.classify(e, options.ownerPassword(), "write_failed");
                File labelFile = fileProperty.getInputFile();
                listener.onFile(BatchFileOutcome.importFailed(
                        record.source(),
                        null,
                        labelFile.getPath(),
                        BatchFailures.message(e),
                        failure));
                continue;
            }

            File output;
            try {
                output = options.pathPolicy().validateOutput(fileProperty.getOutputFile(false));
            } catch (Exception e) {
                BatchFailure failure = BatchFailures.classify(e, options.ownerPassword(), "write_failed");
                listener.onFile(BatchFileOutcome.importFailed(
                        record.source(),
                        inputFile,
                        inputFile.getPath(),
                        BatchFailures.message(e),
                        failure));
                continue;
            }

            String password = mdParams.prop.ownerPassword != null
                    && !mdParams.prop.ownerPassword.isEmpty()
                    ? mdParams.prop.ownerPassword
                    : options.ownerPassword();

            try {
                MetadataInfo mdFile = new MetadataInfo();
                reader.loadFromPDF(inputFile, password, mdFile);
                mdFile.copyFromWithExpand(mdParams, mdFile);
                if (password != null) {
                    mdFile.prop.ownerPassword = password;
                }

                boolean allSupported = writer.allFieldsSupported(mdParams);
                writer.saveAsPDF(mdFile, inputFile, output);
                listener.onFile(BatchFileOutcome.imported(
                        record.source(),
                        inputFile,
                        output,
                        "imported",
                        enabledFields(mdParams),
                        allSupported ? List.of() : List.of(BatchWarnings.unsupportedFields()),
                        inputFile.getPath(),
                        "Done"));
            } catch (Exception e) {
                logger.error("import", e);
                BatchFailure failure = BatchFailures.classify(e, password, "write_failed");
                listener.onFile(BatchFileOutcome.importFailed(
                        record.source(),
                        inputFile,
                        inputFile.getPath(),
                        BatchFailures.message(e),
                        failure));
            }
        }
    }

    public void xmptodoc(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir, listener, options.reportIgnoredFiles()) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), mdFile);
                    mdFile.copyXMPToDoc();
                    File output = getOutputFile(file);
                    if (output != null) {
                        output = options.pathPolicy().validateOutput(output);
                    }
                    boolean allSupported = writer.allFieldsSupported(mdFile);
                    writer.saveAsPDF(mdFile, file, output);
                    success(listener, file, output, "xmp_to_doc",
                            List.of(), allSupported ? List.of() : List.of(BatchWarnings.unsupportedFields()),
                            outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("xmptodoc", e);
                    failure(listener, file, inputFileRelativeName(file), e, "write_failed");
                }
            }
        });
    }

    public void doctoxmp(FilesWalker filesWalker, File outDir, final BatchListener listener) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir, listener, options.reportIgnoredFiles()) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, options.ownerPassword(), mdFile);
                    mdFile.copyDocToXMP();
                    File output = getOutputFile(file);
                    if (output != null) {
                        output = options.pathPolicy().validateOutput(output);
                    }
                    boolean allSupported = writer.allFieldsSupported(mdFile);
                    writer.saveAsPDF(mdFile, file, output);
                    success(listener, file, output, "doc_to_xmp",
                            List.of(), allSupported ? List.of() : List.of(BatchWarnings.unsupportedFields()),
                            outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("doctoxmp", e);
                    failure(listener, file, inputFileRelativeName(file), e, "write_failed");
                }
            }
        });
    }

    public void runCommand(CommandDescription command, List<File> batchFileList, File outDir, BatchListener listener) {
        new BatchEngine().run(command, params, new BatchRequest(batchFileList, outDir, options), listener);
    }

    public void runCommand(CommandDescription command, FilesWalker filesWalker, File outDir, BatchListener listener) {
        if (command == null) {
            batchError(listener, "invalid_command", "Invalid command", null);
        } else if (command.is("rename")) {
            rename(filesWalker, outDir, listener);
        } else if (command.is("fromfilename")) {
            fromFilename(filesWalker, outDir, listener);
        } else if (command.is("edit")) {
            edit(filesWalker, outDir, listener);
        } else if (command.is("clear")) {
            clear(filesWalker, outDir, listener);
        } else if (command.is("fromjson")) {
            fromjson(filesWalker, outDir, listener);
        } else if (command.is("tojson")) {
            tojson(filesWalker, outDir, listener);
        } else if (command.is("fromyaml")) {
            fromyaml(filesWalker, outDir, listener);
        } else if (command.is("toyaml")) {
            toyaml(filesWalker, outDir, listener);
        } else if (command.is("fromcsv")) {
            fromcsv(filesWalker, outDir, listener);
        } else if (command.is("tocsv")) {
            tocsv(filesWalker, outDir, listener);
        } else if (command.is("xmptodoc")) {
            xmptodoc(filesWalker, outDir, listener);
        } else if (command.is("doctoxmp")) {
            doctoxmp(filesWalker, outDir, listener);
        } else {
            batchError(listener, "invalid_command", "Invalid command", null);
        }
    }

    private void ignoreInvalidFile(File file, BatchListener listener) {
        if (!options.reportIgnoredFiles()) {
            return;
        }
        String message = "Invalid file: " + file.getAbsolutePath();
        listener.onFile(BatchFileOutcome.error(
                file,
                file.getName(),
                message,
                new BatchFailure("not_a_pdf", message, null)));
    }

    private static void success(
            BatchListener listener,
            File input,
            File output,
            String action,
            List<String> fields,
            List<String> warnings,
            String displayLabel,
            String displayMessage) {
        listener.onFile(BatchFileOutcome.success(
                input,
                output != null ? output : input,
                action,
                fields,
                warnings,
                displayLabel,
                displayMessage));
    }

    private void failure(
            BatchListener listener,
            File input,
            String displayLabel,
            Throwable error,
            String defaultCode) {
        BatchFailure failure = BatchFailures.classify(error, options.ownerPassword(), defaultCode);
        listener.onFile(BatchFileOutcome.error(
                input,
                displayLabel,
                BatchFailures.message(error),
                failure));
    }

    private static void batchError(
            BatchListener listener,
            String code,
            String message,
            Throwable cause) {
        listener.onBatchError(new BatchError("*", code, message, cause));
    }

    private static List<String> enabledFields(MetadataInfo metadata) {
        List<String> fields = new ArrayList<>();
        if (metadata == null) {
            return fields;
        }
        for (String id : MetadataInfo.keys()) {
            if (metadata.isEnabled(id)) {
                fields.add(id);
            }
        }
        return fields;
    }

    public record ImportRecord(File source, MetadataInfo metadata) {
    }

    public record ImportParseFailure(File source, String displayMessage, BatchFailure failure) {
    }

    public record ImportPlan(List<ImportRecord> records, List<ImportParseFailure> parseFailures) {
    }

    public static class FilePathProperty {
        File file;
        File outDir;
        File propertyInputFile;

        FilePathProperty(String file, File outDir, File propertyInputFile) {
            this.file = new File(file);
            this.outDir = outDir;
            this.propertyInputFile = propertyInputFile;
        }

        File getInputFile() {
            if (!file.isAbsolute()) {
                return new File(propertyInputFile.getParentFile(), file.getPath());
            }
            return file;
        }

        File getOutputFile(boolean mkDirs) {
            File outputFile;
            if (file.isAbsolute()) {
                if (outDir != null) {
                    Path csvFileDir = propertyInputFile.getParentFile().toPath().toAbsolutePath();
                    Path targetPath = file.toPath().toAbsolutePath();
                    if (targetPath.startsWith(csvFileDir)) {
                        // input file is in the (sub) dir of the csvFile, keep the structure relative to the CSV file
                        outputFile = new File(outDir, csvFileDir.relativize(targetPath).toString());
                    } else {
                        // input file is outside (sub) dir of the csvFile, flatten structure
                        outputFile = new File(outDir, file.getName());
                    }
                } else {
                    outputFile = file;
                }
            } else {
                if (outDir == null) {
                    outputFile = new File(propertyInputFile.getParentFile(), file.getPath());
                } else {
                    outputFile = new File(outDir, file.getPath());
                }
            }

            if (mkDirs) {
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
            }

            return outputFile;
        }

    }

    public abstract static class ExportFileAction extends FileAction {
        public record ExportedObject(File file, Map<String, Object> data) {
        }

        boolean singleFile;
        List<ExportedObject> collection = new ArrayList<>();

        ExportFileAction(File outDir, boolean singleFile, BatchListener listener, boolean reportIgnored) {
            super(outDir, listener, reportIgnored);
            this.singleFile = singleFile;
        }


        public void afterEach() {
            if (!singleFile) {
                if (!collection.isEmpty()) {
                    exportRecords(collection);
                    collection.clear();
                }
            }
        }

        public void afterAll() {
            if (singleFile) {
                if (!collection.isEmpty()) {
                    exportRecords(collection);
                }
            }
        }

        void addRecordForExport(ExportedObject record) {
            collection.add(record);
        }

        abstract void exportRecords(List<ExportedObject> record);
    }
}
