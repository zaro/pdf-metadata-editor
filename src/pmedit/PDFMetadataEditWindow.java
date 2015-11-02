package pmedit;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.event.ActionEvent;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import com.toedter.calendar.JDateChooser;

import javax.swing.JCheckBox;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.plaf.basic.BasicArrowButton;

import java.awt.FlowLayout;

public class PDFMetadataEditWindow {

	public static interface FieldSetGet {
		public void apply(Object field, FieldID anno);
	}

	final JFileChooser fc = new JFileChooser();

	private File pdfFile;
	private PDDocument document;
	private MetadataInfo metadataInfo;
	private MetadataInfo defaultMetadata = new MetadataInfo();

	private JTextField filename;

	private JFrame frmPdfMetadataEditor;

	private PreferencesWindow preferencesWindow;


	static private Preferences prefs;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		prefs = Preferences.userRoot().node("PDFMetadataEditor");
		final String f;
		if (args.length > 0) {
			f = args[0];
		} else {
			f = null;
		}
		// try {
		// UIManager.setLookAndFeel(
		// UIManager.getSystemLookAndFeelClassName());
		// } catch (ClassNotFoundException e1) {
		// } catch (InstantiationException e1) {
		// } catch (IllegalAccessException e1) {
		// } catch (UnsupportedLookAndFeelException e1) {
		// }
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PDFMetadataEditWindow window = new PDFMetadataEditWindow(f);
					java.net.URL imgURL = PDFMetadataEditWindow.class
							.getResource("pdf-metadata-edit.png");
					ImageIcon img = new ImageIcon(imgURL);
					window.frmPdfMetadataEditor.setIconImage(img.getImage());
					window.frmPdfMetadataEditor.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PDFMetadataEditWindow(String filePath) {
		initialize();
		PdfFilter pdfFilter = new PdfFilter();
		fc.addChoosableFileFilter(pdfFilter);
		fc.setFileFilter(pdfFilter);
		clear();
		if (filePath != null) {
			try {
				pdfFile = new File(filePath);
				loadFile();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(frmPdfMetadataEditor,
						"Error while opening file:\n" + e.toString());
			}
		}
	}


	private void clear() {

		filename.setText("");
		metadataEditor.clear();
	}



	private void loadFile() {
		if (document != null) {
			try {
				document.close();
			} catch (IOException e) {
			}
			document = null;
		}
		clear();
		try {
			document = PDDocument.load(new FileInputStream(pdfFile));
			filename.setText(pdfFile.getAbsolutePath());
			metadataInfo = new MetadataInfo();
			metadataInfo.copyFrom(defaultMetadata);
			metadataInfo.loadFromPDF(document);

			metadataEditor.fillFromMetadata(metadataInfo);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frmPdfMetadataEditor,
					"Error while opening file:\n" + e.toString());
		}
	}

	private void saveFile() {
		try {
			metadataEditor.copyToMetadata(metadataInfo);
			metadataInfo.saveToPDF(document, pdfFile);

			metadataEditor.fillFromMetadata(metadataInfo);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frmPdfMetadataEditor,
					"Error while saving file:\n" + e.toString());
		}
	}
	private MetadataEditPane metadataEditor;

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPdfMetadataEditor = new JFrame();
		frmPdfMetadataEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPdfMetadataEditor.setTitle("PDF Metadata Editor");
		frmPdfMetadataEditor.setBounds(100, 100, 640, 480);
		frmPdfMetadataEditor.setMinimumSize(new Dimension(640, 480));
		frmPdfMetadataEditor.getContentPane()
				.setLayout(
						new MigLayout("insets 5", "[grow,fill]", "[][grow,fill][grow]"));

		JPanel panel = new JPanel();
		frmPdfMetadataEditor.getContentPane().add(panel, "cell 0 0,growx");
		panel.setLayout(new MigLayout("", "[][grow,fill][][]", "[]"));

		JButton btnOpenPdf = new JButton("Open PDF");
		panel.add(btnOpenPdf, "cell 0 0,alignx left,aligny center");

		filename = new JTextField();
		panel.add(filename, "cell 1 0,growx,aligny center");
		filename.setEditable(false);
		filename.setColumns(10);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panel.add(horizontalStrut, "cell 2 0");

		JButton btnPreferences = new JButton("");

		panel.add(btnPreferences, "cell 3 0,aligny center");
		java.net.URL prefImgURL = PDFMetadataEditWindow.class
				.getResource("settings-icon.png");
		ImageIcon img = new ImageIcon(prefImgURL);
		btnPreferences.setIcon(img);

		btnOpenPdf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String dir = prefs.get("LastDir", null);
				if (dir != null) {
					try {
						fc.setCurrentDirectory(new File(dir));
					} catch (Exception e) {
					}
				}
				int returnVal = fc.showOpenDialog(frmPdfMetadataEditor);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					pdfFile = fc.getSelectedFile();
					// This is where a real application would open the file.
					loadFile();
					// save dir as last opened
					prefs.put("LastDir", pdfFile.getParent());
				}
			}
		});
		
		metadataEditor = new MetadataEditPane();
		frmPdfMetadataEditor.getContentPane().add(metadataEditor.tabbedaPane, "cell 0 1,grow");
		

//		metadataEditor = createMetadataEditor();
//		frmPdfMetadataEditor.getContentPane().add(metadataEditor,
//				"cell 0 1,growy");


		JPanel panel_4 = new JPanel();
		frmPdfMetadataEditor.getContentPane().add(panel_4, "cell 0 2,growx");
		panel_4.setLayout(new MigLayout("insets 0", "[grow,fill][grow,fill]", "[][][]"));

		JButton btnCopyBasicTo = new JButton("Copy Basic To XMP");
		btnCopyBasicTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				metadataInfo.copyBasicToXMP();
				metadataEditor.fillFromMetadata(metadataInfo);
			}
		});
		
		JPanel panel_1 = new JPanel();
		panel_4.add(panel_1, "cell 1 0 1 3,grow");
				panel_1.setLayout(new MigLayout("", "[grow,fill]0[]", "[grow,fill]"));
		
				final JButton btnSave = new JButton("Save");
				panel_1.add(btnSave, "cell 0 0,alignx left,aligny top, gapright 0");
				btnSave.setIcon(new ImageIcon(
						PDFMetadataEditWindow.class
								.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
				
				final ActionListener saveAction = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveFile();
					}
				}; 

				final ActionListener saveRenameAction = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveFile();
						String renameTemplate =  prefs.get("renameTemplate", null);
						if(renameTemplate == null){
							return;
						}
						TemplateString ts = new TemplateString(renameTemplate); 
						String toName = ts.process(metadataInfo);
						String toDir= pdfFile.getParent();
						File to = new File(toDir,toName);
						pdfFile.renameTo(to);
					}
				}; 

				final ActionListener saveAsAction = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						final JFileChooser fcSaveAs = new JFileChooser();

						String dir = prefs.get("LastDir", null);
						if (dir != null) {
							try {
								fcSaveAs.setCurrentDirectory(new File(dir));
							} catch (Exception e1) {
							}
						}
						int returnVal = fcSaveAs.showSaveDialog(frmPdfMetadataEditor);

						if (returnVal == JFileChooser.APPROVE_OPTION) {
							pdfFile = fcSaveAs.getSelectedFile();

							saveFile();
							loadFile();

							// save dir as last opened
							prefs.put("LastDir", pdfFile.getParent());
						}
					}
				}; 
				
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
				
		
		JButton button = new JButton("Copy XMP To Basic");
		panel_4.add(button, "cell 0 1");

		panel_4.add(btnCopyBasicTo, "cell 0 2");

		final Runnable updateSaveButton = new Runnable() {
			
			@Override
			public void run() {
				String saveActionS = prefs.get("defaultSaveAction", "save");

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
		
		btnPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (preferencesWindow == null){
					preferencesWindow = new PreferencesWindow(prefs, defaultMetadata, frmPdfMetadataEditor);
					preferencesWindow.onSaveAction(updateSaveButton);
				}
				preferencesWindow.setVisible(true);
			}
		});

		updateSaveButton.run();
	}

	public MetadataEditPane createMetadataEditor() {
		return new MetadataEditPane();
	}
}
