package pmedit.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.annotations.FieldDataType;
import pmedit.annotations.FieldEnabled;
import pmedit.annotations.FieldID;
import pmedit.prefs.Preferences;
import pmedit.ui.components.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.toedter.calendar.JDateChooser;
import pmedit.*;
import pmedit.ext.PmeExtension;
import pmedit.preset.PresetValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class MetadataEditPane {
    static final Logger LOG = LoggerFactory.getLogger(MetadataEditPane.class);
    final static  boolean isTesting = System.getProperty("junitTest", "").equals("true");
    Logger logger = LoggerFactory.getLogger(MetadataEditPane.class);

    public JTabbedPane tabbedPane;
    public JPanel panel1;

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
    @FieldID("doc.creationDate")
    public DateTimePicker basicCreationDate;
    @FieldID("doc.modificationDate")
    public DateTimePicker basicModificationDate;
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
    @FieldID("basic.identifiers")
    public JTextArea xmpBasicIdentifiers;
    @FieldID("basic.advisories")
    public JTextArea xmpBasicAdvisories;
    @FieldID("basic.modifyDate")
    public DateTimePicker xmpBasicModifyDate;
    @FieldID("basic.createDate")
    public DateTimePicker xmpBasicCreateDate;
    @FieldID("basic.metadataDate")
    public DateTimePicker xmpBasicMetadataDate;
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
    @FieldID("dc.dates")
    public DateTimeList xmpDcDates;
    @FieldID("dc.format")
    public JTextField xmpDcFormat;
    @FieldID("dc.identifier")
    public JTextField xmpDcIdentifier;
    @FieldID("dc.rights")
    public JTextField xmpDcRights;
    @FieldID("dc.source")
    public JTextField xmpDcSource;
    @FieldID("dc.creators")
    public JTextArea xmpDcCreators;
    @FieldID("dc.contributors")
    public JTextArea xmpDcContributors;
    @FieldID("dc.languages")
    public JTextArea xmpDcLanguages;
    @FieldID("dc.publishers")
    public JTextArea xmpDcPublishers;
    @FieldID("dc.relationships")
    public JTextArea xmpDcRelationships;
    @FieldID("dc.subjects")
    public JTextArea xmpDcSubjects;
    @FieldID("dc.types")
    public JTextArea xmpDcTypes;
    @FieldID("rights.certificate")
    public JTextField xmpRightsCertificate;
    @FieldID("rights.marked")
    public JComboBox xmpRightsMarked;
    @FieldID("rights.owner")
    public JTextArea xmpRightsOwner;
    @FieldID("rights.usageTerms")
    public JTextArea xmpRightsUsageTerms;
    @FieldID("rights.webStatement")
    public JTextField xmpRightsWebStatement;

    @FieldID("viewer.hideToolbar")
    public JComboBox hideToolbar;
    @FieldID("viewer.hideMenuBar")
    public JComboBox hideMenuBar;
    @FieldID("viewer.hideWindowUI")
    public JComboBox hideWindowUI;
    @FieldID("viewer.fitWindow")
    public JComboBox fitWindow;
    @FieldID("viewer.centerWindow")
    public JComboBox centerWindow;
    @FieldID("viewer.displayDocTitle")
    public JComboBox displayDocTitle;
    @FieldID("viewer.nonFullScreenPageMode")
    public JComboBox nonFullScreenPageMode;
    @FieldID("viewer.readingDirection")
    public JComboBox readingDirection;
    @FieldID("viewer.pageLayout")
    public JComboBox pageLayout;
    @FieldID("viewer.pageMode")
    public JComboBox pageMode;
    @FieldID("viewer.viewArea")
    public JComboBox viewArea;
    @FieldID("viewer.viewClip")
    public JComboBox viewClip;
    @FieldID("viewer.printArea")
    public JComboBox printArea;
    @FieldID("viewer.printClip")
    public JComboBox printClip;
    @FieldID("viewer.duplex")
    public JComboBox duplex;
    @FieldID("viewer.printScaling")
    public JComboBox printScaling;
    @FieldID("file.fullPath")
    public JTextField fullPath;
    @FieldID("file.nameWithExt")
    public JTextField nameWithExt;
    @FieldID("file.size")
    public JTextField size;
    @FieldID("file.sizeBytes")
    public JTextField sizeBytes;
    @FieldID("file.name")
    public JTextField name;
    @FieldID("file.createTime")
    public DateTimePicker createTime;
    @FieldID("file.modifyTime")
    public DateTimePicker modifyTime;
    @FieldID("file.pdfVersion")
    public PdfVersionPicker pdfVersion;
    @FieldID("file.pdfCompression")
    public JComboBox pdfCompression;

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
    @FieldEnabled("rights.usageTerms")
    public JCheckBox xmpRightsUsageTermsEnabled;
    @FieldEnabled("rights.webStatement")
    public JCheckBox xmpRightsWebStatementEnabled;

    @FieldEnabled("viewer.hideToolbar")
    public JCheckBox hideToolbarEnabled;
    @FieldEnabled("viewer.hideMenuBar")
    public JCheckBox hideMenuBarEnabled;
    @FieldEnabled("viewer.hideWindowUI")
    public JCheckBox hideWindowUIEnabled;
    @FieldEnabled("viewer.fitWindow")
    public JCheckBox fitWindowEnabled;
    @FieldEnabled("viewer.centerWindow")
    public JCheckBox centerWindowEnabled;
    @FieldEnabled("viewer.displayDocTitle")
    public JCheckBox displayDocTitleEnabled;
    @FieldEnabled("viewer.nonFullScreenPageMode")
    public JCheckBox nonFullScreenPageModeEnabled;
    @FieldEnabled("viewer.readingDirection")
    public JCheckBox readingDirectionEnabled;
    @FieldEnabled("viewer.viewArea")
    public JCheckBox viewAreaEnabled;
    @FieldEnabled("viewer.viewClip")
    public JCheckBox viewClipEnabled;
    @FieldEnabled("viewer.printArea")
    public JCheckBox printAreaEnabled;
    @FieldEnabled("viewer.printClip")
    public JCheckBox printClipEnabled;
    @FieldEnabled("viewer.duplex")
    public JCheckBox duplexEnabled;
    @FieldEnabled("viewer.printScaling")
    public JCheckBox printScalingEnabled;
    @FieldEnabled("viewer.pageLayout")
    public JCheckBox pageLayoutEnabled;
    @FieldEnabled("viewer.pageMode")
    public JCheckBox pageModeEnabled;
    @FieldEnabled("file.fullPath")
    public JCheckBox fullPathEnabled;
    @FieldEnabled("file.nameWithExt")
    public JCheckBox nameWithExtEnabled;
    @FieldEnabled("file.pdfCompression")
    public JCheckBox pdfCompressionEnabled;
    @FieldEnabled("file.pdfVersion")
    public JCheckBox pdfVersionEnabled;
    @FieldEnabled("file.modifyTime")
    public JCheckBox modifyTimeEnabled;
    @FieldEnabled("file.createTime")
    public JCheckBox createTimeEnabled;
    @FieldEnabled("file.sizeBytes")
    public JCheckBox sizeBytesEnabled;
    @FieldEnabled("file.size")
    public JCheckBox sizeEnabled;
    @FieldEnabled("file.size")
    public JCheckBox nameEnabled;


    public TextPaneWithLinks licenseRequiredText;


    private PmeExtension extension = PmeExtension.get();
    MetadataInfo initialMetadata;
    Border textFieldDefault;
    Border textAreaDefault;
    Border comboBoxDefault;
    Border datePickerDefault;
    Border changedBorder;
    boolean presetLoadEnableOnlyNonNull = false;

    public static Color darken(Color color, double factor) {
        int red = (int) (color.getRed() * factor);
        int green = (int) (color.getGreen() * factor);
        int blue = (int) (color.getBlue() * factor);
        return new Color(red, green, blue, color.getAlpha());
    }

    public static Color lighten(Color color, double factor) {
        int red = Math.min((int) (color.getRed() * factor), 255);
        int green = Math.min((int) (color.getGreen() * factor), 255);
        int blue = Math.min((int) (color.getBlue() * factor), 255);
        return new Color(red, green, blue, color.getAlpha());
    }

    public MetadataEditPane() {
        extension.initTabs(this);
        initComponents();

        textFieldDefault = basicTitle.getBorder();
        textAreaDefault = basicSubject.getBorder();
        comboBoxDefault = basicTrapped.getBorder();
        datePickerDefault = basicCreationDate.getBorder();
        UIDefaults defaults = UIManager.getDefaults();
        Color focusColor = defaults.contains("Button.default.focusColor")
                ? defaults.getColor("Button.default.focusColor")
                : (defaults.contains("Button.focus")
                ? defaults.getColor("Button.focus")
                : defaults.getColor("Button.highlight")
        );
        Color changedColor = Preferences.isLookAndFeelDark() ? lighten(focusColor, 1.3) : darken(focusColor, 0.7);
        changedBorder = BorderFactory.createLineBorder(changedColor, 2);
        licenseRequiredText.setText("<a href='" + Constants.batchLicenseUrl + "'>(Pro License Required)<a>");

    }

    private void traverseFields(MetadataEditPane.FieldSetGet setGet, MetadataEditPane.FieldEnabledCheckBox fieldEnabled) {
        try {
            for (Field field : this.getClass().getFields()) {
                if (setGet != null) {
                    FieldID annos = field.getAnnotation(FieldID.class);
                    if (annos != null) {
                        if (annos.value() != null && annos.value().length() > 0) {
                            Object f = null;
                            try {
                                f = field.get(this);
                            } catch (IllegalArgumentException | IllegalAccessException e) {
                                logger.error("traverseFields on ({})", annos.value(), e);
                                continue;
                            }
                            setGet.apply(f, annos);
                        }
                    }
                }
                if (fieldEnabled != null) {
                    FieldEnabled annosEnabled = field.getAnnotation(FieldEnabled.class);
                    if (annosEnabled != null) {
                        try {
                            JCheckBox f = (JCheckBox) field.get(this);
                            fieldEnabled.apply(f, annosEnabled);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            logger.error("traverseFields on ({})", annosEnabled.value(), e);
                        }
                    }
                }
            }
        } catch (Exception e){
            LOG.error("traverseFields", e);
            throw e;
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
                if (field instanceof JTextField text) {
                    text.setText(null);
                    text.setBorder(textFieldDefault);
                }

                if (field instanceof JTextArea text) {
                    text.setText(null);
                    text.setBorder(textAreaDefault);
                }
                if(field instanceof PdfVersionPicker vp) {
                    objectToField(vp, null);
                    vp.setBorder(comboBoxDefault);
                }else if (field instanceof JComboBox combo) {
                    MetadataInfo.FieldDescription fd = MetadataInfo.getFieldDescription(anno.value());
                    objectToField(combo, null, fd.type == FieldDataType.FieldType.BoolField, fd.nullValueText);
                    combo.setBorder(comboBoxDefault);
                }
                if (field instanceof JSpinner) {
                    objectToField((JSpinner) field, null);
                }
                if (field instanceof DateTimePicker dateTimePicker) {
                    objectToField(dateTimePicker, null);
                    dateTimePicker.setBorder(datePickerDefault);
                }
            }
        }, new MetadataEditPane.FieldEnabledCheckBox() {
            @Override
            public void apply(JCheckBox field, FieldEnabled anno) {
                field.setSelected(true);

            }
        });

        initialMetadata = null;
    }

    public void fillFromMetadata(final MetadataInfo metadataInfo) {
        fillFromMetadata(metadataInfo, false, false);
    }

    public void fillFromMetadata(final MetadataInfo metadataInfo, boolean loadPreset) {
        fillFromMetadata(metadataInfo, loadPreset, true);

    }

    public void fillFromMetadata(final MetadataInfo metadataInfo, boolean loadPreset, boolean ignoreNulls) {
        if (!loadPreset) {
            initialMetadata = metadataInfo.clone();
        }
        LOG.debug("fillFromMetadata(loadPreset={}, ignoreNulls={})", loadPreset, ignoreNulls);
        traverseFields(new MetadataEditPane.FieldSetGet() {
            @Override
            public void apply(Object field, FieldID anno) {

                if (ignoreNulls) {
                    Object o = metadataInfo.get(anno.value());
                    if (o == null) {
                        return;
                    }
                }

                if (field instanceof JTextField) {
                    ((JTextField) field).setText(metadataInfo.getString(anno.value()));
                }
                if (field instanceof JTextArea) {
                    ((JTextArea) field).setText(metadataInfo.getString(anno.value()));
                }

                Object value = metadataInfo.get(anno.value());
                if (field instanceof PdfVersionPicker vp) {
                    objectToField(vp, value);
                } else if (field instanceof JComboBox) {
                    MetadataInfo.FieldDescription fd = MetadataInfo.getFieldDescription(anno.value());
                    objectToField((JComboBox) field, value, fd.type == FieldDataType.FieldType.BoolField, fd.nullValueText);
                }
                if (field instanceof JDateChooser) {
                    objectToField((JDateChooser) field, value);
                }
                if (field instanceof JSpinner) {
                    objectToField((JSpinner) field, value);
                }
                if (field instanceof DateTimePicker) {
                    objectToField((DateTimePicker) field, value);
                }
                if (field instanceof DateTimeList dtl) {
                    objectToField(dtl, value);
                }
            }
        }, new MetadataEditPane.FieldEnabledCheckBox() {
            @Override
            public void apply(JCheckBox field, FieldEnabled anno) {
                field.setSelected(metadataInfo.isEnabled(anno.value()));

            }
        });

    }

    public void fillEnabledFromMetadata(final MetadataInfo metadataInfo) {

        traverseFields(null, new MetadataEditPane.FieldEnabledCheckBox() {
            @Override
            public void apply(JCheckBox field, FieldEnabled anno) {
                field.setSelected(metadataInfo.isEnabled(anno.value()));
            }
        });

    }

    protected String getComboBoxValue(JComboBox field, MetadataInfo.FieldDescription fieldDescription) {
        String text = (String) ((JComboBox) field).getModel().getSelectedItem();
        if (text != null && text.length() == 0) {
            text = null;
        } else {
            String nullText = fieldDescription.nullValueText.isEmpty() ? "Unset" : fieldDescription.nullValueText;
            if (nullText.equals(text)) {
                text = null;
            }
        }
        return text;
    }

    public void copyToMetadata(final MetadataInfo metadataInfo) {
        boolean hasBatch = BatchMan.hasBatch();

        traverseFields(new MetadataEditPane.FieldSetGet() {
            @Override
            public void apply(Object field, FieldID anno) {
                MetadataInfo.FieldDescription fd = MetadataInfo.getFieldDescription(anno.value());
                boolean isReadonly = fd.isReadonly || (!hasBatch && anno.value().startsWith("file."));

                if(isReadonly){
                    return;
                }

                if (field instanceof JTextField || field instanceof JTextArea) {
                    String text = (field instanceof JTextField) ? ((JTextField) field).getText()
                            : ((JTextArea) field).getText();
                    if (text.length() == 0) {
                        text = null;
                    }
                    metadataInfo.setFromString(anno.value(), text);
                    return;
                }
                if (field instanceof JSpinner) {
                    switch (fd.type) {
                        case IntField:
                            Integer i = (Integer) ((JSpinner) field).getModel().getValue();
                            metadataInfo.set(anno.value(), i);
                            break;
                        default:
                            throw new RuntimeException("Cannot store Integer in :" + fd.type + " for field "+ fd.name);

                    }
                    return;
                }
                if (field instanceof PdfVersionPicker vp) {
                    if (fd.type == FieldDataType.FieldType.FloatField) {
                        metadataInfo.set(anno.value(), vp.getVersion());
                    } else {
                        throw new RuntimeException("Cannot store List<Calendar> in :" +  fd.type + "[isList=" + fd.isList+ "] for field "+ fd.name);

                    }
                    return;
                } else if (field instanceof JComboBox comboBox) {
                    String text = getComboBoxValue(comboBox, fd);
                    switch (fd.type) {
                        case StringField:
                            metadataInfo.set(anno.value(), text);
                            break;
                        case BoolField:
                            metadataInfo.setFromString(anno.value(), text);
                            break;
                        case EnumField:
                            metadataInfo.set(anno.value(), text);
                            break;
                        default:
                            throw new RuntimeException("Cannot (store (choice text) in :" + fd.type + " for field "+ fd.name);

                    }
                    return;
                }
                if (field instanceof DateTimePicker) {
                    switch (fd.type) {
                        case DateField:
                            metadataInfo.set(anno.value(), ((DateTimePicker) field).getCalendar());
                            break;
                        default:
                            throw new RuntimeException("Cannot store Calendar in :" +  fd.type + " for field "+ fd.name);

                    }
                    return;
                }
                if (field instanceof DateTimeList dtl) {
                    if (fd.type == FieldDataType.FieldType.DateField && fd.isList) {
                        metadataInfo.set(anno.value(), dtl.getCalendarList());
                    } else {
                            throw new RuntimeException("Cannot store List<Calendar> in :" +  fd.type + "[isList=" + fd.isList+ "] for field "+ fd.name);

                    }
                    return;
                }
            }
        }, new MetadataEditPane.FieldEnabledCheckBox() {
            @Override
            public void apply(JCheckBox field, FieldEnabled anno) {
                metadataInfo.setEnabled(anno.value(), field.isSelected());

            }
        });

    }

    private void objectToField(JComboBox field, Object o, boolean oIsBool, String nullValueText) {
        String v = nullValueText == null || nullValueText.isEmpty() ? "Unset" : nullValueText;

        if (o instanceof String) {
            field.getModel().setSelectedItem(o);
        } else if (o instanceof Boolean || oIsBool) {
            if (o != null) {
                v = (Boolean) o ? "Yes" : "No";
            }
            field.getModel().setSelectedItem(v);
        } else if (o == null) {
            field.getModel().setSelectedItem(v);
        } else {
            RuntimeException e = new RuntimeException("Cannot store non-String object in JComboBox");
            LOG.error("objectToField(JComboBox)", e);
            throw e;
        }
    }

    private void objectToField(JDateChooser field, Object o) {
        if (o instanceof Calendar) {
            field.setCalendar((Calendar) o);
        } else if (o == null) {
            field.setCalendar(null);
        } else {
            RuntimeException e = new RuntimeException("Cannot store non-Calendar object " + o.getClass().getName() +" in JDateChooser");
            LOG.error("objectToField(JDateChooser)", e);
            throw e;
        }
    }

    private void objectToField(PdfVersionPicker field, Object o) {
        if (o == null || o instanceof Float) {
            field.setVersion((Float) o);
        } else {
            RuntimeException e = new RuntimeException("Cannot store non-Calendar object in JDateChooser");
            LOG.error("objectToField(JDateChooser)", e);
            throw e;
        }
    }

    private void objectToField(DateTimePicker field, Object o) {
        if (o instanceof LocalDateTime d) {
            field.setCalendar(d);
        } else if (o instanceof Calendar c) {
            field.setCalendar(c);
        } else if (o == null) {
            field.setCalendar((Calendar) null);
        } else {
            RuntimeException e = new RuntimeException("Cannot store non-(Calendar|LocalDateTime) object in DateTimePicker");
            LOG.error("objectToField(DateTimePicker)", e);
            throw e;
        }
    }

    private void objectToField(DateTimeList field, Object o) {
        if (o == null) {
            field.clearAllDates();
        } else if (o instanceof List c) {
            field.setCalendarList(c);
        } else if (o instanceof LocalDateTime d) {
            field.setCalendarList(List.of(DateTimePicker.toCalendar(d)));
        } else if (o instanceof Calendar c) {
            field.setCalendarList(List.of(c));
        } else {
            RuntimeException e = new RuntimeException("Cannot store non-(Calendar|LocalDateTime) object in DateTimePicker");
            LOG.error("objectToField(DateTimeList)", e);
            throw e;
        }
    }

    private void objectToField(JSpinner field, Object o) {
        if (o instanceof Integer) {
            field.setValue(o);
        } else if (o == null) {
            field.setValue(0);
        } else {
            RuntimeException e = new RuntimeException("Cannot store non-Integerr object in JSpinner");
            LOG.error("objectToField(JSpinner)", e);
            throw e;
        }
    }

    protected class ChangeBackgroundDocumentListener implements DocumentListener {
        JTextComponent textComponent;
        String metadataKey;

        ChangeBackgroundDocumentListener(JTextComponent textComponent, String metadataKey) {
            this.textComponent = textComponent;
            this.metadataKey = metadataKey;
        }

        protected String currentText(DocumentEvent e) {
            try {
                return e.getDocument().getText(0, e.getDocument().getLength());
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void indicateChange(DocumentEvent e) {
            // Code to handle changes
            String current = currentText(e);
            String initial = initialMetadata != null ? initialMetadata.getString(metadataKey) : "";
            if (current.equals(initial)) {
                textComponent.setBorder(textComponent instanceof JTextArea ? textAreaDefault : textFieldDefault);
            } else {
                textComponent.setBorder(changedBorder);

            }
        }

        public void changedUpdate(DocumentEvent e) {
            indicateChange(e);
        }

        public void removeUpdate(DocumentEvent e) {
            indicateChange(e);
        }

        public void insertUpdate(DocumentEvent e) {
            indicateChange(e);
        }
    }


    protected void resetFieldValue(Object component, final String fieldName) {
        if (component instanceof JTextComponent tc) {
            tc.setText(initialMetadata != null ? initialMetadata.getString(fieldName) : null);
            ;
        } else if (component instanceof DateTimePicker dtp) {
            dtp.setCalendar(initialMetadata != null ? (Calendar) initialMetadata.get(fieldName) : null);
        } else if (component instanceof DateTimeList dtl) {
            dtl.setCalendarList(initialMetadata != null ? (List) initialMetadata.get(fieldName) : null);
        } else {
            throw new RuntimeException("Trying to reset value on unsupported component: " + component.getClass().getName());
        }
    }

    public void initComponents() {
        boolean hasBatch = BatchMan.hasBatch();
        licenseRequiredText.setVisible(!hasBatch);

        traverseFields(new MetadataEditPane.FieldSetGet() {

            @Override
            public void apply(Object field, FieldID anno) {
                if (isTesting) {
                    if (field instanceof JComponent jc) {
                        jc.putClientProperty("MetadataFieldId", anno.value());
                    }

                    if (field instanceof MetadataFormComponent fc) {
                        fc.initMetadataFieldId(anno.value());
                    }
                }
                final MetadataInfo.FieldDescription fieldDescription = MetadataInfo.getFieldDescription(anno.value());
                boolean isReadonly = fieldDescription.isReadonly || (!hasBatch && anno.value().startsWith("file."));

                if(isReadonly ){
                    if(field instanceof JTextComponent c) {
                        c.setEditable(false);
                    } else if(field instanceof JComponent c) {
                        c.setEnabled(false);
                    }
                }

                if (field instanceof JTextComponent textComponent) {
                    textComponent.getDocument().addDocumentListener(new ChangeBackgroundDocumentListener(textComponent, anno.value()));
                    new TextFieldContextMenu(textComponent, (component, ignored) -> {
                        resetFieldValue(component, anno.value());
                    }).createContextMenu().addTemplatePlaceholders(!isReadonly);
                }
                if (field instanceof JSpinner spinner) {
                    throw new RuntimeException("JSpinner NOT SUPPORTED!");
                }
                if(field instanceof PdfVersionPicker vp) {
                    vp.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Float selectedValue = vp.getVersion();
                            Object initial = initialMetadata != null ? initialMetadata.get(fieldDescription.name) : null;
                            if ((selectedValue != null && selectedValue.equals(initial)) || (selectedValue == null && initial == null)) {
                                vp.setBorder(comboBoxDefault);
                            } else {
                                vp.setBorder(changedBorder);

                            }
                        }
                    });
                } else if (field instanceof JComboBox combo) {
                    combo.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JComboBox<?> cb = (JComboBox<?>) e.getSource();
                            String selectedValue = getComboBoxValue(cb, fieldDescription);
                            Object initial = initialMetadata != null ? initialMetadata.get(fieldDescription.name) : null;
                            String initialS = null;
                            if (initial != null) {
                                if (initial instanceof String s) {
                                    initialS = s;
                                }
                                if (initial instanceof Boolean b) {
                                    initialS = b ? "Yes" : "No";
                                }
                            }
                            if ((selectedValue != null && selectedValue.equals(initialS)) || (selectedValue == null && initialS == null)) {
                                combo.setBorder(comboBoxDefault);
                            } else {
                                combo.setBorder(changedBorder);

                            }
                        }
                    });
                    if (fieldDescription.type == FieldDataType.FieldType.EnumField) {
                        combo.setModel(new DefaultComboBoxModel(fieldDescription.getEnumValuesAsStrings()));
                    }
                }
                if (field instanceof DateTimePicker dtPicker) {
                    dtPicker.addPropertyChangeListener("date", new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            Calendar selectedValueC = dtPicker.getCalendar();
                            Calendar initialC = initialMetadata != null ? (Calendar) initialMetadata.get(anno.value()) : null;

                            if ((selectedValueC == null && initialC == null) || (selectedValueC != null && initialC != null && (selectedValueC.compareTo(initialC) == 0))) {
                                dtPicker.setBorder(datePickerDefault);
                            } else {
                                dtPicker.setBorder(changedBorder);
                            }
                        }
                    });
                    new TextFieldContextMenu(dtPicker, (component, textComponent) -> {
                        resetFieldValue(component, anno.value());
                    }).createContextMenu();
                }

                if (field instanceof DateTimeList dtList) {
                    dtList.addDataChangeLister(new ListDataListener() {
                        @Override
                        public void intervalAdded(ListDataEvent e) {
                            dataChange(e);
                        }

                        @Override
                        public void intervalRemoved(ListDataEvent e) {
                            dataChange(e);
                        }

                        @Override
                        public void contentsChanged(ListDataEvent e) {
                            dataChange(e);
                        }

                        public void dataChange(ListDataEvent evt) {
                            List<Calendar> selectedValueC = dtList.getCalendarList();
                            List<Calendar> initialC = initialMetadata != null ? (List<Calendar>) initialMetadata.get(anno.value()) : null;
                            List<Calendar> selectedValue = selectedValueC != null ? selectedValueC : List.of();
                            List<Calendar> initial = initialC != null ? initialC : List.of();

                            boolean hasChange = selectedValue.size() != initial.size();
                            if (!hasChange) {
                                for (int i = 0; i < selectedValue.size(); i++) {
                                    if (selectedValue.get(i).compareTo(initial.get(i)) != 0) {
                                        hasChange = true;
                                        break;
                                    }
                                }
                            }

                            if (!hasChange) {
                                dtList.topPanel.setBorder(datePickerDefault);
                            } else {
                                dtList.topPanel.setBorder(changedBorder);
                            }
                        }
                    });
                    new TextFieldContextMenu(dtList, (component, textComponent) -> {
                        resetFieldValue(component, anno.value());
                    }).createContextMenu();
                }
            }
        }, isTesting
                ? new MetadataEditPane.FieldEnabledCheckBox() {
                    @Override
                    public void apply(JCheckBox field, FieldEnabled anno) {
                        field.putClientProperty("EnabledMetadataFieldId", anno.value());
                    }
                }
                : null
        );


    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane = new JTabbedPane();
        panel1.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 300), null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Document", panel2);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(10, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane1.setViewportView(panel3);
        final JLabel label1 = new JLabel();
        label1.setText("Title");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        basicTitleEnabled = new JCheckBox();
        basicTitleEnabled.setText("");
        panel3.add(basicTitleEnabled, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicTitle = new JTextField();
        panel3.add(basicTitle, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Author");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicAuthorEnabled = new JCheckBox();
        basicAuthorEnabled.setText("");
        panel3.add(basicAuthorEnabled, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicAuthor = new JTextField();
        panel3.add(basicAuthor, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Subject");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicSubjectEnabled = new JCheckBox();
        basicSubjectEnabled.setText("");
        panel3.add(basicSubjectEnabled, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel3.add(scrollPane2, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        basicSubject = new JTextArea();
        scrollPane2.setViewportView(basicSubject);
        final JLabel label4 = new JLabel();
        label4.setText("Keywords");
        panel3.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicKeywordsEnabled = new JCheckBox();
        basicKeywordsEnabled.setText("");
        panel3.add(basicKeywordsEnabled, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel3.add(scrollPane3, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        basicKeywords = new JTextArea();
        scrollPane3.setViewportView(basicKeywords);
        final JLabel label5 = new JLabel();
        label5.setText("Creator");
        panel3.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicCreatorEnabled = new JCheckBox();
        basicCreatorEnabled.setText("");
        panel3.add(basicCreatorEnabled, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicCreator = new JTextField();
        panel3.add(basicCreator, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Producer");
        panel3.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicProducerEnabled = new JCheckBox();
        basicProducerEnabled.setText("");
        panel3.add(basicProducerEnabled, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicProducer = new JTextField();
        panel3.add(basicProducer, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Creation Date");
        panel3.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicCreationDateEnabled = new JCheckBox();
        basicCreationDateEnabled.setText("");
        panel3.add(basicCreationDateEnabled, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Modification Date");
        panel3.add(label8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicModificationDateEnabled = new JCheckBox();
        basicModificationDateEnabled.setText("");
        panel3.add(basicModificationDateEnabled, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Trapped");
        panel3.add(label9, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicTrappedEnabled = new JCheckBox();
        basicTrappedEnabled.setText("");
        panel3.add(basicTrappedEnabled, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicTrapped = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("True");
        defaultComboBoxModel1.addElement("False");
        defaultComboBoxModel1.addElement("Unset");
        basicTrapped.setModel(defaultComboBoxModel1);
        panel3.add(basicTrapped, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicCreationDate = new DateTimePicker();
        panel3.add(basicCreationDate, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        basicModificationDate = new DateTimePicker();
        panel3.add(basicModificationDate, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(11, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("XMP Basic", panel4);
        final JScrollPane scrollPane4 = new JScrollPane();
        panel4.add(scrollPane4, new GridConstraints(0, 0, 11, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(11, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane4.setViewportView(panel5);
        final JLabel label10 = new JLabel();
        label10.setText("Creator tool");
        panel5.add(label10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpBasicCreatorToolEnabled = new JCheckBox();
        xmpBasicCreatorToolEnabled.setText("");
        panel5.add(xmpBasicCreatorToolEnabled, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicCreatorTool = new JTextField();
        panel5.add(xmpBasicCreatorTool, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Create Date");
        panel5.add(label11, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicCreateDateEnabled = new JCheckBox();
        xmpBasicCreateDateEnabled.setText("");
        panel5.add(xmpBasicCreateDateEnabled, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicCreateDate = new DateTimePicker();
        panel5.add(xmpBasicCreateDate, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Modify Date");
        panel5.add(label12, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicModifyDateEnabled = new JCheckBox();
        xmpBasicModifyDateEnabled.setText("");
        panel5.add(xmpBasicModifyDateEnabled, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicModifyDate = new DateTimePicker();
        panel5.add(xmpBasicModifyDate, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Base URL");
        panel5.add(label13, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicBaseURLEnabled = new JCheckBox();
        xmpBasicBaseURLEnabled.setText("");
        panel5.add(xmpBasicBaseURLEnabled, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicBaseURL = new JTextField();
        panel5.add(xmpBasicBaseURL, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Rating");
        panel5.add(label14, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicRatingEnable = new JCheckBox();
        xmpBasicRatingEnable.setText("");
        panel5.add(xmpBasicRatingEnable, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicRating = new JTextField();
        panel5.add(xmpBasicRating, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Label");
        panel5.add(label15, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicLabelEnabled = new JCheckBox();
        xmpBasicLabelEnabled.setText("");
        panel5.add(xmpBasicLabelEnabled, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicLabel = new JTextField();
        panel5.add(xmpBasicLabel, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Nickname");
        panel5.add(label16, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicNicknameEnabled = new JCheckBox();
        xmpBasicNicknameEnabled.setText("");
        panel5.add(xmpBasicNicknameEnabled, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicNickname = new JTextField();
        panel5.add(xmpBasicNickname, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Identifiers");
        panel5.add(label17, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicIdentifiersEnabled = new JCheckBox();
        xmpBasicIdentifiersEnabled.setText("");
        panel5.add(xmpBasicIdentifiersEnabled, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane5 = new JScrollPane();
        panel5.add(scrollPane5, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpBasicIdentifiers = new JTextArea();
        scrollPane5.setViewportView(xmpBasicIdentifiers);
        final JLabel label18 = new JLabel();
        label18.setText("Advisories");
        panel5.add(label18, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicAdvisoriesEnabled = new JCheckBox();
        xmpBasicAdvisoriesEnabled.setText("");
        panel5.add(xmpBasicAdvisoriesEnabled, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane6 = new JScrollPane();
        panel5.add(scrollPane6, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpBasicAdvisories = new JTextArea();
        scrollPane6.setViewportView(xmpBasicAdvisories);
        final JLabel label19 = new JLabel();
        label19.setText("Metadata Date");
        panel5.add(label19, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicMetadataDateEnabled = new JCheckBox();
        xmpBasicMetadataDateEnabled.setText("");
        panel5.add(xmpBasicMetadataDateEnabled, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpBasicMetadataDate = new DateTimePicker();
        panel5.add(xmpBasicMetadataDate, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("XMP PDF", panel6);
        final JScrollPane scrollPane7 = new JScrollPane();
        panel6.add(scrollPane7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(4, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane7.setViewportView(panel7);
        final JLabel label20 = new JLabel();
        label20.setText("Keywords");
        panel7.add(label20, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel7.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpPdfKeywordsEnabled = new JCheckBox();
        xmpPdfKeywordsEnabled.setText("");
        panel7.add(xmpPdfKeywordsEnabled, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane8 = new JScrollPane();
        panel7.add(scrollPane8, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpPdfKeywords = new JTextArea();
        scrollPane8.setViewportView(xmpPdfKeywords);
        final JLabel label21 = new JLabel();
        label21.setText("PDF Version");
        panel7.add(label21, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpPdfVersionEnabled = new JCheckBox();
        xmpPdfVersionEnabled.setText("");
        panel7.add(xmpPdfVersionEnabled, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpPdfVersion = new JTextField();
        xmpPdfVersion.setEditable(true);
        panel7.add(xmpPdfVersion, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("Producer");
        panel7.add(label22, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpPdfProducerEnabled = new JCheckBox();
        xmpPdfProducerEnabled.setText("");
        panel7.add(xmpPdfProducerEnabled, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpPdfProducer = new JTextField();
        panel7.add(xmpPdfProducer, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("XMP Dublin Core", panel8);
        final JScrollPane scrollPane9 = new JScrollPane();
        panel8.add(scrollPane9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(16, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane9.setViewportView(panel9);
        final JLabel label23 = new JLabel();
        label23.setText("Title");
        panel9.add(label23, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel9.add(spacer4, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmlDcTitleEnabled = new JCheckBox();
        xmlDcTitleEnabled.setText("");
        panel9.add(xmlDcTitleEnabled, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcTitle = new JTextField();
        panel9.add(xmpDcTitle, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label24 = new JLabel();
        label24.setText("Description");
        panel9.add(label24, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcDescriptionEnabled = new JCheckBox();
        xmpDcDescriptionEnabled.setText("");
        panel9.add(xmpDcDescriptionEnabled, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcDescription = new JTextField();
        panel9.add(xmpDcDescription, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label25 = new JLabel();
        label25.setText("Creators");
        panel9.add(label25, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcCreatorsEnabled = new JCheckBox();
        xmpDcCreatorsEnabled.setText("");
        panel9.add(xmpDcCreatorsEnabled, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane10 = new JScrollPane();
        panel9.add(scrollPane10, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcCreators = new JTextArea();
        scrollPane10.setViewportView(xmpDcCreators);
        final JLabel label26 = new JLabel();
        label26.setText("Contributors");
        panel9.add(label26, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcContributorsEnabled = new JCheckBox();
        xmpDcContributorsEnabled.setText("");
        panel9.add(xmpDcContributorsEnabled, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane11 = new JScrollPane();
        panel9.add(scrollPane11, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcContributors = new JTextArea();
        scrollPane11.setViewportView(xmpDcContributors);
        final JLabel label27 = new JLabel();
        label27.setText("Coverage");
        panel9.add(label27, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcCoverageEnabled = new JCheckBox();
        xmpDcCoverageEnabled.setText("");
        panel9.add(xmpDcCoverageEnabled, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcCoverage = new JTextField();
        panel9.add(xmpDcCoverage, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label28 = new JLabel();
        label28.setText("Dates");
        panel9.add(label28, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcDatesEnabled = new JCheckBox();
        xmpDcDatesEnabled.setText("");
        panel9.add(xmpDcDatesEnabled, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcDates = new DateTimeList();
        panel9.add(xmpDcDates.$$$getRootComponent$$$(), new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label29 = new JLabel();
        label29.setText("Format");
        panel9.add(label29, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcFormatEnabled = new JCheckBox();
        xmpDcFormatEnabled.setText("");
        panel9.add(xmpDcFormatEnabled, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcFormat = new JTextField();
        panel9.add(xmpDcFormat, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label30 = new JLabel();
        label30.setText("Identifier");
        panel9.add(label30, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcIdentifierEnabled = new JCheckBox();
        xmpDcIdentifierEnabled.setText("");
        panel9.add(xmpDcIdentifierEnabled, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcIdentifier = new JTextField();
        panel9.add(xmpDcIdentifier, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label31 = new JLabel();
        label31.setText("Languages");
        panel9.add(label31, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcLanguagesEnabled = new JCheckBox();
        xmpDcLanguagesEnabled.setText("");
        panel9.add(xmpDcLanguagesEnabled, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane12 = new JScrollPane();
        panel9.add(scrollPane12, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcLanguages = new JTextArea();
        scrollPane12.setViewportView(xmpDcLanguages);
        final JLabel label32 = new JLabel();
        label32.setText("Publishers");
        panel9.add(label32, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcPublishersEnabled = new JCheckBox();
        xmpDcPublishersEnabled.setText("");
        panel9.add(xmpDcPublishersEnabled, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane13 = new JScrollPane();
        panel9.add(scrollPane13, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcPublishers = new JTextArea();
        scrollPane13.setViewportView(xmpDcPublishers);
        final JLabel label33 = new JLabel();
        label33.setText("Relationships");
        panel9.add(label33, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcRelationshipsEnabled = new JCheckBox();
        xmpDcRelationshipsEnabled.setText("");
        panel9.add(xmpDcRelationshipsEnabled, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane14 = new JScrollPane();
        panel9.add(scrollPane14, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcRelationships = new JTextArea();
        scrollPane14.setViewportView(xmpDcRelationships);
        final JLabel label34 = new JLabel();
        label34.setText("Rights");
        panel9.add(label34, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcRightsEnabled = new JCheckBox();
        xmpDcRightsEnabled.setText("");
        panel9.add(xmpDcRightsEnabled, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcRights = new JTextField();
        panel9.add(xmpDcRights, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label35 = new JLabel();
        label35.setText("Source");
        panel9.add(label35, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcSourceEnabled = new JCheckBox();
        xmpDcSourceEnabled.setText("");
        panel9.add(xmpDcSourceEnabled, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcSource = new JTextField();
        panel9.add(xmpDcSource, new GridConstraints(12, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label36 = new JLabel();
        label36.setText("Subjects");
        panel9.add(label36, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcSubjectsEnabled = new JCheckBox();
        xmpDcSubjectsEnabled.setText("");
        panel9.add(xmpDcSubjectsEnabled, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane15 = new JScrollPane();
        panel9.add(scrollPane15, new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcSubjects = new JTextArea();
        scrollPane15.setViewportView(xmpDcSubjects);
        final JLabel label37 = new JLabel();
        label37.setText("Types");
        panel9.add(label37, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcTypesEnabled = new JCheckBox();
        xmpDcTypesEnabled.setText("");
        panel9.add(xmpDcTypesEnabled, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane16 = new JScrollPane();
        panel9.add(scrollPane16, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcTypes = new JTextArea();
        scrollPane16.setViewportView(xmpDcTypes);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("XMP Rights", panel10);
        final JScrollPane scrollPane17 = new JScrollPane();
        panel10.add(scrollPane17, new GridConstraints(0, 0, 7, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(6, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane17.setViewportView(panel11);
        final JLabel label38 = new JLabel();
        label38.setText("Certificate");
        panel11.add(label38, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel11.add(spacer5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpRightsCertificateEnabled = new JCheckBox();
        xmpRightsCertificateEnabled.setText("");
        panel11.add(xmpRightsCertificateEnabled, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsCertificate = new JTextField();
        panel11.add(xmpRightsCertificate, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label39 = new JLabel();
        label39.setText("Marked");
        panel11.add(label39, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsMarkedEnabled = new JCheckBox();
        xmpRightsMarkedEnabled.setText("");
        panel11.add(xmpRightsMarkedEnabled, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsMarked = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("Unset");
        defaultComboBoxModel2.addElement("Yes");
        defaultComboBoxModel2.addElement("No");
        xmpRightsMarked.setModel(defaultComboBoxModel2);
        panel11.add(xmpRightsMarked, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label40 = new JLabel();
        label40.setText("Owners");
        panel11.add(label40, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsOwnerEnabled = new JCheckBox();
        xmpRightsOwnerEnabled.setText("");
        panel11.add(xmpRightsOwnerEnabled, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane18 = new JScrollPane();
        panel11.add(scrollPane18, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpRightsOwner = new JTextArea();
        scrollPane18.setViewportView(xmpRightsOwner);
        final JLabel label41 = new JLabel();
        label41.setText("Usage Terms");
        panel11.add(label41, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsUsageTermsEnabled = new JCheckBox();
        xmpRightsUsageTermsEnabled.setText("");
        panel11.add(xmpRightsUsageTermsEnabled, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane19 = new JScrollPane();
        panel11.add(scrollPane19, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpRightsUsageTerms = new JTextArea();
        scrollPane19.setViewportView(xmpRightsUsageTerms);
        final JLabel label42 = new JLabel();
        label42.setText("Web Statement");
        panel11.add(label42, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsWebStatementEnabled = new JCheckBox();
        xmpRightsWebStatementEnabled.setText("");
        panel11.add(xmpRightsWebStatementEnabled, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsWebStatement = new JTextField();
        panel11.add(xmpRightsWebStatement, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Viewer Options", panel12);
        final JScrollPane scrollPane20 = new JScrollPane();
        panel12.add(scrollPane20, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(17, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane20.setViewportView(panel13);
        final JLabel label43 = new JLabel();
        label43.setText("Hide tool bars");
        panel13.add(label43, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel13.add(spacer6, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        hideToolbar = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Unset");
        defaultComboBoxModel3.addElement("Yes");
        defaultComboBoxModel3.addElement("No");
        hideToolbar.setModel(defaultComboBoxModel3);
        panel13.add(hideToolbar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideToolbarEnabled = new JCheckBox();
        hideToolbarEnabled.setText("");
        panel13.add(hideToolbarEnabled, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label44 = new JLabel();
        label44.setText("Hide menu bar");
        panel13.add(label44, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label45 = new JLabel();
        label45.setText("Hide window controls");
        panel13.add(label45, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label46 = new JLabel();
        label46.setText("Resize window to inital page");
        panel13.add(label46, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label47 = new JLabel();
        label47.setText("Center window on screen");
        panel13.add(label47, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label48 = new JLabel();
        label48.setText("Display Title in window caption");
        panel13.add(label48, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideMenuBarEnabled = new JCheckBox();
        hideMenuBarEnabled.setText("");
        panel13.add(hideMenuBarEnabled, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideWindowUIEnabled = new JCheckBox();
        hideWindowUIEnabled.setText("");
        panel13.add(hideWindowUIEnabled, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fitWindowEnabled = new JCheckBox();
        fitWindowEnabled.setText("");
        panel13.add(fitWindowEnabled, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        centerWindowEnabled = new JCheckBox();
        centerWindowEnabled.setText("");
        panel13.add(centerWindowEnabled, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        displayDocTitleEnabled = new JCheckBox();
        displayDocTitleEnabled.setText("");
        panel13.add(displayDocTitleEnabled, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideMenuBar = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("Unset");
        defaultComboBoxModel4.addElement("Yes");
        defaultComboBoxModel4.addElement("No");
        hideMenuBar.setModel(defaultComboBoxModel4);
        panel13.add(hideMenuBar, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideWindowUI = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel5 = new DefaultComboBoxModel();
        defaultComboBoxModel5.addElement("Unset");
        defaultComboBoxModel5.addElement("Yes");
        defaultComboBoxModel5.addElement("No");
        hideWindowUI.setModel(defaultComboBoxModel5);
        panel13.add(hideWindowUI, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fitWindow = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel6 = new DefaultComboBoxModel();
        defaultComboBoxModel6.addElement("Unset");
        defaultComboBoxModel6.addElement("Yes");
        defaultComboBoxModel6.addElement("No");
        fitWindow.setModel(defaultComboBoxModel6);
        panel13.add(fitWindow, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        centerWindow = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel7 = new DefaultComboBoxModel();
        defaultComboBoxModel7.addElement("Unset");
        defaultComboBoxModel7.addElement("Yes");
        defaultComboBoxModel7.addElement("No");
        centerWindow.setModel(defaultComboBoxModel7);
        panel13.add(centerWindow, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        displayDocTitle = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel8 = new DefaultComboBoxModel();
        defaultComboBoxModel8.addElement("Unset");
        defaultComboBoxModel8.addElement("Yes");
        defaultComboBoxModel8.addElement("No");
        displayDocTitle.setModel(defaultComboBoxModel8);
        panel13.add(displayDocTitle, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label49 = new JLabel();
        label49.setText("Non full screen page mode");
        panel13.add(label49, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label50 = new JLabel();
        label50.setText("Reading direction for text");
        panel13.add(label50, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nonFullScreenPageModeEnabled = new JCheckBox();
        nonFullScreenPageModeEnabled.setText("");
        panel13.add(nonFullScreenPageModeEnabled, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        readingDirectionEnabled = new JCheckBox();
        readingDirectionEnabled.setText("");
        panel13.add(readingDirectionEnabled, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nonFullScreenPageMode = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel9 = new DefaultComboBoxModel();
        nonFullScreenPageMode.setModel(defaultComboBoxModel9);
        panel13.add(nonFullScreenPageMode, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        readingDirection = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel10 = new DefaultComboBoxModel();
        readingDirection.setModel(defaultComboBoxModel10);
        panel13.add(readingDirection, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label51 = new JLabel();
        label51.setText("View Area");
        panel13.add(label51, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label52 = new JLabel();
        label52.setText("View Clip");
        panel13.add(label52, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label53 = new JLabel();
        label53.setText("Print Area");
        panel13.add(label53, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label54 = new JLabel();
        label54.setText("Print Clip");
        panel13.add(label54, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label55 = new JLabel();
        label55.setText("Duplex");
        panel13.add(label55, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label56 = new JLabel();
        label56.setText("Print Scaling");
        panel13.add(label56, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        viewAreaEnabled = new JCheckBox();
        viewAreaEnabled.setText("");
        panel13.add(viewAreaEnabled, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        viewClipEnabled = new JCheckBox();
        viewClipEnabled.setText("");
        panel13.add(viewClipEnabled, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printAreaEnabled = new JCheckBox();
        printAreaEnabled.setText("");
        panel13.add(printAreaEnabled, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printClipEnabled = new JCheckBox();
        printClipEnabled.setText("");
        panel13.add(printClipEnabled, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        duplexEnabled = new JCheckBox();
        duplexEnabled.setText("");
        panel13.add(duplexEnabled, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printScalingEnabled = new JCheckBox();
        printScalingEnabled.setText("");
        panel13.add(printScalingEnabled, new GridConstraints(15, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        viewArea = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel11 = new DefaultComboBoxModel();
        viewArea.setModel(defaultComboBoxModel11);
        panel13.add(viewArea, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        viewClip = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel12 = new DefaultComboBoxModel();
        viewClip.setModel(defaultComboBoxModel12);
        panel13.add(viewClip, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printArea = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel13 = new DefaultComboBoxModel();
        printArea.setModel(defaultComboBoxModel13);
        panel13.add(printArea, new GridConstraints(12, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printClip = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel14 = new DefaultComboBoxModel();
        printClip.setModel(defaultComboBoxModel14);
        panel13.add(printClip, new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        duplex = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel15 = new DefaultComboBoxModel();
        duplex.setModel(defaultComboBoxModel15);
        panel13.add(duplex, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printScaling = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel16 = new DefaultComboBoxModel();
        printScaling.setModel(defaultComboBoxModel16);
        panel13.add(printScaling, new GridConstraints(15, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label57 = new JLabel();
        label57.setText("Page Layout");
        panel13.add(label57, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageLayoutEnabled = new JCheckBox();
        pageLayoutEnabled.setText("");
        panel13.add(pageLayoutEnabled, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageLayout = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel17 = new DefaultComboBoxModel();
        pageLayout.setModel(defaultComboBoxModel17);
        panel13.add(pageLayout, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label58 = new JLabel();
        label58.setText("Page mode");
        panel13.add(label58, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageModeEnabled = new JCheckBox();
        pageModeEnabled.setText("");
        panel13.add(pageModeEnabled, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageMode = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel18 = new DefaultComboBoxModel();
        pageMode.setModel(defaultComboBoxModel18);
        panel13.add(pageMode, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("File Properties", panel14);
        final JScrollPane scrollPane21 = new JScrollPane();
        panel14.add(scrollPane21, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(12, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane21.setViewportView(panel15);
        final JLabel label59 = new JLabel();
        label59.setText("Full Path");
        panel15.add(label59, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel15.add(spacer7, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fullPathEnabled = new JCheckBox();
        fullPathEnabled.setText("");
        panel15.add(fullPathEnabled, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fullPath = new JTextField();
        panel15.add(fullPath, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label60 = new JLabel();
        label60.setText("Size");
        panel15.add(label60, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sizeEnabled = new JCheckBox();
        sizeEnabled.setText("");
        panel15.add(sizeEnabled, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        size = new JTextField();
        panel15.add(size, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label61 = new JLabel();
        label61.setText("Size Bytes");
        panel15.add(label61, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sizeBytesEnabled = new JCheckBox();
        sizeBytesEnabled.setText("");
        panel15.add(sizeBytesEnabled, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sizeBytes = new JTextField();
        sizeBytes.setText("");
        panel15.add(sizeBytes, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label62 = new JLabel();
        label62.setText("Create Time");
        panel15.add(label62, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTimeEnabled = new JCheckBox();
        createTimeEnabled.setText("");
        panel15.add(createTimeEnabled, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label63 = new JLabel();
        label63.setText("Modify Time");
        panel15.add(label63, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modifyTimeEnabled = new JCheckBox();
        modifyTimeEnabled.setText("");
        panel15.add(modifyTimeEnabled, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label64 = new JLabel();
        label64.setText("PDF Version");
        panel15.add(label64, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pdfVersionEnabled = new JCheckBox();
        pdfVersionEnabled.setText("");
        panel15.add(pdfVersionEnabled, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pdfVersion = new PdfVersionPicker();
        panel15.add(pdfVersion, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label65 = new JLabel();
        label65.setText("PDF Compressed");
        panel15.add(label65, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pdfCompressionEnabled = new JCheckBox();
        pdfCompressionEnabled.setText("");
        panel15.add(pdfCompressionEnabled, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTime = new DateTimePicker();
        panel15.add(createTime, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        modifyTime = new DateTimePicker();
        panel15.add(modifyTime, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pdfCompression = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel19 = new DefaultComboBoxModel();
        defaultComboBoxModel19.addElement("Yes");
        defaultComboBoxModel19.addElement("No");
        pdfCompression.setModel(defaultComboBoxModel19);
        panel15.add(pdfCompression, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), null, null, 0, false));
        final JLabel label66 = new JLabel();
        Font label66Font = this.$$$getFont$$$(null, Font.BOLD | Font.ITALIC, -1, label66.getFont());
        if (label66Font != null) label66.setFont(label66Font);
        label66.setText("Read Only Properties");
        panel15.add(label66, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 2, false));
        final JLabel label67 = new JLabel();
        Font label67Font = this.$$$getFont$$$(null, Font.BOLD | Font.ITALIC, -1, label67.getFont());
        if (label67Font != null) label67.setFont(label67Font);
        label67.setText("Editable Properties");
        panel15.add(label67, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 2, false));
        licenseRequiredText = new TextPaneWithLinks();
        panel15.add(licenseRequiredText, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label68 = new JLabel();
        label68.setText("Name");
        panel15.add(label68, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameEnabled = new JCheckBox();
        nameEnabled.setText("");
        panel15.add(nameEnabled, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        name = new JTextField();
        panel15.add(name, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label69 = new JLabel();
        label69.setText("File Name");
        panel15.add(label69, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameWithExtEnabled = new JCheckBox();
        nameWithExtEnabled.setText("");
        panel15.add(nameWithExtEnabled, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameWithExt = new JTextField();
        panel15.add(nameWithExt, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    public interface FieldSetGet {
        void apply(Object field, FieldID anno);
    }


    public interface FieldEnabledCheckBox {
        void apply(JCheckBox field, FieldEnabled anno);
    }

    public <T extends PresetValues> void onLoadPreset(T values) {
        extension.onLoadPreset(values);
        MetadataInfo presetMetadata = new MetadataInfo();

        if (values.metadata != null && !values.metadata.isEmpty()) {
            presetMetadata.fromFlatMap(values.metadata);
        }

        if (values.metadataEnabled != null && !values.metadataEnabled.isEmpty()) {
            presetMetadata.setEnabled(false);
            for (String k : values.metadataEnabled.keySet()) {
                presetMetadata.setEnabled(k, values.metadataEnabled.get(k));
            }
        } else {
            presetMetadata.enableOnlyNonNull();
        }

        fillFromMetadata(presetMetadata, true);
    }

    public <T extends PresetValues> void onDeletePreset(T values) {
        extension.onDeletePreset(values);
    }

    public <T extends PresetValues> void onSavePreset(T values) {
        extension.onSavePreset(values);
        MetadataInfo initial = initialMetadata != null ? initialMetadata : new MetadataInfo();
        Map<String, Object> initialMap = initial.asFlatMap();
        MetadataInfo current = new MetadataInfo();
        copyToMetadata(current);
        Map<String, Object> currentMap = current.asFlatMap();
        Map<String, Boolean> enabledMap = new HashMap<>();

        for (String k : initialMap.keySet()) {
            Object oi = initialMap.get(k);
            Object ci = currentMap.get(k);

            if ((oi == null && ci == null) || (oi != null && oi.equals(ci))) {
                currentMap.remove(k);
            } else {
                if (initial.isEnabled(k)) {
                    enabledMap.put(k, true);
                }
            }
        }
        if (!currentMap.isEmpty()) {
            values.metadata = currentMap;
        }
        if (!enabledMap.isEmpty()) {
            values.metadataEnabled = enabledMap;
        }

    }
}
