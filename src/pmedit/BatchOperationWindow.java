package pmedit;

import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import pmedit.PDFMetadataEditBatch.ActionStatus;

import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.FlowLayout;

public class BatchOperationWindow extends JFrame {
	private JTextPane statusText;
	private ActionListener closeWindowActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			dispatchEvent(new WindowEvent(BatchOperationWindow.this, WindowEvent.WINDOW_CLOSING));
		}
	};
	private JButton btnAction;
	private JButton btnCancel;
	private JScrollPane statusScrollPane;
	private JTextPane fileList;
	private JLabel lblStatus;
	private JScrollPane scrollPane_1;
	private JComboBox<CommandDescription> selectedBatchOperation;

	private BatchParametersWindow parametersWindow ;
	private Map<String, BatchOperationParameters> batchParameters =new HashMap<String, BatchOperationParameters>();

	
	List<File> batchFileList = new ArrayList<File>();
	
	final static String LAST_USED_COMMAND_KEY = "lastUsedBatchCommand";
	public BatchOperationWindow( CommandDescription command) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Batch PDF metadata edit");
		setBounds(100, 100, 640, 480);
		setMinimumSize(new Dimension(640, 480));
		getContentPane().setLayout(new MigLayout("", "[grow][right]", "[][70px:n][][grow,fill][]"));

		selectedBatchOperation = new JComboBox<CommandDescription>();
		getContentPane().add(selectedBatchOperation, "cell 0 0,growx");

		if( command != null){
			selectedBatchOperation.setModel(new DefaultComboBoxModel<CommandDescription>(new CommandDescription[]{ command }));
		} else {
			selectedBatchOperation.setModel(new DefaultComboBoxModel<CommandDescription>(CommandDescription.batchCommands));
			String lastUsedCommand = Main.getPreferences().get(LAST_USED_COMMAND_KEY, null);
			if(lastUsedCommand != null){
				CommandDescription lastCommand = CommandDescription.getBatchCommand(lastUsedCommand);
				if(lastCommand != null){
					selectedBatchOperation.setSelectedItem(lastCommand);
				}
			}
		}
		
		
		btnParameters = new JButton("Parameters");
		btnParameters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createBatchParametersWindow();
			}
		});
		getContentPane().add(btnParameters, "cell 1 0");
		
		scrollPane_1 = new JScrollPane();
		getContentPane().add(scrollPane_1, "cell 0 1 2 1,grow");
		
		fileList = new JTextPane();
		fileList.setText("Drop files here to batch process them ...");
		scrollPane_1.setViewportView(fileList);
		fileList.setEditable(false);
		
		lblStatus = new JLabel("Status");
		getContentPane().add(lblStatus, "cell 0 2");
		
		statusScrollPane = new JScrollPane();
		getContentPane().add(statusScrollPane, "cell 0 3 2 1,grow");
		
		statusText = new JTextPane();
		statusScrollPane.setViewportView(statusText);
		statusText.setEditable(false);

        Style estyle = statusText.addStyle("ERROR", null);
        StyleConstants.setForeground(estyle, Color.red);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, "flowx,cell 0 4");
		panel.setLayout(new BorderLayout(0, 0));
		
		btnAction = new JButton("Begin");
		btnAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runBatch();
			}
		});

		getContentPane().add(btnAction, "cell 1 4");
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(closeWindowActionListener);
		getContentPane().add(btnCancel, "cell 0 4");
		
		createBatchParametersWindowButton();
		
		String defaultMetadataYAML = Main.getPreferences().get("defaultMetadata", null);
		if (defaultMetadataYAML != null && defaultMetadataYAML.length() > 0) {
			MetadataInfo editMetadata = new MetadataInfo();
			editMetadata.fromYAML(defaultMetadataYAML);
			editMetadata.enableOnlyNonNull();
			BatchOperationParameters params = new BatchOperationParameters();
			params.metadata = editMetadata;
			
			batchParameters.put("edit", params);
		}

		selectedBatchOperation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createBatchParametersWindowButton();
			}
		});

		new FileDrop( this, new FileDrop.Listener() {   
			public void filesDropped( java.io.File[] files, Point where ) {
				getGlassPane().setVisible(false);
				repaint();
				appendFiles(Arrays.asList(files));
			}

			@Override
			public void dragEnter() {
				getGlassPane().setVisible(true);
				
			}

			@Override
			public void dragLeave() {
				getGlassPane().setVisible(false);
				repaint();		
			}

			@Override
			public void dragOver(Point where) {
			}
		});
		setGlassPane(new FileDropMessage());
	}
	
	
	boolean hasErrors = false;
	private JButton btnParameters;
	public void append(String s) {
	   try {
	      Document doc = statusText.getDocument();
	      doc.insertString(doc.getLength(), s, null);
	      statusScrollPane.getVerticalScrollBar().setValue(statusScrollPane.getVerticalScrollBar().getMaximum());
	   } catch(BadLocationException exc) {
	      exc.printStackTrace();
	   }
	}
	public void appendError(String s) {
	  hasErrors = true;
	   try {
	      StyledDocument doc = statusText.getStyledDocument();
	      doc.insertString(doc.getLength(), s, statusText.getStyle("ERROR"));
	      statusScrollPane.getVerticalScrollBar().setValue(statusScrollPane.getVerticalScrollBar().getMaximum());
	   } catch(BadLocationException exc) {
	      exc.printStackTrace();
	   }
	}
	public void appendError(Throwable e) {
		  hasErrors = true;
		   try {
			  StringWriter sw = new StringWriter();
			  PrintWriter pw = new PrintWriter(sw);
			  e.printStackTrace(pw);
			  sw.toString(); // stack trace as a string		      
			  StyledDocument doc = statusText.getStyledDocument();
		      doc.insertString(doc.getLength(), sw.toString(), statusText.getStyle("ERROR"));
		      statusScrollPane.getVerticalScrollBar().setValue(statusScrollPane.getVerticalScrollBar().getMaximum());
		   } catch(BadLocationException exc) {
		      exc.printStackTrace();
		   }
		}	
	public void appendFiles(List<File> files){
		if(batchFileList.isEmpty() && files.size() > 0){
	      Document doc = fileList.getDocument();
	      try {
			doc.remove(0, doc.getLength());
			} catch (BadLocationException e) {}
		}
		for(File file:files){
		   try {
		      Document doc = fileList.getDocument();
		      doc.insertString(doc.getLength(), file.getAbsolutePath() +"\n", null);
		   } catch(BadLocationException exc) {
		      exc.printStackTrace();
		   }
		}
		batchFileList.addAll(files);
	}

	
	public static void clearActionListeners(AbstractButton btn){
	    for( ActionListener al : btn.getActionListeners() ) {
	        btn.removeActionListener( al );
	    }
	}

	public void runBatch(){
		final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());
		Main.getPreferences().put(LAST_USED_COMMAND_KEY, command.name);

		(new Worker(){
			ActionStatus actionStatus = new ActionStatus() {
				@Override
				public void addStatus(String filename, String message) {
					publish(new FileOpResult(filename, message, false));
				}

				@Override
				public void addError(String filename, String error) {
					publish(new FileOpResult(filename, error, true));
				}
				
			};
			@Override
			protected Void doInBackground() throws Exception {
				BatchOperationParameters params = batchParameters.get(command.name);

				PDFMetadataEditBatch batch = new PDFMetadataEditBatch(params);	
				batch.runCommand(command, batchFileList, actionStatus);
				return null;
			}
	       @Override
	       protected void done() {
				try {
				   get();
				} catch (InterruptedException e) {
					appendError(e);
				} catch (ExecutionException e) {
					appendError(e);
 				}
 			    onDone();
	       }
		}).execute();
	}
	
	void onDone(){
        try {
    	   	append("------\n");
    	   	if( hasErrors){
    	   		appendError("Done (with Errors)\n");
    	   	} else {
    	   		append("Done");
    	   	}
			clearActionListeners(btnAction);
			btnAction.setText("Close");
			btnAction.addActionListener(closeWindowActionListener);
			btnCancel.setVisible(false);
       } catch (Exception ignore) {
       }		
	}
	static class FileOpResult{
		public FileOpResult(String filename, String message, boolean error) {
			this.filename = filename;
			this.message = message;
			this.error = error;
		}
		String filename;
		String message;
		boolean error;
	};
	
	abstract class Worker extends SwingWorker<Void, FileOpResult>{
	    @Override
	    protected void process(List<FileOpResult> chunks) {
	         for (FileOpResult chunk : chunks) {
        	 	if (chunk.error){
        	 		appendError(chunk.filename + " -> " +chunk.message + "\n");
        	 	} else {
        	 		append(chunk.filename + " -> " +chunk.message + "\n");	
        	 	}
			}
    	}
    }

	
	public void createBatchParametersWindow(){
		final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());

		if( parametersWindow != null) {
			parametersWindow.setVisible(false);
			parametersWindow.dispose();
			parametersWindow = null;
		}

		BatchOperationParameters params = batchParameters.get(command.name);
		if( params == null){
			params = new BatchOperationParameters();
			batchParameters.put(command.name, params);
		}
		
		if( command.is("clear") ){	
			parametersWindow = new BatchParametersClear(params, this);
		}
		if( command.is("edit")){
			parametersWindow = new BatchParametersEdit(params, this);
		}
		if( command.is("rename")){
			parametersWindow = new BatchParametersRename(params, this);
		}
		if(parametersWindow != null) {
			parametersWindow.setModal(true);
			parametersWindow.setVisible(true);
		}
		
	}

	public void createBatchParametersWindowButton(){
		final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());

		if( command.is("clear") || command.is("rename") || command.is("edit")){
			btnParameters.setEnabled(true);
		} else {
			btnParameters.setEnabled(false);
		}
	}

}
