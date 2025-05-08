package pmedit.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import pmedit.EncryptionOptions;
import pmedit.preset.PresetStore;
import pmedit.preset.PresetValues;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.List;
import java.util.stream.Collectors;

public class PresetSelector {
    public static final String ON_LOAD_PRESET = "ON_LOAD_PRESET";
    public static final String ON_SAVE_PRESET = "ON_SAVE_PRESET";

    class PresetActionData {
        String action;
        PresetValues values;

        public PresetActionData(PresetValues values, String name) {
            action = name;
            this.values = values;
        }

        PresetValues getPresetValues() {
            return values;
        }

        boolean isOnLoad() {
            return ON_LOAD_PRESET.equals(action);
        }

        boolean isOnSave() {
            return ON_SAVE_PRESET.equals(action);
        }
    }

    private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public JPanel topPanel;
    public JComboBox<String> selectedPreset;
    public JButton savePresetButton;
    public JButton loadPresetButton;
    public JButton deletePresetButton;


    public PresetSelector() {
        loadPresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String currentPresetName = getCurrentPresetName();
                PresetValues values = PresetStore.loadPreset(currentPresetName);
                if (values == null) {
                    JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(topPanel),
                            "Preset doesn't exist :\n" + currentPresetName);
                    return;
                }

                fireActionPerformed(new ActionEvent(new PresetActionData(values, ON_LOAD_PRESET), ActionEvent.ACTION_PERFORMED, ON_LOAD_PRESET));
            }
        });

        savePresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PresetValues values = PresetStore.getPresetValuesInstance();

                fireActionPerformed(new ActionEvent(new PresetActionData(values, ON_SAVE_PRESET), ActionEvent.ACTION_PERFORMED, ON_SAVE_PRESET));

                PresetStore.savePreset(getCurrentPresetName(), values);
                updatePresets();
            }
        });

        deletePresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PresetStore.deletePreset(getCurrentPresetName());
                updatePresets();

            }
        });

        selectedPreset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                updatePresets(true);
            }
        });

        final JTextComponent tc = (JTextComponent) selectedPreset.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new DocumentListener() {
            public void update(Document document) {
                try {
                    String presetName = document.getText(0, document.getLength());
                    boolean en = PresetStore.presetExists(presetName);
                    deletePresetButton.setEnabled(en);
                    loadPresetButton.setEnabled(en);
                    savePresetButton.setEnabled(presetName != null && !presetName.isEmpty());
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e.getDocument());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e.getDocument());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        updatePresets();
    }

    // Method to add action listeners
    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    // Method to remove action listeners
    public void removeActionListeners() {
        actionListeners.clear();
    }

    // Helper method to fire the event to all listeners
    protected void fireActionPerformed(ActionEvent event) {
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed(event);
        }
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
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), 3, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 4, new Insets(3, 3, 3, 3), -1, -1));
        topPanel.add(panel1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Preset Values", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        selectedPreset = new JComboBox();
        selectedPreset.setEditable(true);
        panel1.add(selectedPreset, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        savePresetButton = new JButton();
        savePresetButton.setEnabled(false);
        savePresetButton.setText("Save");
        panel1.add(savePresetButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadPresetButton = new JButton();
        loadPresetButton.setEnabled(false);
        loadPresetButton.setText("Load");
        panel1.add(loadPresetButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deletePresetButton = new JButton();
        deletePresetButton.setEnabled(false);
        deletePresetButton.setText("Delete");
        panel1.add(deletePresetButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

}
