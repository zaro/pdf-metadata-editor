package pmedit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;

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
			if(isPdfExtension(file)){
				action.apply(file);
			} else {
				action.ignore(file);
			}
		} else if( file.isDirectory() ){
			for(File f:file.listFiles(filter)){
				action.apply(f);
			}			
		} else {
			action.ignore(file);
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
				MetadataInfo mdParams = params != null ? params.metadata : new MetadataInfo();
				try {
					MetadataInfo mdFile = new MetadataInfo();
					mdFile.loadFromPDF(file);
					MetadataInfo md = mdParams.clone(); 
					md.expand(mdFile);
					md.saveAsPDF(file);
					status.addStatus(file.getName(), "Done");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}

			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());				
			}
		});
	}
	
	public void clear(List<File> files, final ActionStatus status){
		forFiles(files, new FileAction() {
			
			@Override
			public void apply(File file) {
				MetadataInfo md = params != null ? params.metadata : new MetadataInfo();
				try {
					md.saveAsPDF(file);
					status.addStatus(file.getName(), "Cleared");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}

			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());				
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
						status.addError(file.getName(), "Destination file already exists:  " + to.getName());
					} else {
						try {
							Files.move(file.toPath(), to.toPath());
							status.addStatus(file.getName(), to.getName());
						} catch (IOException e) {
							status.addError(file.getName(), "Rename failed with " + to.getName() +" : " + e);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}
			
			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());				
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
				} catch (Exception e) {
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}
			
			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());				
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
				} catch (Exception e) {
					e.printStackTrace();
					status.addError(file.getName(), "Failed: "  + e.toString());
				}
			}
			
			@Override
			public void ignore(File file) {
				status.addError(file.getName(), "Invalid file:" + file.getAbsolutePath());				
			}
		});	
	}
	
	public void fromcsv(List<File> csvFiles, final ActionStatus status){
		for(File csvFile: csvFiles) {
			try {
				List<MetadataInfo> actionList = CsvMetadata.readFile(csvFile);
				for(MetadataInfo mdParams : actionList) {
					File file = new File(mdParams.file.fullPath);
					try {
						MetadataInfo mdFile = new MetadataInfo();
						mdFile.loadFromPDF(file);
						MetadataInfo md = mdParams.clone(); 
						md.expand(mdFile);
						md.saveAsPDF(file);
						status.addStatus(file.getName(), "Done");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						status.addError(file.getName(), "Failed: "  + e.toString());
					}
				}
				
			} catch (Exception e) {
				status.addError(csvFile.getName(), "Failed: "  + e.toString());
			}
		}
	}


	public void runCommand(CommandDescription command, List<File> batchFileList, ActionStatus actionStatus){
		if( !BatchMan.hasBatch() ){
			actionStatus.addError("*", "Invalid license, you can get a license at " + Constants.batchLicenseUrl);
		} else if( command.is("rename") ){
			rename(batchFileList, actionStatus);
		} else if( command.is("edit")){
			edit(batchFileList, actionStatus);
		} else if( command.is("clear")){
			clear(batchFileList, actionStatus);
		} else if( command.is("tojson")){
			tojson(batchFileList, actionStatus);
		} else if( command.is("toyaml")){
			toyaml(batchFileList, actionStatus);
		} else if( command.is("fromcsv")){
			fromcsv(batchFileList, actionStatus);
		} else {
			actionStatus.addError("*", "Invalid command");
		}
	}
	
}
