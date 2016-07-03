package pmedit;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

	public static boolean ddeEnabled = "yes".equals(System.getProperty("pmeditUseDDE"));
	
	public static void makeBatchWindow(final String commandName, CommandDescription command, List<String> fileList){
		BatchOperationWindow bs = new BatchOperationWindow(command);
		bs.appendFiles(PDFMetadataEditBatch.fileList(fileList));
		bs.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		bs.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	batchInstances.remove(commandName);
	        	maybeExit();
	        }
	    });
		batchInstances.put(commandName, bs);
		bs.setVisible(true);
	}
	
	public static void executeCommand(List<String> args){
		StringBuffer sb = new StringBuffer();
		String delim = "'";
		for (String i : args) {
		    sb.append(delim).append(i);
		    delim = "', '";
		}
		logLine("executeCommand", sb.toString());
		final List<String> fileList= new ArrayList<String>(args);
	
		if( fileList.size() > 0) {
			final String commandName = fileList.get(0);
			final CommandDescription command = CommandDescription.getBatchCommand(commandName);
			if(command != null) {
				fileList.remove(0);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							BatchOperationWindow bs = batchInstances.get(command.name);
							if(bs != null){
								bs.appendFiles(PDFMetadataEditBatch.fileList(fileList));
							} else {
								if(command.is("rename")){
									String renameTemplate =  PDFMetadataEditWindow.getPreferences().get("renameTemplate", null);
									if(renameTemplate == null){
										JOptionPane.showMessageDialog(null, "Configure a rename template first, to be able to do a batch rename", "Error", JOptionPane.ERROR_MESSAGE);
										return;
									}
								}
								makeBatchWindow(command.name, command, fileList);

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				return;
			}
			if(commandName.startsWith("batch-gui")){
				fileList.remove(0);
				makeBatchWindow(commandName, null, fileList);
				return;
			}
		}
	
		final String fileName = fileList.size() > 0 ?  fileList.get(0) : null;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String fileAbsPath = fileName != null ? new File(fileName).getAbsolutePath() : null;
					// If we have file, and a single open empty window, load the file in it
					if(fileAbsPath != null && editorInstances.size() == 1 && editorInstances.get(0).getCurrentFile() == null){
						editorInstances.get(0).loadFile(fileAbsPath);
						return;
					}
			    	logLine("executeCommand fileName", fileAbsPath);
					for(PDFMetadataEditWindow window: editorInstances){
						File wFile = window.getCurrentFile();
				    	logLine("check ", wFile != null ? wFile.getAbsolutePath() : null);
						if( (fileAbsPath == null && wFile == null)  || (wFile != null && wFile.getAbsolutePath().equals(fileAbsPath)) ){
							logLine("match", null);
							if(window.getState() == JFrame.ICONIFIED){
								window.setState(JFrame.NORMAL);
							}
							window.toFront();
							window.repaint();
							return;
						}
					}
			    	logLine("open editor", fileName);
					final PDFMetadataEditWindow window = new PDFMetadataEditWindow(fileName);
					window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					window.addWindowListener(new java.awt.event.WindowAdapter() {
				        public void windowClosing(WindowEvent winEvt) {
				        	editorInstances.remove(window);
				        	maybeExit();
				        }
				    });
					editorInstances.add(window);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected static void logLine(String context, String line){
//		System.out.println(context+":: " + line);
//		try{
//			PrintWriter output = new PrintWriter(new FileWriter("log.txt",true));
//	
//		    output.printf("%s:: %s\r\n", context, line == null ? "null" : line);
//		    output.close();
//		}  catch (Exception e) {} 	
	}
	
	static Map<String, BatchOperationWindow> batchInstances= new HashMap<String, BatchOperationWindow>();
	static List<PDFMetadataEditWindow> editorInstances = new ArrayList<PDFMetadataEditWindow>();

	public static void maybeExit(){
		if( batchInstances.size() == 0 && editorInstances.size() == 0 ){
			System.exit(0);
		}
	}

	public static void main(final String[] args) {
		executeCommand(Arrays.asList(args));
	}
}
