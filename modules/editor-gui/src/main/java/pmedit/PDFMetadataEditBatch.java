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
                    boolean allSupported = writer.allFieldsSupported(mdFile);
                    writer.saveAsPDF(mdFile, file, output);
                    success(listener, file, output, "updated",
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
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File jsonFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromJSONFileAsList(jsonFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return md;
                }).toList();
                imports.add(new ImportFileParsed(jsonFile, actionList));
            } catch (Exception e) {
                logger.error("fromjson", e);
                failure(listener, jsonFile, jsonFile.getName(),
                        new BatchException("parse_failed", "Failed to parse: " + e, e), "parse_failed");
            }
        }
        fromImportFile(imports, outDir, listener, "fromjson");
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
                    listener.onFile(BatchFileOutcome.success(
                            firstRecord.file, outFile, "exported", List.of(), List.of(),
                            inputFileRelativeName(outFile),
                            "Wrote: " + outputFileRelativeName(outFile)));
                } catch (Exception e) {
                    logger.error("tojson", e);
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
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File yamlFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromYAMLFileAsList(yamlFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return md;
                }).toList();
                imports.add(new ImportFileParsed(yamlFile, actionList));
            } catch (Exception e) {
                logger.error("fromyaml", e);
                failure(listener, yamlFile, yamlFile.getName(),
                        new BatchException("parse_failed", "Failed to parse YAML: " + e, e), "parse_failed");
            }
        }
        fromImportFile(imports, outDir, listener, "fromyaml");
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
                    listener.onFile(BatchFileOutcome.success(
                            firstRecord.file, outFile, "exported", List.of(), List.of(),
                            inputFileRelativeName(outFile),
                            "Wrote: " + outputFileRelativeName(outFile)));
                } catch (Exception e) {
                    logger.error("toyaml", e);
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
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File csvFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = CsvMetadata.readFile(csvFile);
                imports.add(new ImportFileParsed(csvFile, actionList));
            } catch (Exception e) {
                logger.error("fromcsv", e);
                failure(listener, csvFile, csvFile.getName(),
                        new BatchException("parse_failed", "Failed to parse CSV: " + e, e), "parse_failed");
            }
        }
        fromImportFile(imports, outDir, listener, "fromcsv");
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
                    listener.onFile(BatchFileOutcome.success(
                            firstRecord.file, outFile, "exported", List.of(), List.of(),
                            inputFileRelativeName(outFile),
                            "Wrote: " + outputFileRelativeName(outFile)));
                } catch (Exception e) {
                    logger.error("tocsv", e);
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

    public void fromImportFile(
            List<ImportFileParsed> importFiles,
            File outDir,
            final BatchListener listener,
            String commandName) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        for (ImportFileParsed importFile : importFiles) {
            for (MetadataInfo mdParams : importFile.data) {
                FilePathProperty fileProperty = new FilePathProperty(mdParams.file.fullPath, outDir, importFile.importFile);
                File inputFile = null;
                try {
                    inputFile = options.pathPolicy().validateInput(fileProperty.getInputFile());
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(inputFile, options.ownerPassword(), mdFile);
                    mdFile.copyFromWithExpand(mdParams, mdFile);
                    File output = options.pathPolicy().validateOutput(fileProperty.getOutputFile(false));
                    writer.saveAsPDF(mdFile, inputFile, output);
                    success(listener, inputFile, output, "imported",
                            enabledFields(mdParams), List.of(),
                            inputFile.getPath(), "Done");
                } catch (Exception e) {
                    logger.error(commandName, e);
                    File labelFile = inputFile != null ? inputFile : fileProperty.getInputFile();
                    failure(listener, labelFile, labelFile.getPath(), e, "write_failed");
                }
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

    public record ImportFileParsed(File importFile, List<MetadataInfo> data) {
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
