package pmedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.serdes.SerDeslUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class PDFMetadataEditBatch {
    Logger logger = LoggerFactory.getLogger(PDFMetadataEditBatch.class);

    protected FileFilter defaultFileFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return isPdfExtension(pathname) || pathname.isDirectory();
        }
    };
    BatchOperationParameters params;

    public PDFMetadataEditBatch() {
        this(null);
    }

    public PDFMetadataEditBatch(BatchOperationParameters params) {
        this.params = params;
    }

    public static boolean isPdfExtension(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".pdf");
    }

    protected void _forFiles(File file, FileFilter filter, FileAction action) {
        if (file.isFile()) {
            if (isPdfExtension(file)) {
                action.beforeEach();
                action.apply(file);
                action.afterEach();
            } else {
                action.ignore(file);
            }
        } else if (file.isDirectory()) {
            File[]  files = file.listFiles(filter);
            action.pushOutDirFromInputFile(file);
            if(files != null) {
                for (File dirEntry : files) {
                    _forFiles(dirEntry, filter, action);
                }
            }
            action.popOutDir();
        } else {
            action.ignore(file);
        }
    }


    public void forFiles(List<File> files, FileAction action) {
        if(files == null || files.isEmpty()){
            return;
        }
        action.setCurrentInputDir(files.get(0));
        action.beforeAll();
        for (File file : files) {
            action.setCurrentInputDir(file);
            _forFiles(file, defaultFileFilter, action);
        }
        action.afterAll();
    }

    public void edit(List<File> files, File outDir, final ActionStatus status) {
        if (params == null) {
            status.addError("*", "No metadata defined");
            return;
        }
        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                MetadataInfo mdParams = params != null ? params.metadata : new MetadataInfo();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    mdFile.loadFromPDF(file);
                    mdFile.copyFromWithExpand(mdParams);
                    mdFile.saveAsPDF(file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("edit", e);
                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(inputFileRelativeName(file), "Invalid file");
            }
        });
    }

    public void clear(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                MetadataInfo md =  new MetadataInfo();
                if(params != null){
                    md.copyEnabled(params.metadata);
                }
                try {
                    md.saveAsPDF(file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Cleared");
                } catch (Exception e) {
                    logger.error("clear", e);

                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(inputFileRelativeName(file), "Invalid file");
            }
        });
    }

    public void rename(List<File> files, File outDir, final ActionStatus status) {
        String template = null;
        if (params != null) {
            template = params.renameTemplate;
            if (!template.toLowerCase().endsWith(".pdf"))
                template += ".pdf";
        }
        if (template == null) {
            status.addError("*", "Rename template not configured");
            return;
        }
        final TemplateString ts = new TemplateString(template);

        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    md.loadFromPDF(file);
                    String toName = ts.process(md);
                    String toDir = outDir() != null ? outDir().toString() : file.getParent();
                    File to = new File(toDir, toName);
                    if (to.exists()) {
                        status.addError(outputFileRelativeName(file), "Destination file already exists:  " + to.getName());
                    } else {
                        try {
                            Files.move(file.toPath(), to.toPath());
                            status.addStatus(outputFileRelativeName(file), to.getName());
                        } catch (IOException e) {
                            status.addError(outputFileRelativeName(file), "Rename failed with " + to.getName() + " : " + e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("rename", e);

                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(inputFileRelativeName(file), "Invalid file");
            }
        });
    }

    public void fromjson(List<File> jsonFiles, File outDir, final ActionStatus status) {
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File jsonFile : jsonFiles) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromJSONFileAsList(jsonFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return  md;
                }).toList();
                imports.add(new ImportFileParsed(jsonFile, actionList));
            } catch (Exception e) {
                status.addError(jsonFile.getName(), "Failed to parse: " + e);
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

    public void tojson(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new ExportFileAction(outDir, params.isSingleFileExport()) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                if(singleFile){
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(new File(params.outputFile)), ".json"));
                    SerDeslUtils.toJSONFile(outFile, records.stream().map(e -> e.data).toList());
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                } else {
                    ExportedObject record = records.get(0);
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(record.file), ".json"));
                    SerDeslUtils.toJSONFile(outFile, record.data);
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                }
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    md.loadFromPDF(file);
                    if(params.shouldUseRelativePaths()){
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, md.asFlatMap()));
                } catch (Exception e) {
                    logger.error("tojson", e);
                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file" );
            }
        });
    }

    public void fromyaml(List<File> jsonFiles, File outDir, final ActionStatus status) {
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File yamlFile : jsonFiles) {
            try {
                List<MetadataInfo> actionList = SerDeslUtils.fromYAMLFileAsList(yamlFile).stream().map(e -> {
                    MetadataInfo md = new MetadataInfo();
                    md.fromFlatMap(e);
                    return  md;
                }).toList();
                imports.add(new ImportFileParsed(yamlFile, actionList));
            } catch (Exception e) {
                status.addError(yamlFile.getName(), "Failed to parse: " + e);
            }
        }
        fromImportFile(imports, outDir, status, "fromyaml");
    }

    public void toyaml(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new ExportFileAction(outDir, params.isSingleFileExport()) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                if(singleFile){
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(new File(params.outputFile)), ".yaml"));
                    SerDeslUtils.toYamlFile(outFile, records.stream().map(e -> e.data).toList());
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                } else {
                    ExportedObject record = records.get(0);
                    File outFile = new File(makeExportFilename(getOutputFileNonNull(record.file), ".yaml"));
                    SerDeslUtils.toYamlFile(outFile, record.data);
                    status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
                }
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    md.loadFromPDF(file);
                    if(params.shouldUseRelativePaths()){
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, md.asFlatMap()));
                } catch (Exception e) {
                    logger.error("toyaml", e);
                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file" );
            }
        });
    }

    public void fromcsv(List<File> csvFiles, File outDir, final ActionStatus status) {
        List<ImportFileParsed> imports = new ArrayList<>();
        for (File csvFile : csvFiles) {
            try {
                List<MetadataInfo> actionList = CsvMetadata.readFile(csvFile);
                imports.add(new ImportFileParsed(csvFile, actionList));
            } catch (Exception e) {
                status.addError(csvFile.getName(), "Failed to parse: " + e);
            }
        }
        fromImportFile(imports, outDir, status, "fromcsv");
    }

    public void tocsv(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new ExportFileAction(outDir, params.isSingleFileExport()) {
            @Override
            void exportRecords(List<ExportedObject> records) {
                File outFile;
                if(singleFile){
                   outFile = new File(makeExportFilename(getOutputFileNonNull(new File(params.outputFile)), ".csv"));
                } else {
                    ExportedObject record = records.get(0);
                    outFile = new File(makeExportFilename(getOutputFileNonNull(record.file), ".csv"));
                }
                CsvMetadata.writeFile(outFile, records.stream().map(e -> (Map)e.data() ).toList());
                status.addStatus(inputFileRelativeName(outFile), "Wrote: " + outputFileRelativeName(outFile));
            }

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    md.loadFromPDF(file);
                    if(params.shouldUseRelativePaths()){
                        md.file.fullPath = inputFileRelativeName(file);
                    }
                    addRecordForExport(new ExportedObject(file, md.asFlatMap()));
                } catch (Exception e) {
                    logger.error("tocsv", e);
                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file" );
            }
        });
    }

    public void fromImportFile(List<ImportFileParsed> importFiles, File outDir, final ActionStatus status, String commandName) {
        for (ImportFileParsed importFile : importFiles) {
            for (MetadataInfo mdParams : importFile.data) {
                FilePathProperty fileProperty = new FilePathProperty(mdParams.file.fullPath, outDir, importFile.importFile);
                File inputFile = fileProperty.getInputFile();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    mdFile.loadFromPDF(inputFile);
                    mdFile.copyFromWithExpand(mdParams);
                    mdFile.saveAsPDF(inputFile, fileProperty.getOutputFile(true));
                    status.addStatus(inputFile.getPath(), "Done");
                } catch (Exception e) {
                    logger.error(commandName, e);
                    status.addError(inputFile.getPath(), "Failed: " + e);
                }
            }

        }
    }

    public void xmptodoc(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                MetadataInfo mdParams = params != null ? params.metadata : new MetadataInfo();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    mdFile.loadFromPDF(file);
                    mdFile.copyXMPToDoc();
                    mdFile.saveAsPDF(file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("xmptodoc", e);
                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(inputFileRelativeName(file), "Invalid file");
            }
        });
    }

    public void doctoxmp(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                MetadataInfo mdParams = params != null ? params.metadata : new MetadataInfo();
                try {
                    MetadataInfo mdFile = new MetadataInfo();
                    mdFile.loadFromPDF(file);
                    mdFile.copyDocToXMP();
                    mdFile.saveAsPDF(file, getOutputFile(file));
                    status.addStatus(outputFileRelativeName(file), "Done");
                } catch (Exception e) {
                    logger.error("doctoxmp", e);
                    status.addError(inputFileRelativeName(file), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(inputFileRelativeName(file), "Invalid file");
            }
        });
    }

    public void runCommand(CommandDescription command, List<File> batchFileList, File outDir, ActionStatus actionStatus) {
        if (!BatchMan.hasBatch()) {
            actionStatus.addError("*", "Invalid license, you can get a license at " + Constants.batchLicenseUrl);
        } else if (command.is("rename")) {
            rename(batchFileList, outDir,  actionStatus);
        } else if (command.is("edit")) {
            edit(batchFileList, outDir,  actionStatus);
        } else if (command.is("clear")) {
            clear(batchFileList, outDir,  actionStatus);
        } else if (command.is("fromjson")) {
            fromjson(batchFileList, outDir,  actionStatus);
        } else if (command.is("tojson")) {
            tojson(batchFileList, outDir,  actionStatus);
        } else if (command.is("fromyaml")) {
            fromyaml(batchFileList, outDir,  actionStatus);
        } else if (command.is("toyaml")) {
            toyaml(batchFileList, outDir,  actionStatus);
        } else if (command.is("fromcsv")) {
            fromcsv(batchFileList, outDir,  actionStatus);
        } else if (command.is("tocsv")) {
            tocsv(batchFileList, outDir,  actionStatus);
        } else if (command.is("xmptodoc")) {
            xmptodoc(batchFileList, outDir,  actionStatus);
        } else if (command.is("doctoxmp")) {
            doctoxmp(batchFileList, outDir,  actionStatus);
        } else {
            actionStatus.addError("*", "Invalid command");
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

    public abstract static class  FileAction {
        File currentInputDir;
        Stack<File> outDirStack = new Stack<>();
        FileAction(File outDir) {
            outDirStack.push(outDir);
        }

        File outDir() {
            return outDirStack.peek();
        }

        File pushOutDirFromInputFile(File input){
            File toPush = null;
            if(outDir() != null) {
                toPush = new File(outDir(), input.getName());
            }
            return outDirStack.push(toPush);
        }

        File popOutDir(){
            return outDirStack.pop();
        }

        File getOutputFile(File inputFile){
            File outDir = outDir();
            if(outDir == null){
                return null;
            }
            if(!outDir.exists()){
                outDir.mkdirs();
            }
            return new File(outDir, inputFile.getName());
        }

        File getOutputFileNonNull(File inputFile){
            File o = getOutputFile(inputFile);
            if(o == null) {
                return inputFile;
            }
            return o;
        }

        File getNewOutputFile(String name){
            File outDir = outDir();
            if(outDir == null){
                outDir = currentInputDir;
            } else {
                if(!outDir.exists()){
                    outDir.mkdirs();
                }
            }
            return new File(outDir, name);
        }

        String outputFileRelativeName(File file){
            if(outDir() == null){
                return file.getName();
            }
            Path basePath = outDir().toPath().toAbsolutePath();
            Path targetPath = file.toPath().toAbsolutePath();
            if(targetPath.startsWith(basePath)) {
                return basePath.relativize(targetPath).toString();
            }
            return file.getName();
        }

        void setCurrentInputDir(File inputFile){
            if(inputFile.isDirectory()){
                currentInputDir = inputFile;
            } else {
                currentInputDir = inputFile.getParentFile();
            }
        }

        String inputFileRelativeName(File file){
            if(currentInputDir == null){
                return file.getName();
            }
            Path basePath =currentInputDir.toPath().toAbsolutePath();
            Path targetPath = file.toPath().toAbsolutePath();
            if(targetPath.startsWith(basePath)) {
                return basePath.relativize(targetPath).toString();
            }
            return file.getName();
        }

        void beforeAll(){
        }

        void afterAll(){
        }

        void beforeEach(){

        }

        void afterEach(){
        }

        abstract void apply(File file);

        abstract void ignore(File file);
    }

    public abstract static class  ExportFileAction extends FileAction {
        public record ExportedObject(File file, Map<String, Object> data){}

        boolean singleFile;
        List<ExportedObject> collection = new ArrayList<>();

        ExportFileAction(File outDir, boolean singleFile) {
            super(outDir);
            this.singleFile = singleFile;
        }


        void afterEach(){
            if(!singleFile){
                if(!collection.isEmpty()) {
                    exportRecords(collection);
                    collection.clear();
                }
            }
        }

        void afterAll(){
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

