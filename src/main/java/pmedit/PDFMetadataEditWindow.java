package pmedit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;

import net.miginfocom.swing.MigLayout;

public class PDFMetadataEditWindow extends JFrame{

	final JFileChooser fc;

	private File pdfFile;
	private MetadataInfo metadataInfo = new MetadataInfo();
	private MetadataInfo defaultMetadata;

	private JTextField filename;

	private PreferencesWindow preferencesWindow;

	/**
	 * Create the application.
	 */
	public PDFMetadataEditWindow(String filePath) {
		fc = new JFileChooser();
		defaultMetadata = new MetadataInfo();
		initialize();
		PdfFilter pdfFilter = new PdfFilter();
		fc.addChoosableFileFilter(pdfFilter);
		fc.setFileFilter(pdfFilter);
		clear();
		if (filePath != null) {
			try {
				pdfFile = new File(filePath);
				reloadFile();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error while opening file:\n" + e.toString());
			}
		}
		
		new FileDrop( this, new FileDrop.Listener() {   
			public void filesDropped( java.io.File[] files, Point where ) {
				FileDropSelectMessage fdm = ((FileDropSelectMessage)getGlassPane());
				getGlassPane().setVisible(false);
				repaint();

				List<String> fileNames = new ArrayList<String>();

				for(File file: files){
					fileNames.add(file.getAbsolutePath());
				}
				Main.executeCommand(new CommandLine(fileNames, fdm.isBatchOperation()));
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
				((FileDropSelectMessage)getGlassPane()).setDropPos(where);
				repaint();
				
			}
		});
		setGlassPane(new FileDropSelectMessage());
	}
	
	public File getCurrentFile(){
		return pdfFile;
	}


	private void clear() {

		filename.setText("");
		metadataEditor.clear();
	}

	public void loadFile(String fileName){
		pdfFile = new File(fileName);
		reloadFile();
	}

	public void reloadFile() {
		clear();
		try {
			filename.setText(pdfFile.getAbsolutePath());
			metadataInfo = new MetadataInfo();
			metadataInfo.loadFromPDF(pdfFile);
			metadataInfo.copyUnsetExpanded(defaultMetadata, metadataInfo);

			metadataEditor.fillFromMetadata(metadataInfo);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error while opening file:\n" + e.toString());
		}
	}

	private void saveFile(File newFile) {
		try {
			metadataEditor.copyToMetadata(metadataInfo);
			metadataInfo.copyUnsetExpanded(defaultMetadata, metadataInfo);
			
			metadataInfo.saveAsPDF(pdfFile, newFile);
			
			metadataEditor.fillFromMetadata(metadataInfo);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error while saving file:\n" + e.toString());
		}
	}
	private MetadataEditPane metadataEditor;

	final ActionListener saveAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			saveFile(null);
		}
	}; 

	final ActionListener saveRenameAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			saveFile(null);
			String renameTemplate =  Main.getPreferences().get("renameTemplate", null);
			if(renameTemplate == null){
				return;
			}
			TemplateString ts = new TemplateString(renameTemplate); 
			String toName = ts.process(metadataInfo);
			String toDir= pdfFile.getParent();
			File to = new File(toDir,toName);
			try {
				Files.move(pdfFile.toPath(), to.toPath());
				pdfFile = to;
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(PDFMetadataEditWindow.this,
						"Error while renaming file:\n" + e1.toString());
			}
			reloadFile();
		}
	}; 

	final ActionListener saveAsAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			final JFileChooser fcSaveAs = new JFileChooser();

			String dir = Main.getPreferences().get("LastDir", null);
			if (dir != null) {
				try {
					fcSaveAs.setCurrentDirectory(new File(dir));
				} catch (Exception e1) {
				}
			}
			int returnVal = fcSaveAs.showSaveDialog(PDFMetadataEditWindow.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selected = fcSaveAs.getSelectedFile();
				if(!selected.getName().toLowerCase().endsWith(".pdf")){
					selected = new File(selected.getAbsolutePath() + ".pdf");
				}

				saveFile(selected);
				pdfFile = selected;
				reloadFile();

				// save dir as last opened
				Main.getPreferences().put("LastDir", pdfFile.getParent());
			}
		}
	}; 
	final Runnable updateSaveButton = new Runnable() {
		
		@Override
		public void run() {
			String saveActionS = Main.getPreferences().get("defaultSaveAction", "save");

			for(ActionListener l : btnSave.getActionListeners()){
				btnSave.removeActionListener(l);
			}

			if(saveActionS.equals("saveRename")){
				btnSave.setText("Save & rename");
				btnSave.addActionListener(saveRenameAction);
			} else  if(saveActionS.equals("saveAs")){
				btnSave.setText("Save As ...");
				btnSave.addActionListener(saveAsAction);
			} else {
				btnSave.setText("Save");
				btnSave.addActionListener(saveAction);
				
			}
		}
	};
	private JButton btnSave;
	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("PDF Metadata Editor");
		setBounds(100, 100, 640, 480);
		setMinimumSize(new Dimension(640, 480));
		getContentPane()
				.setLayout(
						new MigLayout("insets 5", "[grow,fill]", "[][grow,fill][grow]"));

		JPanel panel = new JPanel();
		getContentPane().add(panel, "cell 0 0,growx");
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{105, 421, 20, 40, 0};
		gbl_panel.rowHeights = new int[]{36, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
				JButton btnOpenPdf = new JButton("Open PDF");
				GridBagConstraints gbc_btnOpenPdf = new GridBagConstraints();
				gbc_btnOpenPdf.anchor = GridBagConstraints.WEST;
				gbc_btnOpenPdf.insets = new Insets(0, 0, 0, 5);
				gbc_btnOpenPdf.gridx = 0;
				gbc_btnOpenPdf.gridy = 0;
				panel.add(btnOpenPdf, gbc_btnOpenPdf);
				
						btnOpenPdf.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								String dir = Main.getPreferences().get("LastDir", null);
								if (dir != null) {
									try {
										fc.setCurrentDirectory(new File(dir));
									} catch (Exception e) {
									}
								}
								int returnVal = fc.showOpenDialog(PDFMetadataEditWindow.this);
				
								if (returnVal == JFileChooser.APPROVE_OPTION) {
									pdfFile = fc.getSelectedFile();
									// This is where a real application would open the file.
									reloadFile();
									// save dir as last opened
									Main.getPreferences().put("LastDir", pdfFile.getParent());
								}
							}
						});
				
						filename = new JTextField();
						GridBagConstraints gbc_filename = new GridBagConstraints();
						gbc_filename.fill = GridBagConstraints.HORIZONTAL;
						gbc_filename.insets = new Insets(0, 0, 0, 5);
						gbc_filename.gridx = 1;
						gbc_filename.gridy = 0;
						panel.add(filename, gbc_filename);
						filename.setEditable(false);
						filename.setColumns(10);
				
						Component horizontalStrut = Box.createHorizontalStrut(20);
						GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
						gbc_horizontalStrut.anchor = GridBagConstraints.WEST;
						gbc_horizontalStrut.insets = new Insets(0, 0, 0, 5);
						gbc_horizontalStrut.gridx = 2;
						gbc_horizontalStrut.gridy = 0;
						panel.add(horizontalStrut, gbc_horizontalStrut);

						java.net.URL prefImgURL = PDFMetadataEditWindow.class
								.getResource("settings-icon.png");
						ImageIcon img = new ImageIcon(prefImgURL);
		
				JButton btnPreferences = new JButton("");
				
						GridBagConstraints gbc_btnPreferences = new GridBagConstraints();
						gbc_btnPreferences.anchor = GridBagConstraints.WEST;
						gbc_btnPreferences.gridx = 3;
						gbc_btnPreferences.gridy = 0;
						panel.add(btnPreferences, gbc_btnPreferences);
						btnPreferences.setIcon(img);
						
								btnPreferences.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										SwingUtilities.invokeLater(new Runnable() {
						
											@Override
											public void run() {
												if (preferencesWindow == null){
													preferencesWindow = new PreferencesWindow(Main.getPreferences(), defaultMetadata, PDFMetadataEditWindow.this);
													preferencesWindow.onSaveAction(updateSaveButton);
												}
												preferencesWindow.setVisible(true);
											}
										});
						
									}
								});
		
		metadataEditor = new MetadataEditPane();
		getContentPane().add(metadataEditor.tabbedaPane, "cell 0 1,grow");
		metadataEditor.showEnabled(false);
		

//		metadataEditor = createMetadataEditor();
//		getContentPane().add(metadataEditor,
//				"cell 0 1,growy");


		JPanel panel_4 = new JPanel();
		getContentPane().add(panel_4, "cell 0 2,growx");
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{340, 286, 0};
		gbl_panel_4.rowHeights = new int[]{33, 29, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		
		JButton btnCopyXmpTo = new JButton("Copy XMP To Document");
		btnCopyXmpTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(metadataInfo != null){
					metadataEditor.copyToMetadata(metadataInfo);
					metadataInfo.copyXMPToDoc();
					metadataEditor.fillFromMetadata(metadataInfo);
				}
			}
		});
		GridBagConstraints gbc_btnCopyXmpTo = new GridBagConstraints();
		gbc_btnCopyXmpTo.anchor = GridBagConstraints.SOUTH;
		gbc_btnCopyXmpTo.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCopyXmpTo.insets = new Insets(0, 0, 5, 5);
		gbc_btnCopyXmpTo.gridx = 0;
		gbc_btnCopyXmpTo.gridy = 0;
		panel_4.add(btnCopyXmpTo, gbc_btnCopyXmpTo);
		
				JButton btnCopyDocumentTo = new JButton("Copy Document To XMP");
				btnCopyDocumentTo.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(metadataInfo != null){
							metadataEditor.copyToMetadata(metadataInfo);
							metadataInfo.copyDocToXMP();
							metadataEditor.fillFromMetadata(metadataInfo);
						}
					}
				});
						
						JPanel panel_1 = new JPanel();
						GridBagConstraints gbc_panel_1 = new GridBagConstraints();
						gbc_panel_1.fill = GridBagConstraints.BOTH;
						gbc_panel_1.gridheight = 2;
						gbc_panel_1.gridx = 1;
						gbc_panel_1.gridy = 0;
						panel_4.add(panel_1, gbc_panel_1);
						panel_1.setLayout(new MigLayout("", "[grow,fill]0[]", "[grow,fill]"));
						
								btnSave = new JButton("Save");
								panel_1.add(btnSave, "cell 0 0,alignx left,aligny top, gapright 0");
								btnSave.setIcon(new ImageIcon(
										PDFMetadataEditWindow.class
												.getResource("save-icon.png")));
								
								final BasicArrowButton btnSaveMenu = new BasicArrowButton(BasicArrowButton.SOUTH);
								btnSaveMenu.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										JPopupMenu menu = new JPopupMenu();
										JMenuItem save = menu.add("Save");
										save.addActionListener(saveAction);;
										JMenuItem saveRename = menu.add("Save & rename");
										saveRename.addActionListener(saveRenameAction);
										JMenuItem saveAs = menu.add("Save As ...");
										saveAs.addActionListener(saveAsAction);
										
										int x,y;
										Point pos = btnSaveMenu.getLocationOnScreen();
										x = pos.x;
										y = pos.y + btnSaveMenu.getHeight();

										menu.show(btnSaveMenu, btnSaveMenu.getWidth() - (int)menu.getPreferredSize().getWidth(), btnSaveMenu.getHeight());
										
									}
								});
								panel_1.add(btnSaveMenu, "cell 1 0,growx,aligny center, gapleft 0");
				
						GridBagConstraints gbc_btnCopyDocumentTo = new GridBagConstraints();
						gbc_btnCopyDocumentTo.anchor = GridBagConstraints.NORTH;
						gbc_btnCopyDocumentTo.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnCopyDocumentTo.insets = new Insets(0, 0, 0, 5);
						gbc_btnCopyDocumentTo.gridx = 0;
						gbc_btnCopyDocumentTo.gridy = 1;
						panel_4.add(btnCopyDocumentTo, gbc_btnCopyDocumentTo);
				


		updateSaveButton.run();
		
		java.net.URL imgURL = PDFMetadataEditWindow.class
				.getResource("pdf-metadata-edit.png");
		ImageIcon icoImg = new ImageIcon(imgURL);
		setIconImage(icoImg.getImage());
		setVisible(true);
	}

	public MetadataEditPane createMetadataEditor() {
		return new MetadataEditPane();
	}
	protected JButton getBtnSave() {
		return btnSave;
	}
}
