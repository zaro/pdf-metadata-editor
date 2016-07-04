package pmedit;

import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import pmedit.CommandLine.ParseError;

public class Main {
	
	protected static int batchGuiCounter = 0;
	public static String getBatchGuiCommand(){
		return "batch-gui-"+ batchGuiCounter++;
	}
	
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
	
	public static void executeCommand(final CommandLine cmdLine){
		logLine("executeCommand", cmdLine.toString());
	
		if( cmdLine.hasCommand()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						BatchOperationWindow bs = batchInstances.get(cmdLine.command.name);
						if(bs != null){
							bs.appendFiles(PDFMetadataEditBatch.fileList(cmdLine.fileList));
						} else {
							makeBatchWindow(cmdLine.command.name, cmdLine.command, cmdLine.fileList);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return;
		}
		if(cmdLine.batchGui){
			makeBatchWindow(getBatchGuiCommand(), null, cmdLine.fileList);
			return;
		}
	
		final String fileName = cmdLine.fileList.size() > 0 ?  cmdLine.fileList.get(0) : null;
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
		CommandLine cmdLine = null; 
	    try {
	    	cmdLine = CommandLine.parse(args);
		} catch (ParseError e) {
			System.err.println(e);
		}
	    //System.out.println(cmdLine);
	    if(cmdLine.noGui){
	    	MainCli.main(cmdLine);
	    	return;
	    }
//	    try {
//    	UIManager.setLookAndFeel(
//    			UIManager.getCrossPlatformLookAndFeelClassName());
//    } 
//    catch (UnsupportedLookAndFeelException e) {}
//    catch (ClassNotFoundException e) {}
//    catch (InstantiationException e) {}
//    catch (IllegalAccessException e) {}
		executeCommand(cmdLine);
	}

	static Preferences _prefs;
	public static Preferences getPreferences() {
		if(_prefs == null){
			_prefs = Preferences.userRoot().node("PDFMetadataEditor");
		}
		return _prefs;
	}
}
