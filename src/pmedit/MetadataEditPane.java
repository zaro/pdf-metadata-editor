package pmedit;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;

import pmedit.PDFMetadataEditWindow.FieldSetGet;
import net.miginfocom.swing.MigLayout;

import com.toedter.calendar.JDateChooser;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JEditorPane;

public class MetadataEditPane {

	private static final long serialVersionUID = 6994489903939856136L;
	public JPanel basicMetaPanel;

	@FieldID("basic.title")
	public JTextField basicTitle;
	@FieldID("basic.author")
	public JTextField basicAuthor;
	@FieldID("basic.subject")
	public JTextArea basicSubject;
	@FieldID(value = "basic.keywords")
	public JTextArea basicKeywords;
	@FieldID("basic.creator")
	public JTextField basicCreator;
	@FieldID("basic.producer")
	public JTextField basicProducer;
	@FieldID("basic.trapped")
	public JComboBox basicTrapped;
	@FieldID(value = "basic.creationDate", type = FieldID.FieldType.DateField)
	public JDateChooser basicCreationDate;
	@FieldID(value = "basic.modificationDate", type = FieldID.FieldType.DateField)
	public JDateChooser basicModificationDate;

	@FieldID("xmpBasic.creatorTool")
	public JTextField xmpBasicCreatorTool;
	@FieldID("xmpBasic.baseURL")
	public JTextField xmpBasicBaseURL;
	@FieldID("xmpBasic.label")
	public JTextField xmpBasicLabel;
	@FieldID("xmpBasic.nickname")
	public JTextField xmpBasicNickname;
	@FieldID(value = "xmpBasic.rating", type = FieldID.FieldType.IntField)
	public JSpinner xmpBasicRating;
	@FieldID("xmpBasic.title")
	public JTextField xmpBasicTitle;
	@FieldID(value = "xmpBasic.identifiers", type = FieldID.FieldType.TextField)
	public JTextArea xmpBasicIdentifiers;
	@FieldID(value = "xmpBasic.advisories", type = FieldID.FieldType.TextField)
	public JTextArea xmpBasicAdvisories;
	@FieldID(value = "xmpBasic.modifyDate", type = FieldID.FieldType.DateField)
	public JDateChooser xmpBasicModifyDate;
	@FieldID(value = "xmpBasic.createDate", type = FieldID.FieldType.DateField)
	public JDateChooser xmpBasicCreateDate;
	@FieldID(value = "xmpBasic.metadataDate", type = FieldID.FieldType.DateField)
	public JDateChooser xmpBasicMetadataDate;

	@FieldID("xmpPdf.keywords")
	public JTextArea xmpPdfKeywords;
	@FieldID("xmpPdf.pdfVersion")
	public JTextField xmpPdfVersion;
	@FieldID("xmpPdf.producer")
	public JTextField xmpPdfProducer;

	@FieldID("xmpDc.title")
	public JTextField xmpDcTitle;
	@FieldID("xmpDc.coverage")
	public JTextField xmpDcCoverage;
	@FieldID("xmpDc.description")
	public JTextField xmpDcDescription;
	@FieldID(value = "xmpDc.dates", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcDates;
	@FieldID("xmpDc.format")
	public JTextField xmpDcFormat;
	@FieldID("xmpDc.identifier")
	public JTextField xmpDcIdentifier;
	@FieldID("xmpDc.rights")
	public JTextField xmpDcRights;
	@FieldID("xmpDc.source")
	public JTextField xmpDcSource;
	@FieldID(value = "xmpDc.creators", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcCreators;
	@FieldID(value = "xmpDc.contributors", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcContributors;
	@FieldID(value = "xmpDc.languages", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcLanguages;
	@FieldID(value = "xmpDc.publishers", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcPublishers;
	@FieldID(value = "xmpDc.relationships", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcRelationships;
	@FieldID(value = "xmpDc.subjects", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcSubjects;
	@FieldID(value = "xmpDc.types", type = FieldID.FieldType.TextField)
	public JTextArea xmpDcTypes;
	public JPanel xmlBasicMetaPanel;
	public JPanel xmlPdfMetaPanel;
	public JPanel xmpDcMetaPanel;

	public JTabbedPane tabbedaPane;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JScrollPane scrollPane_2;

	public MetadataEditPane() {
		initialize();
	}

	private void initialize() {
		long startTime = System.nanoTime(), cnt=1;

		tabbedaPane = new JTabbedPane(JTabbedPane.TOP);
		JScrollPane basicScrollpane = new JScrollPane();
		basicScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("Basic Metadata", null, basicScrollpane, null);

		basicMetaPanel = new JPanel();
		basicScrollpane.setViewportView(basicMetaPanel);
		GridBagLayout gbl_basicMetaPanel = new GridBagLayout();
		gbl_basicMetaPanel.columnWidths = new int[] { 112, 284, 0 };
		gbl_basicMetaPanel.rowHeights = new int[] { 26, 26, 16, 16, 26, 26, 26, 26, 27, 0 };
		gbl_basicMetaPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_basicMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		basicMetaPanel.setLayout(gbl_basicMetaPanel);


		JLabel lblTitle = new JLabel("Title");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.EAST;
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		basicMetaPanel.add(lblTitle, gbc_lblTitle);

		basicTitle = new JTextField();
		GridBagConstraints gbc_basicTitle = new GridBagConstraints();
		gbc_basicTitle.weightx = 1.0;
		gbc_basicTitle.anchor = GridBagConstraints.WEST;
		gbc_basicTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicTitle.insets = new Insets(0, 0, 5, 0);
		gbc_basicTitle.gridx = 1;
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

		basicAuthor = new JTextField();
		GridBagConstraints gbc_basicAuthor = new GridBagConstraints();
		gbc_basicAuthor.weightx = 1.0;
		gbc_basicAuthor.anchor = GridBagConstraints.WEST;
		gbc_basicAuthor.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicAuthor.insets = new Insets(0, 0, 5, 0);
		gbc_basicAuthor.gridx = 1;
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
		
		scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.weighty = 0.5;
		gbc_scrollPane_1.anchor = GridBagConstraints.WEST;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
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
		
		scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.weighty = 0.5;
		gbc_scrollPane_2.anchor = GridBagConstraints.WEST;
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.gridx = 1;
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

		basicCreator = new JTextField();
		GridBagConstraints gbc_basicCreator = new GridBagConstraints();
		gbc_basicCreator.weightx = 1.0;
		gbc_basicCreator.anchor = GridBagConstraints.WEST;
		gbc_basicCreator.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicCreator.insets = new Insets(0, 0, 5, 0);
		gbc_basicCreator.gridx = 1;
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

		basicProducer = new JTextField();
		GridBagConstraints gbc_basicProducer = new GridBagConstraints();
		gbc_basicProducer.weightx = 1.0;
		gbc_basicProducer.anchor = GridBagConstraints.WEST;
		gbc_basicProducer.fill = GridBagConstraints.HORIZONTAL;
		gbc_basicProducer.insets = new Insets(0, 0, 5, 0);
		gbc_basicProducer.gridx = 1;
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

		basicCreationDate = new JDateChooser();
		basicCreationDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_basicCreationDate = new GridBagConstraints();
		gbc_basicCreationDate.anchor = GridBagConstraints.WEST;
		gbc_basicCreationDate.insets = new Insets(0, 0, 5, 0);
		gbc_basicCreationDate.gridx = 1;
		gbc_basicCreationDate.gridy = 6;
		basicMetaPanel.add(basicCreationDate, gbc_basicCreationDate);

		JLabel lblModificationDate = new JLabel("Modification Date");
		GridBagConstraints gbc_lblModificationDate = new GridBagConstraints();
		gbc_lblModificationDate.anchor = GridBagConstraints.WEST;
		gbc_lblModificationDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblModificationDate.gridx = 0;
		gbc_lblModificationDate.gridy = 7;
		basicMetaPanel.add(lblModificationDate, gbc_lblModificationDate);

		basicModificationDate = new JDateChooser();
		basicModificationDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_basicModificationDate = new GridBagConstraints();
		gbc_basicModificationDate.anchor = GridBagConstraints.WEST;
		gbc_basicModificationDate.insets = new Insets(0, 0, 5, 0);
		gbc_basicModificationDate.gridx = 1;
		gbc_basicModificationDate.gridy = 7;
		basicMetaPanel.add(basicModificationDate, gbc_basicModificationDate);

		JLabel lblTrapped = new JLabel("Trapped");
		GridBagConstraints gbc_lblTrapped = new GridBagConstraints();
		gbc_lblTrapped.anchor = GridBagConstraints.EAST;
		gbc_lblTrapped.insets = new Insets(0, 0, 0, 5);
		gbc_lblTrapped.gridx = 0;
		gbc_lblTrapped.gridy = 8;
		basicMetaPanel.add(lblTrapped, gbc_lblTrapped);

		basicTrapped = new JComboBox();
		basicTrapped.setModel(new DefaultComboBoxModel(new String[] { "True", "False", "Unknown" }));
		GridBagConstraints gbc_basicTrapped = new GridBagConstraints();
		gbc_basicTrapped.anchor = GridBagConstraints.WEST;
		gbc_basicTrapped.gridx = 1;
		gbc_basicTrapped.gridy = 8;
		basicMetaPanel.add(basicTrapped, gbc_basicTrapped);


		JScrollPane xmpBasicScrollpane = new JScrollPane();
		xmpBasicScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP Basic", null, xmpBasicScrollpane, null);

		xmlBasicMetaPanel = new JPanel();
		xmpBasicScrollpane.setViewportView(xmlBasicMetaPanel);
		GridBagLayout gbl_xmlBasicMetaPanel = new GridBagLayout();
		gbl_xmlBasicMetaPanel.columnWidths = new int[] {112, 284, 0};
		gbl_xmlBasicMetaPanel.rowHeights = new int[] { 26, 26, 26, 26, 26, 26, 26, 26, 16, 16, 26, 0 };
		gbl_xmlBasicMetaPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_xmlBasicMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		xmlBasicMetaPanel.setLayout(gbl_xmlBasicMetaPanel);

		JLabel lblCreatorTool = new JLabel("Creator tool");
		GridBagConstraints gbc_lblCreatorTool = new GridBagConstraints();
		gbc_lblCreatorTool.anchor = GridBagConstraints.EAST;
		gbc_lblCreatorTool.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatorTool.gridx = 0;
		gbc_lblCreatorTool.gridy = 0;
		xmlBasicMetaPanel.add(lblCreatorTool, gbc_lblCreatorTool);

		xmpBasicCreatorTool = new JTextField();
		GridBagConstraints gbc_xmpBasicCreatorTool = new GridBagConstraints();
		gbc_xmpBasicCreatorTool.weightx = 1.0;
		gbc_xmpBasicCreatorTool.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicCreatorTool.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicCreatorTool.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicCreatorTool.gridx = 1;
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

		xmpBasicCreateDate = new JDateChooser();
		xmpBasicCreateDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_xmpBasicCreateDate = new GridBagConstraints();
		gbc_xmpBasicCreateDate.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicCreateDate.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicCreateDate.gridx = 1;
		gbc_xmpBasicCreateDate.gridy = 1;
		xmlBasicMetaPanel.add(xmpBasicCreateDate, gbc_xmpBasicCreateDate);

		JLabel lblModifyDate = new JLabel("Modify Date");
		GridBagConstraints gbc_lblModifyDate = new GridBagConstraints();
		gbc_lblModifyDate.anchor = GridBagConstraints.EAST;
		gbc_lblModifyDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblModifyDate.gridx = 0;
		gbc_lblModifyDate.gridy = 2;
		xmlBasicMetaPanel.add(lblModifyDate, gbc_lblModifyDate);

		xmpBasicModifyDate = new JDateChooser();
		xmpBasicModifyDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_xmpBasicModifyDate = new GridBagConstraints();
		gbc_xmpBasicModifyDate.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicModifyDate.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicModifyDate.gridx = 1;
		gbc_xmpBasicModifyDate.gridy = 2;
		xmlBasicMetaPanel.add(xmpBasicModifyDate, gbc_xmpBasicModifyDate);

		JLabel lblTitle_1 = new JLabel("Title");
		GridBagConstraints gbc_lblTitle_1 = new GridBagConstraints();
		gbc_lblTitle_1.anchor = GridBagConstraints.EAST;
		gbc_lblTitle_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle_1.gridx = 0;
		gbc_lblTitle_1.gridy = 3;
		xmlBasicMetaPanel.add(lblTitle_1, gbc_lblTitle_1);

		xmpBasicTitle = new JTextField();
		GridBagConstraints gbc_xmpBasicTitle = new GridBagConstraints();
		gbc_xmpBasicTitle.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicTitle.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicTitle.gridx = 1;
		gbc_xmpBasicTitle.gridy = 3;
		xmlBasicMetaPanel.add(xmpBasicTitle, gbc_xmpBasicTitle);
		xmpBasicTitle.setColumns(10);

		JLabel lblBaseUrl = new JLabel("Base URL");
		GridBagConstraints gbc_lblBaseUrl = new GridBagConstraints();
		gbc_lblBaseUrl.anchor = GridBagConstraints.EAST;
		gbc_lblBaseUrl.insets = new Insets(0, 0, 5, 5);
		gbc_lblBaseUrl.gridx = 0;
		gbc_lblBaseUrl.gridy = 4;
		xmlBasicMetaPanel.add(lblBaseUrl, gbc_lblBaseUrl);

		xmpBasicBaseURL = new JTextField();
		GridBagConstraints gbc_xmpBasicBaseURL = new GridBagConstraints();
		gbc_xmpBasicBaseURL.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicBaseURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicBaseURL.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicBaseURL.gridx = 1;
		gbc_xmpBasicBaseURL.gridy = 4;
		xmlBasicMetaPanel.add(xmpBasicBaseURL, gbc_xmpBasicBaseURL);
		xmpBasicBaseURL.setColumns(10);

		JLabel lblRating = new JLabel("Rating");
		GridBagConstraints gbc_lblRating = new GridBagConstraints();
		gbc_lblRating.anchor = GridBagConstraints.EAST;
		gbc_lblRating.insets = new Insets(0, 0, 5, 5);
		gbc_lblRating.gridx = 0;
		gbc_lblRating.gridy = 5;
		xmlBasicMetaPanel.add(lblRating, gbc_lblRating);

		xmpBasicRating = new JSpinner();
		xmpBasicRating.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
		GridBagConstraints gbc_xmpBasicRating = new GridBagConstraints();
		gbc_xmpBasicRating.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicRating.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicRating.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicRating.gridx = 1;
		gbc_xmpBasicRating.gridy = 5;
		xmlBasicMetaPanel.add(xmpBasicRating, gbc_xmpBasicRating);

		JLabel lblLabel = new JLabel("Label");
		GridBagConstraints gbc_lblLabel = new GridBagConstraints();
		gbc_lblLabel.anchor = GridBagConstraints.EAST;
		gbc_lblLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblLabel.gridx = 0;
		gbc_lblLabel.gridy = 6;
		xmlBasicMetaPanel.add(lblLabel, gbc_lblLabel);

		xmpBasicLabel = new JTextField();
		GridBagConstraints gbc_xmpBasicLabel = new GridBagConstraints();
		gbc_xmpBasicLabel.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicLabel.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicLabel.gridx = 1;
		gbc_xmpBasicLabel.gridy = 6;
		xmlBasicMetaPanel.add(xmpBasicLabel, gbc_xmpBasicLabel);
		xmpBasicLabel.setColumns(10);

		JLabel lblNickname = new JLabel("Nickname");
		GridBagConstraints gbc_lblNickname = new GridBagConstraints();
		gbc_lblNickname.anchor = GridBagConstraints.EAST;
		gbc_lblNickname.insets = new Insets(0, 0, 5, 5);
		gbc_lblNickname.gridx = 0;
		gbc_lblNickname.gridy = 7;
		xmlBasicMetaPanel.add(lblNickname, gbc_lblNickname);

		xmpBasicNickname = new JTextField();
		GridBagConstraints gbc_xmpBasicNickname = new GridBagConstraints();
		gbc_xmpBasicNickname.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicNickname.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicNickname.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicNickname.gridx = 1;
		gbc_xmpBasicNickname.gridy = 7;
		xmlBasicMetaPanel.add(xmpBasicNickname, gbc_xmpBasicNickname);
		xmpBasicNickname.setColumns(10);

		JLabel label_1 = new JLabel("Identifiers");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.NORTHEAST;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 8;
		xmlBasicMetaPanel.add(label_1, gbc_label_1);

		xmpBasicIdentifiers = new JTextArea();
		GridBagConstraints gbc_xmpBasicIdentifiers = new GridBagConstraints();
		gbc_xmpBasicIdentifiers.weighty = 0.5;
		gbc_xmpBasicIdentifiers.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicIdentifiers.fill = GridBagConstraints.BOTH;
		gbc_xmpBasicIdentifiers.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicIdentifiers.gridx = 1;
		gbc_xmpBasicIdentifiers.gridy = 8;
		xmlBasicMetaPanel.add(xmpBasicIdentifiers, gbc_xmpBasicIdentifiers);

		JLabel label = new JLabel("Advisories");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 9;
		xmlBasicMetaPanel.add(label, gbc_label);

		xmpBasicAdvisories = new JTextArea();
		GridBagConstraints gbc_xmpBasicAdvisories = new GridBagConstraints();
		gbc_xmpBasicAdvisories.weighty = 0.5;
		gbc_xmpBasicAdvisories.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicAdvisories.fill = GridBagConstraints.BOTH;
		gbc_xmpBasicAdvisories.insets = new Insets(0, 0, 5, 0);
		gbc_xmpBasicAdvisories.gridx = 1;
		gbc_xmpBasicAdvisories.gridy = 9;
		xmlBasicMetaPanel.add(xmpBasicAdvisories, gbc_xmpBasicAdvisories);

		JLabel lblMetadataDate = new JLabel("Metadata Date");
		GridBagConstraints gbc_lblMetadataDate = new GridBagConstraints();
		gbc_lblMetadataDate.anchor = GridBagConstraints.WEST;
		gbc_lblMetadataDate.insets = new Insets(0, 0, 0, 5);
		gbc_lblMetadataDate.gridx = 0;
		gbc_lblMetadataDate.gridy = 10;
		xmlBasicMetaPanel.add(lblMetadataDate, gbc_lblMetadataDate);

		xmpBasicMetadataDate = new JDateChooser();
		xmpBasicMetadataDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		GridBagConstraints gbc_xmpBasicMetadataDate = new GridBagConstraints();
		gbc_xmpBasicMetadataDate.anchor = GridBagConstraints.WEST;
		gbc_xmpBasicMetadataDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpBasicMetadataDate.gridx = 1;
		gbc_xmpBasicMetadataDate.gridy = 10;
		xmlBasicMetaPanel.add(xmpBasicMetadataDate, gbc_xmpBasicMetadataDate);

		JScrollPane xmpPdfScrollpane = new JScrollPane();
		xmpPdfScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP PDF", null, xmpPdfScrollpane, null);

		xmlPdfMetaPanel = new JPanel();
		xmpPdfScrollpane.setViewportView(xmlPdfMetaPanel);
		GridBagLayout gbl_xmlPdfMetaPanel = new GridBagLayout();
		gbl_xmlPdfMetaPanel.columnWidths = new int[] {112, 284, 0};
		gbl_xmlPdfMetaPanel.rowHeights = new int[] { 16, 26, 26, 0 };
		gbl_xmlPdfMetaPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_xmlPdfMetaPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		xmlPdfMetaPanel.setLayout(gbl_xmlPdfMetaPanel);

		JLabel lblKeywords_1 = new JLabel("Keywords");
		GridBagConstraints gbc_lblKeywords_1 = new GridBagConstraints();
		gbc_lblKeywords_1.anchor = GridBagConstraints.EAST;
		gbc_lblKeywords_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeywords_1.gridx = 0;
		gbc_lblKeywords_1.gridy = 0;
		xmlPdfMetaPanel.add(lblKeywords_1, gbc_lblKeywords_1);
		

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.anchor = GridBagConstraints.WEST;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 1;
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

		xmpPdfVersion = new JTextField();
		xmpPdfVersion.setEditable(false);
		GridBagConstraints gbc_xmpPdfVersion = new GridBagConstraints();
		gbc_xmpPdfVersion.anchor = GridBagConstraints.WEST;
		gbc_xmpPdfVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpPdfVersion.insets = new Insets(0, 0, 5, 0);
		gbc_xmpPdfVersion.gridx = 1;
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

		xmpPdfProducer = new JTextField();
		GridBagConstraints gbc_xmpPdfProducer = new GridBagConstraints();
		gbc_xmpPdfProducer.anchor = GridBagConstraints.WEST;
		gbc_xmpPdfProducer.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpPdfProducer.gridx = 1;
		gbc_xmpPdfProducer.gridy = 2;
		xmlPdfMetaPanel.add(xmpPdfProducer, gbc_xmpPdfProducer);
		xmpPdfProducer.setColumns(10);

		JScrollPane xmpDcScrollpane = new JScrollPane();
		xmpDcScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP Dublin Core", null, xmpDcScrollpane, null);

		xmpDcMetaPanel = new JPanel();
		xmpDcScrollpane.setViewportView(xmpDcMetaPanel);
		GridBagLayout gbl_xmpDcMetaPanel = new GridBagLayout();
		gbl_xmpDcMetaPanel.columnWidths = new int[] {112, 284, 0};
		gbl_xmpDcMetaPanel.rowHeights = new int[] { 26, 26, 16, 16, 26, 16, 26, 26, 16, 16, 16, 26, 26, 16, 16, 0 };
		gbl_xmpDcMetaPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
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

		xmpDcTitle = new JTextField();
		GridBagConstraints gbc_xmpDcTitle = new GridBagConstraints();
		gbc_xmpDcTitle.weightx = 1.0;
		gbc_xmpDcTitle.anchor = GridBagConstraints.WEST;
		gbc_xmpDcTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcTitle.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcTitle.gridx = 1;
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

		xmpDcDescription = new JTextField();
		GridBagConstraints gbc_xmpDcDescription = new GridBagConstraints();
		gbc_xmpDcDescription.anchor = GridBagConstraints.WEST;
		gbc_xmpDcDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcDescription.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcDescription.gridx = 1;
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

		xmpDcCreators = new JTextArea();
		GridBagConstraints gbc_xmpDcCreators = new GridBagConstraints();
		gbc_xmpDcCreators.weighty = 0.125;
		gbc_xmpDcCreators.anchor = GridBagConstraints.WEST;
		gbc_xmpDcCreators.fill = GridBagConstraints.BOTH;
		gbc_xmpDcCreators.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcCreators.gridx = 1;
		gbc_xmpDcCreators.gridy = 2;
		xmpDcMetaPanel.add(xmpDcCreators, gbc_xmpDcCreators);

		JLabel lblContributors = new JLabel("Contributors");
		GridBagConstraints gbc_lblContributors = new GridBagConstraints();
		gbc_lblContributors.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblContributors.insets = new Insets(0, 0, 5, 5);
		gbc_lblContributors.gridx = 0;
		gbc_lblContributors.gridy = 3;
		xmpDcMetaPanel.add(lblContributors, gbc_lblContributors);

		xmpDcContributors = new JTextArea();
		GridBagConstraints gbc_xmpDcContributors = new GridBagConstraints();
		gbc_xmpDcContributors.weighty = 0.125;
		gbc_xmpDcContributors.anchor = GridBagConstraints.WEST;
		gbc_xmpDcContributors.fill = GridBagConstraints.BOTH;
		gbc_xmpDcContributors.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcContributors.gridx = 1;
		gbc_xmpDcContributors.gridy = 3;
		xmpDcMetaPanel.add(xmpDcContributors, gbc_xmpDcContributors);

		JLabel lblCoverage = new JLabel("Coverage");
		GridBagConstraints gbc_lblCoverage = new GridBagConstraints();
		gbc_lblCoverage.anchor = GridBagConstraints.EAST;
		gbc_lblCoverage.insets = new Insets(0, 0, 5, 5);
		gbc_lblCoverage.gridx = 0;
		gbc_lblCoverage.gridy = 4;
		xmpDcMetaPanel.add(lblCoverage, gbc_lblCoverage);

		xmpDcCoverage = new JTextField();
		GridBagConstraints gbc_xmpDcCoverage = new GridBagConstraints();
		gbc_xmpDcCoverage.anchor = GridBagConstraints.WEST;
		gbc_xmpDcCoverage.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcCoverage.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcCoverage.gridx = 1;
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

		xmpDcDates = new JTextArea();
		xmpDcDates.setEditable(false);
		GridBagConstraints gbc_xmpDcDates = new GridBagConstraints();
		gbc_xmpDcDates.weighty = 0.125;
		gbc_xmpDcDates.anchor = GridBagConstraints.WEST;
		gbc_xmpDcDates.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcDates.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcDates.gridx = 1;
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

		xmpDcFormat = new JTextField();
		GridBagConstraints gbc_xmpDcFormat = new GridBagConstraints();
		gbc_xmpDcFormat.anchor = GridBagConstraints.WEST;
		gbc_xmpDcFormat.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcFormat.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcFormat.gridx = 1;
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

		xmpDcIdentifier = new JTextField();
		GridBagConstraints gbc_xmpDcIdentifier = new GridBagConstraints();
		gbc_xmpDcIdentifier.anchor = GridBagConstraints.WEST;
		gbc_xmpDcIdentifier.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcIdentifier.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcIdentifier.gridx = 1;
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

		xmpDcLanguages = new JTextArea();
		GridBagConstraints gbc_xmpDcLanguages = new GridBagConstraints();
		gbc_xmpDcLanguages.weighty = 0.125;
		gbc_xmpDcLanguages.anchor = GridBagConstraints.WEST;
		gbc_xmpDcLanguages.fill = GridBagConstraints.BOTH;
		gbc_xmpDcLanguages.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcLanguages.gridx = 1;
		gbc_xmpDcLanguages.gridy = 8;
		xmpDcMetaPanel.add(xmpDcLanguages, gbc_xmpDcLanguages);

		JLabel lblPublishers = new JLabel("Publishers");
		GridBagConstraints gbc_lblPublishers = new GridBagConstraints();
		gbc_lblPublishers.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPublishers.insets = new Insets(0, 0, 5, 5);
		gbc_lblPublishers.gridx = 0;
		gbc_lblPublishers.gridy = 9;
		xmpDcMetaPanel.add(lblPublishers, gbc_lblPublishers);

		xmpDcPublishers = new JTextArea();
		GridBagConstraints gbc_xmpDcPublishers = new GridBagConstraints();
		gbc_xmpDcPublishers.weighty = 0.125;
		gbc_xmpDcPublishers.anchor = GridBagConstraints.WEST;
		gbc_xmpDcPublishers.fill = GridBagConstraints.BOTH;
		gbc_xmpDcPublishers.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcPublishers.gridx = 1;
		gbc_xmpDcPublishers.gridy = 9;
		xmpDcMetaPanel.add(xmpDcPublishers, gbc_xmpDcPublishers);

		JLabel lblRelationships = new JLabel("Relationships");
		GridBagConstraints gbc_lblRelationships = new GridBagConstraints();
		gbc_lblRelationships.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblRelationships.insets = new Insets(0, 0, 5, 5);
		gbc_lblRelationships.gridx = 0;
		gbc_lblRelationships.gridy = 10;
		xmpDcMetaPanel.add(lblRelationships, gbc_lblRelationships);

		xmpDcRelationships = new JTextArea();
		GridBagConstraints gbc_xmpDcRelationships = new GridBagConstraints();
		gbc_xmpDcRelationships.weighty = 0.125;
		gbc_xmpDcRelationships.anchor = GridBagConstraints.WEST;
		gbc_xmpDcRelationships.fill = GridBagConstraints.BOTH;
		gbc_xmpDcRelationships.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcRelationships.gridx = 1;
		gbc_xmpDcRelationships.gridy = 10;
		xmpDcMetaPanel.add(xmpDcRelationships, gbc_xmpDcRelationships);

		JLabel lblRights = new JLabel("Rights");
		GridBagConstraints gbc_lblRights = new GridBagConstraints();
		gbc_lblRights.anchor = GridBagConstraints.EAST;
		gbc_lblRights.insets = new Insets(0, 0, 5, 5);
		gbc_lblRights.gridx = 0;
		gbc_lblRights.gridy = 11;
		xmpDcMetaPanel.add(lblRights, gbc_lblRights);

		xmpDcRights = new JTextField();
		GridBagConstraints gbc_xmpDcRights = new GridBagConstraints();
		gbc_xmpDcRights.anchor = GridBagConstraints.WEST;
		gbc_xmpDcRights.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcRights.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcRights.gridx = 1;
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

		xmpDcSource = new JTextField();
		GridBagConstraints gbc_xmpDcSource = new GridBagConstraints();
		gbc_xmpDcSource.anchor = GridBagConstraints.WEST;
		gbc_xmpDcSource.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmpDcSource.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcSource.gridx = 1;
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

		xmpDcSubjects = new JTextArea();
		GridBagConstraints gbc_xmpDcSubjects = new GridBagConstraints();
		gbc_xmpDcSubjects.weighty = 0.125;
		gbc_xmpDcSubjects.anchor = GridBagConstraints.WEST;
		gbc_xmpDcSubjects.fill = GridBagConstraints.BOTH;
		gbc_xmpDcSubjects.insets = new Insets(0, 0, 5, 0);
		gbc_xmpDcSubjects.gridx = 1;
		gbc_xmpDcSubjects.gridy = 13;
		xmpDcMetaPanel.add(xmpDcSubjects, gbc_xmpDcSubjects);

		JLabel lblTypes = new JLabel("Types");
		GridBagConstraints gbc_lblTypes = new GridBagConstraints();
		gbc_lblTypes.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblTypes.insets = new Insets(0, 0, 0, 5);
		gbc_lblTypes.gridx = 0;
		gbc_lblTypes.gridy = 14;
		xmpDcMetaPanel.add(lblTypes, gbc_lblTypes);

		xmpDcTypes = new JTextArea();
		GridBagConstraints gbc_xmpDcTypes = new GridBagConstraints();
		gbc_xmpDcTypes.weighty = 0.125;
		gbc_xmpDcTypes.anchor = GridBagConstraints.WEST;
		gbc_xmpDcTypes.fill = GridBagConstraints.BOTH;
		gbc_xmpDcTypes.gridx = 1;
		gbc_xmpDcTypes.gridy = 14;
		xmpDcMetaPanel.add(xmpDcTypes, gbc_xmpDcTypes);

	}

	private void traverseFields(FieldSetGet setGet) {
		for (Field field : this.getClass().getFields()) {
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
	}

	void clear() {
		traverseFields(new FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {
				if (field instanceof JTextField) {
					objectToField((JTextField) field, null);
				}
				if (field instanceof JTextArea) {
					objectToField((JTextArea) field, null);
				}
				if (field instanceof JComboBox) {
					objectToField((JComboBox) field, null);
				}
				if (field instanceof JDateChooser) {
					objectToField((JDateChooser) field, null);
				}
				if (field instanceof JSpinner) {
					objectToField((JSpinner) field, null);
				}
			}
		});
	}

	void fillFromMetadata(final MetadataInfo metadataInfo) {

		traverseFields(new FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {
				Object value = metadataInfo.get(anno.value());

				if (field instanceof JTextField) {
					objectToField((JTextField) field, value);
				}
				if (field instanceof JTextArea) {
					objectToField((JTextArea) field, value);
				}
				if (field instanceof JComboBox) {
					objectToField((JComboBox) field, value);
				}
				if (field instanceof JDateChooser) {
					objectToField((JDateChooser) field, value);
				}
				if (field instanceof JSpinner) {
					objectToField((JSpinner) field, value);
				}
			}
		});

	}

	void copyToMetadata(final MetadataInfo metadataInfo) {

		traverseFields(new FieldSetGet() {
			@Override
			public void apply(Object field, FieldID anno) {

				if (field instanceof JTextField || field instanceof JTextArea) {
					String text = (field instanceof JTextField) ? ((JTextField) field).getText()
							: ((JTextArea) field).getText();
					if (text.length() == 0) {
						text = null;
					}
					switch (anno.type()) {
					case StringField:
						metadataInfo.set(anno.value(), text);
						break;
					case TextField:
						metadataInfo.set(anno.value(), text == null ? null : Arrays.asList(text.split("\n")));
						break;
					default:
						throw new RuntimeException("Cannot store text in :" + anno.type());

					}
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
		});

	}

	private void objectToField(JTextField field, Object o) {
		if (o instanceof String) {
			field.setText((String) o);
		} else if (o == null) {
			field.setText("");
		} else {
			throw new RuntimeException("Cannot store non-String object in JTextField");
		}
	}

	private void objectToField(JTextArea field, Object o) {
		if (o instanceof String) {
			field.setText((String) o);
		} else if (o instanceof List<?>) {
			field.setText(stringListToText((List<String>) o));
		} else if (o == null) {
			field.setText("");
		} else {
			RuntimeException e = new RuntimeException("Cannot store non-String/List<String> object in JTextArea");
			e.printStackTrace();
			throw e;
		}
	}

	private void objectToField(JComboBox field, Object o) {
		if (o instanceof String) {
			field.getModel().setSelectedItem(o);
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

	private <T> T formatItem(T s) {
		return s;
	}

	// private String formatItem(Calendar date) {
	// return DateFormat.getInstance().format(date.getTime());
	// }

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

}
