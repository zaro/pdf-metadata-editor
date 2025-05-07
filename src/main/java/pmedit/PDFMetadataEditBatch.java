package pmedit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Stack;

public class PDFMetadataEditBatch {

    protected FileFilter defaultFileFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return isPdfExtension(pathname);
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

    public void forFiles(File file, FileFilter filter, FileAction action) {
        if (file.isFile()) {
            if (isPdfExtension(file)) {
                action.apply(file);
            } else {
                action.ignore(file);
            }
        } else if (file.isDirectory()) {
            File[]  files = file.listFiles(filter);
            if(files != null) {
                forFiles(List.of(files), filter, action);
            }
        } else {
            action.ignore(file);
        }
    }

    public void forFiles(List<File> files, FileFilter filter, FileAction action) {
        if(files == null){
            return;
        }
        for (File file : files) {
            forFiles(file, filter, action);
        }
    }

    public void forFiles(File file, FileAction action) {
        forFiles(file, defaultFileFilter, action);
    }

    public void forFiles(List<File> files, FileAction action) {
        forFiles(files, defaultFileFilter, action);
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
                    status.addStatus(file.getName(), "Done");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    status.addError(file.getName(), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());
            }
        });
    }

    public void clear(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                MetadataInfo md = params != null ? params.metadata : new MetadataInfo();
                try {
                    md.saveAsPDF(file, getOutputFile(file));
                    status.addStatus(file.getName(), "Cleared");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    status.addError(file.getName(), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());
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
                        status.addError(file.getName(), "Destination file already exists:  " + to.getName());
                    } else {
                        try {
                            Files.move(file.toPath(), to.toPath());
                            status.addStatus(file.getName(), to.getName());
                        } catch (IOException e) {
                            status.addError(file.getName(), "Rename failed with " + to.getName() + " : " + e);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    status.addError(file.getName(), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());
            }
        });
    }

    public void tojson(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    md.loadFromPDF(file);
                    String outFile = getOutputFileNonNull(file).getAbsolutePath().replaceFirst("\\.[Pp][Dd][Ff]$", ".json");
                    if (!outFile.endsWith(".json")) {
                        outFile +=  ".json";
                    }
                    Writer out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(outFile), StandardCharsets.UTF_8));
                    out.write(md.toJson(true));
                    out.close();
                    status.addStatus(file.getName(), outFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    status.addError(file.getName(), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());
            }
        });
    }

    public void toyaml(List<File> files, File outDir, final ActionStatus status) {
        forFiles(files, new FileAction(outDir) {

            @Override
            public void apply(File file) {
                try {
                    MetadataInfo md = new MetadataInfo();
                    md.loadFromPDF(file);
                    String outFile = getOutputFileNonNull(file).getAbsolutePath().replaceFirst("\\.[Pp][Dd][Ff]$", ".yaml");
                    if (!outFile.endsWith(".yaml")) {
                        outFile = ".yaml";
                    }
                    Writer out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(outFile), StandardCharsets.UTF_8));
                    out.write(md.toYAML(true));
                    out.close();
                    status.addStatus(file.getName(), outFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    status.addError(file.getName(), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());
            }
        });
    }

    public void fromcsv(List<File> csvFiles, File outDir, final ActionStatus status) {
        for (File csvFile : csvFiles) {
            try {
                List<MetadataInfo> actionList = CsvMetadata.readFile(csvFile);
                for (MetadataInfo mdParams : actionList) {
                    File file = new File(mdParams.file.fullPath);
                    try {
                        MetadataInfo mdFile = new MetadataInfo();
                        mdFile.loadFromPDF(file);
                        mdFile.copyFromWithExpand(mdParams);
                        mdFile.saveAsPDF(file);
                        status.addStatus(file.getName(), "Done");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        status.addError(file.getName(), "Failed: " + e);
                    }
                }

            } catch (Exception e) {
                status.addError(csvFile.getName(), "Failed: " + e);
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
                    status.addStatus(file.getName(), "Done");
                } catch (Exception e) {
                    e.printStackTrace();
                    status.addError(file.getName(), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());
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
                    status.addStatus(file.getName(), "Done");
                } catch (Exception e) {
                    e.printStackTrace();
                    status.addError(file.getName(), "Failed: " + e);
                }
            }

            @Override
            public void ignore(File file) {
                status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());
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
        } else if (command.is("tojson")) {
            tojson(batchFileList, outDir,  actionStatus);
        } else if (command.is("toyaml")) {
            toyaml(batchFileList, outDir,  actionStatus);
        } else if (command.is("fromcsv")) {
            fromcsv(batchFileList, outDir,  actionStatus);
        } else if (command.is("xmptodoc")) {
            xmptodoc(batchFileList, outDir,  actionStatus);
        } else if (command.is("doctoxmp")) {
            doctoxmp(batchFileList, outDir,  actionStatus);
        } else {
            actionStatus.addError("*", "Invalid command");
        }
    }

    abstract class  FileAction {
        Stack<File> outDirStack = new Stack<>();
        FileAction(File outDir) {
            outDirStack.push(outDir);
        }

        File outDir() {
            return outDirStack.peek();
        }

        File pushOutDirFromInputFile(File input){
            if(outDir() == null){
                return  null;
            }
            return outDirStack.push(new File(outDir(), input.getName()));
        }

        File popOutDir(){
            return outDirStack.pop();
        }

        File getOutputFile(File inputFile){
            File outDir = outDir();
            if(outDir == null){
                return null;
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

        abstract void apply(File file);

        abstract void ignore(File file);
    }

}
