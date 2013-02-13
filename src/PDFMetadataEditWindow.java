import java.awt.EventQueue;
import java.awt.Image;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import com.toedter.calendar.JDateChooser;
import java.awt.Toolkit;

public class PDFMetadataEditWindow {

	private JFrame frmPdfMetadataEditor;
	private JTextField bTitle;
	private JTextField bAuthor;
	private JTextField bSubject;
	private JTextArea bKeywords;
	private JTextField bCreator;
	private JTextField bProducer;
	final JFileChooser fc = new JFileChooser();

	private File pdfFile;
	private JTextField filename;
	private JTextField xmpBasicCreatorTool;
	private JTextField xmpBasicBaseURL;
	private JTextField xmpBasicLabel;
	private JTextField xmpBasicNickname;
	private JTextField xmpBasicRating;
	private JTextField xmpBasicTitle;
	private JTextArea xmpPdfKeywords;
	private JTextField xmpPdfVersion;
	private JTextField xmpPdfProducer;
	private JTextField xmpDcTitle;
	private JTextField xmpDcCoverage;
	private JTextField xmpDcDescription;
	private JTextField xmpDcDates;
	private JTextField xmpDcFormat;
	private JTextField xmpDcIdentifier;
	private JTextField xmpDcRights;
	private JTextField xmpDcSource;
	private JTextArea xmpBasicIdentifiers;
	private JTextArea xmpBasicAdvisories;
	private JTextArea xmpDcCreators;
	private JTextArea xmpDcContributors;
	private JTextArea xmpDcLanguages;
	private JTextArea xmpDcPublishers;
	private JTextArea xmpDcRelationships;
	private JTextArea xmpDcSubjects;
	private JTextArea xmpDcTypes;
	private JComboBox bTrapped;
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

	private void setSlist(JTextArea field, List<String> slist) {
		field.setText(stringListToText(slist));
	}

	private List<String> getSlist(JTextArea field) {
		return Arrays.asList(field.getText().split("\n"));
	}

	private void setDate(JDateChooser field, Calendar date) {
		field.setCalendar(date);
	}
	
	private Calendar getDate(JDateChooser field) {
		return field.getCalendar();
	}

	private void clear() {
		bTitle.setText("");
		bAuthor.setText("");
		bSubject.setText("");
		bKeywords.setText("");
		bCreator.setText("");
		bProducer.setText("");

		filename.setText("");
		xmpBasicCreatorTool.setText("");
		xmpBasicBaseURL.setText("");
		xmpBasicLabel.setText("");
		xmpBasicNickname.setText("");
		xmpBasicRating.setText("");
		xmpBasicTitle.setText("");
		xmpPdfKeywords.setText("");
		xmpPdfVersion.setText("");
		xmpPdfProducer.setText("");
		xmpDcTitle.setText("");
		xmpDcCoverage.setText("");
		xmpDcDescription.setText("");
		xmpDcDates.setText("");
		xmpDcFormat.setText("");
		xmpDcIdentifier.setText("");
		xmpDcRights.setText("");
		xmpDcSource.setText("");
		xmpBasicIdentifiers.setText("");
		xmpBasicAdvisories.setText("");
		xmpDcCreators.setText("");
		xmpDcContributors.setText("");
		xmpDcLanguages.setText("");
		xmpDcPublishers.setText("");
		xmpDcRelationships.setText("");
		xmpDcSubjects.setText("");
		xmpDcTypes.setText("");
		bTrapped.setSelectedIndex(-1);
	}
	
	private void loadFile() {
		PDDocument document = null;
		clear();
		try {
			document = PDDocument.load(new FileInputStream(pdfFile));
			PDDocumentInformation info = document.getDocumentInformation();
			// If we succeeded so far , most probably it is a valid PDF
			// so show it as current filename
			filename.setText(pdfFile.getAbsolutePath());
			// Basic info
			bTitle.setText(info.getTitle());
			bAuthor.setText(info.getAuthor());
			bSubject.setText(info.getSubject());
			bKeywords.setText(info.getKeywords());
			bCreator.setText(info.getCreator());
			bProducer.setText(info.getProducer());
			setDate(bCreationDate, info.getCreationDate());
			setDate(bModificationDate, info.getModificationDate());
			bTrapped.getModel().setSelectedItem(info.getTrapped());
			// XMP
			PDDocumentCatalog catalog = document.getDocumentCatalog();
			PDMetadata metadata = catalog.getMetadata();
			if (metadata == null) {
				document.close();
				return;
			}
			// XMP Basic
			XMPMetadata xmp = XMPMetadata.load(metadata.createInputStream());
			XMPSchemaBasic bi = xmp.getBasicSchema();
			if (bi != null) {
				xmpBasicAdvisories
						.setText(stringListToText(bi.getAdvisories()));
				xmpBasicBaseURL.setText(bi.getBaseURL());
				setDate(xmpBasicCreateDate, bi.getCreateDate());
				setDate(xmpBasicModifyDate, bi.getModifyDate());
				xmpBasicCreatorTool.setText(bi.getCreatorTool());
				xmpBasicIdentifiers.setText(stringListToText(bi
						.getIdentifiers()));
				xmpBasicLabel.setText(bi.getLabel());
				setDate(xmpBasicMetadataDate, bi.getMetadataDate());
				xmpBasicNickname.setText(bi.getNickname());
				if (bi.getRating() != null) {
					xmpBasicRating.setText(String.format("%i", bi.getRating()));
				} else {
					xmpBasicRating.setText(null);
				}
				xmpBasicTitle.setText(bi.getTitle());
			}
			// XMP PDF
			XMPSchemaPDF pi = xmp.getPDFSchema();
			if (pi != null) {
				xmpPdfVersion.setText(pi.getPDFVersion());
				xmpPdfKeywords.setText(pi.getKeywords());
				xmpPdfProducer.setText(pi.getProducer());
			}
			// XMP Dublin Core
			XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
			if (dc != null) {
				xmpDcTitle.setText(dc.getTitle());
				setSlist(xmpDcContributors, dc.getContributors());
				setSlist(xmpDcPublishers, dc.getPublishers());
				setSlist(xmpDcRelationships, dc.getRelationships());
				setSlist(xmpDcSubjects, dc.getSubjects());
				setSlist(xmpDcTypes, dc.getTypes());
				setSlist(xmpDcLanguages, dc.getLanguages());
				setSlist(xmpDcCreators, dc.getCreators());
				xmpDcCoverage.setText(dc.getCoverage());
				xmpDcFormat.setText(dc.getFormat());
				xmpDcIdentifier.setText(dc.getIdentifier());
				xmpDcRights.setText(dc.getRights());
				xmpDcSource.setText(dc.getSource());
				xmpDcDescription.setText(dc.getDescription());
				xmpDcDates.setText(itemListToText(dc.getDates(), ","));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frmPdfMetadataEditor,
					"Error while opening file:\n" + e.toString());
		}
		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}
	}

	private String getField(JTextField field){
		if(field.getText().length()>0){
			return field.getText();
		}
		return null;
	}
	private String getField(JTextArea field){
		if(field.getText().length()>0){
			return field.getText();
		}
		return null;
	}
	private void saveFile() {
		PDDocument document = null;
		try {
			document = PDDocument.load(new FileInputStream(pdfFile));
			// Basic info
			PDDocumentInformation info = document.getDocumentInformation();
			info.setTitle(getField(bTitle));
			info.setAuthor(getField(bAuthor));
			info.setSubject(getField(bSubject));
			info.setKeywords(getField(bKeywords));
			info.setCreator(getField(bCreator));
			info.setProducer(getField(bProducer));
			info.setCreationDate(getDate(bCreationDate));
			info.setModificationDate(getDate(bModificationDate));
			info.setTrapped((String) bTrapped.getModel().getSelectedItem());
			document.setDocumentInformation(info);
			// XMP
			PDDocumentCatalog catalog = document.getDocumentCatalog();
			PDMetadata metadata = catalog.getMetadata();
			XMPMetadata xmp = null;
			if (metadata != null) {
				xmp = XMPMetadata.load(metadata.createInputStream());
			}
			if (xmp == null) {
				xmp = new XMPMetadata();
			}
			// XMP Basic
			XMPSchemaBasic bi = xmp.getBasicSchema();
			if (bi == null) {
				bi = xmp.addBasicSchema();
			}
			if (bi.getAdvisories() != null) {
				for (String a : bi.getAdvisories()) {
					bi.removeAdvisory(a);
				}
			}
			if (xmpBasicAdvisories.getText().length() > 0) {
				for (String a : getSlist(xmpBasicAdvisories)) {
					bi.addAdvisory(a);
				}
			}
			bi.setBaseURL(getField(xmpBasicBaseURL));
			if(getDate(xmpBasicCreateDate) != null){
				bi.setCreateDate(getDate(xmpBasicCreateDate));
			}
			if(getDate(xmpBasicModifyDate) != null){
				bi.setModifyDate(getDate(xmpBasicModifyDate));
			}
			bi.setCreatorTool(getField(xmpBasicCreatorTool));
			if (bi.getIdentifiers() != null) {
				for (String i : bi.getIdentifiers()) {
					bi.removeIdentifier(i);
				}
			}
			if (xmpBasicIdentifiers.getText().length() > 0) {
				for (String i : getSlist(xmpBasicIdentifiers)) {
					bi.addIdentifier(i);
				}
			}
			bi.setLabel(getField(xmpBasicLabel));
			if(getDate(xmpBasicMetadataDate)!=null){
				bi.setMetadataDate(getDate(xmpBasicMetadataDate));
			}
			bi.setNickname(getField(xmpBasicNickname));
			if(xmpBasicRating.getText().length()>0){
				bi.setRating(Integer.parseInt(xmpBasicRating.getText()));
			}else{
				bi.setRating(null);
			}
			bi.setTitle(getField(xmpBasicTitle));
			// XMP PDF
			XMPSchemaPDF pi = xmp.getPDFSchema();
			if (pi == null) {
				pi = xmp.addPDFSchema();
			}
			pi.setKeywords(getField(xmpPdfKeywords));
			pi.setProducer(getField(xmpPdfProducer));
			// XMP Dublin Core
			XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
			if (dc == null) {
				dc = xmp.addDublinCoreSchema();
			}
			dc.setTitle(getField(xmpDcTitle));
			if (dc.getContributors() != null) {
				for (String i : dc.getContributors()) {
					dc.removeContributor(i);
				}
			}
			if (xmpDcContributors.getText().length() > 0) {
				for (String i : getSlist(xmpDcContributors)) {
					dc.addContributor(i);
				}
			}
			//
			if (dc.getPublishers() != null) {
				for (String i : dc.getPublishers()) {
					dc.removePublisher(i);
				}
			}
			if (xmpDcPublishers.getText().length() > 0) {
				for (String i : getSlist(xmpDcPublishers)) {
					dc.addPublisher(i);
				}
			}
			//
			if (dc.getRelationships() != null) {
				for (String i : dc.getRelationships()) {
					dc.removeRelation(i);
				}
			}
			if (xmpDcRelationships.getText().length() > 0) {
				for (String i : getSlist(xmpDcRelationships)) {
					dc.addRelation(i);
				}
			}
			//
			if (dc.getSubjects() != null) {
				for (String i : dc.getSubjects()) {
					dc.removeSubject(i);
				}
			}
			if (xmpDcSubjects.getText().length() > 0) {
				for (String i : getSlist(xmpDcSubjects)) {
					dc.addSubject(i);
				}
			}
			//
			// for(String i: dc.getTypes()){
			// dc.removeType(i);
			// }
			if (xmpDcTypes.getText().length() > 0) {
				for (String i : getSlist(xmpDcTypes)) {
					dc.addType(i);
				}
			}
			//
			if (dc.getLanguages() != null) {
				for (String i : dc.getLanguages()) {
					dc.removeLanguage(i);
				}
			}
			if (xmpDcLanguages.getText().length() > 0) {
				for (String i : getSlist(xmpDcLanguages)) {
					dc.addLanguage(i);
				}
			}
			//
			if (dc.getCreators() != null) {
				for (String i : dc.getCreators()) {
					dc.removeCreator(i);
				}
			}
			if (xmpDcCreators.getText().length() > 0) {
				for (String i : getSlist(xmpDcCreators)) {
					dc.addCreator(i);
				}
			}
			//
			dc.setCoverage(getField(xmpDcCoverage));
			dc.setFormat(getField(xmpDcFormat));
			dc.setIdentifier(getField(xmpDcIdentifier));
			dc.setRights(getField(xmpDcRights));
			dc.setSource(getField(xmpDcSource));
			dc.setDescription(getField(xmpDcDescription));
			// xmpDcDates.setText(itemListToText(dc.getDates(),","));

			// Do the save
			PDMetadata metadataStream = new PDMetadata(document);
			metadataStream.importXMPMetadata(xmp);
			catalog.setMetadata(metadataStream);
			document.save(pdfFile.getAbsolutePath());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frmPdfMetadataEditor,
					"Error while saving file:\n" + e.toString());
		}
		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}
	}

	private void copyField(JTextField from, JTextField to) {
		if (from.getText().length() > 0) {
			to.setText(from.getText());
		}

	}

	private void copyField(JTextField from, JTextArea to) {
		if (from.getText().length() > 0) {
			to.setText(from.getText());
		}

	}

	private void copyField(JTextArea from, JTextField to) {
		if (from.getText().length() > 0) {
			to.setText(from.getText().replace("\n", ","));
		}

	}
	private void copyField(JTextArea from, JTextArea to) {
		if (from.getText().length() > 0) {
			to.setText(from.getText());
		}

	}

	private void copyBasicToXMP() {
		copyField(bKeywords, xmpPdfKeywords);
		copyField(bProducer, xmpPdfProducer);

		copyField(bCreator, xmpBasicCreatorTool);

		copyField(bTitle, xmpDcTitle);
		copyField(bSubject, xmpDcDescription);
		copyField(bAuthor, xmpDcCreators);
	}

	private void copyXMPToBasic() {
		copyField(xmpPdfKeywords, bKeywords);
		copyField(xmpPdfProducer, bProducer);

		copyField(xmpBasicCreatorTool, bCreator);

		copyField(xmpDcTitle, bTitle);
		copyField(xmpDcDescription, bSubject);
		copyField(xmpDcCreators, bAuthor);

	}
	
	private List<JTextField>  customFieldsNames = new ArrayList<JTextField>();
	private List<JTextField>  customFieldsValues = new ArrayList<JTextField>();
	private JDateChooser xmpBasicModifyDate;
	private JDateChooser xmpBasicCreateDate;
	private JDateChooser bCreationDate;
	private JDateChooser bModificationDate;
	private JDateChooser xmpBasicMetadataDate;
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPdfMetadataEditor = new JFrame();
		frmPdfMetadataEditor.setTitle("PDF Metadata Editor");
		frmPdfMetadataEditor.setBounds(100, 100, 640, 480);
		frmPdfMetadataEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPdfMetadataEditor.getContentPane().setLayout(
				new MigLayout("", "[][grow,fill]", "[][grow,fill][grow]"));

		JButton btnOpenPdf = new JButton("Open PDF");
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
		frmPdfMetadataEditor.getContentPane().add(btnOpenPdf,
				"cell 0 0,alignx left,aligny top");

		filename = new JTextField();
		filename.setEditable(false);
		frmPdfMetadataEditor.getContentPane().add(filename, "cell 1 0,growx");
		filename.setColumns(10);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmPdfMetadataEditor.getContentPane().add(tabbedPane,
				"cell 0 1 2 1,grow");

		JScrollPane scrollPane_5 = new JScrollPane();
		tabbedPane.addTab("Basic Metadata", null, scrollPane_5, null);
		
		basicMetaPanel = new JPanel();
		scrollPane_5.setViewportView(basicMetaPanel);
		basicMetaPanel.setLayout(new MigLayout("", "[][grow,fill]", "[][][][][][][][][][][]"));

		JLabel lblTitle = new JLabel("Title");
		basicMetaPanel.add(lblTitle, "cell 0 0,alignx trailing");

		bTitle = new JTextField();
		basicMetaPanel.add(bTitle, "cell 1 0,growx");
		bTitle.setColumns(10);

		JLabel lblNewLabel = new JLabel("Author");
		basicMetaPanel.add(lblNewLabel, "cell 0 1,alignx trailing");

		bAuthor = new JTextField();
		basicMetaPanel.add(bAuthor, "cell 1 1,growx");
		bAuthor.setColumns(10);

		JLabel lblSubject = new JLabel("Subject");
		basicMetaPanel.add(lblSubject, "cell 0 2,alignx trailing");

		bSubject = new JTextField();
		basicMetaPanel.add(bSubject, "cell 1 2,growx");
		bSubject.setColumns(10);

		JLabel lblKeywords = new JLabel("Keywords");
		basicMetaPanel.add(lblKeywords, "cell 0 3,alignx trailing");

		bKeywords = new JTextArea();
		bKeywords.setWrapStyleWord(true);
		bKeywords.setLineWrap(true);
		basicMetaPanel.add(bKeywords, "cell 1 3,growx");
		bKeywords.setColumns(10);

		JLabel lblCreator = new JLabel("Creator");
		basicMetaPanel.add(lblCreator, "cell 0 4,alignx trailing");

		bCreator = new JTextField();
		basicMetaPanel.add(bCreator, "cell 1 4,growx");
		bCreator.setColumns(10);

		JLabel lblProducer = new JLabel("Producer");
		basicMetaPanel.add(lblProducer, "cell 0 5,alignx trailing");

		bProducer = new JTextField();
		basicMetaPanel.add(bProducer, "cell 1 5,growx");
		bProducer.setColumns(10);

		JLabel lblCreationDate = new JLabel("Creation Date");
		basicMetaPanel.add(lblCreationDate, "cell 0 6,alignx trailing");
		
		bCreationDate = new JDateChooser();
		basicMetaPanel.add(bCreationDate, "cell 1 6,grow");

		JLabel lblModificationDate = new JLabel("Modification Date");
		basicMetaPanel.add(lblModificationDate, "cell 0 7,alignx trailing");
		
		bModificationDate = new JDateChooser();
		basicMetaPanel.add(bModificationDate, "cell 1 7,grow");

		JLabel lblTrapped = new JLabel("Trapped");
		basicMetaPanel.add(lblTrapped, "cell 0 8,alignx trailing");

		bTrapped = new JComboBox();
		bTrapped.setModel(new DefaultComboBoxModel(new String[] { "True",
				"False", "Unknown" }));
		basicMetaPanel.add(bTrapped, "cell 1 8,growx");

		JScrollPane scrollPane = new JScrollPane();
		tabbedPane.addTab("XMP Basic", null, scrollPane, null);

		JPanel panel_1 = new JPanel();
		scrollPane.setViewportView(panel_1);
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

		JScrollPane scrollPane_1 = new JScrollPane();
		tabbedPane.addTab("XMP PDF", null, scrollPane_1, null);

		JPanel panel_2 = new JPanel();
		scrollPane_1.setViewportView(panel_2);
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

		JScrollPane scrollPane_2 = new JScrollPane();
		tabbedPane.addTab("XMP Dublin Core", null, scrollPane_2, null);

		JPanel panel_3 = new JPanel();
		scrollPane_2.setViewportView(panel_3);
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
		frmPdfMetadataEditor.getContentPane().add(panel_4, "cell 0 2 2 1,grow");
		panel_4.setLayout(new MigLayout("",
				"[grow,fill][grow,fill][grow,fill]", "[]"));

		JButton btnCopyBasicTo = new JButton("Copy Basic To XMP");
		btnCopyBasicTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				copyBasicToXMP();
			}
		});
		panel_4.add(btnCopyBasicTo, "cell 0 0");

		JButton btnCopyXmpTo = new JButton("Copy XMP To Basic");
		btnCopyXmpTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyXMPToBasic();
			}
		});
		panel_4.add(btnCopyXmpTo, "cell 1 0");

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		panel_4.add(btnSave, "cell 2 0");
	}

}
