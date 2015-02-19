import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

import javax.swing.Box;
import javax.swing.ScrollPaneConstants;

public class PDFMetadataEditWindow {
	
	public static interface FieldSetGet {
		public void apply(Object field, FieldID anno);
	}

	final JFileChooser fc = new JFileChooser();

	private File pdfFile;
	private PDDocument document;
	private MetadataInfo metadataInfo;

	private JTextField filename;

	private JFrame frmPdfMetadataEditor;
	
	@FieldID("basic.title")
	public JTextField basicTitle;
	@FieldID("basic.author")
	public JTextField basicAuthor;
	@FieldID("basic.subject")
	public JTextArea basicSubject;
	@FieldID(value="basic.keywords")
	public JTextArea basicKeywords;
	@FieldID("basic.creator")
	public JTextField basicCreator;
	@FieldID("basic.producer")
	public JTextField basicProducer;
	@FieldID("basic.trapped")
	public JComboBox basicTrapped;
	@FieldID(value="basic.creationDate", type=FieldID.FieldType.DateField)
	public JDateChooser basicCreationDate;
	@FieldID(value="basic.modificationDate", type=FieldID.FieldType.DateField)
	public JDateChooser basicModificationDate;


	@FieldID("xmpBasic.creatorTool")
	public JTextField xmpBasicCreatorTool;
	@FieldID("xmpBasic.baseURL")
	public JTextField xmpBasicBaseURL;
	@FieldID("xmpBasic.label")
	public JTextField xmpBasicLabel;
	@FieldID("xmpBasic.nickname")
	public JTextField xmpBasicNickname;
	@FieldID(value="xmpBasic.rating", type=FieldID.FieldType.IntField)
	public JTextField xmpBasicRating;
	@FieldID("xmpBasic.title")
	public JTextField xmpBasicTitle;
	@FieldID(value="xmpBasic.identifiers", type=FieldID.FieldType.TextField)
	public JTextArea xmpBasicIdentifiers;
	@FieldID(value="xmpBasic.advisories", type=FieldID.FieldType.TextField)
	public JTextArea xmpBasicAdvisories;
	@FieldID(value="xmpBasic.modifyDate", type=FieldID.FieldType.DateField)
	public JDateChooser xmpBasicModifyDate;
	@FieldID(value="xmpBasic.createDate", type=FieldID.FieldType.DateField)
	public JDateChooser xmpBasicCreateDate;
	@FieldID(value="xmpDc.metadataDate", type=FieldID.FieldType.DateField)
	public JDateChooser xmpBasicMetadataDate;

	@FieldID("xmpPdf.keywords")
	public JTextArea xmpPdfKeywords;
	@FieldID("xmpPdf.version")
	public JTextField xmpPdfVersion;
	@FieldID("xmpPdf.producer")
	public JTextField xmpPdfProducer;
	
	@FieldID("xmpDc.title")
	public JTextField xmpDcTitle;
	@FieldID("xmpDc.coverage")
	public JTextField xmpDcCoverage;
	@FieldID("xmpDc.description")
	public JTextField xmpDcDescription;
	@FieldID("xmpDc.dates")
	public JTextField xmpDcDates;
	@FieldID("xmpDc.format")
	public JTextField xmpDcFormat;
	@FieldID("xmpDc.identifier")
	public JTextField xmpDcIdentifier;
	@FieldID("xmpDc.rights")
	public JTextField xmpDcRights;
	@FieldID("xmpDc.source")
	public JTextField xmpDcSource;
	@FieldID(value="xmpDc.creators", type=FieldID.FieldType.TextField)
	public JTextArea xmpDcCreators;
	@FieldID(value="xmpDc.contributors", type=FieldID.FieldType.TextField)
	public JTextArea xmpDcContributors;
	@FieldID(value="xmpDc.languages", type=FieldID.FieldType.TextField)
	public JTextArea xmpDcLanguages;
	@FieldID(value="xmpDc.publishers", type=FieldID.FieldType.TextField)
	public JTextArea xmpDcPublishers;
	@FieldID(value="xmpDc.relationships", type=FieldID.FieldType.TextField)
	public JTextArea xmpDcRelationships;
	@FieldID(value="xmpDc.subjects", type=FieldID.FieldType.TextField)
	public JTextArea xmpDcSubjects;
	@FieldID(value="xmpDc.types", type=FieldID.FieldType.TextField)
	public JTextArea xmpDcTypes;

	private JPanel basicMetaPanel;

	static private Preferences prefs;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		prefs = Preferences.userRoot().node("PDFMetadataEditor");
		final String f;
		if( args.length > 0 ){
			f = args[0];
		} else {
			f = null;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PDFMetadataEditWindow window = new PDFMetadataEditWindow(f);
					java.net.URL imgURL = PDFMetadataEditWindow.class.getResource("pdf-metadata-edit.png");
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

	private <T> T formatItem(T s) {
		return s;
	}

//	private String formatItem(Calendar date) {
//		return DateFormat.getInstance().format(date.getTime());
//	}

	private <T> String itemListToText(List<T> slist, String separator) {
		if (slist == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < slist.size(); ++i) {
			b.append(formatItem(slist.get(i)));
			if (i < (slist.size() - 1)) {
				b.append(separator);
			}
		}
		return b.toString();
	}

	private String stringListToText(List<String> slist) {
		return itemListToText(slist, "\n");
	}

	private void clear() {

		filename.setText("");
		traverseFields(new FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {
        		if(field instanceof JTextField){
        			objectToField( (JTextField)field , null);
        		}
        		if(field instanceof JTextArea){
        			objectToField( (JTextArea)field , null);
        		}
        		if(field instanceof JComboBox){
        			objectToField( (JComboBox)field , null);
        		}
        		if(field instanceof JDateChooser){
        			objectToField( (JDateChooser)field , null);
        		}
			}
		});
	}
	
	private void objectToField(JTextField field, Object o) {
		if(o instanceof String){
			field.setText( (String)o );
		} else if(o == null){
			field.setText( "" );
		} else {
			throw new RuntimeException("Cannot store non-String object in JTextField");
		}
	}

	private void objectToField(JTextArea field, Object o) {
		if(o instanceof String){
			field.setText( (String)o );
		} else if(o instanceof List<?>){
			field.setText(stringListToText((List<String>)o));
		} else if(o == null){
			field.setText( "" );
		} else {
			throw new RuntimeException("Cannot store non-String/List<String> object in JTextArea");
		}
	}

	private void objectToField(JComboBox field, Object o) {
		if(o instanceof String){
			field.getModel().setSelectedItem(o);
		} else if(o == null){
			field.setSelectedIndex(-1);
		} else {
			throw new RuntimeException("Cannot store non-String object in JComboBox");
		}
	}

	private void objectToField(JDateChooser field, Object o) {
		if(o instanceof Calendar){
			field.setCalendar((Calendar)o);
		} else if(o == null){
			field.setCalendar(null);
		} else {
			throw new RuntimeException("Cannot store non-Calendar object in JDateChooser");
		}
	}

	private void traverseFields(FieldSetGet setGet) {
		for (Field field : this.getClass().getFields()) {
			FieldID annos = field.getAnnotation(FieldID.class);
            if (annos != null) {
            	if( annos.value() != null && annos.value().length() > 0 ) {
            		Object f = null;
					try {
						f = field.get(this);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
					setGet.apply(f, annos);
            	}
            }
        }
	}
	
	private void fillFromMetadata() {

		traverseFields(new FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {
            	Object value = metadataInfo.get(anno.value());

        		if(field instanceof JTextField){
        			objectToField( (JTextField)field , value);
        		}
        		if(field instanceof JTextArea){
        			objectToField( (JTextArea)field , value);
        		}
        		if(field instanceof JComboBox){
        			objectToField( (JComboBox)field , value);
        		}
        		if(field instanceof JDateChooser){
        			objectToField( (JDateChooser)field , value);
        		}
			}
		});
		
	}

	private void copyToMetadata() {

		traverseFields(new FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {

        		if(field instanceof JTextField || field instanceof JTextArea){
        			String text = (field instanceof JTextField) ? ((JTextField)field).getText() : ((JTextArea)field).getText();
        			switch(anno.type()){
        			case StringField:
        				metadataInfo.set(anno.value(), text);
        				break;
        			case TextField:
        				metadataInfo.set(anno.value(),Arrays.asList(text.split("\n")));
        				break;
        			case IntField:
        				Integer i = null;
        				if(text!= null && text.length() > 0){
        					i = Integer.parseInt(text);
        				}
        				metadataInfo.set(anno.value(),i);
        				break;
        			default:
        				throw new RuntimeException("Cannot store text in :" +anno.type());

        			}
        		}
        		if(field instanceof JComboBox){
        			String text = (String)((JComboBox)field).getModel().getSelectedItem();
        			switch(anno.type()){
        			case StringField:
        				metadataInfo.set(anno.value(), text);
        				break;
        			default:
        				throw new RuntimeException("Cannot (store (choice text) in :" +anno.type());

        			}
        		}
        		if(field instanceof JDateChooser){
        			switch(anno.type()){
        			case DateField:
        				metadataInfo.set(anno.value(), ((JDateChooser)field).getCalendar());
        				break;
        			default:
        				throw new RuntimeException("Cannot store Calendar in :" +anno.type());

        			}
        		}
			}
		});
		
	}
	
	private void loadFile() {
		if(document != null) {
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
			metadataInfo.loadFromPDF(document);
			
			fillFromMetadata();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frmPdfMetadataEditor,
					"Error while opening file:\n" + e.toString());
		}
	}

	private void saveFile() {
		if(onsaveCopyBasicTo.isSelected()){
			metadataInfo.copyBasicToXMP();
		}
		if(onsaveCopyXmpTo.isSelected()){
			metadataInfo.copyXMPToBasic();;
		}
		try {
			metadataInfo = new MetadataInfo();
			copyToMetadata();
			metadataInfo.saveToPDF(document,pdfFile);

			fillFromMetadata();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frmPdfMetadataEditor,
					"Error while saving file:\n" + e.toString());
		}
	}

	
	private JCheckBox onsaveCopyBasicTo;
	private JCheckBox onsaveCopyXmpTo;
	private JTabbedPane metadataEditor;
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPdfMetadataEditor = new JFrame();
		frmPdfMetadataEditor.setTitle("PDF Metadata Editor");
		frmPdfMetadataEditor.setBounds(100, 100, 640, 480);
		frmPdfMetadataEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPdfMetadataEditor.getContentPane().setLayout(
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
						java.net.URL prefImgURL = PDFMetadataEditWindow.class.getResource("settings-icon.png");
						ImageIcon img = new ImageIcon(prefImgURL);
						btnPreferences.setIcon(img);

						btnOpenPdf.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						String dir = prefs.get("LastDir",null);
						if(dir != null){
							try {
								fc.setCurrentDirectory(new File(dir));
							} catch (Exception e){
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

		metadataEditor = new JTabbedPane(JTabbedPane.TOP);
		frmPdfMetadataEditor.getContentPane().add(metadataEditor,
				"cell 0 1,grow");

		JScrollPane basicScrollpane = new JScrollPane();
		metadataEditor.addTab("Basic Metadata", null, basicScrollpane, null);
		
		basicMetaPanel = new JPanel();
		basicScrollpane.setViewportView(basicMetaPanel);
		basicMetaPanel.setLayout(new MigLayout("", "[][grow,fill]", "[][][][][][][][][][][]"));

		JLabel lblTitle = new JLabel("Title");
		basicMetaPanel.add(lblTitle, "cell 0 0,alignx trailing");

		basicTitle = new JTextField();
		basicMetaPanel.add(basicTitle, "cell 1 0,growx");
		basicTitle.setColumns(10);

		JLabel lblNewLabel = new JLabel("Author");
		basicMetaPanel.add(lblNewLabel, "cell 0 1,alignx trailing");

		basicAuthor = new JTextField();
		basicMetaPanel.add(basicAuthor, "cell 1 1,growx");
		basicAuthor.setColumns(10);

		JLabel lblSubject = new JLabel("Subject");
		basicMetaPanel.add(lblSubject, "cell 0 2,alignx trailing");

		basicSubject = new JTextArea();
		basicSubject.setWrapStyleWord(true);
		basicMetaPanel.add(basicSubject, "cell 1 2,growx");
		basicSubject.setColumns(10);

		JLabel lblKeywords = new JLabel("Keywords");
		basicMetaPanel.add(lblKeywords, "cell 0 3,alignx trailing");

		basicKeywords = new JTextArea();
		basicKeywords.setWrapStyleWord(true);
		basicKeywords.setLineWrap(true);
		basicMetaPanel.add(basicKeywords, "cell 1 3,growx");
		basicKeywords.setColumns(10);

		JLabel lblCreator = new JLabel("Creator");
		basicMetaPanel.add(lblCreator, "cell 0 4,alignx trailing");

		basicCreator = new JTextField();
		basicMetaPanel.add(basicCreator, "cell 1 4,growx");
		basicCreator.setColumns(10);

		JLabel lblProducer = new JLabel("Producer");
		basicMetaPanel.add(lblProducer, "cell 0 5,alignx trailing");

		basicProducer = new JTextField();
		basicMetaPanel.add(basicProducer, "cell 1 5,growx");
		basicProducer.setColumns(10);

		JLabel lblCreationDate = new JLabel("Creation Date");
		basicMetaPanel.add(lblCreationDate, "cell 0 6,alignx trailing");
		
		basicCreationDate = new JDateChooser();
		basicMetaPanel.add(basicCreationDate, "cell 1 6,grow");

		JLabel lblModificationDate = new JLabel("Modification Date");
		basicMetaPanel.add(lblModificationDate, "cell 0 7,alignx trailing");
		
		basicModificationDate = new JDateChooser();
		basicMetaPanel.add(basicModificationDate, "cell 1 7,grow");

		JLabel lblTrapped = new JLabel("Trapped");
		basicMetaPanel.add(lblTrapped, "cell 0 8,alignx trailing");

		basicTrapped = new JComboBox();
		basicTrapped.setModel(new DefaultComboBoxModel(new String[] { "True",
				"False", "Unknown" }));
		basicMetaPanel.add(basicTrapped, "cell 1 8");

		JScrollPane xmpBasicScrollpane = new JScrollPane();
		metadataEditor.addTab("XMP Basic", null, xmpBasicScrollpane, null);

		JPanel panel_1 = new JPanel();
		xmpBasicScrollpane.setViewportView(panel_1);
		panel_1.setLayout(new MigLayout("", "[][grow,fill]", "[][][][][][][][][grow][grow][]"));

		JLabel lblCreatorTool = new JLabel("Creator tool");
		panel_1.add(lblCreatorTool, "cell 0 0,alignx trailing");

		xmpBasicCreatorTool = new JTextField();
		panel_1.add(xmpBasicCreatorTool, "cell 1 0,growx");
		xmpBasicCreatorTool.setColumns(10);

		JLabel lblCreateDate = new JLabel("Create Date");
		panel_1.add(lblCreateDate, "cell 0 1,alignx trailing");
		
		xmpBasicCreateDate = new JDateChooser();
		panel_1.add(xmpBasicCreateDate, "cell 1 1,grow");

		JLabel lblModifyDate = new JLabel("Modify Date");
		panel_1.add(lblModifyDate, "cell 0 2,alignx trailing");
		
		xmpBasicModifyDate = new JDateChooser();
		panel_1.add(xmpBasicModifyDate, "cell 1 2,grow");

		JLabel lblTitle_1 = new JLabel("Title");
		panel_1.add(lblTitle_1, "cell 0 3,alignx trailing");

		xmpBasicTitle = new JTextField();
		panel_1.add(xmpBasicTitle, "cell 1 3,growx");
		xmpBasicTitle.setColumns(10);

		JLabel lblBaseUrl = new JLabel("Base URL");
		panel_1.add(lblBaseUrl, "cell 0 4,alignx trailing");

		xmpBasicBaseURL = new JTextField();
		panel_1.add(xmpBasicBaseURL, "cell 1 4,growx");
		xmpBasicBaseURL.setColumns(10);

		JLabel lblRating = new JLabel("Rating");
		panel_1.add(lblRating, "cell 0 5,alignx trailing");

		xmpBasicRating = new JTextField();
		panel_1.add(xmpBasicRating, "cell 1 5,growx");
		xmpBasicRating.setColumns(10);

		JLabel lblLabel = new JLabel("Label");
		panel_1.add(lblLabel, "cell 0 6,alignx trailing");

		xmpBasicLabel = new JTextField();
		panel_1.add(xmpBasicLabel, "cell 1 6,growx");
		xmpBasicLabel.setColumns(10);

		JLabel lblNickname = new JLabel("Nickname");
		panel_1.add(lblNickname, "cell 0 7,alignx trailing");

		xmpBasicNickname = new JTextField();
		panel_1.add(xmpBasicNickname, "cell 1 7,growx");
		xmpBasicNickname.setColumns(10);

		JLabel label_1 = new JLabel("Identifiers");
		panel_1.add(label_1, "cell 0 8,alignx trailing");

		xmpBasicIdentifiers = new JTextArea();
		panel_1.add(xmpBasicIdentifiers, "cell 1 8,grow");

		JLabel label = new JLabel("Advisories");
		panel_1.add(label, "cell 0 9,alignx trailing");

		xmpBasicAdvisories = new JTextArea();
		panel_1.add(xmpBasicAdvisories, "cell 1 9,grow");

		JLabel lblMetadataDate = new JLabel("Metadata Date");
		panel_1.add(lblMetadataDate, "cell 0 10,alignx trailing");
		
		xmpBasicMetadataDate = new JDateChooser();
		panel_1.add(xmpBasicMetadataDate, "cell 1 10,grow");

		JScrollPane xmpPdfScrollpane = new JScrollPane();
		metadataEditor.addTab("XMP PDF", null, xmpPdfScrollpane, null);

		JPanel panel_2 = new JPanel();
		xmpPdfScrollpane.setViewportView(panel_2);
		panel_2.setLayout(new MigLayout("", "[][grow,fill]", "[][][]"));

		JLabel lblKeywords_1 = new JLabel("Keywords");
		panel_2.add(lblKeywords_1, "cell 0 0,alignx trailing");

		xmpPdfKeywords = new JTextArea();
		xmpPdfKeywords.setWrapStyleWord(true);
		xmpPdfKeywords.setLineWrap(true);
		panel_2.add(xmpPdfKeywords, "cell 1 0,growx");
		xmpPdfKeywords.setColumns(10);

		JLabel lblPdfVersion = new JLabel("PDF Version");
		panel_2.add(lblPdfVersion, "cell 0 1,alignx trailing");

		xmpPdfVersion = new JTextField();
		xmpPdfVersion.setEditable(false);
		panel_2.add(xmpPdfVersion, "cell 1 1,growx");
		xmpPdfVersion.setColumns(10);

		JLabel lblProducer_1 = new JLabel("Producer");
		panel_2.add(lblProducer_1, "cell 0 2,alignx trailing");

		xmpPdfProducer = new JTextField();
		panel_2.add(xmpPdfProducer, "cell 1 2,growx");
		xmpPdfProducer.setColumns(10);

		JScrollPane xmpDcScrollpane = new JScrollPane();
		metadataEditor.addTab("XMP Dublin Core", null, xmpDcScrollpane, null);

		JPanel panel_3 = new JPanel();
		xmpDcScrollpane.setViewportView(panel_3);
		panel_3.setLayout(new MigLayout("", "[][grow,fill]",
				"[][][grow][grow][][][][][grow][grow][grow][][][grow][grow]"));

		JLabel lblTitle_2 = new JLabel("Title");
		panel_3.add(lblTitle_2, "cell 0 0,alignx trailing");

		xmpDcTitle = new JTextField();
		panel_3.add(xmpDcTitle, "cell 1 0,growx");
		xmpDcTitle.setColumns(10);

		JLabel lblCreators = new JLabel("Creators");
		panel_3.add(lblCreators, "cell 0 2,alignx trailing");

		xmpDcCreators = new JTextArea();
		panel_3.add(xmpDcCreators, "cell 1 2,grow");

		JLabel lblContributors = new JLabel("Contributors");
		panel_3.add(lblContributors, "cell 0 3,alignx trailing");

		xmpDcContributors = new JTextArea();
		panel_3.add(xmpDcContributors, "cell 1 3,grow");

		JLabel lblCoverage = new JLabel("Coverage");
		panel_3.add(lblCoverage, "cell 0 4,alignx trailing");

		xmpDcCoverage = new JTextField();
		panel_3.add(xmpDcCoverage, "cell 1 4,growx");
		xmpDcCoverage.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		panel_3.add(lblDescription, "cell 0 1,alignx trailing");

		xmpDcDescription = new JTextField();
		panel_3.add(xmpDcDescription, "cell 1 1,growx");
		xmpDcDescription.setColumns(10);

		JLabel lblDates = new JLabel("Dates");
		panel_3.add(lblDates, "cell 0 5,alignx trailing");

		xmpDcDates = new JTextField();
		xmpDcDates.setEditable(false);
		panel_3.add(xmpDcDates, "cell 1 5,growx");
		xmpDcDates.setColumns(10);

		JLabel lblFormat = new JLabel("Format");
		panel_3.add(lblFormat, "cell 0 6,alignx trailing");

		xmpDcFormat = new JTextField();
		panel_3.add(xmpDcFormat, "cell 1 6,growx");
		xmpDcFormat.setColumns(10);

		JLabel lblIdentifier = new JLabel("Identifier");
		panel_3.add(lblIdentifier, "cell 0 7,alignx trailing");

		xmpDcIdentifier = new JTextField();
		panel_3.add(xmpDcIdentifier, "cell 1 7,growx");
		xmpDcIdentifier.setColumns(10);

		JLabel lblLanguages = new JLabel("Languages");
		panel_3.add(lblLanguages, "cell 0 8,alignx trailing");

		xmpDcLanguages = new JTextArea();
		panel_3.add(xmpDcLanguages, "cell 1 8,grow");

		JLabel lblPublishers = new JLabel("Publishers");
		panel_3.add(lblPublishers, "cell 0 9,alignx trailing");

		xmpDcPublishers = new JTextArea();
		panel_3.add(xmpDcPublishers, "cell 1 9,grow");

		JLabel lblRelationships = new JLabel("Relationships");
		panel_3.add(lblRelationships, "cell 0 10,alignx trailing");

		xmpDcRelationships = new JTextArea();
		panel_3.add(xmpDcRelationships, "cell 1 10,grow");

		JLabel lblRights = new JLabel("Rights");
		panel_3.add(lblRights, "cell 0 11,alignx trailing");

		xmpDcRights = new JTextField();
		panel_3.add(xmpDcRights, "cell 1 11,growx");
		xmpDcRights.setColumns(10);

		JLabel lblSource = new JLabel("Source");
		panel_3.add(lblSource, "cell 0 12,alignx trailing");

		xmpDcSource = new JTextField();
		panel_3.add(xmpDcSource, "cell 1 12,growx");
		xmpDcSource.setColumns(10);

		JLabel lblSubjects = new JLabel("Subjects");
		panel_3.add(lblSubjects, "cell 0 13,alignx trailing");

		xmpDcSubjects = new JTextArea();
		panel_3.add(xmpDcSubjects, "cell 1 13,grow");

		JLabel lblTypes = new JLabel("Types");
		panel_3.add(lblTypes, "cell 0 14,alignx trailing");

		xmpDcTypes = new JTextArea();
		panel_3.add(xmpDcTypes, "cell 1 14,grow");

		JPanel panel_4 = new JPanel();
		frmPdfMetadataEditor.getContentPane().add(panel_4, "cell 0 2,growx");
		panel_4.setLayout(new MigLayout("insets 0", "[grow,fill][grow,fill][grow,fill]", "[][]"));

		JButton btnCopyBasicTo = new JButton("Copy Basic To XMP");
		btnCopyBasicTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				metadataInfo.copyBasicToXMP();
				fillFromMetadata();
			}
		});
		
		onsaveCopyBasicTo = new JCheckBox("Copy Basic To XMP on Save");
		panel_4.add(onsaveCopyBasicTo, "cell 0 0");
		onsaveCopyXmpTo = new JCheckBox("Copy XMP To Basic on Save");
		panel_4.add(onsaveCopyXmpTo, "cell 1 0");
		onsaveCopyBasicTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(onsaveCopyBasicTo.isSelected()){
					onsaveCopyXmpTo.setSelected(false);
				}
				prefs.putBoolean("onsaveCopyXmpTo", onsaveCopyXmpTo.isSelected());
				prefs.putBoolean("onsaveCopyBasicTo", onsaveCopyBasicTo.isSelected());
			}
		});
		onsaveCopyXmpTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(onsaveCopyXmpTo.isSelected()){
					onsaveCopyBasicTo.setSelected(false);
				}
				prefs.putBoolean("onsaveCopyXmpTo", onsaveCopyXmpTo.isSelected());
				prefs.putBoolean("onsaveCopyBasicTo", onsaveCopyBasicTo.isSelected());
			}
		});
		onsaveCopyXmpTo.setSelected(prefs.getBoolean("onsaveCopyXmpTo", false));
		onsaveCopyBasicTo.setSelected(prefs.getBoolean("onsaveCopyBasicTo", false));

		panel_4.add(btnCopyBasicTo, "cell 0 1");

		JButton btnCopyXmpTo = new JButton("Copy XMP To Basic");
		btnCopyXmpTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				metadataInfo.copyXMPToBasic();
				fillFromMetadata();
			}
		});
		panel_4.add(btnCopyXmpTo, "cell 1 1");

		JButton btnSave = new JButton("Save");
		btnSave.setIcon(new ImageIcon(PDFMetadataEditWindow.class.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		panel_4.add(btnSave, "cell 2 0 1 2,grow");
	}
	public JTabbedPane getMetadataEditor() {
		return metadataEditor;
	}
}
