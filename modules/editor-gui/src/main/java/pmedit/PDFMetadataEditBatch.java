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

    public PDFMetadataEditBatch() {
        this(null);
    }

    public PDFMetadataEditBatch(BatchOperationParameters params) {
        this.params = params;
    }

    public void edit(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        if (params == null) {
            status.addError("*", new Exception("No metadata defined"));
            return;
        }
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();
        filesWalker.forFiles(new FileAction(outDir, status) {

            @Override
            public void apply(File file) {
                MetadataInfo mdParams = params != null ? params.metadata : new MetadataInfo();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, mdFile);
                    mdFile.copyFromWithExpand(mdParams, mdFile);
                    writer.saveAsPDF(mdFile, file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("edit", e);
                    status.addError(inputFileRelativeName(file), e);
                }
            }
        });
    }

    public void clear(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();

        filesWalker.forFiles(new FileAction(outDir,status) {

            @Override
            public void apply(File file) {
                MetadataInfo md =  new MetadataInfo();
                if(params != null){
                    md.copyEnabled(params.metadata);
                }
                try {
                    writer.saveAsPDF(md, file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Cleared");
                } catch (Exception e) {
                    logger.error("clear", e);

                    status.addError(inputFileRelativeName(file), e);
                }
            }
        });
    }

    public void rename(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        String template = null;
        if (params != null) {
            template = params.renameTemplate;
            if (!template.toLowerCase().endsWith(".pdf"))
                template += ".pdf";
        }
        if (template == null) {
            status.addError("*", new Exception("Rename template not configured"));
            return;
        }
        final TemplateString ts = new TemplateString(template);
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir,status) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, md);
                    String toName = ts.process(md);
                    File outFile = getOutputFile(file);
                    File to ;
                    boolean copy;
                    if(outFile != null) {
                        to = new File(outFile.getParentFile(), toName);
                        copy = true;
                    } else {
                        to = new File(file.getParentFile(), toName);
                        copy = false;
                    }
                    if (to.exists()) {
                        status.addError(outputFileRelativeName(file), new Exception("Destination file already exists:  " + to.getName()));
                    } else {
                        try {
                            if(copy){
                                Files.copy(file.toPath(), to.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                            } else {
                                Files.move(file.toPath(), to.toPath());
                            }
                            status.addStatus(outputFileRelativeName(file), to.getName());
                        } catch (IOException e) {
                            status.addErrorWithCause(outputFileRelativeName(file), "Rename failed",e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("rename", e);

                    status.addError(inputFileRelativeName(file), e);
                }
            }

        });
    }

    public void fromFilename(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        if (params == null || params.extractTemplate == null || params.extractTemplate.isEmpty()) {
            status.addError("*", new Exception("Extract template not configured"));
            return;
        }
        TemplateString extractor = new TemplateString(params.extractTemplate);
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir, status) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, mdFile);
                    MetadataInfo mdExtracted = extractor.extract(mdFile.file.name);
                    mdFile.copyOnlyEnabled(mdExtracted);
                    writer.saveAsPDF(mdFile, file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("fromFileName", e);
                    status.addError(inputFileRelativeName(file),  e);
                }
            }

        });
    }

    public void fromjson(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File jsonFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromJSONFileAsList(jsonFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return  md;
                }).toList();
                imports.add(new ImportFileParsed(jsonFile, actionList));
            } catch (Exception e) {
                status.addErrorWithCause(jsonFile.getName(), "Failed to parse: " + e, e);
            }
        }
        fromImportFile(imports, outDir, status, "fromjson");
    }

    protected String makeExportFilename(File f, String extension){
        String outFile = f.getAbsolutePath().replaceFirst("\\.[Pp][Dd][Ff]$", extension);
        if (!outFile.endsWith(extension)) {
            outFile +=  extension;
        }
        return  outFile;
    }

    public void tojson(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        PdfReader reader = PmeExtension.get().newPdfReader();
        filesWalker.forFiles(new ExportFileAction(outDir, params.isSingleFileExport(), status) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                ExportedObject firstRecord = records.get(0);
                if(singleFile){
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(new File(params.outputFile), firstRecord.file.getParentFile()), ".json"));
                    SerDeslUtils.toJSONFile(outFile, records.stream().map(e -> e.data).toList());
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                } else {
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(firstRecord.file), ".json"));
                    SerDeslUtils.toJSONFile(outFile, firstRecord.data);
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                }
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, md);
                    if(params.shouldUseRelativePaths()){
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, md.asFlatMap()));
                } catch (Exception e) {
                    logger.error("tojson", e);
                    status.addError(inputFileRelativeName(file), e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), new Exception("Invalid file: " + file.getAbsolutePath()) );
            }
        });
    }

    public void fromyaml(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File yamlFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromYAMLFileAsList(yamlFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return  md;
                }).toList();
                imports.add(new ImportFileParsed(yamlFile, actionList));
            } catch (Exception e) {
                status.addErrorWithCause(yamlFile.getName(), "Failed to parse YAML: " + e, e);
            }
        }
        fromImportFile(imports, outDir, status, "fromyaml");
    }

    public void toyaml(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new ExportFileAction(outDir, params.isSingleFileExport(), status) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                ExportedObject firstRecord = records.get(0);
                if(singleFile){
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(new File(params.outputFile), firstRecord.file.getParentFile()), ".yaml"));
                    SerDeslUtils.toYamlFile(outFile, records.stream().map(e -> e.data).toList());
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                } else {
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(firstRecord.file), ".yaml"));
                    SerDeslUtils.toYamlFile(outFile, firstRecord.data);
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                }
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, md);
                    if(params.shouldUseRelativePaths()){
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, md.asFlatMap()));
                } catch (Exception e) {
                    logger.error("toyaml", e);
                    status.addError(inputFileRelativeName(file), e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), new Exception("Invalid file: " + file.getAbsolutePath()) );
            }
        });
    }

    public void fromcsv(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File csvFile : filesWalker.list()) {
            try {
                List<MetadataInfo> actionList = CsvMetadata.readFile(csvFile);
                imports.add(new ImportFileParsed(csvFile, actionList));
            } catch (Exception e) {
                status.addErrorWithCause(csvFile.getName(), "Failed to parse CSV: " + e,e);
            }
        }
        fromImportFile(imports, outDir, status, "fromcsv");
    }

    public void tocsv(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new ExportFileAction(outDir, params.isSingleFileExport(), status) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                ExportedObject firstRecord = records.get(0);
                File outFile;
                if(singleFile){
                   outFile = new File(makeExportFilename(getOutputFileNonNull(new File(params.outputFile), firstRecord.file.getParentFile()), ".csv"));
                } else {
                    outFile = new File(makeExportFilename(getOutputFileNonNull(firstRecord.file), ".csv"));
                }
                CsvMetadata.writeFile(outFile, records.stream().map(e -> (Map)e.data() ).toList());
                status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    reader.loadFromPDF(file, md);
                    if(params.shouldUseRelativePaths()){
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, (Map<String, Object>) (Object) md.asFlatStringMap()));
                } catch (Exception e) {
                    logger.error("tocsv", e);
                    status.addError(inputFileRelativeName(file), e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), new Exception("Invalid file: " + file.getAbsolutePath()));
            }
        });
    }

    public void fromImportFile(List<ImportFileParsed> importFiles, File outDir, final ActionStatus status, String commandName) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        for (ImportFileParsed importFile : importFiles) {
            for (MetadataInfo mdParams : importFile.data) {
                FilePathProperty fileProperty = new FilePathProperty(mdParams.file.fullPath, outDir, importFile.importFile);
                File inputFile = fileProperty.getInputFile();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(inputFile, mdFile);
                    mdFile.copyFromWithExpand(mdParams, mdFile);
                    writer.saveAsPDF(mdFile, inputFile, fileProperty.getOutputFile(true));
                    status.addStatus(inputFile.getPath(), "Done");
                } catch (Exception e) {
                    logger.error(commandName, e);
                    status.addError(inputFile.getPath(), e);
                }
            }

        }
    }

    public void xmptodoc(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir, status) {

            @Override
            public void apply(File file) {
                MetadataInfo mdParams = params != null ? params.metadata : new MetadataInfo();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, mdFile);
                    mdFile.copyXMPToDoc();
                    writer.saveAsPDF(mdFile, file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("xmptodoc", e);
                    status.addError(inputFileRelativeName(file), e);
                }
            }
        });
    }

    public void doctoxmp(FilesWalker filesWalker, File outDir, final ActionStatus status) {
        PdfWriter writer = PmeExtension.get().newPdfWriter();
        PdfReader reader = PmeExtension.get().newPdfReader();

        filesWalker.forFiles(new FileAction(outDir,status) {

            @Override
            public void apply(File file) {
                MetadataInfo mdParams = params != null ? params.metadata : new MetadataInfo();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    reader.loadFromPDF(file, mdFile);
                    mdFile.copyDocToXMP();
                    writer.saveAsPDF(mdFile, file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("doctoxmp", e);
                    status.addError(inputFileRelativeName(file), e);
                }
            }
        });
    }

    public void runCommand(CommandDescription command, List<File> batchFileList, File outDir, ActionStatus actionStatus) {
        FilesWalker filesWalker = new FilesWalker(command.inputFileExtensions, batchFileList, outDir);
        if (!PmeExtension.get().hasBatch()) {
            actionStatus.addError("*", new Exception("Invalid license, you can get a license at " + Constants.batchLicenseUrl));
        } else if (command.is("rename")) {
            rename(filesWalker, outDir,  actionStatus);
        } else if (command.is("fromfilename")) {
            fromFilename(filesWalker, outDir,  actionStatus);
        } else if (command.is("edit")) {
            edit(filesWalker, outDir,  actionStatus);
        } else if (command.is("clear")) {
            clear(filesWalker, outDir,  actionStatus);
        } else if (command.is("fromjson")) {
            fromjson(filesWalker, outDir,  actionStatus);
        } else if (command.is("tojson")) {
            tojson(filesWalker, outDir,  actionStatus);
        } else if (command.is("fromyaml")) {
            fromyaml(filesWalker, outDir,  actionStatus);
        } else if (command.is("toyaml")) {
            toyaml(filesWalker, outDir,  actionStatus);
        } else if (command.is("fromcsv")) {
            fromcsv(filesWalker, outDir,  actionStatus);
        } else if (command.is("tocsv")) {
            tocsv(filesWalker, outDir,  actionStatus);
        } else if (command.is("xmptodoc")) {
            xmptodoc(filesWalker, outDir,  actionStatus);
        } else if (command.is("doctoxmp")) {
            doctoxmp(filesWalker, outDir,  actionStatus);
        } else {
            actionStatus.addError("*", new Exception("Invalid command"));
        }
    }

    public record ImportFileParsed(File importFile, List<MetadataInfo> data) {
    }

    public static class FilePathProperty {
        File file;
        File outDir;
        File propertyInputFile;
        FilePathProperty(String file, File outDir, File propertyInputFile){
            this.file = new File(file);
            this.outDir = outDir;
            this.propertyInputFile = propertyInputFile;
        }

        File getInputFile(){
            if(!propertyInputFile.isAbsolute()){
                new File(propertyInputFile.getParentFile(), file.getPath());
            }
            return file;
        }

        File getOutputFile(boolean mkDirs){
            File outputFile;
            if(file.isAbsolute()) {
                if(outDir != null){
                    Path csvFileDir = propertyInputFile.getParentFile().toPath().toAbsolutePath();
                    Path targetPath = file.toPath().toAbsolutePath();
                    if(targetPath.startsWith(csvFileDir)) {
                        // input file is in the (sub) dir of the csvFile, keep the structure relative to the CSV file
                        outputFile = new File(outDir,  csvFileDir.relativize(targetPath).toString());
                    } else {
                        // input file is outside (sub) dir of the csvFile, flatten structure
                        outputFile = new File(outDir,  file.getName());
                    }
                } else {
                    outputFile = file;
                }
            } else {
                if(outDir == null){
                    outputFile = new File(propertyInputFile.getParentFile(), file.getPath());
                } else {
                    outputFile = new File(outDir, file.getPath());
                }
            }

            if(mkDirs) {
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
            }

            return outputFile;
        }

    }

    public abstract static class  ExportFileAction extends FileAction {
        public record ExportedObject(File file, Map<String, Object> data){}

        boolean singleFile;
        List<ExportedObject> collection = new ArrayList<>();

        ExportFileAction(File outDir, boolean singleFile, ActionStatus status) {
            super(outDir, status);
            this.singleFile = singleFile;
        }


        public void afterEach(){
            if(!singleFile){
                if(!collection.isEmpty()) {
                    exportRecords(collection);
                    collection.clear();
                }
            }
        }

        public void afterAll(){
            if(singleFile){
                if(!collection.isEmpty()) {
                    exportRecords(collection);
                }
            }
        }

        void addRecordForExport(ExportedObject record){
            collection.add(record);
        }

        abstract void exportRecords(List<ExportedObject> record);
    }
}

