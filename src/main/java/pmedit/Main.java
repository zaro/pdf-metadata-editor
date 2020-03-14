package pmedit;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import pmedit.CommandLine.ParseError;
import pmedit.prefs.FilePreferencesFactory;

public class Main {

	protected static int batchGuiCounter = 0;
	public static String getBatchGuiCommand(){
		return "batch-gui-"+ batchGuiCounter++;
	}

	static BlockingQueue<CommandLine> cmdQueue = new LinkedBlockingDeque<CommandLine>();
	static class CommandsExecutor extends SwingWorker<Void, CommandLine> {
		 CommandsExecutor() {
			 //initialize
		 }

		 @Override
		 public Void doInBackground() {
			 while(true){
				CommandLine cmdLine;
				try {
					cmdLine = cmdQueue.take();
					logLine("publish", cmdLine.toString());

					publish(cmdLine);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		 }

		 @Override
		 protected void process(List<CommandLine> chunks) {
			 for(CommandLine cmdLine: chunks){
				 executeCommandSwingWorker(cmdLine);
			 }
		 }
	}

	// this must be swing worker
	public static void makeBatchWindow(final String commandName, final CommandDescription command, final List<String> fileList){
		logLine("makeBatchWindow", commandName);
		BatchOperationWindow bs = new BatchOperationWindow(command);
		bs.appendFiles(FileList.fileList(fileList));
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

	protected static void executeCommandSwingWorker(final CommandLine cmdLine){
		logLine("executeCommandSwingWorker", cmdLine.toString());

		if( cmdLine.hasCommand()) {
			try {
				BatchOperationWindow bs = batchInstances.get(cmdLine.command.name);
				if(bs != null){
					bs.appendFiles(FileList.fileList(cmdLine.fileList));
				} else {
					makeBatchWindow(cmdLine.command.name, cmdLine.command, cmdLine.fileList);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		if(cmdLine.batchGui){
			makeBatchWindow(getBatchGuiCommand(), null, cmdLine.fileList);
			return;
		}
		List<String> files = new ArrayList<String>(cmdLine.fileList);
		if( files.size() == 0){
			files.add(null);
		}
		for(final String file: files){
			try {
				String fileAbsPath = file != null ? new File(file).getAbsolutePath() : null;
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
						window.reloadFile();
						return;
					}
				}
		    	logLine("open editor", file);
				final PDFMetadataEditWindow window = new PDFMetadataEditWindow(file);
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
	}
	public static void executeCommand(final CommandLine cmdLine){
	   Main.logLine("executeCommand:", cmdLine.toString());

		try {
			cmdQueue.put(cmdLine);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final static String debugLog = System.getProperty("debugLog");
	protected static void logLine(String context, String line){
		if(debugLog == null){
			return;
		}
		System.out.println(context+":: " + line);
		try{
			PrintWriter output = new PrintWriter(new FileWriter(System.getProperty("java.io.tmpdir") + File.separator + "pdf-metada-editor-log.txt",true));

		    output.printf("%s:: %s\r\n", context, line == null ? "null" : line);
		    output.close();
		}  catch (Exception e) {}
	}

	static Map<String, BatchOperationWindow> batchInstances= new HashMap<String, BatchOperationWindow>();
	static List<PDFMetadataEditWindow> editorInstances = new ArrayList<PDFMetadataEditWindow>();

	public static void maybeExit(){
		if( batchInstances.size() == 0 && editorInstances.size() == 0 && cmdQueue.size() == 0){
			System.exit(0);
		}
	}

	public static int numWindows(){
		return batchInstances.size() + editorInstances.size();
	}

	public static void main(final String[] args) {
		CommandLine cmdLine = null;
	    try {
	    	cmdLine = CommandLine.parse(args);
		} catch (ParseError e) {
			Main.logLine("ParseError", e.toString());
			System.err.println(e);
			return;
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
	   if(WindowsSingletonApplication.isAlreadyRunning()){
		   System.out.println(">>>> already running");
	   }
	   executeCommand(cmdLine);
	   DDE.init();
	   Main.logLine("DDE:", "DONE");
	   CommandsExecutor commandsExecutor = new CommandsExecutor();
	   commandsExecutor.execute();

	   // Wait for at least on windows to open up, or the program
	   // terminates without showing anything
	   try {
		   while(numWindows() == 0){
				try {
					commandsExecutor.get(50, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {
				}
		   }
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	static Preferences _prefs;
	public static Preferences getPreferences() {
		if(_prefs == null){
		    System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
			_prefs = Preferences.userRoot().node("PDFMetadataEditor");
		}
		return _prefs;
	}
}
