package pmedit;

import pmedit.CommandLine.ParseError;
import pmedit.ext.PmeExtension;
import pmedit.util.HttpResponseCallback;

import java.io.File;

public class MainCli {

    static String helpMessage =
            "Usage pmedit-cli [OPTIONS] COMMAND [METADATA FIELD(S)] file [files...]\n" +
                    "\n" +
                    "OPTIONS\n" +
                    "\n" +
                    "  -h,  --help                     show this help message\n" +
                    "  -rt, --renameTemplate=STRING    set a rename template for 'rename' command\n" +
                    "                                  any metadata field enclosed in {} will be substituted\n" +
                    "                                  with the actual field value\n" +
                    "       --license=email,key        install Pro license and quit\n" +
                    "                                  pass email and license ID separated with comma (no spaces) to \n" +
                    "                                  install Pro license from the command line.\n" +
                    "       --releaseLicense           release current Pro license and quit\n" +
                    "\n" +
                    "COMMANDS\n" +
                    "\n" +
                    CommandDescription.helpMessage(32) +
                    "\n" +
                    "METADATA FIELDS\n" +
                    "\n" +
                    "Enable field : [!]FIEDLNAME\n" +
                    "  A field is enabled by specifying it's name. If the name is prefixed wiht ! it will be disabled.\n" +
                    "  There are two special fields `all` and `none` which respectively enable and disable all of the fields.\n" +
                    "  By default all fields are disabled, so you must enable at least one or the command will be a no-op.\n" +
                    "\n" +
                    "Set a value: FIEDLNAME=value\n" +
                    "  A field can be assigned a value with =, for example doc.title=WeeklyReport.\n" +
                    "  Assigning a value to field also enables it.\n" +
                    "  Fields that represent lists can be specified multiple times. \n" +
                    "  Dates can be specified in ISO format, e.g : \n" +
                    "    \"2016-06-16'T'00:15:00.000'Z'\" or \"2016-06-16'T'00:15:00\"  or\n" +
                    "    \"2016-06-16 00:15:00\" or \"2016-06-16\"\n" +
                    "\n" +
                    "Available fields :\n" +
                    CommandLine.mdFieldsHelpMessage(80, true) +
                    "\n  * field is read only, assignment to it will be ignored\n" +
                    "EXAMPLES\n" +
                    "\n" +
                    "Clear all metadata:\n" +
                    "  pmedit-cli clear all file1.pdf file2.pdf\n" +
                    "\n" +
                    "Clear only author and title:\n" +
                    "  pmedit-cli clear doc.title doc.author file1.pdf file2.pdf\n" +
                    "\n" +
                    "Clear all except author and title:\n" +
                    "  pmedit-cli clear all !doc.title !doc.author file1.pdf file2.pdf\n" +
                    "\n" +
                    "Set author and title:\n" +
                    "  pmedit-cli edit \"doc.title=The funniest book ever\" \"doc.author=Funny Guy\" file1.pdf file2.pdf\n" +
                    "\n" +
                    "Rename file from author and title:\n" +
                    "  pmedit-cli --renameTemplate \"{doc.author} - {doc.title}.pdf\" rename file1.pdf file2.pdf\n" +
                    "\n";

    public static void executeCommand(final CommandLine cmdLine) {
        if (cmdLine.showHelp) {
            System.out.print(helpMessage);
            return;
        }
        ActionStatus status = new ActionStatus() {
            public void showStatus(String filename, String message) {
                System.out.print(filename);
                System.out.print(" -> ");
                System.out.println(message);
            }

            public void showError(String filename, Throwable error) {
                System.out.print(filename);
                System.out.print(" -> ");
                System.out.println(error.getMessage());
            }
        };

        if (cmdLine.hasCommand()) {
            PDFMetadataEditBatch batch = new PDFMetadataEditBatch(cmdLine.params);
            File outDir = cmdLine.outputDir != null ? new File(cmdLine.outputDir) : null;
            batch.runCommand(cmdLine.command, FileList.fileList(cmdLine.fileList),outDir,  status);
        } else if (cmdLine.batchGui) {
            status.addError("*", new Exception("Batch gui command not allowed in console mode"));
        } else {
            status.addError("*", new Exception("No command specified"));
        }
    }

    public static void main(CommandLine cmdLine) {
        if (cmdLine.licenseKey != null) {
            PmeExtension ext = PmeExtension.get();
            if (ext.giveBatch(cmdLine.licenseKey, new HttpResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    System.out.println("Installed license for : " + ext.getBatch());
                }

                @Override
                public void onError(String errorMessage) {
                    System.out.println("Invalid license!");

                }
            })) {
                System.out.println("Getting license ... ");
            } else {
                System.out.println("Invalid license specification!");
            }
            return;
        }
        if(cmdLine.releaseLicense){
            PmeExtension ext = PmeExtension.get();
            String sub  = ext.getBatch();
            if (ext.removeBatch(new HttpResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    System.out.println("Released license for : " + sub);
                }

                @Override
                public void onError(String errorMessage) {
                    System.out.println("Failed to release license: " + errorMessage);
                }
            })) {
                System.out.println("Releasing license ... ");
            } else {
                System.out.println("Failed to initiate release license operation!");
            }
            return;
        }
        executeCommand(cmdLine);
    }

    public static void main(final String[] args) {
        try {
            main(CommandLine.parse(args));
        } catch (ParseError e) {
            System.err.println(e);
        }
    }

}
