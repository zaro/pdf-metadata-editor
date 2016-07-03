package pmedit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.util.Properties;
import java.util.Vector;
// Unifinshed attemp for a coomand line tool :)

public class PDFMetadataEditBatch {

	BatchOperationParameters params;
	
	public PDFMetadataEditBatch() {
		this(null);
	}
	
	public PDFMetadataEditBatch(BatchOperationParameters params){
		this.params = params;
	}
	
	/**
	 * @param args
	 */
	public interface ActionStatus {
		public void addStatus(String filename, String message);
		public void addError(String filename, String error);
	}
	
	interface FileAction {
		public void apply(File file);
		public void ignore(File file);
	};
	
	public void forFiles(File file, FileFilter filter, FileAction action){
		if(file.isFile()){
			action.apply(file);
		} else if( file.isDirectory() ){
			for(File f:file.listFiles(filter)){
				action.apply(f);
			}			
		}
	}
	
	public void forFiles(List<File> files, FileFilter filter, FileAction action){
		for(File file: files){
			forFiles(file, filter, action);
		}
	}

	protected FileFilter defaultFileFilter= new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			if( isPdfExtension( pathname) ){
				return true;
			}
			return false;
		}
	};
	
	public void forFiles(File file, FileAction action){
		forFiles(file, defaultFileFilter, action);
	}

	public void forFiles(List<File> files, FileAction action){
		forFiles(files, defaultFileFilter, action);
	}
	
	public static boolean isPdfExtension(File pathname) {
		return pathname.getName().toLowerCase().endsWith(".pdf");
	}

	public void edit(List<File> files, final ActionStatus status){
		if(params == null) {
			status.addError("*", "No metadata defined");
			return;
		}
		forFiles(files, new FileAction() {
			
			@Override
			public void apply(File file) {
				MetadataInfo md = params != null ? params.metadata : new MetadataInfo();
				try {
					md.saveToPDF(file);
					status.addStatus(file.getName(), "Cleared");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}

			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file!");				
			}
		});
	}
	
	public void clear(List<File> files, final ActionStatus status){
		forFiles(files, new FileAction() {
			
			@Override
			public void apply(File file) {
				MetadataInfo md = params != null ? params.metadata : new MetadataInfo();
				try {
					md.saveToPDF(file);
					status.addStatus(file.getName(), "Cleared");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}

			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file!");				
			}
		});
	}
	
	
	public void rename(List<File> files, final ActionStatus status){
		String template = null;
		if(params != null ){
			template = params.renameTemplate;
			if(!template.toLowerCase().endsWith(".pdf"))
				template += ".pdf";
		}
		if(template == null){
			status.addError("*", "Rename template not configured");
			return;
		}		
		final TemplateString ts = new TemplateString(template);
		
		forFiles(files, new FileAction() {
			
			@Override
			public void apply(File file) {
				try {
					MetadataInfo md = new MetadataInfo();
					md.loadFromPDF(file);
					String toName = ts.process(md);
					String toDir= file.getParent();
					File to = new File(toDir,toName);
					if (to.exists()){
						status.addError(file.getName(), "Destination file already exists" + to.getName());
					} else {
						if(file.renameTo(to)){
							status.addStatus(file.getName(), to.getName());
						} else {
							status.addError(file.getName(), "Failed to rename to" + to.getName());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}
			
			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file!");				
			}
		});	
	}
	

	public void tojson(List<File> files, final ActionStatus status){
		forFiles(files, new FileAction() {
			
			@Override
			public void apply(File file) {
				try {
					MetadataInfo md = new MetadataInfo();
					md.loadFromPDF(file);
					String outFile = file.getAbsolutePath().replaceFirst("\\.[Pp][Dd][Ff]$", ".json");
					if(!outFile.endsWith(".json")){
						outFile = file.getAbsolutePath() + ".json";
					}
					Writer out = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outFile), "UTF8"));
					out.write(md.toJson(2));
					out.close();
					status.addStatus(file.getName(), outFile);
				} catch (IOException e) {
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}
			
			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file!");				
			}
		});	
	}

	public void toyaml(List<File> files, final ActionStatus status){
		forFiles(files, new FileAction() {
			
			@Override
			public void apply(File file) {
				try {
					MetadataInfo md = new MetadataInfo();
					md.loadFromPDF(file);
					String outFile = file.getAbsolutePath().replaceFirst("\\.[Pp][Dd][Ff]$", ".yaml");
					if(!outFile.endsWith(".yaml")){
						outFile = file.getAbsolutePath() + ".yaml";
					}
					Writer out = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outFile), "UTF8"));
					out.write(md.toYAML(true));
					out.close();
					status.addStatus(file.getName(), outFile);
				} catch (IOException e) {
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}
			
			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file!");				
			}
		});	
	}

	public static List<File> fileList(List<String> fileNames){
		ArrayList<File> rval = new ArrayList<File>();
		for(String fileName: fileNames){
			File file =  new File(fileName);
			if(file.exists()){
				rval.add(file);
			}
		}
		return rval;
	}
	public static List<File> fileList(String[] fileNames){
		return fileList(Arrays.asList(fileNames));
	}

	
	public void runCommand(CommandDescription command, List<File> batchFileList, ActionStatus actionStatus){
		if( command.is("rename")){
			rename(batchFileList, actionStatus);
		} else if( command.is("rename")){
			edit(batchFileList, actionStatus);
		} else if( command.is("clear")){
			clear(batchFileList, actionStatus);
		} else if( command.is("tojson")){
			tojson(batchFileList, actionStatus);
		} else if( command.is("toyaml")){
			toyaml(batchFileList, actionStatus);
		} else {
			actionStatus.addError("*", "Invalid command");
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			System.out.println("Available commands:");
			System.out.println("dump - dump metadata");
			System.out.println("rename - rename file accorfing to metadata");
			System.exit(1);
		}
		
		ActionStatus status = new ActionStatus() {
			@Override
			public void addStatus(String filename, String message) {
				System.out.println(filename + " -> " + message);
			}

			@Override
			public void addError(String filename, String error) {
				System.err.println(filename + " -> " + error);
			}
		};
		
		final PDFMetadataEditBatch cli = new PDFMetadataEditBatch();

		if (args[0].equals("clear")) {
			cli.clear(fileList(args), status);
		}
		if (args[0].equals("rename")) {
			String template = args[1];
			if(!template.toLowerCase().endsWith(".pdf"))
				template += ".pdf";
			TemplateString ts = new TemplateString(template);
			try{
				for(int i=2; i < args.length; ++i){
					MetadataInfo md = new MetadataInfo();
					File from = new File(args[i]);
					md.loadFromPDF(from);
					String toName = ts.process(md);
					System.out.print(from.toString() + " -> " + toName);
					String toDir= from.getParent();
					File to = new File(toDir,toName);
					boolean success =  from.renameTo(to);
					System.out.println(success? " OK" : " FAIL");
					
				}
			} catch (IOException e) {
				System.out.println("Failed to parse: " + args[1]);
				System.out.println(e.toString());
				System.exit(1);
			}
		}
		if(args[0].equals("list")){
			for(File f:new File(args[1]).listFiles()){
				System.out.println(f.getPath());
			}
		}
	}

}
