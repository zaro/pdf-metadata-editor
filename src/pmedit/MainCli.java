package pmedit;

import pmedit.CommandLine.ParseError;

public class MainCli {
	
	public static void executeCommand(final CommandLine cmdLine){
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
			batch.runCommand(cmdLine.command, PDFMetadataEditBatch.fileList(cmdLine.fileList), status);
			return;
		} else if(cmdLine.batchGui){
			status.addError("*", "Batch gui command not allowed in console mode");
		} else { 
			status.addError("*", "No command specified");
		}
	}

	public static void main(CommandLine cmdLine){
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
