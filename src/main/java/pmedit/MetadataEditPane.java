package pmedit;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Field;
import java.util.Calendar;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.toedter.calendar.JDateChooser;

public class MetadataEditPane {

	public static interface FieldSetGet {
		public void apply(Object field, FieldID anno);
	}

	public static interface FieldEnabledCheckBox {
		public void apply(JCheckBox field, FieldEnabled anno);
	}

	private static final long serialVersionUID = 6994489903939856136L;
	public JPanel basicMetaPanel;

	@FieldID("doc.title")
	public JTextField basicTitle;
	@FieldID("doc.author")
	public JTextField basicAuthor;
	@FieldID("doc.subject")
	public JTextArea basicSubject;
	@FieldID(value = "doc.keywords")
	public JTextArea basicKeywords;
	@FieldID("doc.creator")
	public JTextField basicCreator;
	@FieldID("doc.producer")
	public JTextField basicProducer;
	@FieldID("doc.trapped")
	public JComboBox basicTrapped;
	@FieldID(value = "doc.creationDate", type = FieldID.FieldType.DateField)
	public JDateChooser basicCreationDate;
	@FieldID(value = "doc.modificationDate", type = FieldID.FieldType.DateField)
	public JDateChooser basicModificationDate;

	@FieldID("basic.creatorTool")
	public JTextField xmpBasicCreatorTool;
	@FieldID("basic.baseURL")
	public JTextField xmpBasicBaseURL;
	@FieldID("basic.rating")
	public JTextField xmpBasicRating;
	@FieldID("basic.label")
	public JTextField xmpBasicLabel;
	@FieldID("basic.nickname")
	public JTextField xmpBasicNickname;
	@FieldID(value = "basic.identifiers", type = FieldID.FieldType.TextField)
	public JTextArea xmpBasicIdentifiers;
	@FieldID(value = "basic.advisories", type = FieldID.FieldType.TextField)
	public JTextArea xmpBasicAdvisories;
	@FieldID(value = "basic.modifyDate", type = FieldID.FieldType.DateField)
	public JDateChooser xmpBasicModifyDate;
	@FieldID(value = "basic.createDate", type = FieldID.FieldType.DateField)
	public JDateChooser xmpBasicCreateDate;
	@FieldID(value = "basic.metadataDate", type = FieldID.FieldType.DateField)
	public JDateChooser xmpBasicMetadataDate;

	@FieldID("pdf.keywords")
	public JTextArea xmpPdfKeywords;
	@FieldID("pdf.pdfVersion")
	public JTextField xmpPdfVersion;
	@FieldID("pdf.producer")
	public JTextField xmpPdfProducer;

	@FieldID("dc.title")
	public JTextField xmpDcTitle;
	@FieldID("dc.coverage")
	public JTextField xmpDcCoverage;
	@FieldID("dc.description")
	public JTextField xmpDcDescription;
	@FieldID(value = "dc.dates", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcDates;
	@FieldID("dc.format")
	public JTextField xmpDcFormat;
	@FieldID("dc.identifier")
	public JTextField xmpDcIdentifier;
	@FieldID("dc.rights")
	public JTextField xmpDcRights;
	@FieldID("dc.source")
	public JTextField xmpDcSource;
	@FieldID(value = "dc.creators", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcCreators;
	@FieldID(value = "dc.contributors", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcContributors;
	@FieldID(value = "dc.languages", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcLanguages;
	@FieldID(value = "dc.publishers", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcPublishers;
	@FieldID(value = "dc.relationships", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcRelationships;
	@FieldID(value = "dc.subjects", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcSubjects;
	@FieldID(value = "dc.types", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcTypes;
	
	@FieldID("rights.certificate")
	public JTextField xmpRightsCertificate;
	@FieldID(value = "rights.marked", type = FieldID.FieldType.BoolField)
	public JComboBox xmpRightsMarked;
	@FieldID(value = "rights.owner" , type = FieldID.FieldType.TextField)
	public JTextArea xmpRightsOwner;
	@FieldID(value = "rights.copyright")
	public JTextArea xmpRightsCopyright;
	@FieldID("rights.usageTerms")
	public JTextArea xmpRightsUsageTerms;
	@FieldID("rights.webStatement")
	public JTextField xmpRightsWebStatement;

	
	
	public JPanel xmlBasicMetaPanel;
	public JPanel xmlPdfMetaPanel;
	public JPanel xmpDcMetaPanel;
	public JPanel xmpRightsMetaPanel;

	public JTabbedPane tabbedaPane;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JScrollPane scrollPane_2;

	@FieldEnabled("doc.title")
	public JCheckBox basicTitleEnabled;
	@FieldEnabled("doc.author")
	public JCheckBox basicAuthorEnabled;
	@FieldEnabled("doc.subject")
	public JCheckBox basicSubjectEnabled;
	@FieldEnabled("doc.keywords")
	public JCheckBox basicKeywordsEnabled;
	@FieldEnabled("doc.creator")
	public JCheckBox basicCreatorEnabled;
	@FieldEnabled("doc.producer")
	public JCheckBox basicProducerEnabled;
	@FieldEnabled("doc.creationDate")
	public JCheckBox basicCreationDateEnabled;
	@FieldEnabled("doc.modificationDate")
	public JCheckBox basicModificationDateEnabled;
	@FieldEnabled("doc.trapped")
	public JCheckBox basicTrappedEnabled;
	
	@FieldEnabled("basic.creatorTool")
	public JCheckBox xmpBasicCreatorToolEnabled;
	@FieldEnabled("basic.createDate")
	public JCheckBox xmpBasicCreateDateEnabled;
	@FieldEnabled("basic.modifyDate")
	public JCheckBox xmpBasicModifyDateEnabled;
	@FieldEnabled("basic.baseURL")
	public JCheckBox xmpBasicBaseURLEnabled;
	@FieldEnabled("basic.rating")
	public JCheckBox xmpBasicRatingEnable;
	@FieldEnabled("basic.label")
	public JCheckBox xmpBasicLabelEnabled;
	@FieldEnabled("basic.nickname")
	public JCheckBox xmpBasicNicknameEnabled;
	@FieldEnabled("basic.identifiers")
	public JCheckBox xmpBasicIdentifiersEnabled;
	@FieldEnabled("basic.advisories")
	public JCheckBox xmpBasicAdvisoriesEnabled;
	@FieldEnabled("basic.metadataDate")
	public JCheckBox xmpBasicMetadataDateEnabled;
	
	@FieldEnabled("pdf.keywords")
	public JCheckBox xmpPdfKeywordsEnabled;
	@FieldEnabled("pdf.pdfVersion")
	public JCheckBox xmpPdfVersionEnabled;
	@FieldEnabled("pdf.producer")
	public JCheckBox xmpPdfProducerEnabled;

	@FieldEnabled("dc.title")
	public JCheckBox xmlDcTitleEnabled;
	@FieldEnabled("dc.description")
	public JCheckBox xmpDcDescriptionEnabled;
	@FieldEnabled("dc.creators")
	public JCheckBox xmpDcCreatorsEnabled;
	@FieldEnabled("dc.contributors")
	public JCheckBox xmpDcContributorsEnabled;
	@FieldEnabled("dc.coverage")
	public JCheckBox xmpDcCoverageEnabled;
	@FieldEnabled("dc.dates")
	public JCheckBox xmpDcDatesEnabled;
	@FieldEnabled("dc.format")
	public JCheckBox xmpDcFormatEnabled;
	@FieldEnabled("dc.identifier")
	public JCheckBox xmpDcIdentifierEnabled;
	@FieldEnabled("dc.languages")
	public JCheckBox xmpDcLanguagesEnabled;
	@FieldEnabled("dc.publishers")
	public JCheckBox xmpDcPublishersEnabled;
	@FieldEnabled("dc.relationships")
	public JCheckBox xmpDcRelationshipsEnabled;
	@FieldEnabled("dc.rights")
	public JCheckBox xmpDcRightsEnabled;
	@FieldEnabled("dc.source")
	public JCheckBox xmpDcSourceEnabled;
	@FieldEnabled("dc.subjects")
	public JCheckBox xmpDcSubjectsEnabled;
	@FieldEnabled("dc.types")
	public JCheckBox xmpDcTypesEnabled;
	
	@FieldEnabled("rights.certificate")
	public JCheckBox xmpRightsCertificateEnabled;
	@FieldEnabled("rights.marked")
	public JCheckBox xmpRightsMarkedEnabled;
	@FieldEnabled("rights.owner")
	public JCheckBox xmpRightsOwnerEnabled;
	@FieldEnabled("rights.copyright")
	public JCheckBox xmpRightsCopyrightEnabled;
	@FieldEnabled("rights.usageTerms")
	public JCheckBox xmpRightsUsageTermsEnabled;
	@FieldEnabled("rights.webStatement")
	public JCheckBox xmpRightsWebStatementEnabled;

	
	
	private JScrollPane scrollPane_3;
	private JScrollPane scrollPane_4;

	public MetadataEditPane() {
		initialize();
	}

	private void initialize() {
		long startTime = System.nanoTime(), cnt=1;

		tabbedaPane = new JTabbedPane(JTabbedPane.TOP);
		JScrollPane basicScrollpane = new JScrollPane();
		basicScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("Document", null, basicScrollpane, null);

		basicMetaPanel = new JPanel();
		basicScrollpane.setViewportView(basicMetaPanel);
		GridBagLayout gbl_basicMetaPanel = new GridBagLayout();
		gbl_basicMetaPanel.columnWidths = new int[] { 112, 0, 284, 0 };
		gbl_basicMetaPanel.rowHeights = new int[] { 26, 26, 16, 16, 26, 26, 26, 26, 27, 0 };
		gbl_basicMetaPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_basicMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		basicMetaPanel.setLayout(gbl_basicMetaPanel);


		JLabel lblTitle = new JLabel("Title");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.EAST;
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		basicMetaPanel.add(lblTitle, gbc_lblTitle);
		
		basicTitleEnabled = new JCheckBox("");
		basicTitleEnabled.setSelected(true);
		basicTitleEnabled.setEnabled(false);
		GridBagConstraints gbc_basicTitleEnabled = new GridBagConstraints();
		gbc_basicTitleEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicTitleEnabled.gridx = 1;
		gbc_basicTitleEnabled.gridy = 0;
		basicMetaPanel.add(basicTitleEnabled, gbc_basicTitleEnabled);

		basicTitle = new JTextField();
		GridBagConstraints gbc_basicTitle = new GridBagConstraints();
		gbc_basicTitle.weightx = 1.0;
		gbc_basicTitle.anchor = GridBagConstraints.WEST;
		gbc_basicTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicTitle.insets = new Insets(0, 0, 5, 0);
		gbc_basicTitle.gridx = 2;
		gbc_basicTitle.gridy = 0;
		basicMetaPanel.add(basicTitle, gbc_basicTitle);
		basicTitle.setColumns(10);

		JLabel lblNewLabel = new JLabel("Author");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		basicMetaPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		basicAuthorEnabled = new JCheckBox("");
		basicAuthorEnabled.setSelected(true);
		basicAuthorEnabled.setEnabled(false);
		GridBagConstraints gbc_basicAuthorEnabled = new GridBagConstraints();
		gbc_basicAuthorEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicAuthorEnabled.gridx = 1;
		gbc_basicAuthorEnabled.gridy = 1;
		basicMetaPanel.add(basicAuthorEnabled, gbc_basicAuthorEnabled);

		basicAuthor = new JTextField();
		GridBagConstraints gbc_basicAuthor = new GridBagConstraints();
		gbc_basicAuthor.weightx = 1.0;
		gbc_basicAuthor.anchor = GridBagConstraints.WEST;
		gbc_basicAuthor.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicAuthor.insets = new Insets(0, 0, 5, 0);
		gbc_basicAuthor.gridx = 2;
		gbc_basicAuthor.gridy = 1;
		basicMetaPanel.add(basicAuthor, gbc_basicAuthor);
		basicAuthor.setColumns(10);

		JLabel lblSubject = new JLabel("Subject");
		GridBagConstraints gbc_lblSubject = new GridBagConstraints();
		gbc_lblSubject.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblSubject.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubject.gridx = 0;
		gbc_lblSubject.gridy = 2;
		basicMetaPanel.add(lblSubject, gbc_lblSubject);
		
		basicSubjectEnabled = new JCheckBox("");
		basicSubjectEnabled.setSelected(true);
		basicSubjectEnabled.setEnabled(false);
		GridBagConstraints gbc_basicSubjectEnabled = new GridBagConstraints();
		gbc_basicSubjectEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicSubjectEnabled.gridx = 1;
		gbc_basicSubjectEnabled.gridy = 2;
		basicMetaPanel.add(basicSubjectEnabled, gbc_basicSubjectEnabled);
		
		scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.weighty = 0.5;
		gbc_scrollPane_1.anchor = GridBagConstraints.WEST;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 2;
		basicMetaPanel.add(scrollPane_1, gbc_scrollPane_1);

		basicSubject = new JTextArea();
		basicSubject.setLineWrap(true);
		basicSubject.setWrapStyleWord(true);
		scrollPane_1.setViewportView(basicSubject);

		JLabel lblKeywords = new JLabel("Keywords");
		GridBagConstraints gbc_lblKeywords = new GridBagConstraints();
		gbc_lblKeywords.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblKeywords.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeywords.gridx = 0;
		gbc_lblKeywords.gridy = 3;
		basicMetaPanel.add(lblKeywords, gbc_lblKeywords);
		
		basicKeywordsEnabled = new JCheckBox("");
		basicKeywordsEnabled.setEnabled(false);
		basicKeywordsEnabled.setSelected(true);
		GridBagConstraints gbc_basicKeywordsEnabled = new GridBagConstraints();
		gbc_basicKeywordsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicKeywordsEnabled.gridx = 1;
		gbc_basicKeywordsEnabled.gridy = 3;
		basicMetaPanel.add(basicKeywordsEnabled, gbc_basicKeywordsEnabled);
		
		scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.weighty = 0.5;
		gbc_scrollPane_2.anchor = GridBagConstraints.WEST;
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.gridx = 2;
		gbc_scrollPane_2.gridy = 3;
		basicMetaPanel.add(scrollPane_2, gbc_scrollPane_2);

		basicKeywords = new JTextArea();
		basicKeywords.setLineWrap(true);
		basicKeywords.setWrapStyleWord(true);
		scrollPane_2.setViewportView(basicKeywords);

		JLabel lblCreator = new JLabel("Creator");
		GridBagConstraints gbc_lblCreator = new GridBagConstraints();
		gbc_lblCreator.anchor = GridBagConstraints.EAST;
		gbc_lblCreator.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreator.gridx = 0;
		gbc_lblCreator.gridy = 4;
		basicMetaPanel.add(lblCreator, gbc_lblCreator);
		
		basicCreatorEnabled = new JCheckBox("");
		basicCreatorEnabled.setEnabled(false);
		basicCreatorEnabled.setSelected(true);
		GridBagConstraints gbc_basicCreatorEnabled = new GridBagConstraints();
		gbc_basicCreatorEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicCreatorEnabled.gridx = 1;
		gbc_basicCreatorEnabled.gridy = 4;
		basicMetaPanel.add(basicCreatorEnabled, gbc_basicCreatorEnabled);

		basicCreator = new JTextField();
		GridBagConstraints gbc_basicCreator = new GridBagConstraints();
		gbc_basicCreator.weightx = 1.0;
		gbc_basicCreator.anchor = GridBagConstraints.WEST;
		gbc_basicCreator.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicCreator.insets = new Insets(0, 0, 5, 0);
		gbc_basicCreator.gridx = 2;
		gbc_basicCreator.gridy = 4;
		basicMetaPanel.add(basicCreator, gbc_basicCreator);
		basicCreator.setColumns(10);

		JLabel lblProducer = new JLabel("Producer");
		GridBagConstraints gbc_lblProducer = new GridBagConstraints();
		gbc_lblProducer.anchor = GridBagConstraints.EAST;
		gbc_lblProducer.insets = new Insets(0, 0, 5, 5);
		gbc_lblProducer.gridx = 0;
		gbc_lblProducer.gridy = 5;
		basicMetaPanel.add(lblProducer, gbc_lblProducer);
		
		basicProducerEnabled = new JCheckBox("");
		basicProducerEnabled.setEnabled(false);
		basicProducerEnabled.setSelected(true);
		GridBagConstraints gbc_basicProducerEnabled = new GridBagConstraints();
		gbc_basicProducerEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicProducerEnabled.gridx = 1;
		gbc_basicProducerEnabled.gridy = 5;
		basicMetaPanel.add(basicProducerEnabled, gbc_basicProducerEnabled);

		basicProducer = new JTextField();
		GridBagConstraints gbc_basicProducer = new GridBagConstraints();
		gbc_basicProducer.weightx = 1.0;
		gbc_basicProducer.anchor = GridBagConstraints.WEST;
		gbc_basicProducer.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicProducer.insets = new Insets(0, 0, 5, 0);
		gbc_basicProducer.gridx = 2;
		gbc_basicProducer.gridy = 5;
		basicMetaPanel.add(basicProducer, gbc_basicProducer);
		basicProducer.setColumns(10);

		JLabel lblCreationDate = new JLabel("Creation Date");
		GridBagConstraints gbc_lblCreationDate = new GridBagConstraints();
		gbc_lblCreationDate.anchor = GridBagConstraints.EAST;
		gbc_lblCreationDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreationDate.gridx = 0;
		gbc_lblCreationDate.gridy = 6;
		basicMetaPanel.add(lblCreationDate, gbc_lblCreationDate);
		
		basicCreationDateEnabled = new JCheckBox("");
		basicCreationDateEnabled.setEnabled(false);
		basicCreationDateEnabled.setSelected(true);
		GridBagConstraints gbc_basicCreationDateEnabled = new GridBagConstraints();
		gbc_basicCreationDateEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicCreationDateEnabled.gridx = 1;
		gbc_basicCreationDateEnabled.gridy = 6;
		basicMetaPanel.add(basicCreationDateEnabled, gbc_basicCreationDateEnabled);

		basicCreationDate = new JDateChooser();
		basicCreationDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_basicCreationDate = new GridBagConstraints();
		gbc_basicCreationDate.anchor = GridBagConstraints.WEST;
		gbc_basicCreationDate.insets = new Insets(0, 0, 5, 0);
		gbc_basicCreationDate.gridx = 2;
		gbc_basicCreationDate.gridy = 6;
		basicMetaPanel.add(basicCreationDate, gbc_basicCreationDate);

		JLabel lblModificationDate = new JLabel("Modification Date");
		GridBagConstraints gbc_lblModificationDate = new GridBagConstraints();
		gbc_lblModificationDate.anchor = GridBagConstraints.WEST;
		gbc_lblModificationDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblModificationDate.gridx = 0;
		gbc_lblModificationDate.gridy = 7;
		basicMetaPanel.add(lblModificationDate, gbc_lblModificationDate);
		
		basicModificationDateEnabled = new JCheckBox("");
		basicModificationDateEnabled.setEnabled(false);
		basicModificationDateEnabled.setSelected(true);
		GridBagConstraints gbc_basicModificationDateEnabled = new GridBagConstraints();
		gbc_basicModificationDateEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_basicModificationDateEnabled.gridx = 1;
		gbc_basicModificationDateEnabled.gridy = 7;
		basicMetaPanel.add(basicModificationDateEnabled, gbc_basicModificationDateEnabled);

		basicModificationDate = new JDateChooser();
		basicModificationDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_basicModificationDate = new GridBagConstraints();
		gbc_basicModificationDate.anchor = GridBagConstraints.WEST;
		gbc_basicModificationDate.insets = new Insets(0, 0, 5, 0);
		gbc_basicModificationDate.gridx = 2;
		gbc_basicModificationDate.gridy = 7;
		basicMetaPanel.add(basicModificationDate, gbc_basicModificationDate);

		JLabel lblTrapped = new JLabel("Trapped");
		GridBagConstraints gbc_lblTrapped = new GridBagConstraints();
		gbc_lblTrapped.anchor = GridBagConstraints.EAST;
		gbc_lblTrapped.insets = new Insets(0, 0, 0, 5);
		gbc_lblTrapped.gridx = 0;
		gbc_lblTrapped.gridy = 8;
		basicMetaPanel.add(lblTrapped, gbc_lblTrapped);
		
		basicTrappedEnabled = new JCheckBox("");
		basicTrappedEnabled.setEnabled(false);
		basicTrappedEnabled.setSelected(true);
		GridBagConstraints gbc_basicTrappedEnabled = new GridBagConstraints();
		gbc_basicTrappedEnabled.insets = new Insets(0, 0, 0, 5);
		gbc_basicTrappedEnabled.gridx = 1;
		gbc_basicTrappedEnabled.gridy = 8;
		basicMetaPanel.add(basicTrappedEnabled, gbc_basicTrappedEnabled);

		basicTrapped = new JComboBox();
		basicTrapped.setModel(new DefaultComboBoxModel(new String[] { "True", "False", "Unknown" }));
		GridBagConstraints gbc_basicTrapped = new GridBagConstraints();
		gbc_basicTrapped.anchor = GridBagConstraints.WEST;
		gbc_basicTrapped.gridx = 2;
		gbc_basicTrapped.gridy = 8;
		basicMetaPanel.add(basicTrapped, gbc_basicTrapped);


		JScrollPane xmpBasicScrollpane = new JScrollPane();
		xmpBasicScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP Basic", null, xmpBasicScrollpane, null);

		xmlBasicMetaPanel = new JPanel();
		xmpBasicScrollpane.setViewportView(xmlBasicMetaPanel);
		GridBagLayout gbl_xmlBasicMetaPanel = new GridBagLayout();
		gbl_xmlBasicMetaPanel.columnWidths = new int[] {112, 0, 284, 0};
		gbl_xmlBasicMetaPanel.rowHeights = new int[] { 26, 26, 26, 26, 26, 26, 26, 16, 16, 26, 0 };
		gbl_xmlBasicMetaPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_xmlBasicMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		xmlBasicMetaPanel.setLayout(gbl_xmlBasicMetaPanel);

		JLabel lblCreatorTool = new JLabel("Creator tool");
		GridBagConstraints gbc_lblCreatorTool = new GridBagConstraints();
		gbc_lblCreatorTool.anchor = GridBagConstraints.EAST;
		gbc_lblCreatorTool.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatorTool.gridx = 0;
		gbc_lblCreatorTool.gridy = 0;
		xmlBasicMetaPanel.add(lblCreatorTool, gbc_lblCreatorTool);
		
		xmpBasicCreatorToolEnabled = new JCheckBox("");
		xmpBasicCreatorToolEnabled.setEnabled(false);
		xmpBasicCreatorToolEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicCreatorToolEnabled = new GridBagConstraints();
		gbc_xmpBasicCreatorToolEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicCreatorToolEnabled.gridx = 1;
		gbc_xmpBasicCreatorToolEnabled.gridy = 0;
		xmlBasicMetaPanel.add(xmpBasicCreatorToolEnabled, gbc_xmpBasicCreatorToolEnabled);

		xmpBasicCreatorTool = new JTextField();
		GridBagConstraints gbc_xmpBasicCreatorTool = new GridBagConstraints();
		gbc_xmpBasicCreatorTool.weightx = 1.0;
		gbc_xmpBasicCreatorTool.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicCreatorTool.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicCreatorTool.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicCreatorTool.gridx = 2;
		gbc_xmpBasicCreatorTool.gridy = 0;
		xmlBasicMetaPanel.add(xmpBasicCreatorTool, gbc_xmpBasicCreatorTool);
		xmpBasicCreatorTool.setColumns(10);

		JLabel lblCreateDate = new JLabel("Create Date");
		GridBagConstraints gbc_lblCreateDate = new GridBagConstraints();
		gbc_lblCreateDate.anchor = GridBagConstraints.EAST;
		gbc_lblCreateDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreateDate.gridx = 0;
		gbc_lblCreateDate.gridy = 1;
		xmlBasicMetaPanel.add(lblCreateDate, gbc_lblCreateDate);
		
		xmpBasicCreateDateEnabled = new JCheckBox("");
		xmpBasicCreateDateEnabled.setEnabled(false);
		xmpBasicCreateDateEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicCreateDateEnabled = new GridBagConstraints();
		gbc_xmpBasicCreateDateEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicCreateDateEnabled.gridx = 1;
		gbc_xmpBasicCreateDateEnabled.gridy = 1;
		xmlBasicMetaPanel.add(xmpBasicCreateDateEnabled, gbc_xmpBasicCreateDateEnabled);

		xmpBasicCreateDate = new JDateChooser();
		xmpBasicCreateDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_xmpBasicCreateDate = new GridBagConstraints();
		gbc_xmpBasicCreateDate.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicCreateDate.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicCreateDate.gridx = 2;
		gbc_xmpBasicCreateDate.gridy = 1;
		xmlBasicMetaPanel.add(xmpBasicCreateDate, gbc_xmpBasicCreateDate);

		JLabel lblModifyDate = new JLabel("Modify Date");
		GridBagConstraints gbc_lblModifyDate = new GridBagConstraints();
		gbc_lblModifyDate.anchor = GridBagConstraints.EAST;
		gbc_lblModifyDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblModifyDate.gridx = 0;
		gbc_lblModifyDate.gridy = 2;
		xmlBasicMetaPanel.add(lblModifyDate, gbc_lblModifyDate);
		
		xmpBasicModifyDateEnabled = new JCheckBox("");
		xmpBasicModifyDateEnabled.setEnabled(false);
		xmpBasicModifyDateEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicModifyDateEnabled = new GridBagConstraints();
		gbc_xmpBasicModifyDateEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicModifyDateEnabled.gridx = 1;
		gbc_xmpBasicModifyDateEnabled.gridy = 2;
		xmlBasicMetaPanel.add(xmpBasicModifyDateEnabled, gbc_xmpBasicModifyDateEnabled);

		xmpBasicModifyDate = new JDateChooser();
		xmpBasicModifyDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_xmpBasicModifyDate = new GridBagConstraints();
		gbc_xmpBasicModifyDate.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicModifyDate.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicModifyDate.gridx = 2;
		gbc_xmpBasicModifyDate.gridy = 2;
		xmlBasicMetaPanel.add(xmpBasicModifyDate, gbc_xmpBasicModifyDate);

		JLabel lblBaseUrl = new JLabel("Base URL");
		GridBagConstraints gbc_lblBaseUrl = new GridBagConstraints();
		gbc_lblBaseUrl.anchor = GridBagConstraints.EAST;
		gbc_lblBaseUrl.insets = new Insets(0, 0, 5, 5);
		gbc_lblBaseUrl.gridx = 0;
		gbc_lblBaseUrl.gridy = 3;
		xmlBasicMetaPanel.add(lblBaseUrl, gbc_lblBaseUrl);
		
		xmpBasicBaseURLEnabled = new JCheckBox("");
		xmpBasicBaseURLEnabled.setEnabled(false);
		xmpBasicBaseURLEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicBaseURLEnabled = new GridBagConstraints();
		gbc_xmpBasicBaseURLEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicBaseURLEnabled.gridx = 1;
		gbc_xmpBasicBaseURLEnabled.gridy = 3;
		xmlBasicMetaPanel.add(xmpBasicBaseURLEnabled, gbc_xmpBasicBaseURLEnabled);

		xmpBasicBaseURL = new JTextField();
		GridBagConstraints gbc_xmpBasicBaseURL = new GridBagConstraints();
		gbc_xmpBasicBaseURL.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicBaseURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicBaseURL.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicBaseURL.gridx = 2;
		gbc_xmpBasicBaseURL.gridy = 3;
		xmlBasicMetaPanel.add(xmpBasicBaseURL, gbc_xmpBasicBaseURL);
		xmpBasicBaseURL.setColumns(10);

		JLabel lblRating = new JLabel("Rating");
		GridBagConstraints gbc_lblRating = new GridBagConstraints();
		gbc_lblRating.anchor = GridBagConstraints.EAST;
		gbc_lblRating.insets = new Insets(0, 0, 5, 5);
		gbc_lblRating.gridx = 0;
		gbc_lblRating.gridy = 4;
		xmlBasicMetaPanel.add(lblRating, gbc_lblRating);
		
		xmpBasicRatingEnable = new JCheckBox("");
		xmpBasicRatingEnable.setEnabled(false);
		xmpBasicRatingEnable.setSelected(true);
		GridBagConstraints gbc_xmpBasicRatingEnable = new GridBagConstraints();
		gbc_xmpBasicRatingEnable.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicRatingEnable.gridx = 1;
		gbc_xmpBasicRatingEnable.gridy = 4;
		xmlBasicMetaPanel.add(xmpBasicRatingEnable, gbc_xmpBasicRatingEnable);
		
		xmpBasicRating = new JTextField();
		GridBagConstraints gbc_xmpBasicRating = new GridBagConstraints();
		gbc_xmpBasicRating.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicRating.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicRating.gridx = 2;
		gbc_xmpBasicRating.gridy = 4;
		xmlBasicMetaPanel.add(xmpBasicRating, gbc_xmpBasicRating);

		JLabel lblLabel = new JLabel("Label");
		GridBagConstraints gbc_lblLabel = new GridBagConstraints();
		gbc_lblLabel.anchor = GridBagConstraints.EAST;
		gbc_lblLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblLabel.gridx = 0;
		gbc_lblLabel.gridy = 5;
		xmlBasicMetaPanel.add(lblLabel, gbc_lblLabel);
		
		xmpBasicLabelEnabled = new JCheckBox("");
		xmpBasicLabelEnabled.setEnabled(false);
		xmpBasicLabelEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicLabelEnabled = new GridBagConstraints();
		gbc_xmpBasicLabelEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicLabelEnabled.gridx = 1;
		gbc_xmpBasicLabelEnabled.gridy = 5;
		xmlBasicMetaPanel.add(xmpBasicLabelEnabled, gbc_xmpBasicLabelEnabled);

		xmpBasicLabel = new JTextField();
		GridBagConstraints gbc_xmpBasicLabel = new GridBagConstraints();
		gbc_xmpBasicLabel.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicLabel.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicLabel.gridx = 2;
		gbc_xmpBasicLabel.gridy = 5;
		xmlBasicMetaPanel.add(xmpBasicLabel, gbc_xmpBasicLabel);
		xmpBasicLabel.setColumns(10);

		JLabel lblNickname = new JLabel("Nickname");
		GridBagConstraints gbc_lblNickname = new GridBagConstraints();
		gbc_lblNickname.anchor = GridBagConstraints.EAST;
		gbc_lblNickname.insets = new Insets(0, 0, 5, 5);
		gbc_lblNickname.gridx = 0;
		gbc_lblNickname.gridy = 6;
		xmlBasicMetaPanel.add(lblNickname, gbc_lblNickname);
		
		xmpBasicNicknameEnabled = new JCheckBox("");
		xmpBasicNicknameEnabled.setEnabled(false);
		xmpBasicNicknameEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicNicknameEnabled = new GridBagConstraints();
		gbc_xmpBasicNicknameEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicNicknameEnabled.gridx = 1;
		gbc_xmpBasicNicknameEnabled.gridy = 6;
		xmlBasicMetaPanel.add(xmpBasicNicknameEnabled, gbc_xmpBasicNicknameEnabled);

		xmpBasicNickname = new JTextField();
		GridBagConstraints gbc_xmpBasicNickname = new GridBagConstraints();
		gbc_xmpBasicNickname.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicNickname.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicNickname.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicNickname.gridx = 2;
		gbc_xmpBasicNickname.gridy = 6;
		xmlBasicMetaPanel.add(xmpBasicNickname, gbc_xmpBasicNickname);
		xmpBasicNickname.setColumns(10);

		JLabel label_1 = new JLabel("Identifiers");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.NORTHEAST;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 7;
		xmlBasicMetaPanel.add(label_1, gbc_label_1);
		
		xmpBasicIdentifiersEnabled = new JCheckBox("");
		xmpBasicIdentifiersEnabled.setEnabled(false);
		xmpBasicIdentifiersEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicIdentifiersEnabled = new GridBagConstraints();
		gbc_xmpBasicIdentifiersEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicIdentifiersEnabled.gridx = 1;
		gbc_xmpBasicIdentifiersEnabled.gridy = 7;
		xmlBasicMetaPanel.add(xmpBasicIdentifiersEnabled, gbc_xmpBasicIdentifiersEnabled);

		xmpBasicIdentifiers = new JTextArea();
		GridBagConstraints gbc_xmpBasicIdentifiers = new GridBagConstraints();
		gbc_xmpBasicIdentifiers.weighty = 0.5;
		gbc_xmpBasicIdentifiers.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicIdentifiers.fill = GridBagConstraints.BOTH;
		gbc_xmpBasicIdentifiers.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicIdentifiers.gridx = 2;
		gbc_xmpBasicIdentifiers.gridy = 7;
		xmlBasicMetaPanel.add(xmpBasicIdentifiers, gbc_xmpBasicIdentifiers);

		JLabel label = new JLabel("Advisories");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 8;
		xmlBasicMetaPanel.add(label, gbc_label);
		
		xmpBasicAdvisoriesEnabled = new JCheckBox("");
		xmpBasicAdvisoriesEnabled.setEnabled(false);
		xmpBasicAdvisoriesEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicAdvisoriesEnabled = new GridBagConstraints();
		gbc_xmpBasicAdvisoriesEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpBasicAdvisoriesEnabled.gridx = 1;
		gbc_xmpBasicAdvisoriesEnabled.gridy = 8;
		xmlBasicMetaPanel.add(xmpBasicAdvisoriesEnabled, gbc_xmpBasicAdvisoriesEnabled);

		xmpBasicAdvisories = new JTextArea();
		GridBagConstraints gbc_xmpBasicAdvisories = new GridBagConstraints();
		gbc_xmpBasicAdvisories.weighty = 0.5;
		gbc_xmpBasicAdvisories.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicAdvisories.fill = GridBagConstraints.BOTH;
		gbc_xmpBasicAdvisories.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicAdvisories.gridx = 2;
		gbc_xmpBasicAdvisories.gridy = 8;
		xmlBasicMetaPanel.add(xmpBasicAdvisories, gbc_xmpBasicAdvisories);

		JLabel lblMetadataDate = new JLabel("Metadata Date");
		GridBagConstraints gbc_lblMetadataDate = new GridBagConstraints();
		gbc_lblMetadataDate.anchor = GridBagConstraints.WEST;
		gbc_lblMetadataDate.insets = new Insets(0, 0, 0, 5);
		gbc_lblMetadataDate.gridx = 0;
		gbc_lblMetadataDate.gridy = 9;
		xmlBasicMetaPanel.add(lblMetadataDate, gbc_lblMetadataDate);
		
		xmpBasicMetadataDateEnabled = new JCheckBox("");
		xmpBasicMetadataDateEnabled.setEnabled(false);
		xmpBasicMetadataDateEnabled.setSelected(true);
		GridBagConstraints gbc_xmpBasicMetadataDateEnabled = new GridBagConstraints();
		gbc_xmpBasicMetadataDateEnabled.insets = new Insets(0, 0, 0, 5);
		gbc_xmpBasicMetadataDateEnabled.gridx = 1;
		gbc_xmpBasicMetadataDateEnabled.gridy = 9;
		xmlBasicMetaPanel.add(xmpBasicMetadataDateEnabled, gbc_xmpBasicMetadataDateEnabled);

		xmpBasicMetadataDate = new JDateChooser();
		xmpBasicMetadataDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_xmpBasicMetadataDate = new GridBagConstraints();
		gbc_xmpBasicMetadataDate.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicMetadataDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicMetadataDate.gridx = 2;
		gbc_xmpBasicMetadataDate.gridy = 9;
		xmlBasicMetaPanel.add(xmpBasicMetadataDate, gbc_xmpBasicMetadataDate);

		JScrollPane xmpPdfScrollpane = new JScrollPane();
		xmpPdfScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP PDF", null, xmpPdfScrollpane, null);

		xmlPdfMetaPanel = new JPanel();
		xmpPdfScrollpane.setViewportView(xmlPdfMetaPanel);
		GridBagLayout gbl_xmlPdfMetaPanel = new GridBagLayout();
		gbl_xmlPdfMetaPanel.columnWidths = new int[] {112, 0, 284, 0};
		gbl_xmlPdfMetaPanel.rowHeights = new int[] { 16, 26, 26, 0 };
		gbl_xmlPdfMetaPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_xmlPdfMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		xmlPdfMetaPanel.setLayout(gbl_xmlPdfMetaPanel);

		JLabel lblKeywords_1 = new JLabel("Keywords");
		GridBagConstraints gbc_lblKeywords_1 = new GridBagConstraints();
		gbc_lblKeywords_1.anchor = GridBagConstraints.EAST;
		gbc_lblKeywords_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeywords_1.gridx = 0;
		gbc_lblKeywords_1.gridy = 0;
		xmlPdfMetaPanel.add(lblKeywords_1, gbc_lblKeywords_1);
		
		xmpPdfKeywordsEnabled = new JCheckBox("");
		xmpPdfKeywordsEnabled.setEnabled(false);
		xmpPdfKeywordsEnabled.setSelected(true);
		GridBagConstraints gbc_xmpPdfKeywordsEnabled = new GridBagConstraints();
		gbc_xmpPdfKeywordsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpPdfKeywordsEnabled.gridx = 1;
		gbc_xmpPdfKeywordsEnabled.gridy = 0;
		xmlPdfMetaPanel.add(xmpPdfKeywordsEnabled, gbc_xmpPdfKeywordsEnabled);
		

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.anchor = GridBagConstraints.WEST;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = 0;
		xmlPdfMetaPanel.add(scrollPane, gbc_scrollPane);

		xmpPdfKeywords = new JTextArea();
		scrollPane.setViewportView(xmpPdfKeywords);
		xmpPdfKeywords.setWrapStyleWord(true);
		xmpPdfKeywords.setLineWrap(true);
		xmpPdfKeywords.setColumns(10);

		JLabel lblPdfVersion = new JLabel("PDF Version");
		GridBagConstraints gbc_lblPdfVersion = new GridBagConstraints();
		gbc_lblPdfVersion.anchor = GridBagConstraints.EAST;
		gbc_lblPdfVersion.insets = new Insets(0, 0, 5, 5);
		gbc_lblPdfVersion.gridx = 0;
		gbc_lblPdfVersion.gridy = 1;
		xmlPdfMetaPanel.add(lblPdfVersion, gbc_lblPdfVersion);
		
		xmpPdfVersionEnabled = new JCheckBox("");
		xmpPdfVersionEnabled.setEnabled(false);
		xmpPdfVersionEnabled.setSelected(true);
		GridBagConstraints gbc_xmpPdfVersionEnabled = new GridBagConstraints();
		gbc_xmpPdfVersionEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpPdfVersionEnabled.gridx = 1;
		gbc_xmpPdfVersionEnabled.gridy = 1;
		xmlPdfMetaPanel.add(xmpPdfVersionEnabled, gbc_xmpPdfVersionEnabled);

		xmpPdfVersion = new JTextField();
		xmpPdfVersion.setEditable(false);
		GridBagConstraints gbc_xmpPdfVersion = new GridBagConstraints();
		gbc_xmpPdfVersion.anchor = GridBagConstraints.WEST;
		gbc_xmpPdfVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpPdfVersion.insets = new Insets(0, 0, 5, 0);
		gbc_xmpPdfVersion.gridx = 2;
		gbc_xmpPdfVersion.gridy = 1;
		xmlPdfMetaPanel.add(xmpPdfVersion, gbc_xmpPdfVersion);
		xmpPdfVersion.setColumns(10);

		JLabel lblProducer_1 = new JLabel("Producer");
		GridBagConstraints gbc_lblProducer_1 = new GridBagConstraints();
		gbc_lblProducer_1.anchor = GridBagConstraints.EAST;
		gbc_lblProducer_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblProducer_1.gridx = 0;
		gbc_lblProducer_1.gridy = 2;
		xmlPdfMetaPanel.add(lblProducer_1, gbc_lblProducer_1);
		
		xmpPdfProducerEnabled = new JCheckBox("");
		xmpPdfProducerEnabled.setEnabled(false);
		xmpPdfProducerEnabled.setSelected(true);
		GridBagConstraints gbc_xmpPdfProducerEnabled = new GridBagConstraints();
		gbc_xmpPdfProducerEnabled.insets = new Insets(0, 0, 0, 5);
		gbc_xmpPdfProducerEnabled.gridx = 1;
		gbc_xmpPdfProducerEnabled.gridy = 2;
		xmlPdfMetaPanel.add(xmpPdfProducerEnabled, gbc_xmpPdfProducerEnabled);

		xmpPdfProducer = new JTextField();
		GridBagConstraints gbc_xmpPdfProducer = new GridBagConstraints();
		gbc_xmpPdfProducer.anchor = GridBagConstraints.WEST;
		gbc_xmpPdfProducer.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpPdfProducer.gridx = 2;
		gbc_xmpPdfProducer.gridy = 2;
		xmlPdfMetaPanel.add(xmpPdfProducer, gbc_xmpPdfProducer);
		xmpPdfProducer.setColumns(10);

		JScrollPane xmpDcScrollpane = new JScrollPane();
		xmpDcScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP Dublin Core", null, xmpDcScrollpane, null);

		xmpDcMetaPanel = new JPanel();
		xmpDcScrollpane.setViewportView(xmpDcMetaPanel);
		GridBagLayout gbl_xmpDcMetaPanel = new GridBagLayout();
		gbl_xmpDcMetaPanel.columnWidths = new int[] {112, 0, 284, 0};
		gbl_xmpDcMetaPanel.rowHeights = new int[] { 26, 26, 16, 16, 26, 16, 26, 26, 16, 16, 16, 26, 26, 16, 16, 0 };
		gbl_xmpDcMetaPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_xmpDcMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, Double.MIN_VALUE };
		xmpDcMetaPanel.setLayout(gbl_xmpDcMetaPanel);

		JLabel lblTitle_2 = new JLabel("Title");
		GridBagConstraints gbc_lblTitle_2 = new GridBagConstraints();
		gbc_lblTitle_2.anchor = GridBagConstraints.EAST;
		gbc_lblTitle_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle_2.gridx = 0;
		gbc_lblTitle_2.gridy = 0;
		xmpDcMetaPanel.add(lblTitle_2, gbc_lblTitle_2);
		
		xmlDcTitleEnabled = new JCheckBox("");
		xmlDcTitleEnabled.setEnabled(false);
		xmlDcTitleEnabled.setSelected(true);
		GridBagConstraints gbc_xmlDcTitleEnabled = new GridBagConstraints();
		gbc_xmlDcTitleEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmlDcTitleEnabled.gridx = 1;
		gbc_xmlDcTitleEnabled.gridy = 0;
		xmpDcMetaPanel.add(xmlDcTitleEnabled, gbc_xmlDcTitleEnabled);

		xmpDcTitle = new JTextField();
		GridBagConstraints gbc_xmpDcTitle = new GridBagConstraints();
		gbc_xmpDcTitle.weightx = 1.0;
		gbc_xmpDcTitle.anchor = GridBagConstraints.WEST;
		gbc_xmpDcTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcTitle.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcTitle.gridx = 2;
		gbc_xmpDcTitle.gridy = 0;
		xmpDcMetaPanel.add(xmpDcTitle, gbc_xmpDcTitle);
		xmpDcTitle.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.EAST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 1;
		xmpDcMetaPanel.add(lblDescription, gbc_lblDescription);
		
		xmpDcDescriptionEnabled = new JCheckBox("");
		xmpDcDescriptionEnabled.setEnabled(false);
		xmpDcDescriptionEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcDescriptionEnabled = new GridBagConstraints();
		gbc_xmpDcDescriptionEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcDescriptionEnabled.gridx = 1;
		gbc_xmpDcDescriptionEnabled.gridy = 1;
		xmpDcMetaPanel.add(xmpDcDescriptionEnabled, gbc_xmpDcDescriptionEnabled);

		xmpDcDescription = new JTextField();
		GridBagConstraints gbc_xmpDcDescription = new GridBagConstraints();
		gbc_xmpDcDescription.anchor = GridBagConstraints.WEST;
		gbc_xmpDcDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcDescription.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcDescription.gridx = 2;
		gbc_xmpDcDescription.gridy = 1;
		xmpDcMetaPanel.add(xmpDcDescription, gbc_xmpDcDescription);
		xmpDcDescription.setColumns(10);

		JLabel lblCreators = new JLabel("Creators");
		GridBagConstraints gbc_lblCreators = new GridBagConstraints();
		gbc_lblCreators.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblCreators.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreators.gridx = 0;
		gbc_lblCreators.gridy = 2;
		xmpDcMetaPanel.add(lblCreators, gbc_lblCreators);
		
		xmpDcCreatorsEnabled = new JCheckBox("");
		xmpDcCreatorsEnabled.setEnabled(false);
		xmpDcCreatorsEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcCreatorsEnabled = new GridBagConstraints();
		gbc_xmpDcCreatorsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcCreatorsEnabled.gridx = 1;
		gbc_xmpDcCreatorsEnabled.gridy = 2;
		xmpDcMetaPanel.add(xmpDcCreatorsEnabled, gbc_xmpDcCreatorsEnabled);

		xmpDcCreators = new JTextArea();
		GridBagConstraints gbc_xmpDcCreators = new GridBagConstraints();
		gbc_xmpDcCreators.weighty = 0.125;
		gbc_xmpDcCreators.anchor = GridBagConstraints.WEST;
		gbc_xmpDcCreators.fill = GridBagConstraints.BOTH;
		gbc_xmpDcCreators.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcCreators.gridx = 2;
		gbc_xmpDcCreators.gridy = 2;
		xmpDcMetaPanel.add(xmpDcCreators, gbc_xmpDcCreators);

		JLabel lblContributors = new JLabel("Contributors");
		GridBagConstraints gbc_lblContributors = new GridBagConstraints();
		gbc_lblContributors.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblContributors.insets = new Insets(0, 0, 5, 5);
		gbc_lblContributors.gridx = 0;
		gbc_lblContributors.gridy = 3;
		xmpDcMetaPanel.add(lblContributors, gbc_lblContributors);
		
		xmpDcContributorsEnabled = new JCheckBox("");
		xmpDcContributorsEnabled.setEnabled(false);
		xmpDcContributorsEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcContributorsEnabled = new GridBagConstraints();
		gbc_xmpDcContributorsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcContributorsEnabled.gridx = 1;
		gbc_xmpDcContributorsEnabled.gridy = 3;
		xmpDcMetaPanel.add(xmpDcContributorsEnabled, gbc_xmpDcContributorsEnabled);

		xmpDcContributors = new JTextArea();
		GridBagConstraints gbc_xmpDcContributors = new GridBagConstraints();
		gbc_xmpDcContributors.weighty = 0.125;
		gbc_xmpDcContributors.anchor = GridBagConstraints.WEST;
		gbc_xmpDcContributors.fill = GridBagConstraints.BOTH;
		gbc_xmpDcContributors.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcContributors.gridx = 2;
		gbc_xmpDcContributors.gridy = 3;
		xmpDcMetaPanel.add(xmpDcContributors, gbc_xmpDcContributors);

		JLabel lblCoverage = new JLabel("Coverage");
		GridBagConstraints gbc_lblCoverage = new GridBagConstraints();
		gbc_lblCoverage.anchor = GridBagConstraints.EAST;
		gbc_lblCoverage.insets = new Insets(0, 0, 5, 5);
		gbc_lblCoverage.gridx = 0;
		gbc_lblCoverage.gridy = 4;
		xmpDcMetaPanel.add(lblCoverage, gbc_lblCoverage);
		
		xmpDcCoverageEnabled = new JCheckBox("");
		xmpDcCoverageEnabled.setEnabled(false);
		xmpDcCoverageEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcCoverageEnabled = new GridBagConstraints();
		gbc_xmpDcCoverageEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcCoverageEnabled.gridx = 1;
		gbc_xmpDcCoverageEnabled.gridy = 4;
		xmpDcMetaPanel.add(xmpDcCoverageEnabled, gbc_xmpDcCoverageEnabled);

		xmpDcCoverage = new JTextField();
		GridBagConstraints gbc_xmpDcCoverage = new GridBagConstraints();
		gbc_xmpDcCoverage.anchor = GridBagConstraints.WEST;
		gbc_xmpDcCoverage.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcCoverage.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcCoverage.gridx = 2;
		gbc_xmpDcCoverage.gridy = 4;
		xmpDcMetaPanel.add(xmpDcCoverage, gbc_xmpDcCoverage);
		xmpDcCoverage.setColumns(10);

		JLabel lblDates = new JLabel("Dates");
		GridBagConstraints gbc_lblDates = new GridBagConstraints();
		gbc_lblDates.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblDates.insets = new Insets(0, 0, 5, 5);
		gbc_lblDates.gridx = 0;
		gbc_lblDates.gridy = 5;
		xmpDcMetaPanel.add(lblDates, gbc_lblDates);
		
		xmpDcDatesEnabled = new JCheckBox("");
		xmpDcDatesEnabled.setEnabled(false);
		xmpDcDatesEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcDatesEnabled = new GridBagConstraints();
		gbc_xmpDcDatesEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcDatesEnabled.gridx = 1;
		gbc_xmpDcDatesEnabled.gridy = 5;
		xmpDcMetaPanel.add(xmpDcDatesEnabled, gbc_xmpDcDatesEnabled);

		xmpDcDates = new JTextArea();
		xmpDcDates.setEditable(false);
		GridBagConstraints gbc_xmpDcDates = new GridBagConstraints();
		gbc_xmpDcDates.weighty = 0.125;
		gbc_xmpDcDates.anchor = GridBagConstraints.WEST;
		gbc_xmpDcDates.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcDates.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcDates.gridx = 2;
		gbc_xmpDcDates.gridy = 5;
		xmpDcMetaPanel.add(xmpDcDates, gbc_xmpDcDates);
		xmpDcDates.setColumns(10);

		JLabel lblFormat = new JLabel("Format");
		GridBagConstraints gbc_lblFormat = new GridBagConstraints();
		gbc_lblFormat.anchor = GridBagConstraints.EAST;
		gbc_lblFormat.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormat.gridx = 0;
		gbc_lblFormat.gridy = 6;
		xmpDcMetaPanel.add(lblFormat, gbc_lblFormat);
		
		xmpDcFormatEnabled = new JCheckBox("");
		xmpDcFormatEnabled.setEnabled(false);
		xmpDcFormatEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcFormatEnabled = new GridBagConstraints();
		gbc_xmpDcFormatEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcFormatEnabled.gridx = 1;
		gbc_xmpDcFormatEnabled.gridy = 6;
		xmpDcMetaPanel.add(xmpDcFormatEnabled, gbc_xmpDcFormatEnabled);

		xmpDcFormat = new JTextField();
		GridBagConstraints gbc_xmpDcFormat = new GridBagConstraints();
		gbc_xmpDcFormat.anchor = GridBagConstraints.WEST;
		gbc_xmpDcFormat.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcFormat.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcFormat.gridx = 2;
		gbc_xmpDcFormat.gridy = 6;
		xmpDcMetaPanel.add(xmpDcFormat, gbc_xmpDcFormat);
		xmpDcFormat.setColumns(10);

		JLabel lblIdentifier = new JLabel("Identifier");
		GridBagConstraints gbc_lblIdentifier = new GridBagConstraints();
		gbc_lblIdentifier.anchor = GridBagConstraints.EAST;
		gbc_lblIdentifier.insets = new Insets(0, 0, 5, 5);
		gbc_lblIdentifier.gridx = 0;
		gbc_lblIdentifier.gridy = 7;
		xmpDcMetaPanel.add(lblIdentifier, gbc_lblIdentifier);
		
		xmpDcIdentifierEnabled = new JCheckBox("");
		xmpDcIdentifierEnabled.setEnabled(false);
		xmpDcIdentifierEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcIdentifierEnabled = new GridBagConstraints();
		gbc_xmpDcIdentifierEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcIdentifierEnabled.gridx = 1;
		gbc_xmpDcIdentifierEnabled.gridy = 7;
		xmpDcMetaPanel.add(xmpDcIdentifierEnabled, gbc_xmpDcIdentifierEnabled);

		xmpDcIdentifier = new JTextField();
		GridBagConstraints gbc_xmpDcIdentifier = new GridBagConstraints();
		gbc_xmpDcIdentifier.anchor = GridBagConstraints.WEST;
		gbc_xmpDcIdentifier.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcIdentifier.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcIdentifier.gridx = 2;
		gbc_xmpDcIdentifier.gridy = 7;
		xmpDcMetaPanel.add(xmpDcIdentifier, gbc_xmpDcIdentifier);
		xmpDcIdentifier.setColumns(10);

		JLabel lblLanguages = new JLabel("Languages");
		GridBagConstraints gbc_lblLanguages = new GridBagConstraints();
		gbc_lblLanguages.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblLanguages.insets = new Insets(0, 0, 5, 5);
		gbc_lblLanguages.gridx = 0;
		gbc_lblLanguages.gridy = 8;
		xmpDcMetaPanel.add(lblLanguages, gbc_lblLanguages);
		
		xmpDcLanguagesEnabled = new JCheckBox("");
		xmpDcLanguagesEnabled.setEnabled(false);
		xmpDcLanguagesEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcLanguagesEnabled = new GridBagConstraints();
		gbc_xmpDcLanguagesEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcLanguagesEnabled.gridx = 1;
		gbc_xmpDcLanguagesEnabled.gridy = 8;
		xmpDcMetaPanel.add(xmpDcLanguagesEnabled, gbc_xmpDcLanguagesEnabled);

		xmpDcLanguages = new JTextArea();
		GridBagConstraints gbc_xmpDcLanguages = new GridBagConstraints();
		gbc_xmpDcLanguages.weighty = 0.125;
		gbc_xmpDcLanguages.anchor = GridBagConstraints.WEST;
		gbc_xmpDcLanguages.fill = GridBagConstraints.BOTH;
		gbc_xmpDcLanguages.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcLanguages.gridx = 2;
		gbc_xmpDcLanguages.gridy = 8;
		xmpDcMetaPanel.add(xmpDcLanguages, gbc_xmpDcLanguages);

		JLabel lblPublishers = new JLabel("Publishers");
		GridBagConstraints gbc_lblPublishers = new GridBagConstraints();
		gbc_lblPublishers.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPublishers.insets = new Insets(0, 0, 5, 5);
		gbc_lblPublishers.gridx = 0;
		gbc_lblPublishers.gridy = 9;
		xmpDcMetaPanel.add(lblPublishers, gbc_lblPublishers);
		
		xmpDcPublishersEnabled = new JCheckBox("");
		xmpDcPublishersEnabled.setEnabled(false);
		xmpDcPublishersEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcPublishersEnabled = new GridBagConstraints();
		gbc_xmpDcPublishersEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcPublishersEnabled.gridx = 1;
		gbc_xmpDcPublishersEnabled.gridy = 9;
		xmpDcMetaPanel.add(xmpDcPublishersEnabled, gbc_xmpDcPublishersEnabled);

		xmpDcPublishers = new JTextArea();
		GridBagConstraints gbc_xmpDcPublishers = new GridBagConstraints();
		gbc_xmpDcPublishers.weighty = 0.125;
		gbc_xmpDcPublishers.anchor = GridBagConstraints.WEST;
		gbc_xmpDcPublishers.fill = GridBagConstraints.BOTH;
		gbc_xmpDcPublishers.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcPublishers.gridx = 2;
		gbc_xmpDcPublishers.gridy = 9;
		xmpDcMetaPanel.add(xmpDcPublishers, gbc_xmpDcPublishers);

		JLabel lblRelationships = new JLabel("Relationships");
		lblRelationships.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblRelationships = new GridBagConstraints();
		gbc_lblRelationships.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblRelationships.insets = new Insets(0, 0, 5, 5);
		gbc_lblRelationships.gridx = 0;
		gbc_lblRelationships.gridy = 10;
		xmpDcMetaPanel.add(lblRelationships, gbc_lblRelationships);
		
		xmpDcRelationshipsEnabled = new JCheckBox("");
		xmpDcRelationshipsEnabled.setEnabled(false);
		xmpDcRelationshipsEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcRelationshipsEnabled = new GridBagConstraints();
		gbc_xmpDcRelationshipsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcRelationshipsEnabled.gridx = 1;
		gbc_xmpDcRelationshipsEnabled.gridy = 10;
		xmpDcMetaPanel.add(xmpDcRelationshipsEnabled, gbc_xmpDcRelationshipsEnabled);

		xmpDcRelationships = new JTextArea();
		GridBagConstraints gbc_xmpDcRelationships = new GridBagConstraints();
		gbc_xmpDcRelationships.weighty = 0.125;
		gbc_xmpDcRelationships.anchor = GridBagConstraints.WEST;
		gbc_xmpDcRelationships.fill = GridBagConstraints.BOTH;
		gbc_xmpDcRelationships.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcRelationships.gridx = 2;
		gbc_xmpDcRelationships.gridy = 10;
		xmpDcMetaPanel.add(xmpDcRelationships, gbc_xmpDcRelationships);

		JLabel lblRights = new JLabel("Rights");
		GridBagConstraints gbc_lblRights = new GridBagConstraints();
		gbc_lblRights.anchor = GridBagConstraints.EAST;
		gbc_lblRights.insets = new Insets(0, 0, 5, 5);
		gbc_lblRights.gridx = 0;
		gbc_lblRights.gridy = 11;
		xmpDcMetaPanel.add(lblRights, gbc_lblRights);
		
		xmpDcRightsEnabled = new JCheckBox("");
		xmpDcRightsEnabled.setEnabled(false);
		xmpDcRightsEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcRightsEnabled = new GridBagConstraints();
		gbc_xmpDcRightsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcRightsEnabled.gridx = 1;
		gbc_xmpDcRightsEnabled.gridy = 11;
		xmpDcMetaPanel.add(xmpDcRightsEnabled, gbc_xmpDcRightsEnabled);

		xmpDcRights = new JTextField();
		GridBagConstraints gbc_xmpDcRights = new GridBagConstraints();
		gbc_xmpDcRights.anchor = GridBagConstraints.WEST;
		gbc_xmpDcRights.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcRights.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcRights.gridx = 2;
		gbc_xmpDcRights.gridy = 11;
		xmpDcMetaPanel.add(xmpDcRights, gbc_xmpDcRights);
		xmpDcRights.setColumns(10);

		JLabel lblSource = new JLabel("Source");
		GridBagConstraints gbc_lblSource = new GridBagConstraints();
		gbc_lblSource.anchor = GridBagConstraints.EAST;
		gbc_lblSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblSource.gridx = 0;
		gbc_lblSource.gridy = 12;
		xmpDcMetaPanel.add(lblSource, gbc_lblSource);
		
		xmpDcSourceEnabled = new JCheckBox("");
		xmpDcSourceEnabled.setEnabled(false);
		xmpDcSourceEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcSourceEnabled = new GridBagConstraints();
		gbc_xmpDcSourceEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcSourceEnabled.gridx = 1;
		gbc_xmpDcSourceEnabled.gridy = 12;
		xmpDcMetaPanel.add(xmpDcSourceEnabled, gbc_xmpDcSourceEnabled);

		xmpDcSource = new JTextField();
		GridBagConstraints gbc_xmpDcSource = new GridBagConstraints();
		gbc_xmpDcSource.anchor = GridBagConstraints.WEST;
		gbc_xmpDcSource.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcSource.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcSource.gridx = 2;
		gbc_xmpDcSource.gridy = 12;
		xmpDcMetaPanel.add(xmpDcSource, gbc_xmpDcSource);
		xmpDcSource.setColumns(10);

		JLabel lblSubjects = new JLabel("Subjects");
		GridBagConstraints gbc_lblSubjects = new GridBagConstraints();
		gbc_lblSubjects.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblSubjects.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubjects.gridx = 0;
		gbc_lblSubjects.gridy = 13;
		xmpDcMetaPanel.add(lblSubjects, gbc_lblSubjects);
		
		xmpDcSubjectsEnabled = new JCheckBox("");
		xmpDcSubjectsEnabled.setEnabled(false);
		xmpDcSubjectsEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcSubjectsEnabled = new GridBagConstraints();
		gbc_xmpDcSubjectsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcSubjectsEnabled.gridx = 1;
		gbc_xmpDcSubjectsEnabled.gridy = 13;
		xmpDcMetaPanel.add(xmpDcSubjectsEnabled, gbc_xmpDcSubjectsEnabled);

		xmpDcSubjects = new JTextArea();
		GridBagConstraints gbc_xmpDcSubjects = new GridBagConstraints();
		gbc_xmpDcSubjects.weighty = 0.125;
		gbc_xmpDcSubjects.anchor = GridBagConstraints.WEST;
		gbc_xmpDcSubjects.fill = GridBagConstraints.BOTH;
		gbc_xmpDcSubjects.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcSubjects.gridx = 2;
		gbc_xmpDcSubjects.gridy = 13;
		xmpDcMetaPanel.add(xmpDcSubjects, gbc_xmpDcSubjects);

		JLabel lblTypes = new JLabel("Types");
		GridBagConstraints gbc_lblTypes = new GridBagConstraints();
		gbc_lblTypes.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblTypes.insets = new Insets(0, 0, 0, 5);
		gbc_lblTypes.gridx = 0;
		gbc_lblTypes.gridy = 14;
		xmpDcMetaPanel.add(lblTypes, gbc_lblTypes);
		
		xmpDcTypesEnabled = new JCheckBox("");
		xmpDcTypesEnabled.setEnabled(false);
		xmpDcTypesEnabled.setSelected(true);
		GridBagConstraints gbc_xmpDcTypesEnabled = new GridBagConstraints();
		gbc_xmpDcTypesEnabled.insets = new Insets(0, 0, 0, 5);
		gbc_xmpDcTypesEnabled.gridx = 1;
		gbc_xmpDcTypesEnabled.gridy = 14;
		xmpDcMetaPanel.add(xmpDcTypesEnabled, gbc_xmpDcTypesEnabled);

		xmpDcTypes = new JTextArea();
		GridBagConstraints gbc_xmpDcTypes = new GridBagConstraints();
		gbc_xmpDcTypes.insets = new Insets(0, 0, 5, 5);
		gbc_xmpDcTypes.weighty = 0.125;
		gbc_xmpDcTypes.anchor = GridBagConstraints.WEST;
		gbc_xmpDcTypes.fill = GridBagConstraints.BOTH;
		gbc_xmpDcTypes.gridx = 2;
		gbc_xmpDcTypes.gridy = 14;
		xmpDcMetaPanel.add(xmpDcTypes, gbc_xmpDcTypes);
		
		
		JScrollPane xmpRightsScrollpane = new JScrollPane();
		xmpRightsScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP Rights", null, xmpRightsScrollpane, null);

		xmpRightsMetaPanel = new JPanel();
		xmpRightsScrollpane.setViewportView(xmpRightsMetaPanel);
		GridBagLayout gbl_xmpRightsMetaPanel = new GridBagLayout();
		gbl_xmpRightsMetaPanel.columnWidths = new int[] {112, 0, 284, 0};
		gbl_xmpRightsMetaPanel.rowHeights = new int[] { 16, 26, 26, 26, 26, 0 };
		gbl_xmpRightsMetaPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_xmpRightsMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		xmpRightsMetaPanel.setLayout(gbl_xmpRightsMetaPanel);

		JLabel lblRightsCertificate = new JLabel("Certificate");
		GridBagConstraints gbc_lblRightsCertificate = new GridBagConstraints();
		gbc_lblRightsCertificate.anchor = GridBagConstraints.EAST;
		gbc_lblRightsCertificate.insets = new Insets(0, 0, 5, 5);
		gbc_lblRightsCertificate.gridx = 0;
		gbc_lblRightsCertificate.gridy = 0;
		xmpRightsMetaPanel.add(lblRightsCertificate, gbc_lblRightsCertificate);
		
		xmpRightsCertificateEnabled = new JCheckBox("");
		xmpRightsCertificateEnabled.setEnabled(false);
		xmpRightsCertificateEnabled.setSelected(true);
		GridBagConstraints gbc_xmpRightsCertificateEnabled = new GridBagConstraints();
		gbc_xmpRightsCertificateEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpRightsCertificateEnabled.gridx = 1;
		gbc_xmpRightsCertificateEnabled.gridy = 0;
		xmpRightsMetaPanel.add(xmpRightsCertificateEnabled, gbc_xmpRightsCertificateEnabled);

		xmpRightsCertificate = new JTextField();
		GridBagConstraints gbc_xmpRightsCertificate = new GridBagConstraints();
		gbc_xmpRightsCertificate.anchor = GridBagConstraints.WEST;
		gbc_xmpRightsCertificate.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpRightsCertificate.insets = new Insets(0, 0, 5, 0);
		gbc_xmpRightsCertificate.gridx = 2;
		gbc_xmpRightsCertificate.gridy = 0;
		xmpRightsMetaPanel.add(xmpRightsCertificate, gbc_xmpRightsCertificate);
		xmpRightsCertificate.setColumns(10);

		JLabel lblRightsMarked = new JLabel("Marked");
		GridBagConstraints gbc_lblRightsMarked = new GridBagConstraints();
		gbc_lblRightsMarked.anchor = GridBagConstraints.EAST;
		gbc_lblRightsMarked.insets = new Insets(0, 0, 5, 5);
		gbc_lblRightsMarked.gridx = 0;
		gbc_lblRightsMarked.gridy = 1;
		xmpRightsMetaPanel.add(lblRightsMarked, gbc_lblRightsMarked);
		
		xmpRightsMarkedEnabled = new JCheckBox("");
		xmpRightsMarkedEnabled.setEnabled(false);
		xmpRightsMarkedEnabled.setSelected(true);
		GridBagConstraints gbc_xmpRightsMarkedEnabled = new GridBagConstraints();
		gbc_xmpRightsMarkedEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpRightsMarkedEnabled.gridx = 1;
		gbc_xmpRightsMarkedEnabled.gridy = 1;
		xmpRightsMetaPanel.add(xmpRightsMarkedEnabled, gbc_xmpRightsMarkedEnabled);

		xmpRightsMarked = new JComboBox();
		xmpRightsMarked.setModel(new DefaultComboBoxModel(new String[] { "Unset", "Yes", "No" }));
		GridBagConstraints gbc_xmpRightsMarked = new GridBagConstraints();
		gbc_xmpRightsMarked.anchor = GridBagConstraints.WEST;
		gbc_xmpRightsMarked.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpRightsMarked.insets = new Insets(0, 0, 5, 0);
		gbc_xmpRightsMarked.gridx = 2;
		gbc_xmpRightsMarked.gridy = 1;
		xmpRightsMetaPanel.add(xmpRightsMarked, gbc_xmpRightsMarked);

		JLabel lblRightsOwner = new JLabel("Owners");
		GridBagConstraints gbc_lblRightsOwner = new GridBagConstraints();
		gbc_lblRightsOwner.anchor = GridBagConstraints.EAST;
		gbc_lblRightsOwner.insets = new Insets(0, 0, 5, 5);
		gbc_lblRightsOwner.gridx = 0;
		gbc_lblRightsOwner.gridy = 2;
		xmpRightsMetaPanel.add(lblRightsOwner, gbc_lblRightsOwner);
		
		xmpRightsOwnerEnabled = new JCheckBox("");
		xmpRightsOwnerEnabled.setEnabled(false);
		xmpRightsOwnerEnabled.setSelected(true);
		GridBagConstraints gbc_xmpRightsOwnerEnabled = new GridBagConstraints();
		gbc_xmpRightsOwnerEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpRightsOwnerEnabled.gridx = 1;
		gbc_xmpRightsOwnerEnabled.gridy = 2;
		xmpRightsMetaPanel.add(xmpRightsOwnerEnabled, gbc_xmpRightsOwnerEnabled);

		JScrollPane xmpRightsOwnerScroll = new JScrollPane();
		GridBagConstraints gbc_xmpRightsOwner = new GridBagConstraints();
		gbc_xmpRightsOwner.weighty = 1.0;
		gbc_xmpRightsOwner.weightx = 1.0;
		gbc_xmpRightsOwner.anchor = GridBagConstraints.WEST;
		gbc_xmpRightsOwner.fill = GridBagConstraints.BOTH;
		gbc_xmpRightsOwner.insets = new Insets(0, 0, 5, 0);
		gbc_xmpRightsOwner.gridx = 2;
		gbc_xmpRightsOwner.gridy = 2;
		xmpRightsMetaPanel.add(xmpRightsOwnerScroll, gbc_xmpRightsOwner);
		
		xmpRightsOwner = new JTextArea();
		xmpRightsOwnerScroll.setViewportView(xmpRightsOwner);
		xmpRightsOwner.setWrapStyleWord(true);
		xmpRightsOwner.setLineWrap(true);
		xmpRightsOwner.setColumns(10);

		JLabel lblRightsCopyright = new JLabel("Copyright");
		GridBagConstraints gbc_lblRightsCopyright = new GridBagConstraints();
		gbc_lblRightsCopyright.anchor = GridBagConstraints.EAST;
		gbc_lblRightsCopyright.insets = new Insets(0, 0, 5, 5);
		gbc_lblRightsCopyright.gridx = 0;
		gbc_lblRightsCopyright.gridy = 3;
		xmpRightsMetaPanel.add(lblRightsCopyright, gbc_lblRightsCopyright);
		
		xmpRightsCopyrightEnabled = new JCheckBox("");
		xmpRightsCopyrightEnabled.setEnabled(false);
		xmpRightsCopyrightEnabled.setSelected(true);
		GridBagConstraints gbc_xmpRightsCopyrightEnabled = new GridBagConstraints();
		gbc_xmpRightsCopyrightEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpRightsCopyrightEnabled.gridx = 1;
		gbc_xmpRightsCopyrightEnabled.gridy = 3;
		xmpRightsMetaPanel.add(xmpRightsCopyrightEnabled, gbc_xmpRightsCopyrightEnabled);

		JScrollPane xmpRightsCopyrightScroll = new JScrollPane();
		GridBagConstraints gbc_xmpRightsCopyright = new GridBagConstraints();
		gbc_xmpRightsCopyright.weighty = 1.0;
		gbc_xmpRightsCopyright.weightx = 1.0;
		gbc_xmpRightsCopyright.anchor = GridBagConstraints.WEST;
		gbc_xmpRightsCopyright.fill = GridBagConstraints.BOTH;
		gbc_xmpRightsCopyright.insets = new Insets(0, 0, 5, 0);
		gbc_xmpRightsCopyright.gridx = 2;
		gbc_xmpRightsCopyright.gridy = 3;
		xmpRightsMetaPanel.add(xmpRightsCopyrightScroll, gbc_xmpRightsCopyright);
		
		xmpRightsCopyright = new JTextArea();
		xmpRightsCopyrightScroll.setViewportView(xmpRightsCopyright);
		xmpRightsCopyright.setWrapStyleWord(true);
		xmpRightsCopyright.setLineWrap(true);
		xmpRightsCopyright.setColumns(10);

		
		JLabel lblRightsUsageTerms = new JLabel("Usage Terms");
		GridBagConstraints gbc_lblRightsUsageTerms = new GridBagConstraints();
		gbc_lblRightsUsageTerms.anchor = GridBagConstraints.EAST;
		gbc_lblRightsUsageTerms.insets = new Insets(0, 0, 5, 5);
		gbc_lblRightsUsageTerms.gridx = 0;
		gbc_lblRightsUsageTerms.gridy = 4;
		xmpRightsMetaPanel.add(lblRightsUsageTerms, gbc_lblRightsUsageTerms);
		
		xmpRightsUsageTermsEnabled = new JCheckBox("");
		xmpRightsUsageTermsEnabled.setEnabled(false);
		xmpRightsUsageTermsEnabled.setSelected(true);
		GridBagConstraints gbc_xmpRightsUsageTermsEnabled = new GridBagConstraints();
		gbc_xmpRightsUsageTermsEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpRightsUsageTermsEnabled.gridx = 1;
		gbc_xmpRightsUsageTermsEnabled.gridy = 4;
		xmpRightsMetaPanel.add(xmpRightsUsageTermsEnabled, gbc_xmpRightsUsageTermsEnabled);

		JScrollPane xmpRightsUsageTermsScroll = new JScrollPane();
		GridBagConstraints gbc_xmpRightsUsageTerms = new GridBagConstraints();
		gbc_xmpRightsUsageTerms.weighty = 1.0;
		gbc_xmpRightsUsageTerms.weightx = 1.0;
		gbc_xmpRightsUsageTerms.anchor = GridBagConstraints.WEST;
		gbc_xmpRightsUsageTerms.fill = GridBagConstraints.BOTH;
		gbc_xmpRightsUsageTerms.insets = new Insets(0, 0, 5, 0);
		gbc_xmpRightsUsageTerms.gridx = 2;
		gbc_xmpRightsUsageTerms.gridy = 4;
		xmpRightsMetaPanel.add(xmpRightsUsageTermsScroll, gbc_xmpRightsUsageTerms);

		xmpRightsUsageTerms = new JTextArea();
		xmpRightsUsageTermsScroll.setViewportView(xmpRightsUsageTerms);
		xmpRightsUsageTerms.setWrapStyleWord(true);
		xmpRightsUsageTerms.setLineWrap(true);
		xmpRightsUsageTerms.setColumns(10);
	
		JLabel lblRightsWebStatement = new JLabel("Web Statement");
		GridBagConstraints gbc_lblRightsWebStatement = new GridBagConstraints();
		gbc_lblRightsWebStatement.anchor = GridBagConstraints.EAST;
		gbc_lblRightsWebStatement.insets = new Insets(0, 0, 5, 5);
		gbc_lblRightsWebStatement.gridx = 0;
		gbc_lblRightsWebStatement.gridy = 5;
		xmpRightsMetaPanel.add(lblRightsWebStatement, gbc_lblRightsWebStatement);
		
		xmpRightsWebStatementEnabled = new JCheckBox("");
		xmpRightsWebStatementEnabled.setEnabled(false);
		xmpRightsWebStatementEnabled.setSelected(true);
		GridBagConstraints gbc_xmpRightsWebStatementEnabled = new GridBagConstraints();
		gbc_xmpRightsWebStatementEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_xmpRightsWebStatementEnabled.gridx = 1;
		gbc_xmpRightsWebStatementEnabled.gridy = 5;
		xmpRightsMetaPanel.add(xmpRightsWebStatementEnabled, gbc_xmpRightsWebStatementEnabled);

		xmpRightsWebStatement = new JTextField();
		GridBagConstraints gbc_xmpRightsWebStatement = new GridBagConstraints();
		gbc_xmpRightsWebStatement.anchor = GridBagConstraints.WEST;
		gbc_xmpRightsWebStatement.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpRightsWebStatement.insets = new Insets(0, 0, 5, 0);
		gbc_xmpRightsWebStatement.gridx = 2;
		gbc_xmpRightsWebStatement.gridy = 5;
		xmpRightsMetaPanel.add(xmpRightsWebStatement, gbc_xmpRightsWebStatement);
		
		
		// Make rating digits only
		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {
		    @Override
		    public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) 
		        throws BadLocationException 
		    {
		        fb.insertString(off, str.replaceAll("\\D++", ""), attr);  // remove non-digits
		    } 
		    @Override
		    public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr) 
		        throws BadLocationException 
		    {
		        fb.replace(off, len, str.replaceAll("\\D++", ""), attr);  // remove non-digits
		    }
		});

		xmpBasicRating.setDocument(doc);
		
	}

	private void traverseFields(MetadataEditPane.FieldSetGet setGet, MetadataEditPane.FieldEnabledCheckBox fieldEnabled) {
		for (Field field : this.getClass().getFields()) {
			if(setGet != null){
				FieldID annos = field.getAnnotation(FieldID.class);
				if (annos != null) {
					if (annos.value() != null && annos.value().length() > 0) {
						Object f = null;
						try {
							f = field.get(this);
						} catch (IllegalArgumentException e) {
							System.err.println("traverseFields on (" + annos.value() + ")");
							e.printStackTrace();
							continue;
						} catch (IllegalAccessException e) {
							System.err.println("traverseFields on (" + annos.value() + ")");
							e.printStackTrace();
							continue;
						}
						setGet.apply(f, annos);
					}
				}
			}
			if(fieldEnabled != null){
				FieldEnabled annosEnabled = field.getAnnotation(FieldEnabled.class);
				if (annosEnabled != null) {
					try {
						JCheckBox f = (JCheckBox) field.get(this);
						fieldEnabled.apply(f, annosEnabled);
					} catch (IllegalArgumentException e) {
						System.err.println("traverseFields on (" + annosEnabled.value() + ")");
						e.printStackTrace();
						continue;
					} catch (IllegalAccessException e) {
						System.err.println("traverseFields on (" + annosEnabled.value() + ")");
						e.printStackTrace();
						continue;
					}
				}
			}
		}
	}
	
	
	public void showEnabled(final boolean show) {
		traverseFields(null, new MetadataEditPane.FieldEnabledCheckBox() {
			
			@Override
			public void apply(JCheckBox field, FieldEnabled anno) {
				field.setVisible(show);
				field.setEnabled(show);
				
			}
		});
	}

	public void disableEdit() {
		traverseFields(new MetadataEditPane.FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {
				if (field instanceof JComponent) {
					((JComponent) field).setEnabled(false);
				}
			}
		}, null);
	}

	void clear() {
		traverseFields(new MetadataEditPane.FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {
				if (field instanceof JTextField) {
					((JTextField) field).setText(null);
				}
				if (field instanceof JTextArea) {
					((JTextArea) field).setText(null);
				}
				if (field instanceof JComboBox) {
					objectToField((JComboBox) field, null, anno.type() == FieldID.FieldType.BoolField);
				}
				if (field instanceof JDateChooser) {
					objectToField((JDateChooser) field, null);
				}
				if (field instanceof JSpinner) {
					objectToField((JSpinner) field, null);
				}
			}
		}, new MetadataEditPane.FieldEnabledCheckBox() {			
			@Override
			public void apply(JCheckBox field, FieldEnabled anno) {
				field.setSelected(true);
				
			}
		});
	}

	void fillFromMetadata(final MetadataInfo metadataInfo) {

		traverseFields(new MetadataEditPane.FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {

				if (field instanceof JTextField) {
					((JTextField) field).setText(metadataInfo.getString(anno.value()));
				}
				if (field instanceof JTextArea) {
					((JTextArea) field).setText(metadataInfo.getString(anno.value()));
				}

				Object value = metadataInfo.get(anno.value());
				if (field instanceof JComboBox) {
					objectToField((JComboBox) field, value, anno.type() == FieldID.FieldType.BoolField);
				}
				if (field instanceof JDateChooser) {
					objectToField((JDateChooser) field, value);
				}
				if (field instanceof JSpinner) {
					objectToField((JSpinner) field, value);
				}
			}
		}, new MetadataEditPane.FieldEnabledCheckBox() {			
			@Override
			public void apply(JCheckBox field, FieldEnabled anno) {
				field.setSelected(metadataInfo.isEnabled(anno.value()));
				
			}
		});

	}

	void copyToMetadata(final MetadataInfo metadataInfo) {

		traverseFields(new MetadataEditPane.FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {

				if (field instanceof JTextField || field instanceof JTextArea) {
					String text = (field instanceof JTextField) ? ((JTextField) field).getText()
							: ((JTextArea) field).getText();
					if (text.length() == 0) {
						text = null;
					}
					metadataInfo.setFromString(anno.value(), text);
				}
				if (field instanceof JSpinner) {
					switch (anno.type()) {
					case IntField:
						Integer i = (Integer) ((JSpinner) field).getModel().getValue();
						metadataInfo.set(anno.value(), i);
						break;
					default:
						throw new RuntimeException("Cannot store Integer in :" + anno.type());

					}
				}
				if (field instanceof JComboBox) {
					String text = (String) ((JComboBox) field).getModel().getSelectedItem();
					if (text != null && text.length() == 0) {
						text = null;
					}
					switch (anno.type()) {
					case StringField:
						metadataInfo.set(anno.value(), text);
						break;
					case BoolField:
						metadataInfo.setFromString(anno.value(), text);
						break;
					default:
						throw new RuntimeException("Cannot (store (choice text) in :" + anno.type());

					}
				}
				if (field instanceof JDateChooser) {
					switch (anno.type()) {
					case DateField:
						metadataInfo.set(anno.value(), ((JDateChooser) field).getCalendar());
						break;
					default:
						throw new RuntimeException("Cannot store Calendar in :" + anno.type());

					}
				}
			}
		}, new MetadataEditPane.FieldEnabledCheckBox() {			
			@Override
			public void apply(JCheckBox field, FieldEnabled anno) {
				metadataInfo.setEnabled(anno.value(), field.isSelected());
				
			}
		});

	}

	private void objectToField(JComboBox field, Object o, boolean oIsBool) {
		if (o instanceof String) {
			field.getModel().setSelectedItem(o);
		}else if (o instanceof Boolean || oIsBool) {
			String v = "Unset";
			if( o != null ){
				v = (Boolean)o ? "Yes" : "No";
			}
			field.getModel().setSelectedItem(v);
		} else if (o == null) {
			field.setSelectedIndex(-1);
		} else {
			RuntimeException e = new RuntimeException("Cannot store non-String object in JComboBox");
			e.printStackTrace();
			throw e;
		}
	}

	private void objectToField(JDateChooser field, Object o) {
		if (o instanceof Calendar) {
			field.setCalendar((Calendar) o);
		} else if (o == null) {
			field.setCalendar(null);
		} else {
			RuntimeException e = new RuntimeException("Cannot store non-Calendar object in JDateChooser");
			e.printStackTrace();
			throw e;
		}
	}

	private void objectToField(JSpinner field, Object o) {
		if (o instanceof Integer) {
			field.setValue((Integer) o);
		} else if (o == null) {
			field.setValue((Integer) 0);
		} else {
			RuntimeException e = new RuntimeException("Cannot store non-Integerr object in JSpinner");
			e.printStackTrace();
			throw e;
		}
	}

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MetadataEditPane pane = new MetadataEditPane();
					JFrame frame = new JFrame();
					frame.getContentPane().add(pane.tabbedaPane);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setSize(640, 480);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
