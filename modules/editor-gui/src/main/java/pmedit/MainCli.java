package pmedit;

import org.slf4j.LoggerFactory;
import pmedit.CommandLine.ParseError;
import pmedit.ext.PmeExtension;
import pmedit.mcp.McpHelpText;
import pmedit.mcp.McpMain;

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
                    "       --license=email,key        install a license and quit\n" +
                    "                                  pass email and license ID separated with comma (no spaces) to \n" +
                    "                                  install a license from the command line.\n" +
                    "       --releaseLicense           release the current license and quit\n" +
                    "       --showLicense              show the current license and quit\n" +
                    "       --offline                  set/release a license in offline mode\n" +
                    "       --mcp                      start a Model Context Protocol server on stdin/stdout\n" +
                    "       --mcp-root=DIR             root directory for MCP file access (default: current directory)\n" +
                    "       --mcp-read-only            disable mutating MCP tools\n" +
                    "       --mcp-batch-limit=N        maximum files or operations per MCP batch (default: 100, 0 = unlimited)\n" +
                    "       --mcp-walk-timeout=N       maximum seconds to expand directory inputs (default: 20, 0 = unlimited)\n" +
                    "       --help-mcp                 print MCP configuration for the installed desktop app\n" +
                    "\n" +
                    "COMMANDS\n" +
                    "\n" +
                    CommandDescription.helpMessage(32) +
                    "\n" +
                    "MCP SERVER\n" +
                    "\n" +
                    "  pmedit-cli --mcp [--mcp-root=DIR] [--mcp-read-only] [--mcp-batch-limit=N]\n" +
                    "              [--mcp-walk-timeout=N]\n" +
                    "\n" +
                    "  Starts a Model Context Protocol server over stdin/stdout. All paths passed\n" +
                    "  to MCP tools are resolved relative to --mcp-root (default: current working\n" +
                    "  directory) and may not escape it. Mutating tools may require a license.\n" +
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
        if (cmdLine.helpMcp) {
            System.out.print(McpHelpText.text());
            System.out.println();
            System.out.print(McpHelpText.optionsText());
            return;
        }
        if (cmdLine.mcp) {
            McpMain.run(cmdLine);
            return;
        }
        BatchListener listener = new BatchListener() {
            public void onFile(BatchFileOutcome outcome) {
                if (outcome.displayLabel() == null) {
                    return;
                }
                System.out.print(outcome.displayLabel());
                System.out.print(" -> ");
                System.out.println(outcome.displayMessage());
            }

            public void onBatchError(BatchError error) {
                System.out.print(error.displayLabel());
                System.out.print(" -> ");
                System.out.println(error.message());
            }
        };

        if (cmdLine.hasCommand()) {
            PDFMetadataEditBatch batch = new PDFMetadataEditBatch(cmdLine.params);
            File outDir = cmdLine.outputDir != null ? new File(cmdLine.outputDir) : null;
            batch.runCommand(cmdLine.command, FileList.fileList(cmdLine.fileList), outDir, listener);
        } else if (cmdLine.batchGui) {
            listener.onBatchError(new BatchError(
                    "*", "invalid_command", "Batch gui command not allowed in console mode",
                    new Exception("Batch gui command not allowed in console mode")));
        } else {
            listener.onBatchError(new BatchError(
                    "*", "invalid_command", "No command specified",
                    new Exception("No command specified")));
        }
    }

    public static void main(CommandLine cmdLine) {
        if(PmeExtension.get().handleCommandLine(cmdLine)){
            return;
        };
        executeCommand(cmdLine);
    }

    public static void main(final String[] args) {
        Main.setupLogging(false, true);
        try {
            CommandLine cmdLine = null;
            try {
                cmdLine = CommandLine.parse(args);
            } catch (ParseError e) {
                LoggerFactory.getLogger(MainCli.class).error("CommandLine.ParseError", e);
                System.err.println(e.toString());
                System.exit(1);
            }
            main(cmdLine);
        } catch (Throwable e) {
            LoggerFactory.getLogger(MainCli.class).error("Failed cli execution", e);
            System.err.println(e.toString());
            System.exit(1);
        }
    }

}
