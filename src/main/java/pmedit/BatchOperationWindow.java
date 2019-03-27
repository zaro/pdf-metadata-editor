package pmedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
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

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import pmedit.PDFMetadataEditBatch.ActionStatus;

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
		setTitle("Batch PDF metadata edit");
		setBounds(100, 100, 640, 480);
		setMinimumSize(new Dimension(640, 480));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{1, 487, 113, 0};
		gridBagLayout.rowHeights = new int[]{29, 70, 16, 217, 45, 29, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		
		btnParameters = new JButton("Parameters");
		btnParameters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createBatchParametersWindow();
			}
		});
		
				selectedBatchOperation = new JComboBox<CommandDescription>();
				GridBagConstraints gbc_selectedBatchOperation = new GridBagConstraints();
				gbc_selectedBatchOperation.fill = GridBagConstraints.HORIZONTAL;
				gbc_selectedBatchOperation.insets = new Insets(10, 10, 5, 5);
				gbc_selectedBatchOperation.gridwidth = 2;
				gbc_selectedBatchOperation.gridx = 0;
				gbc_selectedBatchOperation.gridy = 0;
				getContentPane().add(selectedBatchOperation, gbc_selectedBatchOperation);
				
						selectedBatchOperation.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								createBatchParametersWindowButton();
							}
						});
		GridBagConstraints gbc_btnParameters = new GridBagConstraints();
		gbc_btnParameters.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnParameters.insets = new Insets(10, 0, 5, 10);
		gbc_btnParameters.gridx = 2;
		gbc_btnParameters.gridy = 0;
		getContentPane().add(btnParameters, gbc_btnParameters);
		
		scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 10, 5, 10);
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		getContentPane().add(scrollPane_1, gbc_scrollPane_1);
		
		fileList = new JTextPane();
		fileList.setText("Drop files here to batch process them ...");
		scrollPane_1.setViewportView(fileList);
		fileList.setEditable(false);
		
		lblStatus = new JLabel("Status");
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblStatus.insets = new Insets(0, 10, 5, 10);
		gbc_lblStatus.gridwidth = 2;
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 2;
		getContentPane().add(lblStatus, gbc_lblStatus);
		
		statusScrollPane = new JScrollPane();
		GridBagConstraints gbc_statusScrollPane = new GridBagConstraints();
		gbc_statusScrollPane.fill = GridBagConstraints.BOTH;
		gbc_statusScrollPane.insets = new Insets(0, 10, 5, 10);
		gbc_statusScrollPane.gridwidth = 3;
		gbc_statusScrollPane.gridx = 0;
		gbc_statusScrollPane.gridy = 3;
		getContentPane().add(statusScrollPane, gbc_statusScrollPane);
		
		statusText = new JTextPane();
		statusScrollPane.setViewportView(statusText);
		statusText.setEditable(false);
		
		        Style estyle = statusText.addStyle("ERROR", null);
		
		txtpnnoBatchLicense = new JTextPane();
		txtpnnoBatchLicense.setEditable(false);
		txtpnnoBatchLicense.setBackground(UIManager.getColor("Panel.background"));
		txtpnnoBatchLicense.setContentType("text/html");
		txtpnnoBatchLicense.setText("<p align=center>No batch license. In order to use batch operations please get a license from <a href='" + Constants.batchLicenseUrl+ "'>" + Constants.batchLicenseUrl+ "<a></p>");
		GridBagConstraints gbc_txtpnnoBatchLicense = new GridBagConstraints();
		gbc_txtpnnoBatchLicense.fill = GridBagConstraints.BOTH;
		gbc_txtpnnoBatchLicense.insets = new Insets(0, 10, 5, 10);
		gbc_txtpnnoBatchLicense.gridwidth = 3;
		gbc_txtpnnoBatchLicense.gridx = 0;
		gbc_txtpnnoBatchLicense.gridy = 4;
		getContentPane().add(txtpnnoBatchLicense, gbc_txtpnnoBatchLicense);
		txtpnnoBatchLicense.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
					return;
				}
				if (!java.awt.Desktop.isDesktopSupported()) {
					return;
				}
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
					return;
				}

				try {
					java.net.URI uri = e.getURL().toURI();
					desktop.browse(uri);
				} catch (Exception e1) {

				}
			}
		});
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.WEST;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 5;
		getContentPane().add(panel, gbc_panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(closeWindowActionListener);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.WEST;
		gbc_btnCancel.insets = new Insets(0, 10, 10, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 5;
		getContentPane().add(btnCancel, gbc_btnCancel);
		
		btnAction = new JButton("Begin");
		btnAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runBatch();
			}
		});
		
				GridBagConstraints gbc_btnAction = new GridBagConstraints();
				gbc_btnAction.insets = new Insets(0, 0, 10, 10);
				gbc_btnAction.anchor = GridBagConstraints.NORTHEAST;
				gbc_btnAction.gridx = 2;
				gbc_btnAction.gridy = 5;
				getContentPane().add(btnAction, gbc_btnAction);

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
        StyleConstants.setForeground(estyle, Color.red);
		
		createBatchParametersWindowButton();
		

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
		if(!BatchMan.hasBatch()){
			btnAction.setEnabled(false);
			txtpnnoBatchLicense.setVisible(true);
		} else {
			btnAction.setEnabled(true);
			gridBagLayout.rowHeights[4] = 0;
			getContentPane().remove(txtpnnoBatchLicense);
			gridBagLayout.removeLayoutComponent(txtpnnoBatchLicense);
			txtpnnoBatchLicense = null;
		}
		
		java.net.URL imgURL = PDFMetadataEditWindow.class
				.getResource("pdf-metadata-edit.png");
		ImageIcon icoImg = new ImageIcon(imgURL);
		setIconImage(icoImg.getImage());
	}
	
	
	boolean hasErrors = false;
	private JButton btnParameters;
	private JTextPane txtpnnoBatchLicense;
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
	public void appendFiles(final List<File> files){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		
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
		});
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
				BatchOperationParameters params =getBatchParameters(command);
				params.storeForCommand(command);

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
			FileDrop.remove(this);
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

	
	protected BatchOperationParameters getBatchParameters(CommandDescription command){
		BatchOperationParameters params = batchParameters.get(command.name);
		if( params == null){
			params = BatchOperationParameters.loadForCommand(command);
			batchParameters.put(command.name, params);
		}
		return params;
	}
	
	
	public void createBatchParametersWindow(){
		final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());

		if( parametersWindow != null) {
			parametersWindow.setVisible(false);
			parametersWindow.dispose();
			parametersWindow = null;
		}

		BatchOperationParameters params =getBatchParameters(command);
		
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
