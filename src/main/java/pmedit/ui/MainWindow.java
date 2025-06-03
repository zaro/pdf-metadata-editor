package pmedit.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.xmpbox.xml.XmpParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.*;
import pmedit.ext.PmeExtension;
import pmedit.prefs.Preferences;
import pmedit.preset.PresetStore;
import pmedit.preset.PresetValues;
import pmedit.ui.components.TextPaneWithLinks;
import pmedit.ui.preferences.DefaultsPreferences;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

    protected static final Dimension MIN_SIZE = new Dimension(640, 530);
    private JPanel contentPane;
    public JButton btnOpenPdf;
    public JTextField filename;
    public JButton btnPreferences;
    public MetadataEditPane metadataEditor;
    public ActionsAndOptions actionsAndOptions;

    //
    private File pdfFile;
    private String password;
    private MetadataInfo metadataInfo = new MetadataInfo();

    private PreferencesWindow preferencesWindow;

    private PmeExtension extension = PmeExtension.get();


    final ActionListener saveAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            saveFile(null);
            reloadFile();
        }
    };
    final ActionListener saveRenameAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String renameTemplate = Preferences.getInstance().get("renameTemplate", null);
            if (renameTemplate == null) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "No Rename template configured!");
                return;
            }

            pdfFile = saveFile(null, renameTemplate);
            reloadFile();
        }
    };
    final ActionListener saveAsAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (pdfFile == null && filename.getText().isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "Please select a pdf file.");
                return;
            }

            final JFileChooser fcSaveAs = new JFileChooser();
            fcSaveAs.setDialogTitle("Save As");

            String dir = Preferences.getInstance().get("LastDir", null);
            if (dir != null) {
                try {
                    fcSaveAs.setCurrentDirectory(new File(dir));
                } catch (Exception e1) {
                }
            }
            int returnVal = fcSaveAs.showSaveDialog(MainWindow.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selected = fcSaveAs.getSelectedFile();
                if (!selected.getName().toLowerCase().endsWith(".pdf")) {
                    selected = new File(selected.getAbsolutePath() + ".pdf");
                }

                saveFile(selected);
                pdfFile = selected;
                reloadFile();

                // save dir as last opened
                Preferences.getInstance().put("LastDir", pdfFile.getParent());
            }
        }
    };
    final ActionListener openAction = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            final PdfFileChooser fc = new PdfFileChooser();

            int returnVal = fc.showOpenDialog(MainWindow.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // This is where a real application would open the file.
                loadFile(fc.getSelectedFile());
            }
        }
    };
    ;

    final Runnable updateSaveButton = new Runnable() {
        @Override
        public void run() {
            JButton btnSave = actionsAndOptions.btnSave;
            String saveActionS = Preferences.getInstance().get("defaultSaveAction", "save");

            for (ActionListener l : btnSave.getActionListeners()) {
                btnSave.removeActionListener(l);
            }

            if (saveActionS.equals("saveRename")) {
                btnSave.setText("Save & rename");
                btnSave.addActionListener(saveRenameAction);
            } else if (saveActionS.equals("saveAs")) {
                btnSave.setText("Save As ...");
                btnSave.addActionListener(saveAsAction);
            } else {
                btnSave.setText("Save");
                btnSave.addActionListener(saveAction);

            }
        }
    };

    final ActionListener onCopyXmpToDoc = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (metadataInfo != null) {
                metadataEditor.copyToMetadata(metadataInfo);
                metadataInfo.copyXMPToDoc();
                metadataEditor.fillFromMetadata(metadataInfo);
            }
        }
    };

    final ActionListener onCopyDocToXmp = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            if (metadataInfo != null) {
                metadataEditor.copyToMetadata(metadataInfo);
                metadataInfo.copyDocToXMP();
                metadataEditor.fillFromMetadata(metadataInfo);
            }
        }
    };

    final ActionListener clearDocument = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (metadataInfo != null) {

                metadataEditor.copyToMetadata(metadataInfo);
                metadataInfo.clearDoc();
                metadataEditor.fillFromMetadata(metadataInfo);
            }
        }
    };

    final ActionListener clearXmp = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (metadataInfo != null) {
                metadataEditor.copyToMetadata(metadataInfo);
                metadataInfo.clearXmp();
                metadataEditor.fillFromMetadata(metadataInfo);
            }
        }
    };

    public MainWindow() {
        this(null);
    }

    public MainWindow(String filePath) {
        setContentPane(contentPane);
        //
        initialize();
        clear();
        if (filePath != null) {
            try {
                loadFile(filePath);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error while opening file:\n" + e);
            }
        }

        new FileDrop(this, new FileDrop.Listener() {
            public void filesDropped(File[] files, Point where) {
                FileDropSelectMessage fdm = ((FileDropSelectMessage) getGlassPane());
                getGlassPane().setVisible(false);
                repaint();

                List<String> fileNames = new ArrayList<String>();

                for (File file : files) {
                    fileNames.add(file.getAbsolutePath());
                }
                Main.executeCommand(new CommandLine(fileNames, fdm.isBatchOperation()));
            }

            @Override
            public void dragEnter() {
                getGlassPane().setVisible(true);

            }

            @Override
            public void dragLeave() {
                getGlassPane().setVisible(false);
                repaint();
            }

            @Override
            public void dragOver(Point where) {
                ((FileDropSelectMessage) getGlassPane()).setDropPos(where);
                repaint();

            }
        });
        setGlassPane(new FileDropSelectMessage());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildMenu();
        setMinimumSize(MIN_SIZE);
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Version.getAppName());
        setBounds(100, 100, MIN_SIZE.width, MIN_SIZE.height);
//        setMinimumSize(MIN_SIZE);

        btnOpenPdf.addActionListener(openAction);

        btnPreferences.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPreferences(null);
            }
        });

        metadataEditor.showEnabled(false);

        actionsAndOptions.copyXMPToDocumentButton.addActionListener(onCopyXmpToDoc);
        actionsAndOptions.copyDocumentToXMPButton.addActionListener(onCopyDocToXmp);


        actionsAndOptions.btnSaveMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem save = menu.add("Save");
                save.addActionListener(saveAction);
                JMenuItem saveRename = menu.add("Save & rename");
                saveRename.addActionListener(saveRenameAction);
                JMenuItem saveAs = menu.add("Save As ...");
                saveAs.addActionListener(saveAsAction);

                int x, y;
                Point pos = actionsAndOptions.btnSaveMenu.getLocationOnScreen();
                x = pos.x;
                y = pos.y + actionsAndOptions.btnSaveMenu.getHeight();

                menu.show(actionsAndOptions.btnSaveMenu, actionsAndOptions.btnSaveMenu.getWidth() - (int) menu.getPreferredSize().getWidth(), actionsAndOptions.btnSaveMenu.getHeight());

            }
        });

        actionsAndOptions.loadPresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String currentPresetName = actionsAndOptions.getCurrentPresetName();
                PresetValues values = PresetStore.loadPreset(currentPresetName);
                if (values == null) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Preset doesn't exist :\n" + currentPresetName);
                    return;
                }
                metadataEditor.onLoadPreset(values);
            }
        });

        actionsAndOptions.savePresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PresetValues values = PresetStore.getPresetValuesInstance();
                metadataEditor.onSavePreset(values);
                PresetStore.savePreset(actionsAndOptions.getCurrentPresetName(), values);
                actionsAndOptions.updatePresets();
            }
        });

        actionsAndOptions.deletePresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PresetStore.deletePreset(actionsAndOptions.getCurrentPresetName());
                actionsAndOptions.updatePresets();

            }
        });

        actionsAndOptions.selectedPreset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionsAndOptions.updatePresets(true);
            }
        });

        actionsAndOptions.documentClearButton.addActionListener(clearDocument);

        actionsAndOptions.xmpClearButton.addActionListener(clearXmp);

        final JTextComponent tc = (JTextComponent) actionsAndOptions.selectedPreset.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new DocumentListener() {
            public void update(Document document) {
                try {
                    String presetName = document.getText(0, document.getLength());
                    boolean en = PresetStore.presetExists(presetName);
                    actionsAndOptions.deletePresetButton.setEnabled(en);
                    actionsAndOptions.loadPresetButton.setEnabled(en);
                    actionsAndOptions.savePresetButton.setEnabled(presetName != null && !presetName.isEmpty());
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
        actionsAndOptions.updatePresets();

        updateSaveButton.run();

        URL imgURL = MainWindow.class
                .getResource("pdf-metadata-edit.png");
        ImageIcon icoImg = new ImageIcon(imgURL);
        setIconImage(icoImg.getImage());
        setVisible(true);

        extension.init();
    }

    public File getCurrentFile() {
        return pdfFile;
    }

    protected MetadataInfo getDefaultMetadata() {
        return DefaultsPreferences.loadDefaultMetadata(Preferences.getInstance());
    }

    private void clear() {

        filename.setText("");
        metadataEditor.clear();
    }

    public void loadFile(String fileName) {
        loadFile(new File(fileName));
    }

    public void loadFile(File file) {
        if (!file.equals(pdfFile)) {
            password = null;
        }
        pdfFile = file;
        clear();
        while (true) {
            try {
                PDDocument document = Loader.loadPDF(pdfFile, password != null ? password : "");

                reloadFileFromDocument(document, pdfFile);
                document.close();
                break;
            } catch (InvalidPasswordException e) {
                password = (String) JOptionPane.showInputDialog(
                        this,
                        "File is encrypted, provide user password:\n",
                        "Encrypted file",
                        JOptionPane.PLAIN_MESSAGE
                );
                if (password == null) {
                    break;
                }
            } catch (Exception e) {
                LOG.error("Error while opening file", e);
                JOptionPane.showMessageDialog(this,
                        "Error while opening file:\n" + e);
                break;
            }
        }
        ;

    }

    protected void reloadFileFromDocument(PDDocument document, File pdfFile) throws XmpParsingException, IOException {
        filename.setText(pdfFile.getAbsolutePath());
        metadataInfo = new MetadataInfo();
        metadataInfo.loadFromPDF(document);

        if (pdfFile != null) {
            metadataInfo.loadPDFFileInfo(pdfFile);
        }

        metadataEditor.fillFromMetadata(metadataInfo);
        MetadataInfo defaultMetadata = new MetadataInfo();
        defaultMetadata.copyFromWithExpand(getDefaultMetadata(), metadataInfo);
        metadataEditor.fillFromMetadata(metadataInfo.defaultsToApply(defaultMetadata), true);


        extension.onDocumentReload(document, pdfFile, metadataEditor);

        actionsAndOptions.setCurrentDocumentVersion(document.getVersion());

        metadataInfo.encryptionOptions.userPassword = password;
        actionsAndOptions.setDocumentProtection(metadataInfo.encryptionOptions);
    }

    public void reloadFile() {
        clear();
        try {
            PDDocument document = Loader.loadPDF(pdfFile, password != null ? password : "");

            reloadFileFromDocument(document, pdfFile);

            document.close();
        } catch (Exception e) {
            LOG.error("Error while opening file", e);
            JOptionPane.showMessageDialog(this,
                    "Error while opening file:\n" + e);
        }
    }

    private File saveFile(File newFile) {
        return saveFile(newFile, null);
    }

    private File saveFile(File newFile, String renameTemplate) {
        if (pdfFile == null && filename.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a pdf file.");
            return null;
        }

        try {
            metadataInfo.removeDocumentInfo = actionsAndOptions.removeDocumentCheckBox.isSelected();
            metadataInfo.removeXmp = actionsAndOptions.removeXMPCheckBox.isSelected();

            metadataInfo.encryptionOptions = actionsAndOptions.getDocumentProtection();

            extension.beforeDocumentSave(metadataEditor);

            if (actionsAndOptions.pdfVersion.getSelectedItem() instanceof Float s) {
                metadataInfo.saveAsVersion = s;
            } else {
                metadataInfo.saveAsVersion = 0;
            }

            metadataEditor.copyToMetadata(metadataInfo);
            metadataInfo.expandVariables();

            if (renameTemplate != null) {
                TemplateString ts = new TemplateString(renameTemplate);
                String toName = ts.process(metadataInfo);
                String toDir = pdfFile.getParent();
                newFile = new File(toDir, toName);
            }

            metadataInfo.saveAsPDF(pdfFile, newFile);

            password = metadataInfo.encryptionOptions.userPassword;

            return newFile;

        } catch (Exception e) {
            LOG.error("Error while saving file", e);
            JOptionPane.showMessageDialog(this,
                    "Error while saving file:\n" + e);
            return null;
        }
    }

    protected void showPreferences(String tabName) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (preferencesWindow == null) {
                    preferencesWindow = new PreferencesWindow(MainWindow.this);
                    preferencesWindow.onSaveAction(updateSaveButton);
                }
                preferencesWindow.showTab(tabName);
                preferencesWindow.setVisible(true);
            }
        });
    }

    protected void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu inputMenu = new JMenu("File");

        JMenuItem openFile = new JMenuItem("Open File");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAsFile = new JMenuItem("Save as");
        JMenuItem saveAndRenameFile = new JMenuItem("Save & Rename");
        JMenuItem close = new JMenuItem("Close");
        JMenuItem quit = new JMenuItem("Quit");

        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveAsFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        saveAndRenameFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));


        int closeKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, closeKeyMask));
        close.addActionListener(e -> {
            this.dispose();
            Main.maybeExit();
        });

        int quitKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, quitKeyMask));
        quit.addActionListener(e -> System.exit(0));

        openFile.addActionListener(openAction);
        saveFile.addActionListener(saveAction);
        saveAsFile.addActionListener(saveAsAction);
        saveAndRenameFile.addActionListener(saveRenameAction);


        inputMenu.add(openFile);
        inputMenu.add(saveFile);
        inputMenu.add(saveAsFile);
        inputMenu.add(saveAndRenameFile);
        inputMenu.addSeparator();
        inputMenu.add(quit);

        JMenu documentMenu = new JMenu("Metadata");
        JMenu documentMd = new JMenu("Document");
        JMenuItem copyToXMP = new JMenuItem("Copy to XMP");
        JMenuItem clearDoc = new JMenuItem("Clear fields");
        JMenu xmpMd = new JMenu("XMP");
        JMenuItem copyToDoc = new JMenuItem("Copy to Document");
        JMenuItem clearXmp = new JMenuItem("Clear fields");
        documentMd.add(copyToXMP);
        documentMd.add(clearDoc);
        documentMenu.add(documentMd);
        xmpMd.add(copyToDoc);
        xmpMd.add(clearXmp);
        documentMenu.add(xmpMd);


        JMenu helpMenu = new JMenu("Help");
        JMenuItem viewHelp = new JMenuItem("View Help");
        JMenuItem website = new JMenuItem("Website");
        JMenuItem about = new JMenuItem("About");

        viewHelp.addActionListener(e -> {
            TextPaneWithLinks.openURL("https://pdf.metadata.care/help/");
        });

        website.addActionListener(e -> {
            TextPaneWithLinks.openURL("https://pdf.metadata.care/");
        });
        about.addActionListener(e -> {
            showPreferences("About");
        });


        helpMenu.add(viewHelp);
        helpMenu.add(website);
        helpMenu.add(about);


        menuBar.add(inputMenu);
        menuBar.add(documentMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnOpenPdf = new JButton();
        btnOpenPdf.setText("Open PDF");
        panel1.add(btnOpenPdf, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filename = new JTextField();
        filename.setColumns(10);
        filename.setEditable(false);
        panel1.add(filename, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        btnPreferences = new JButton();
        btnPreferences.setIcon(new ImageIcon(getClass().getResource("/pmedit/ui/settings-icon.png")));
        btnPreferences.setText("");
        panel1.add(btnPreferences, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        metadataEditor = new MetadataEditPane();
        panel1.add(metadataEditor.$$$getRootComponent$$$(), new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        actionsAndOptions = new ActionsAndOptions();
        panel1.add(actionsAndOptions.$$$getRootComponent$$$(), new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }


    public static void main(String[] args) {
        new MainWindow(null);
    }
}
