package pmedit.ui;

import pmedit.ui.components.DateTimePicker;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.toedter.calendar.JDateChooser;
import pmedit.*;
import pmedit.ext.PmeExtension;
import pmedit.preset.PresetValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;

public class MetadataEditPane {
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
    @FieldID(value = "doc.creationDate", type = FieldID.FieldType.DateField)
    public DateTimePicker basicCreationDate;
    @FieldID(value = "doc.modificationDate", type = FieldID.FieldType.DateField)
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
    @FieldID(value = "basic.identifiers", type = FieldID.FieldType.TextField)
    public JTextArea xmpBasicIdentifiers;
    @FieldID(value = "basic.advisories", type = FieldID.FieldType.TextField)
    public JTextArea xmpBasicAdvisories;
    @FieldID(value = "basic.modifyDate", type = FieldID.FieldType.DateField)
    public DateTimePicker xmpBasicModifyDate;
    @FieldID(value = "basic.createDate", type = FieldID.FieldType.DateField)
    public DateTimePicker xmpBasicCreateDate;
    @FieldID(value = "basic.metadataDate", type = FieldID.FieldType.DateField)
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
    @FieldID(value = "rights.owner", type = FieldID.FieldType.TextField)
    public JTextArea xmpRightsOwner;
    @FieldID(value = "rights.copyright")
    public JTextArea xmpRightsCopyright;
    @FieldID("rights.usageTerms")
    public JTextArea xmpRightsUsageTerms;
    @FieldID("rights.webStatement")
    public JTextField xmpRightsWebStatement;


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
    private PmeExtension extension = PmeExtension.get();
    MetadataInfo initialMetadata;
    Border textFieldDefault;
    Border textAreaDefault;
    Border comboBoxDefault;
    Border datePickerDefault;
    Border changedBorder;

    public MetadataEditPane() {
        extension.initTabs(this);
        initComponents();

        textFieldDefault = basicTitle.getBorder();
        textAreaDefault = basicSubject.getBorder();
        comboBoxDefault = basicTrapped.getBorder();
        datePickerDefault = basicCreationDate.getBorder();
        changedBorder = BorderFactory.createLineBorder(Color.BLACK, 2);
    }

    private void traverseFields(MetadataEditPane.FieldSetGet setGet, MetadataEditPane.FieldEnabledCheckBox fieldEnabled) {
        for (Field field : this.getClass().getFields()) {
            if (setGet != null) {
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
            if (fieldEnabled != null) {
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
                if (field instanceof JTextField text) {
                    text.setText(null);
                    text.setBorder(textFieldDefault);
                }

                if (field instanceof JTextArea text) {
                    text.setText(null);
                    text.setBorder(textAreaDefault);
                }

                if (field instanceof JComboBox combo) {
                    objectToField(combo, null, anno.type() == FieldID.FieldType.BoolField, anno.nullValueText());
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
        fillFromMetadata(metadataInfo, false);
    }

    public void fillFromMetadata(final MetadataInfo metadataInfo, boolean loadPreset) {
        if (!loadPreset) {
            initialMetadata = metadataInfo.clone();
        }

        traverseFields(new MetadataEditPane.FieldSetGet() {
            @Override
            public void apply(Object field, FieldID anno) {

                if (loadPreset) {
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
                if (field instanceof JComboBox) {
                    objectToField((JComboBox) field, value, anno.type() == FieldID.FieldType.BoolField, anno.nullValueText());
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
            }
        }, new MetadataEditPane.FieldEnabledCheckBox() {
            @Override
            public void apply(JCheckBox field, FieldEnabled anno) {
                field.setSelected(metadataInfo.isEnabled(anno.value()));

            }
        });

    }

    protected String getComboBoxValue(JComboBox field, FieldID anno) {
        String text = (String) ((JComboBox) field).getModel().getSelectedItem();
        if (text != null && text.length() == 0) {
            text = null;
        } else {
            String nullText = anno.nullValueText().isEmpty() ? "Unset" : anno.nullValueText();
            if (nullText.equals(text)) {
                text = null;
            }
        }
        return text;
    }

    public void copyToMetadata(final MetadataInfo metadataInfo) {

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
                if (field instanceof JComboBox comboBox) {
                    String text = getComboBoxValue(comboBox, anno);
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
                if (field instanceof DateTimePicker) {
                    switch (anno.type()) {
                        case DateField:
                            metadataInfo.set(anno.value(), ((DateTimePicker) field).getCalendar());
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

    private void objectToField(DateTimePicker field, Object o) {
        if (o instanceof LocalDateTime d) {
            field.setCalendar(d);
        } else if (o instanceof Calendar c) {
            field.setCalendar(c);
        } else if (o == null) {
            field.setCalendar((Calendar) null);
        } else {
            RuntimeException e = new RuntimeException("Cannot store non-(Calendar|LocalDateTime) object in DateTimePicker");
            e.printStackTrace();
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
            e.printStackTrace();
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


    protected void resetFieldValue(JComponent component, final String fieldName) {
        if (component instanceof JTextComponent tc) {
            tc.setText(initialMetadata != null ? initialMetadata.getString(fieldName) : null);
            ;
        } else if (component instanceof DateTimePicker dtp) {
            dtp.setCalendar(initialMetadata != null ? (Calendar) initialMetadata.get(fieldName) : null);
        } else {
            throw new RuntimeException("Trying to reset value on unsupported component: " + component.getClass().getName());
        }
    }


    private void createContextMenu(JComponent component, final String fieldName) {
        JTextComponent textComponent;
        if (component instanceof JTextComponent tc) {
            textComponent = tc;
        } else if (component instanceof DateTimePicker dtp) {
            textComponent = dtp.getTextComponent();
        } else {
            throw new RuntimeException("Trying to create menu on unsupported component: " + component.getClass().getName());
        }

        JPopupMenu popupMenu = new JPopupMenu();

        // Create menu items with actions
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem pasteItem = new JMenuItem("Paste");
        JMenuItem resetItem = new JMenuItem("Reset");

        // Add action listeners
        copyItem.addActionListener(e -> textComponent.copy());
        cutItem.addActionListener(e -> textComponent.cut());
        pasteItem.addActionListener(e -> textComponent.paste());
        resetItem.addActionListener(e -> {
            resetFieldValue(textComponent, fieldName);

        });

        // Add keyboard shortcuts
        copyItem.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        cutItem.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        pasteItem.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));
        resetItem.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));

        // Add items to popup menu
        popupMenu.add(copyItem);
        popupMenu.add(cutItem);
        popupMenu.add(pasteItem);
        popupMenu.add(resetItem);

        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            private void showContextMenu(MouseEvent e) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        // Register the Ctrl+R key binding for the Reset action
        String resetKey = "reset-text";
        InputMap inputMap = textComponent.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textComponent.getActionMap();

        // Add the key binding
        inputMap.put(KeyStroke.getKeyStroke("ctrl R"), resetKey);
        actionMap.put(resetKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFieldValue(component, fieldName);
            }
        });
    }

    public void initComponents() {
        traverseFields(new MetadataEditPane.FieldSetGet() {

            @Override
            public void apply(Object field, FieldID anno) {

                if (field instanceof JTextComponent textComponent) {
                    textComponent.getDocument().addDocumentListener(new ChangeBackgroundDocumentListener(textComponent, anno.value()));
                    createContextMenu(textComponent, anno.value());
                }
                if (field instanceof JSpinner spinner) {
                    throw new RuntimeException("JSpinner NOT SUPPORTED!");
                }
                if (field instanceof JComboBox combo) {
                    combo.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JComboBox<?> cb = (JComboBox<?>) e.getSource();
                            String selectedValue = getComboBoxValue(cb, anno);
                            String initial = initialMetadata != null ? (String) initialMetadata.get(anno.value()) : null;
                            if ((selectedValue != null && selectedValue.equals(initial)) || (selectedValue == null && initial == null)) {
                                combo.setBorder(comboBoxDefault);
                            } else {
                                combo.setBorder(changedBorder);

                            }
                        }
                    });
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
                    createContextMenu(dtPicker, anno.value());
                }
            }
        }, null);


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
        panel1.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
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
        xmpPdfVersion.setEditable(false);
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
        final JScrollPane scrollPane12 = new JScrollPane();
        panel9.add(scrollPane12, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcDates = new JTextArea();
        xmpDcDates.setEditable(false);
        scrollPane12.setViewportView(xmpDcDates);
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
        final JScrollPane scrollPane13 = new JScrollPane();
        panel9.add(scrollPane13, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcLanguages = new JTextArea();
        scrollPane13.setViewportView(xmpDcLanguages);
        final JLabel label32 = new JLabel();
        label32.setText("Publishers");
        panel9.add(label32, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcPublishersEnabled = new JCheckBox();
        xmpDcPublishersEnabled.setText("");
        panel9.add(xmpDcPublishersEnabled, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane14 = new JScrollPane();
        panel9.add(scrollPane14, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcPublishers = new JTextArea();
        scrollPane14.setViewportView(xmpDcPublishers);
        final JLabel label33 = new JLabel();
        label33.setText("Relationships");
        panel9.add(label33, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcRelationshipsEnabled = new JCheckBox();
        xmpDcRelationshipsEnabled.setText("");
        panel9.add(xmpDcRelationshipsEnabled, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane15 = new JScrollPane();
        panel9.add(scrollPane15, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcRelationships = new JTextArea();
        scrollPane15.setViewportView(xmpDcRelationships);
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
        final JScrollPane scrollPane16 = new JScrollPane();
        panel9.add(scrollPane16, new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcSubjects = new JTextArea();
        scrollPane16.setViewportView(xmpDcSubjects);
        final JLabel label37 = new JLabel();
        label37.setText("Types");
        panel9.add(label37, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpDcTypesEnabled = new JCheckBox();
        xmpDcTypesEnabled.setText("");
        panel9.add(xmpDcTypesEnabled, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane17 = new JScrollPane();
        panel9.add(scrollPane17, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpDcTypes = new JTextArea();
        scrollPane17.setViewportView(xmpDcTypes);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("XMP Rights", panel10);
        final JScrollPane scrollPane18 = new JScrollPane();
        panel10.add(scrollPane18, new GridConstraints(0, 0, 7, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(7, 3, new Insets(5, 5, 5, 5), -1, -1));
        scrollPane18.setViewportView(panel11);
        final JLabel label38 = new JLabel();
        label38.setText("Certificate");
        panel11.add(label38, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel11.add(spacer5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        final JScrollPane scrollPane19 = new JScrollPane();
        panel11.add(scrollPane19, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpRightsOwner = new JTextArea();
        scrollPane19.setViewportView(xmpRightsOwner);
        final JLabel label41 = new JLabel();
        label41.setText("Copyright");
        panel11.add(label41, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsCopyrightEnabled = new JCheckBox();
        xmpRightsCopyrightEnabled.setText("");
        panel11.add(xmpRightsCopyrightEnabled, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane20 = new JScrollPane();
        panel11.add(scrollPane20, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpRightsCopyright = new JTextArea();
        scrollPane20.setViewportView(xmpRightsCopyright);
        final JLabel label42 = new JLabel();
        label42.setText("Usage Terms");
        panel11.add(label42, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsUsageTermsEnabled = new JCheckBox();
        xmpRightsUsageTermsEnabled.setText("");
        panel11.add(xmpRightsUsageTermsEnabled, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane21 = new JScrollPane();
        panel11.add(scrollPane21, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        xmpRightsUsageTerms = new JTextArea();
        scrollPane21.setViewportView(xmpRightsUsageTerms);
        final JLabel label43 = new JLabel();
        label43.setText("Web Statement");
        panel11.add(label43, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsWebStatementEnabled = new JCheckBox();
        xmpRightsWebStatementEnabled.setText("");
        panel11.add(xmpRightsWebStatementEnabled, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpRightsWebStatement = new JTextField();
        panel11.add(xmpRightsWebStatement, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        if (values.metadata != null && !values.metadata.isEmpty()) {
            MetadataInfo presetMetadata = new MetadataInfo();
            presetMetadata.fromFlatMap(values.metadata);
            fillFromMetadata(presetMetadata, true);
        }
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
        for (String k : initialMap.keySet()) {
            Object oi = initialMap.get(k);
            Object ci = currentMap.get(k);

            if ((oi == null && ci == null) || (oi != null && oi.equals(ci))) {
                currentMap.remove(k);
            }
        }
        if (!currentMap.isEmpty()) {
            values.metadata = currentMap;
        }

    }
}
