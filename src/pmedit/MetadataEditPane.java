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

	public MetadataEditPane(){
		initialize();
	}
	private void initialize() {
		tabbedaPane = new JTabbedPane(JTabbedPane.TOP);
		JScrollPane basicScrollpane = new JScrollPane();
		basicScrollpane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("Basic Metadata", null, basicScrollpane, null);

		basicMetaPanel = new JPanel();
		basicScrollpane.setViewportView(basicMetaPanel);
		basicMetaPanel.setLayout(new MigLayout("", "[][grow,fill]",
				"[][][grow][grow][][][][][][][]"));

		JLabel lblTitle = new JLabel("Title");
		basicMetaPanel.add(lblTitle, "cell 0 0,alignx trailing");

		basicTitle = new JTextField();
		basicMetaPanel.add(basicTitle, "cell 1 0");
		basicTitle.setColumns(10);

		JLabel lblNewLabel = new JLabel("Author");
		basicMetaPanel.add(lblNewLabel, "cell 0 1,alignx trailing");

		basicAuthor = new JTextField();
		basicMetaPanel.add(basicAuthor, "cell 1 1");
		basicAuthor.setColumns(10);

		JLabel lblSubject = new JLabel("Subject");
		basicMetaPanel.add(lblSubject, "cell 0 2,alignx trailing");

		basicSubject = new JTextArea();
		basicSubject.setLineWrap(true);
		basicSubject.setWrapStyleWord(true);
		basicMetaPanel.add(basicSubject, "cell 1 2,growy");
		basicSubject.setColumns(10);

		JLabel lblKeywords = new JLabel("Keywords");
		basicMetaPanel.add(lblKeywords, "cell 0 3,alignx trailing");

		basicKeywords = new JTextArea();
		basicKeywords.setWrapStyleWord(true);
		basicKeywords.setLineWrap(true);
		basicMetaPanel.add(basicKeywords, "cell 1 3,growy");
		basicKeywords.setColumns(10);

		JLabel lblCreator = new JLabel("Creator");
		basicMetaPanel.add(lblCreator, "cell 0 4,alignx trailing");

		basicCreator = new JTextField();
		basicMetaPanel.add(basicCreator, "cell 1 4");
		basicCreator.setColumns(10);

		JLabel lblProducer = new JLabel("Producer");
		basicMetaPanel.add(lblProducer, "cell 0 5,alignx trailing");

		basicProducer = new JTextField();
		basicMetaPanel.add(basicProducer, "cell 1 5");
		basicProducer.setColumns(10);

		JLabel lblCreationDate = new JLabel("Creation Date");
		basicMetaPanel.add(lblCreationDate, "cell 0 6,alignx trailing");

		basicCreationDate = new JDateChooser();
		basicCreationDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		basicMetaPanel.add(basicCreationDate, "cell 1 6,alignx left");

		JLabel lblModificationDate = new JLabel("Modification Date");
		basicMetaPanel.add(lblModificationDate, "cell 0 7,alignx trailing");

		basicModificationDate = new JDateChooser();
		basicModificationDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		basicMetaPanel.add(basicModificationDate, "cell 1 7,alignx left");

		JLabel lblTrapped = new JLabel("Trapped");
		basicMetaPanel.add(lblTrapped, "cell 0 8,alignx trailing");

		basicTrapped = new JComboBox();
		basicTrapped.setModel(new DefaultComboBoxModel(new String[] { "True",
				"False", "Unknown" }));
		basicMetaPanel.add(basicTrapped, "cell 1 8,alignx left");

		JScrollPane xmpBasicScrollpane = new JScrollPane();
		xmpBasicScrollpane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP Basic", null, xmpBasicScrollpane, null);

		xmlBasicMetaPanel = new JPanel();
		xmpBasicScrollpane.setViewportView(xmlBasicMetaPanel);
		xmlBasicMetaPanel.setLayout(new MigLayout("", "[][grow,fill]",
				"[][][][][][][][][grow][grow][]"));

		JLabel lblCreatorTool = new JLabel("Creator tool");
		xmlBasicMetaPanel.add(lblCreatorTool, "cell 0 0,alignx trailing");

		xmpBasicCreatorTool = new JTextField();
		xmlBasicMetaPanel.add(xmpBasicCreatorTool, "cell 1 0");
		xmpBasicCreatorTool.setColumns(10);

		JLabel lblCreateDate = new JLabel("Create Date");
		xmlBasicMetaPanel.add(lblCreateDate, "cell 0 1,alignx trailing");

		xmpBasicCreateDate = new JDateChooser();
		xmpBasicCreateDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		xmlBasicMetaPanel.add(xmpBasicCreateDate, "cell 1 1");

		JLabel lblModifyDate = new JLabel("Modify Date");
		xmlBasicMetaPanel.add(lblModifyDate, "cell 0 2,alignx trailing");

		xmpBasicModifyDate = new JDateChooser();
		xmpBasicModifyDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		xmlBasicMetaPanel.add(xmpBasicModifyDate, "cell 1 2");

		JLabel lblTitle_1 = new JLabel("Title");
		xmlBasicMetaPanel.add(lblTitle_1, "cell 0 3,alignx trailing");

		xmpBasicTitle = new JTextField();
		xmlBasicMetaPanel.add(xmpBasicTitle, "cell 1 3");
		xmpBasicTitle.setColumns(10);

		JLabel lblBaseUrl = new JLabel("Base URL");
		xmlBasicMetaPanel.add(lblBaseUrl, "cell 0 4,alignx trailing");

		xmpBasicBaseURL = new JTextField();
		xmlBasicMetaPanel.add(xmpBasicBaseURL, "cell 1 4");
		xmpBasicBaseURL.setColumns(10);

		JLabel lblRating = new JLabel("Rating");
		xmlBasicMetaPanel.add(lblRating, "cell 0 5,alignx trailing");

		xmpBasicRating = new JSpinner();
		xmpBasicRating.setModel(new SpinnerNumberModel(new Integer(0), null,
				null, new Integer(1)));
		xmlBasicMetaPanel.add(xmpBasicRating, "cell 1 5");

		JLabel lblLabel = new JLabel("Label");
		xmlBasicMetaPanel.add(lblLabel, "cell 0 6,alignx trailing");

		xmpBasicLabel = new JTextField();
		xmlBasicMetaPanel.add(xmpBasicLabel, "cell 1 6");
		xmpBasicLabel.setColumns(10);

		JLabel lblNickname = new JLabel("Nickname");
		xmlBasicMetaPanel.add(lblNickname, "cell 0 7,alignx trailing");

		xmpBasicNickname = new JTextField();
		xmlBasicMetaPanel.add(xmpBasicNickname, "cell 1 7");
		xmpBasicNickname.setColumns(10);

		JLabel label_1 = new JLabel("Identifiers");
		xmlBasicMetaPanel.add(label_1, "cell 0 8,alignx trailing");

		xmpBasicIdentifiers = new JTextArea();
		xmlBasicMetaPanel.add(xmpBasicIdentifiers, "cell 1 8,growy");

		JLabel label = new JLabel("Advisories");
		xmlBasicMetaPanel.add(label, "cell 0 9,alignx trailing");

		xmpBasicAdvisories = new JTextArea();
		xmlBasicMetaPanel.add(xmpBasicAdvisories, "cell 1 9,growy");

		JLabel lblMetadataDate = new JLabel("Metadata Date");
		xmlBasicMetaPanel.add(lblMetadataDate, "cell 0 10,alignx trailing");

		xmpBasicMetadataDate = new JDateChooser();
		xmpBasicMetadataDate.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		xmlBasicMetaPanel.add(xmpBasicMetadataDate, "cell 1 10");

		JScrollPane xmpPdfScrollpane = new JScrollPane();
		xmpPdfScrollpane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP PDF", null, xmpPdfScrollpane, null);

		xmlPdfMetaPanel = new JPanel();
		xmpPdfScrollpane.setViewportView(xmlPdfMetaPanel);
		xmlPdfMetaPanel.setLayout(new MigLayout("", "[][grow,fill]", "[][][]"));

		JLabel lblKeywords_1 = new JLabel("Keywords");
		xmlPdfMetaPanel.add(lblKeywords_1, "cell 0 0,alignx trailing");

		xmpPdfKeywords = new JTextArea();
		xmpPdfKeywords.setWrapStyleWord(true);
		xmpPdfKeywords.setLineWrap(true);
		xmlPdfMetaPanel.add(xmpPdfKeywords, "cell 1 0,growx");
		xmpPdfKeywords.setColumns(10);

		JLabel lblPdfVersion = new JLabel("PDF Version");
		xmlPdfMetaPanel.add(lblPdfVersion, "cell 0 1,alignx trailing");

		xmpPdfVersion = new JTextField();
		xmpPdfVersion.setEditable(false);
		xmlPdfMetaPanel.add(xmpPdfVersion, "cell 1 1,growx");
		xmpPdfVersion.setColumns(10);

		JLabel lblProducer_1 = new JLabel("Producer");
		xmlPdfMetaPanel.add(lblProducer_1, "cell 0 2,alignx trailing");

		xmpPdfProducer = new JTextField();
		xmlPdfMetaPanel.add(xmpPdfProducer, "cell 1 2,growx");
		xmpPdfProducer.setColumns(10);

		JScrollPane xmpDcScrollpane = new JScrollPane();
		xmpDcScrollpane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedaPane.addTab("XMP Dublin Core", null, xmpDcScrollpane, null);

		xmpDcMetaPanel = new JPanel();
		xmpDcScrollpane.setViewportView(xmpDcMetaPanel);
		xmpDcMetaPanel.setLayout(new MigLayout("", "[][grow,fill]",
				"[][][grow][grow][][][][][grow][grow][grow][][][grow][grow]"));

		JLabel lblTitle_2 = new JLabel("Title");
		xmpDcMetaPanel.add(lblTitle_2, "cell 0 0,alignx trailing");

		xmpDcTitle = new JTextField();
		xmpDcMetaPanel.add(xmpDcTitle, "cell 1 0");
		xmpDcTitle.setColumns(10);

		JLabel lblCreators = new JLabel("Creators");
		xmpDcMetaPanel.add(lblCreators, "cell 0 2,alignx trailing");

		xmpDcCreators = new JTextArea();
		xmpDcMetaPanel.add(xmpDcCreators, "cell 1 2,growy");

		JLabel lblContributors = new JLabel("Contributors");
		xmpDcMetaPanel.add(lblContributors, "cell 0 3,alignx trailing");

		xmpDcContributors = new JTextArea();
		xmpDcMetaPanel.add(xmpDcContributors, "cell 1 3,growy");

		JLabel lblCoverage = new JLabel("Coverage");
		xmpDcMetaPanel.add(lblCoverage, "cell 0 4,alignx trailing");

		xmpDcCoverage = new JTextField();
		xmpDcMetaPanel.add(xmpDcCoverage, "cell 1 4");
		xmpDcCoverage.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		xmpDcMetaPanel.add(lblDescription, "cell 0 1,alignx trailing");

		xmpDcDescription = new JTextField();
		xmpDcMetaPanel.add(xmpDcDescription, "cell 1 1");
		xmpDcDescription.setColumns(10);

		JLabel lblDates = new JLabel("Dates");
		xmpDcMetaPanel.add(lblDates, "cell 0 5,alignx trailing");

		xmpDcDates = new JTextArea();
		xmpDcDates.setEditable(false);
		xmpDcMetaPanel.add(xmpDcDates, "cell 1 5");
		xmpDcDates.setColumns(10);

		JLabel lblFormat = new JLabel("Format");
		xmpDcMetaPanel.add(lblFormat, "cell 0 6,alignx trailing");

		xmpDcFormat = new JTextField();
		xmpDcMetaPanel.add(xmpDcFormat, "cell 1 6");
		xmpDcFormat.setColumns(10);

		JLabel lblIdentifier = new JLabel("Identifier");
		xmpDcMetaPanel.add(lblIdentifier, "cell 0 7,alignx trailing");

		xmpDcIdentifier = new JTextField();
		xmpDcMetaPanel.add(xmpDcIdentifier, "cell 1 7");
		xmpDcIdentifier.setColumns(10);

		JLabel lblLanguages = new JLabel("Languages");
		xmpDcMetaPanel.add(lblLanguages, "cell 0 8,alignx trailing");

		xmpDcLanguages = new JTextArea();
		xmpDcMetaPanel.add(xmpDcLanguages, "cell 1 8,growy");

		JLabel lblPublishers = new JLabel("Publishers");
		xmpDcMetaPanel.add(lblPublishers, "cell 0 9,alignx trailing");

		xmpDcPublishers = new JTextArea();
		xmpDcMetaPanel.add(xmpDcPublishers, "cell 1 9,growy");

		JLabel lblRelationships = new JLabel("Relationships");
		xmpDcMetaPanel.add(lblRelationships, "cell 0 10,alignx trailing");

		xmpDcRelationships = new JTextArea();
		xmpDcMetaPanel.add(xmpDcRelationships, "cell 1 10,growy");

		JLabel lblRights = new JLabel("Rights");
		xmpDcMetaPanel.add(lblRights, "cell 0 11,alignx trailing");

		xmpDcRights = new JTextField();
		xmpDcMetaPanel.add(xmpDcRights, "cell 1 11");
		xmpDcRights.setColumns(10);

		JLabel lblSource = new JLabel("Source");
		xmpDcMetaPanel.add(lblSource, "cell 0 12,alignx trailing");

		xmpDcSource = new JTextField();
		xmpDcMetaPanel.add(xmpDcSource, "cell 1 12");
		xmpDcSource.setColumns(10);

		JLabel lblSubjects = new JLabel("Subjects");
		xmpDcMetaPanel.add(lblSubjects, "cell 0 13,alignx trailing");

		xmpDcSubjects = new JTextArea();
		xmpDcMetaPanel.add(xmpDcSubjects, "cell 1 13,growy");

		JLabel lblTypes = new JLabel("Types");
		xmpDcMetaPanel.add(lblTypes, "cell 0 14,alignx trailing");

		xmpDcTypes = new JTextArea();
		xmpDcMetaPanel.add(xmpDcTypes, "cell 1 14,growy");
		
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
						System.err.println("traverseFields on ("
								+ annos.value() + ")");
						e.printStackTrace();
						continue;
					} catch (IllegalAccessException e) {
						System.err.println("traverseFields on ("
								+ annos.value() + ")");
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
					String text = (field instanceof JTextField) ? ((JTextField) field)
							.getText() : ((JTextArea) field).getText();
					if (text.length() == 0) {
						text = null;
					}
					switch (anno.type()) {
					case StringField:
						metadataInfo.set(anno.value(), text);
						break;
					case TextField:
						metadataInfo.set(anno.value(), text == null ? null
								: Arrays.asList(text.split("\n")));
						break;
					default:
						throw new RuntimeException("Cannot store text in :"
								+ anno.type());

					}
				}
				if (field instanceof JSpinner) {
					switch (anno.type()) {
					case IntField:
						Integer i = (Integer) ((JSpinner) field).getModel()
								.getValue();
						metadataInfo.set(anno.value(), i);
						break;
					default:
						throw new RuntimeException("Cannot store Integer in :"
								+ anno.type());

					}
				}
				if (field instanceof JComboBox) {
					String text = (String) ((JComboBox) field).getModel()
							.getSelectedItem();
					if (text != null && text.length() == 0) {
						text = null;
					}
					switch (anno.type()) {
					case StringField:
						metadataInfo.set(anno.value(), text);
						break;
					default:
						throw new RuntimeException(
								"Cannot (store (choice text) in :"
										+ anno.type());

					}
				}
				if (field instanceof JDateChooser) {
					switch (anno.type()) {
					case DateField:
						metadataInfo.set(anno.value(),
								((JDateChooser) field).getCalendar());
						break;
					default:
						throw new RuntimeException("Cannot store Calendar in :"
								+ anno.type());

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
			throw new RuntimeException(
					"Cannot store non-String object in JTextField");
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
			RuntimeException e = new RuntimeException(
					"Cannot store non-String/List<String> object in JTextArea");
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
			RuntimeException e = new RuntimeException(
					"Cannot store non-String object in JComboBox");
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
			RuntimeException e = new RuntimeException(
					"Cannot store non-Calendar object in JDateChooser");
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
			RuntimeException e = new RuntimeException(
					"Cannot store non-Integerr object in JSpinner");
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
