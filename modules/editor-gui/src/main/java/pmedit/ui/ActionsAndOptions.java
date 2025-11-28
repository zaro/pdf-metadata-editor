package pmedit.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import pmedit.EncryptionOptions;
import pmedit.ext.PmeExtension;
import pmedit.preset.PresetStore;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalComboBoxIcon;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;

public class ActionsAndOptions {
    public JPanel topPanel;
    public JButton copyXMPToDocumentButton;
    public JButton copyDocumentToXMPButton;
    public JCheckBox removeDocumentCheckBox;
    public JCheckBox removeXMPCheckBox;
    public JButton btnSave;
    public JButton btnSaveMenu;
    public JComboBox<Float> pdfVersion;
    public JButton encryptionButton;
    public JCheckBox enableEncryption;
    public JComboBox<String> selectedPreset;
    public JButton savePresetButton;
    public JButton loadPresetButton;
    public JButton deletePresetButton;
    public JButton documentClearButton;
    public JButton xmpClearButton;
    public JRadioButton copyDocToXmpRadioButton;
    public JRadioButton copyXmpToDocRadioButton;
    public EncryptionOptionsDialog encryptionOptionsDialog;
    public EncryptionOptions encryptionOptions;

    void createUIComponents() {
        btnSaveMenu = new JButton(new MetalComboBoxIcon());
        btnSaveMenu.setMargin(new Insets(0, 1, 1, 3));
        btnSaveMenu.setFocusable(false);
    }

    public ActionsAndOptions() {
        $$$setupUI$$$();
        btnSave.setIcon(new ImageIcon(
                MainWindow.class
                        .getResource("save-icon.png")));

        pdfVersion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Double) {
                    // Format the double value to round it to 2 decimal places
                    DecimalFormat df = new DecimalFormat("#.##");
                    setText(df.format((Double) value));
                }

                return renderer;
            }
        });

        encryptionOptionsDialog = new EncryptionOptionsDialog((JFrame) SwingUtilities.windowForComponent(topPanel));
        encryptionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                encryptionOptionsDialog.display(encryptionOptions);
            }
        });
        encryptionButton.setEnabled(encryptionOptions != null);
        enableEncryption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                encryptionOptions.hasEncryption = enableEncryption.isSelected();
                encryptionButton.setEnabled(encryptionOptions.hasEncryption);
            }
        });
    }

    static double[] versions = new double[]{1.3f, 1.4f, 1.5f, 1.6f, 1.7f};

    public void setDocumentVersionModel(ComboBoxModel<Float> model) {
        pdfVersion.setModel(model);
    }

    public void setDocumentProtection(EncryptionOptions options) {
        encryptionOptions = options;
        encryptionOptionsDialog.options = encryptionOptions;
        encryptionButton.setEnabled(encryptionOptions.hasEncryption);
        enableEncryption.setSelected(encryptionOptions.hasEncryption);
    }

    public EncryptionOptions getDocumentProtection() {
        return encryptionOptionsDialog.getCurrentOptions();
    }

    public String getCurrentPresetName() {
        return (String) selectedPreset.getModel().getSelectedItem();
    }

    public void updatePresets() {
        updatePresets(false);
    }

    public void updatePresets(boolean skipModel) {
        String current = getCurrentPresetName();
        if (!skipModel) {
            String[] presets = PresetStore.getPresetNames();
            Vector<String> modelData = new Vector<>(presets.length + 1);
            modelData.add("");
            modelData.addAll(1, Arrays.asList(presets));
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(modelData);
            if (model.getIndexOf(current) >= 0) {
                model.setSelectedItem(current);
            }
            selectedPreset.setModel(model);
        }
        boolean en = PresetStore.presetExists(current);
        deletePresetButton.setEnabled(en);
        loadPresetButton.setEnabled(en);
    }

    void setEnabled(boolean b) {
        documentClearButton.setEnabled(b);
        xmpClearButton.setEnabled(b);
        copyDocumentToXMPButton.setEnabled(b);
        copyXMPToDocumentButton.setEnabled(b);
        btnSave.setEnabled(b);
        btnSaveMenu.setEnabled(b);
        // Actions below allowed only in pro mode
        b = b && PmeExtension.get().hasBatch();
        encryptionButton.setEnabled(b);
        enableEncryption.setEnabled(b);
        pdfVersion.setEnabled(b);


    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(4, 5, new Insets(0, 0, 0, 0), 3, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel1, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("as PDF Version");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pdfVersion = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("1.4");
        defaultComboBoxModel1.addElement("1.5");
        defaultComboBoxModel1.addElement("1.6");
        defaultComboBoxModel1.addElement("1.7");
        pdfVersion.setModel(defaultComboBoxModel1);
        panel1.add(pdfVersion, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 0, -1));
        topPanel.add(panel2, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        btnSave = new JButton();
        btnSave.setText("Save");
        panel2.add(btnSave, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSaveMenu.setText("");
        panel2.add(btnSaveMenu, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel3, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        encryptionButton = new JButton();
        encryptionButton.setText("Encryption");
        panel3.add(encryptionButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enableEncryption = new JCheckBox();
        enableEncryption.setHideActionText(true);
        enableEncryption.setText("");
        panel3.add(enableEncryption, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 4, new Insets(3, 3, 3, 3), -1, -1));
        topPanel.add(panel4, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(null, "Preset Values", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        selectedPreset = new JComboBox();
        selectedPreset.setEditable(true);
        panel4.add(selectedPreset, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        savePresetButton = new JButton();
        savePresetButton.setEnabled(false);
        savePresetButton.setText("Save");
        panel4.add(savePresetButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadPresetButton = new JButton();
        loadPresetButton.setEnabled(false);
        loadPresetButton.setText("Load");
        panel4.add(loadPresetButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deletePresetButton = new JButton();
        deletePresetButton.setEnabled(false);
        deletePresetButton.setText("Delete");
        panel4.add(deletePresetButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel5, new GridConstraints(1, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Actions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        copyDocumentToXMPButton = new JButton();
        copyDocumentToXMPButton.setText("Document to XMP");
        panel5.add(copyDocumentToXMPButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyXMPToDocumentButton = new JButton();
        copyXMPToDocumentButton.setText(" Xmp to Document");
        panel5.add(copyXMPToDocumentButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        documentClearButton = new JButton();
        documentClearButton.setText("Clear Document");
        panel5.add(documentClearButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xmpClearButton = new JButton();
        xmpClearButton.setText("Clear Xmp");
        panel5.add(xmpClearButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel6, new GridConstraints(1, 1, 3, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(null, "On save", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        removeDocumentCheckBox = new JCheckBox();
        removeDocumentCheckBox.setText("Remove Document");
        panel6.add(removeDocumentCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeXMPCheckBox = new JCheckBox();
        removeXMPCheckBox.setText("Remove XMP");
        panel6.add(removeXMPCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyXmpToDocRadioButton = new JRadioButton();
        copyXmpToDocRadioButton.setText("Copy Xmp to Doc");
        panel6.add(copyXmpToDocRadioButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyDocToXmpRadioButton = new JRadioButton();
        copyDocToXmpRadioButton.setText("Copy Doc to Xmp");
        panel6.add(copyDocToXmpRadioButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

}
