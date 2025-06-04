package pmedit.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import pmedit.EncryptionOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EncryptionOptionsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    public JCheckBox canPrintCheckBox;
    public JCheckBox canModifyCheckBox;
    public JCheckBox canExtractContentCheckBox;
    public JCheckBox canModifyAnnotationsCheckBox;
    public JCheckBox canFillFormFieldsCheckBox;
    public JCheckBox canExtractForAccessibilityCheckBox;
    public JCheckBox canAssembleDocumentCheckBox;
    public JCheckBox canPrintFaithfullCheckBox;
    public JTextField ownerPasswordTextField;
    public JTextField usePasswordTextField;
    public EncryptionOptions options;

    public EncryptionOptionsDialog(Frame owner) {
        super(owner, "Encryption Options", ModalityType.MODELESS);
        super.setLocationRelativeTo(owner);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (options != null && options.permission != null) {
                    AccessPermission permissions = options.permission;
                    permissions.setCanPrint(canPrintCheckBox.isSelected());
                    permissions.setCanModify(canModifyCheckBox.isSelected());
                    permissions.setCanExtractContent(canExtractContentCheckBox.isSelected());
                    permissions.setCanExtractContent(canModifyAnnotationsCheckBox.isSelected());
                    permissions.setCanFillInForm(canFillFormFieldsCheckBox.isSelected());
                    permissions.setCanExtractForAccessibility(canExtractForAccessibilityCheckBox.isSelected());
                    permissions.setCanAssembleDocument(canAssembleDocumentCheckBox.isSelected());
                    permissions.setCanPrintFaithful(canPrintFaithfullCheckBox.isSelected());
                }

            }
        };
        canPrintCheckBox.addActionListener(listener);
        canModifyCheckBox.addActionListener(listener);
        canExtractContentCheckBox.addActionListener(listener);
        canModifyAnnotationsCheckBox.addActionListener(listener);
        canFillFormFieldsCheckBox.addActionListener(listener);
        canExtractForAccessibilityCheckBox.addActionListener(listener);
        canAssembleDocumentCheckBox.addActionListener(listener);
        canPrintFaithfullCheckBox.addActionListener(listener);
        pack();
    }

    public void display(EncryptionOptions fileOptions) {
        options = fileOptions;

        fill();
        setVisible(true);
    }

    protected void fill() {

        if (options != null) {
            AccessPermission permissions = options.permission;

            canPrintCheckBox.setSelected(permissions.canPrint());
            canModifyCheckBox.setSelected(permissions.canModify());
            canExtractContentCheckBox.setSelected(permissions.canExtractContent());
            canModifyAnnotationsCheckBox.setSelected(permissions.canExtractContent());
            canFillFormFieldsCheckBox.setSelected(permissions.canFillInForm());
            canExtractForAccessibilityCheckBox.setSelected(permissions.canExtractForAccessibility());
            canAssembleDocumentCheckBox.setSelected(permissions.canAssembleDocument());
            canPrintFaithfullCheckBox.setSelected(permissions.canPrintFaithful());
            if (options.ownerPassword != null) {
                ownerPasswordTextField.setText(options.ownerPassword);
            } else {
                ownerPasswordTextField.setText("");
            }
            if (options.userPassword != null) {
                usePasswordTextField.setText(options.userPassword);
            } else {
                usePasswordTextField.setText("");
            }
        }
    }

    public EncryptionOptions getCurrentOptions() {
        options.ownerPassword = ownerPasswordTextField.getText();
        options.userPassword = usePasswordTextField.getText();
        return options;
    }


    private void onOK() {
        getCurrentOptions();
        dispose();
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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        canPrintCheckBox = new JCheckBox();
        canPrintCheckBox.setText("Can Print");
        panel3.add(canPrintCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        canModifyCheckBox = new JCheckBox();
        canModifyCheckBox.setText("Can Modify");
        panel3.add(canModifyCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        canExtractContentCheckBox = new JCheckBox();
        canExtractContentCheckBox.setText("Can Extract Content");
        panel3.add(canExtractContentCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        canModifyAnnotationsCheckBox = new JCheckBox();
        canModifyAnnotationsCheckBox.setText("Can Modify Annotations");
        panel3.add(canModifyAnnotationsCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        canFillFormFieldsCheckBox = new JCheckBox();
        canFillFormFieldsCheckBox.setText("Can Fill Form Fields");
        panel3.add(canFillFormFieldsCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        canExtractForAccessibilityCheckBox = new JCheckBox();
        canExtractForAccessibilityCheckBox.setText("Can Extract for Accessibility");
        panel3.add(canExtractForAccessibilityCheckBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        canAssembleDocumentCheckBox = new JCheckBox();
        canAssembleDocumentCheckBox.setText("Can Assemble Document");
        panel3.add(canAssembleDocumentCheckBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        canPrintFaithfullCheckBox = new JCheckBox();
        canPrintFaithfullCheckBox.setText("Can Print Faithful");
        panel3.add(canPrintFaithfullCheckBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Owner Password");
        panel3.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ownerPasswordTextField = new JTextField();
        panel3.add(ownerPasswordTextField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("User Password");
        panel3.add(label2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usePasswordTextField = new JTextField();
        panel3.add(usePasswordTextField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
