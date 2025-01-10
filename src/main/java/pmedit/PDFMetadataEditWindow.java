package pmedit;

import net.miginfocom.swing.MigLayout;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.xmpbox.xml.XmpParsingException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PDFMetadataEditWindow extends JFrame {

    final JFileChooser fc;
    private final MetadataInfo defaultMetadata;
    private File pdfFile;
    private String password;
    private MetadataInfo metadataInfo = new MetadataInfo();
    private JTextField filename;

    private PreferencesWindow preferencesWindow;
    private MetadataEditPane metadataEditor;
    private ActionsAndOptions actionsAndOptions;

    final ActionListener saveAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            saveFile(null);
            reloadFile();
        }
    };
    final ActionListener saveRenameAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            saveFile(null);
            String renameTemplate = Main.getPreferences().get("renameTemplate", null);
            if (renameTemplate == null) {
                return;
            }
            TemplateString ts = new TemplateString(renameTemplate);
            String toName = ts.process(metadataInfo);
            String toDir = pdfFile.getParent();
            File to = new File(toDir, toName);
            try {
                Files.move(pdfFile.toPath(), to.toPath());
                pdfFile = to;
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(PDFMetadataEditWindow.this,
                        "Error while renaming file:\n" + e1);
            }
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

            String dir = Main.getPreferences().get("LastDir", null);
            if (dir != null) {
                try {
                    fcSaveAs.setCurrentDirectory(new File(dir));
                } catch (Exception e1) {
                }
            }
            int returnVal = fcSaveAs.showSaveDialog(PDFMetadataEditWindow.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selected = fcSaveAs.getSelectedFile();
                if (!selected.getName().toLowerCase().endsWith(".pdf")) {
                    selected = new File(selected.getAbsolutePath() + ".pdf");
                }

                saveFile(selected);
                pdfFile = selected;
                reloadFile();

                // save dir as last opened
                Main.getPreferences().put("LastDir", pdfFile.getParent());
            }
        }
    };
    final Runnable updateSaveButton = new Runnable() {

        @Override
        public void run() {
            JButton btnSave = actionsAndOptions.btnSave;
            String saveActionS = Main.getPreferences().get("defaultSaveAction", "save");

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

    /**
     * Create the application.
     */
    public PDFMetadataEditWindow(String filePath) {
        fc = new JFileChooser();
        defaultMetadata = new MetadataInfo();
        initialize();
        PdfFilter pdfFilter = new PdfFilter();
        fc.addChoosableFileFilter(pdfFilter);
        fc.setFileFilter(pdfFilter);
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
            public void filesDropped(java.io.File[] files, Point where) {
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
    }

    public File getCurrentFile() {
        return pdfFile;
    }

    private void clear() {

        filename.setText("");
        metadataEditor.clear();
    }

    public void loadFile(String fileName) {
        loadFile(new File(fileName));
    }

    public void loadFile(File file) {
        if(!file.equals(pdfFile)){
            password = null;
        }
        pdfFile = file;
        clear();
        while(true) {
            try {
                PDDocument document = Loader.loadPDF(pdfFile, password != null ? password : "");

                reloadFileFromDocument(document);
                document.close();
                break;
            } catch (InvalidPasswordException e) {
                password = (String)JOptionPane.showInputDialog(
                        this,
                        "File is encrypted, provide user password:\n",
                        "Encrypted file",
                        JOptionPane.PLAIN_MESSAGE
                        );
                if(password == null){
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error while opening file:\n" + e);
                break;
            }
        };

    }

    protected void reloadFileFromDocument(PDDocument document) throws XmpParsingException, IOException {
        filename.setText(pdfFile.getAbsolutePath());
        metadataInfo = new MetadataInfo();
        metadataInfo.loadFromPDF(document);
        metadataInfo.copyUnsetExpanded(defaultMetadata, metadataInfo);

        metadataEditor.fillFromMetadata(metadataInfo);

        actionsAndOptions.setCurrentDocumentVersion(document.getVersion());

        metadataInfo.encryptionOptions.userPassword = password;
        actionsAndOptions.setDocumentProtection(metadataInfo.encryptionOptions);
    }

    public void reloadFile() {
        clear();
        try {
            PDDocument document = Loader.loadPDF(pdfFile, password != null ? password : "");

            reloadFileFromDocument(document);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while opening file:\n" + e);
        }
    }

    private void saveFile(File newFile) {
        if (pdfFile == null && filename.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a pdf file.");
            return;
        }

        try {
            metadataInfo.removeDocumentInfo = actionsAndOptions.removeDocumentCheckBox.isSelected();
            metadataInfo.removeXmp = actionsAndOptions.removeXMPCheckBox.isSelected();

            metadataInfo.encryptionOptions = actionsAndOptions.getDocumentProtection();

            if(actionsAndOptions.pdfVersion.getSelectedItem() instanceof Float s) {
                metadataInfo.saveAsVersion = s;
            } else {
                metadataInfo.saveAsVersion = 0;
            }

            metadataEditor.copyToMetadata(metadataInfo);
            metadataInfo.copyUnsetExpanded(defaultMetadata, metadataInfo);

            metadataInfo.saveAsPDF(pdfFile, newFile);

            metadataEditor.fillFromMetadata(metadataInfo);

            password = metadataInfo.encryptionOptions.userPassword;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while saving file:\n" + e);
        }
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Version.getAppName());
        setBounds(100, 100, 640, 480);
        setMinimumSize(new Dimension(640, 480));
        getContentPane()
                .setLayout(
                        new MigLayout("insets 5", "[grow,fill]", "[][grow,fill][grow]"));

        JPanel panel = new JPanel();
        getContentPane().add(panel, "cell 0 0,growx");
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{105, 421, 20, 40, 0};
        gbl_panel.rowHeights = new int[]{36, 0};
        gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        JButton btnOpenPdf = new JButton("Open PDF");
        GridBagConstraints gbc_btnOpenPdf = new GridBagConstraints();
        gbc_btnOpenPdf.anchor = GridBagConstraints.WEST;
        gbc_btnOpenPdf.insets = new Insets(0, 0, 0, 5);
        gbc_btnOpenPdf.gridx = 0;
        gbc_btnOpenPdf.gridy = 0;
        panel.add(btnOpenPdf, gbc_btnOpenPdf);

        btnOpenPdf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String dir = Main.getPreferences().get("LastDir", null);
                if (dir != null) {
                    try {
                        fc.setCurrentDirectory(new File(dir));
                    } catch (Exception e) {
                    }
                }
                int returnVal = fc.showOpenDialog(PDFMetadataEditWindow.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // This is where a real application would open the file.
                    loadFile(fc.getSelectedFile());
                    // save dir as last opened
                    Main.getPreferences().put("LastDir", pdfFile.getParent());
                }
            }
        });

        filename = new JTextField();
        GridBagConstraints gbc_filename = new GridBagConstraints();
        gbc_filename.fill = GridBagConstraints.HORIZONTAL;
        gbc_filename.insets = new Insets(0, 0, 0, 5);
        gbc_filename.gridx = 1;
        gbc_filename.gridy = 0;
        panel.add(filename, gbc_filename);
        filename.setEditable(false);
        filename.setColumns(10);

        Component horizontalStrut = Box.createHorizontalStrut(20);
        GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
        gbc_horizontalStrut.anchor = GridBagConstraints.WEST;
        gbc_horizontalStrut.insets = new Insets(0, 0, 0, 5);
        gbc_horizontalStrut.gridx = 2;
        gbc_horizontalStrut.gridy = 0;
        panel.add(horizontalStrut, gbc_horizontalStrut);

        java.net.URL prefImgURL = PDFMetadataEditWindow.class
                .getResource("settings-icon.png");
        ImageIcon img = new ImageIcon(prefImgURL);

        JButton btnPreferences = new JButton("");

        GridBagConstraints gbc_btnPreferences = new GridBagConstraints();
        gbc_btnPreferences.anchor = GridBagConstraints.WEST;
        gbc_btnPreferences.gridx = 3;
        gbc_btnPreferences.gridy = 0;
        panel.add(btnPreferences, gbc_btnPreferences);
        btnPreferences.setIcon(img);

        btnPreferences.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (preferencesWindow == null) {
                            preferencesWindow = new PreferencesWindow(Main.getPreferences(), defaultMetadata, PDFMetadataEditWindow.this);
                            preferencesWindow.onSaveAction(updateSaveButton);
                        }
                        preferencesWindow.setVisible(true);
                    }
                });

            }
        });

        metadataEditor = new MetadataEditPane();
        getContentPane().add(metadataEditor.tabbedaPane, "cell 0 1,grow");
        metadataEditor.showEnabled(false);


//		metadataEditor = createMetadataEditor();
//		getContentPane().add(metadataEditor,
//				"cell 0 1,growy");


        actionsAndOptions = new ActionsAndOptions();
        getContentPane().add(actionsAndOptions.topPanel, "cell 0 2,growx");
        actionsAndOptions.copyXMPToDocumentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (metadataInfo != null) {
                    metadataEditor.copyToMetadata(metadataInfo);
                    metadataInfo.copyXMPToDoc();
                    metadataEditor.fillFromMetadata(metadataInfo);
                }
            }
        });
        actionsAndOptions.copyDocumentToXMPButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (metadataInfo != null) {
                    metadataEditor.copyToMetadata(metadataInfo);
                    metadataInfo.copyDocToXMP();
                    metadataEditor.fillFromMetadata(metadataInfo);
                }
            }
        });


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


        updateSaveButton.run();

        java.net.URL imgURL = PDFMetadataEditWindow.class
                .getResource("pdf-metadata-edit.png");
        ImageIcon icoImg = new ImageIcon(imgURL);
        setIconImage(icoImg.getImage());
        setVisible(true);
    }

    public MetadataEditPane createMetadataEditor() {
        return new MetadataEditPane();
    }

}
