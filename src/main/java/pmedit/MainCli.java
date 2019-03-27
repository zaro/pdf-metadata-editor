package pmedit;

import pmedit.CommandLine.ParseError;

public class MainCli {
	
	public static void executeCommand(final CommandLine cmdLine){
		if(cmdLine.showHelp){
			System.out.print(helpMessage);
			return;
		}
		PDFMetadataEditBatch.ActionStatus status = new PDFMetadataEditBatch.ActionStatus() {
			@Override
			public void addStatus(String filename, String message) {
				System.out.print(filename);
				System.out.print(" -> ");
				System.out.println(message);
			}
			
			@Override
			public void addError(String filename, String error) {
				System.out.print(filename);
				System.out.print(" -> ");
				System.out.println(error);
			}
		};
		
		if( cmdLine.hasCommand()) {
			PDFMetadataEditBatch batch = new PDFMetadataEditBatch(cmdLine.params);	
			batch.runCommand(cmdLine.command, FileList.fileList(cmdLine.fileList), status);
			return;
		} else if(cmdLine.batchGui){
			status.addError("*", "Batch gui command not allowed in console mode");
		} else { 
			status.addError("*", "No command specified");
		}
	}
	
	

	public static void main(CommandLine cmdLine){
		if(cmdLine.licenseEmail != null && cmdLine.licenseKey != null){
			if(BatchMan.giveBatch( cmdLine.licenseKey, cmdLine.licenseEmail)) {
				System.out.println("Installed license for email: " + cmdLine.licenseEmail);
			} else {
				System.out.println("Invalid license!");
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
	
	static String helpMessage = 
			"Usage pmedit-cli [OPTIONS] COMMAND [METADATA FIELD(S)] file [files...]\n" +
			"\n" +
			"OPTIONS\n" +
			"\n" +
			"  -h,  --help                     show this help message\n" +
			"  -rt, --renameTemplate=STRING    set a rename template for 'rename' command\n" +
			"                                  any metadata field enclosed in {} will be substituted\n" +
			"                                  with the actual field value\n" +
			"       --license=email,key        install batch license and quit\n" +
			"                                  pass email and key separated with comma (no spaces) to \n" +
			"                                  install batch license from the command line.\n" +
			"\n"+
			"COMMANDS\n" +
			"\n"+
			CommandDescription.helpMessage(32) +
			"\n"+
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
			"\n"+
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

}
