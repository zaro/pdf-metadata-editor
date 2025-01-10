package pmedit;

import net.miginfocom.swing.MigLayout;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;

public class PreferencesWindow extends JDialog {

    final Preferences prefs;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final JPanel contentPane;
    private final String desc = "";
    private final JLabel lblNewLabel;
    private final JComboBox comboBox;
    private final JCheckBox onsaveCopyDocumentTo;
    private final JCheckBox onsaveCopyXmpTo;
    private final String aboutMsg;
    private final JTextPane txtpnDf;
    private final JLabel updateStatusLabel;
    private final JTextField emailField;
    private final JTextField keyField;
    private final JLabel labelLicenseStatus;
    public MetadataEditPane defaultMetadataPane;
    public boolean copyBasicToXmp;
    public boolean copyXmpToBasic;
    public String renameTemplate;
    public String defaultSaveAction;
    protected boolean isWindows;
    MetadataInfo defaultMetadata;
    Runnable onSave;
    private JButton btnClose;

    /**
     * @wbp.parser.constructor
     */
    public PreferencesWindow(final Preferences prefs, MetadataInfo defaultMetadata) {
        this(prefs, defaultMetadata, null);
    }

    /**
     * Create the frame.
     */
    public PreferencesWindow(final Preferences prefs, MetadataInfo defaultMetadata, final Frame owner) {
        super(owner, true);
        setLocationRelativeTo(owner);
        long startTime = System.nanoTime();


        final Future<HttpResponse> status = checkForUpdates();
        isWindows = System.getProperty("os.name").startsWith("Windows");
        this.prefs = prefs;
        if (defaultMetadata != null) {
            this.defaultMetadata = defaultMetadata;
        } else {
            this.defaultMetadata = new MetadataInfo();
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                save();
                if (onSave != null) {
                    onSave.run();
                }
            }
        });
        setTitle("Preferences");
        setMinimumSize(new Dimension(640, 480));
        contentPane = new JPanel();
        setContentPane(contentPane);

        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{725, 0};
        gbl_contentPane.rowHeights = new int[]{389, 29, 0};
        gbl_contentPane.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
        gbc_tabbedPane.weighty = 1.0;
        gbc_tabbedPane.weightx = 1.0;
        gbc_tabbedPane.fill = GridBagConstraints.BOTH;
        gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
        gbc_tabbedPane.gridx = 0;
        gbc_tabbedPane.gridy = 0;
        contentPane.add(tabbedPane, gbc_tabbedPane);

        JPanel panelGeneral = new JPanel();
        tabbedPane.addTab("General", null, panelGeneral, null);
        panelGeneral.setLayout(new MigLayout("", "[grow]", "[][]"));

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "On Save ...",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
        panel_1.setLayout(new MigLayout("", "[]", "[][]"));

        onsaveCopyDocumentTo = new JCheckBox("Copy Document To XMP");
        onsaveCopyDocumentTo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (onsaveCopyDocumentTo.isSelected()) {
                    onsaveCopyXmpTo.setSelected(false);
                }
                copyBasicToXmp = onsaveCopyDocumentTo.isSelected();
                copyXmpToBasic = onsaveCopyXmpTo.isSelected();
            }
        });
        panel_1.add(onsaveCopyDocumentTo, "cell 0 0,alignx left,aligny top");
        onsaveCopyDocumentTo.setSelected(false);

        onsaveCopyXmpTo = new JCheckBox("Copy XMP To Document");
        onsaveCopyXmpTo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (onsaveCopyXmpTo.isSelected()) {
                    onsaveCopyDocumentTo.setSelected(false);
                }
                copyBasicToXmp = onsaveCopyDocumentTo.isSelected();
                copyXmpToBasic = onsaveCopyXmpTo.isSelected();
            }
        });
        panel_1.add(onsaveCopyXmpTo, "cell 0 1");
        onsaveCopyXmpTo.setSelected(false);
        panelGeneral.add(panel_1, "flowx,cell 0 0,alignx left,aligny top");

        onsaveCopyXmpTo.setSelected(prefs.getBoolean("onsaveCopyXmpTo", false));
        onsaveCopyDocumentTo.setSelected(prefs.getBoolean("onsaveCopyBasicTo", false));

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Rename template",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
        panelGeneral.add(panel, "cell 0 1,grow");
        panel.setLayout(new MigLayout("", "[grow]", "[][][]"));

        lblNewLabel = new JLabel("Preview:");
        panel.add(lblNewLabel, "cell 0 1");

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportBorder(null);
        panel.add(scrollPane, "cell 0 2,grow");

        JTextPane txtpnAaa = new JTextPane();
        txtpnAaa.setBackground(UIManager.getColor("Panel.background"));
        txtpnAaa.setEditable(false);
        scrollPane.setViewportView(txtpnAaa);
        txtpnAaa.setContentType("text/html");
        txtpnAaa.setText(
                "Supported fields:<br>\n<pre>\n<i>" + CommandLine.mdFieldsHelpMessage(60, "  {", "}", false) + "</i></pre>");
        txtpnAaa.setFont(UIManager.getFont("TextPane.font"));
        txtpnAaa.setCaretPosition(0);

        comboBox = new JComboBox();
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showPreview((String) getRenameTemplateCombo().getModel().getSelectedItem());
            }
        });
        comboBox.setEditable(true);
        comboBox.setModel(new DefaultComboBoxModel(new String[]{"", "{doc.author} - {doc.title}.pdf",
                "{doc.author} - {doc.creationDate}.pdf"}));
        panel.add(comboBox, "cell 0 0,growx");

        JPanel saveActionPanel = new JPanel();
        saveActionPanel.setBorder(
                new TitledBorder(null, "Default save action", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelGeneral.add(saveActionPanel, "cell 0 0");
        saveActionPanel.setLayout(new MigLayout("", "[][]", "[][]"));

        final JRadioButton rdbtnSave = new JRadioButton("Save");

        buttonGroup.add(rdbtnSave);
        saveActionPanel.add(rdbtnSave, "flowy,cell 0 0,alignx left,aligny top");

        final JRadioButton rdbtnSaveAndRename = new JRadioButton("Save & rename");
        rdbtnSaveAndRename.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        buttonGroup.add(rdbtnSaveAndRename);

        final JRadioButton rdbtnSaveAs = new JRadioButton("Save as ...");
        buttonGroup.add(rdbtnSaveAs);

        saveActionPanel.add(rdbtnSaveAndRename, "cell 0 0,alignx left,aligny top");

        saveActionPanel.add(rdbtnSaveAs, "cell 1 0,aligny top");
        final JTextComponent tcA = (JTextComponent) comboBox.getEditor().getEditorComponent();

        JPanel panelDefaults = new JPanel();
        tabbedPane.addTab("Defaults", null, panelDefaults, null);
        GridBagLayout gbl_panelDefaults = new GridBagLayout();
        gbl_panelDefaults.columnWidths = new int[]{555, 0};
        gbl_panelDefaults.rowHeights = new int[]{32, 100, 0};
        gbl_panelDefaults.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_panelDefaults.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panelDefaults.setLayout(gbl_panelDefaults);


        JLabel lblDefineHereDefault = new JLabel(
                "Define here default values for the fields you would like prefilled if not set in the PDF document ");
        GridBagConstraints gbc_lblDefineHereDefault = new GridBagConstraints();
        gbc_lblDefineHereDefault.insets = new Insets(5, 5, 0, 0);
        gbc_lblDefineHereDefault.weightx = 1.0;
        gbc_lblDefineHereDefault.anchor = GridBagConstraints.NORTH;
        gbc_lblDefineHereDefault.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblDefineHereDefault.gridx = 0;
        gbc_lblDefineHereDefault.gridy = 0;
        panelDefaults.add(lblDefineHereDefault, gbc_lblDefineHereDefault);

        GridBagConstraints gbc_lblDefineHereDefault1 = new GridBagConstraints();
        gbc_lblDefineHereDefault1.weightx = 1.0;
        gbc_lblDefineHereDefault1.weighty = 1.0;
        gbc_lblDefineHereDefault1.anchor = GridBagConstraints.NORTH;
        gbc_lblDefineHereDefault1.fill = GridBagConstraints.BOTH;
        gbc_lblDefineHereDefault1.gridx = 0;
        gbc_lblDefineHereDefault1.gridy = 1;
        defaultMetadataPane = new MetadataEditPane();

        panelDefaults.add(defaultMetadataPane.tabbedaPane, gbc_lblDefineHereDefault1);

        OptimizationPreferenesPane optimizerPrefs = new OptimizationPreferenesPane();
        tabbedPane.addTab("File Optimization", null, optimizerPrefs.topPanel, null);


        // if(false){
        // JPanel panelOsIntegration = new JPanel();
        // tabbedPane.addTab("Os Integration", null, panelOsIntegration, null);
        // panelOsIntegration.setLayout(new MigLayout("", "[grow]", "[grow]"));

        // JPanel panel_2 = new JPanel();
        // panel_2.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
        // 		"Explorer context menu (Windows only)", TitledBorder.LEADING, TitledBorder.TOP, null,
        // 		new Color(0, 0, 0)));
        // panelOsIntegration.add(panel_2, "cell 0 0,grow");
        // panel_2.setLayout(new MigLayout("", "[][]", "[growprio 50,grow][growprio 50,grow]"));

        // JButton btnRegister = new JButton("Add to context menu");
        // btnRegister.addActionListener(new ActionListener() {
        // 	public void actionPerformed(ActionEvent e) {
        // 		try {
        // 			WindowsRegisterContextMenu.register();
        // 		} catch (Exception e1) {
        // 			// StringWriter sw = new StringWriter();
        // 			// PrintWriter pw = new PrintWriter(sw);
        // 			// e1.printStackTrace(pw);
        // 			// JOptionPane.showMessageDialog(owner,
        // 			// "Failed to register context menu:\n" + e1.toString()
        // 			// +"\n" +sw.toString());
        // 			JOptionPane.showMessageDialog(owner, "Failed to register context menu:\n" + e1.toString());
        // 			e1.printStackTrace();
        // 		}

        // 	}
        // });
        // panel_2.add(btnRegister, "cell 0 0,growx,aligny center");

        // JButton btnUnregister = new JButton("Remove from context menu");
        // btnUnregister.addActionListener(new ActionListener() {
        // 	public void actionPerformed(ActionEvent e) {
        // 		WindowsRegisterContextMenu.unregister();
        // 	}
        // });

        // final JLabel lblNewLabel_1 = new JLabel("");
        // panel_2.add(lblNewLabel_1, "cell 1 0 1 2");

        // panel_2.add(btnUnregister, "cell 0 1,growx,aligny center");

        // btnRegister.setEnabled(isWindows);
        // btnUnregister.setEnabled(isWindows);
        // }

        JPanel panelBatchLicense = new JPanel();
        tabbedPane.addTab("License", null, panelBatchLicense, null);
        GridBagLayout gbl_panelBatchLicense = new GridBagLayout();
        gbl_panelBatchLicense.columnWidths = new int[]{0, 0, 0};
        gbl_panelBatchLicense.rowHeights = new int[]{0, 0, 0, 0, 0};
        gbl_panelBatchLicense.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panelBatchLicense.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelBatchLicense.setLayout(gbl_panelBatchLicense);

        JTextPane txtpnEnterLicenseInformation = new JTextPane();
        txtpnEnterLicenseInformation.setEditable(false);
        txtpnEnterLicenseInformation.setBackground(UIManager.getColor("Panel.background"));
        txtpnEnterLicenseInformation.setContentType("text/html");
        txtpnEnterLicenseInformation.setText("<h3 align='center'>Enter license information below to use batch operations.</h3><p align='center'>You can get license at <a href=\"" + Constants.batchLicenseUrl + "\">" + Constants.batchLicenseUrl + "</a></p>");
        GridBagConstraints gbc_txtpnEnterLicenseInformation = new GridBagConstraints();
        gbc_txtpnEnterLicenseInformation.gridwidth = 2;
        gbc_txtpnEnterLicenseInformation.insets = new Insets(15, 0, 5, 0);
        gbc_txtpnEnterLicenseInformation.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtpnEnterLicenseInformation.gridx = 0;
        gbc_txtpnEnterLicenseInformation.gridy = 0;
        panelBatchLicense.add(txtpnEnterLicenseInformation, gbc_txtpnEnterLicenseInformation);
        txtpnEnterLicenseInformation.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                    return;
                }
                if (!java.awt.Desktop.isDesktopSupported()) {
                    return;
                }
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    return;
                }

                try {
                    java.net.URI uri = e.getURL().toURI();
                    desktop.browse(uri);
                } catch (Exception e1) {

                }
            }
        });
        JLabel lblNewLabel_2 = new JLabel("Email");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.insets = new Insets(15, 15, 5, 5);
        gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 1;
        panelBatchLicense.add(lblNewLabel_2, gbc_lblNewLabel_2);

        emailField = new JTextField();
        GridBagConstraints gbc_emailField = new GridBagConstraints();
        gbc_emailField.insets = new Insets(15, 0, 5, 15);
        gbc_emailField.fill = GridBagConstraints.HORIZONTAL;
        gbc_emailField.gridx = 1;
        gbc_emailField.gridy = 1;
        panelBatchLicense.add(emailField, gbc_emailField);
        emailField.setColumns(10);
        emailField.setText(Main.getPreferences().get("email", ""));
        emailField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLicense();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLicense();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        JLabel lblLicenseKey = new JLabel("License key");
        GridBagConstraints gbc_lblLicenseKey = new GridBagConstraints();
        gbc_lblLicenseKey.anchor = GridBagConstraints.EAST;
        gbc_lblLicenseKey.insets = new Insets(0, 15, 5, 5);
        gbc_lblLicenseKey.gridx = 0;
        gbc_lblLicenseKey.gridy = 2;
        panelBatchLicense.add(lblLicenseKey, gbc_lblLicenseKey);

        keyField = new JTextField();
        GridBagConstraints gbc_keyField = new GridBagConstraints();
        gbc_keyField.insets = new Insets(0, 0, 5, 15);
        gbc_keyField.fill = GridBagConstraints.HORIZONTAL;
        gbc_keyField.gridx = 1;
        gbc_keyField.gridy = 2;
        panelBatchLicense.add(keyField, gbc_keyField);
        keyField.setColumns(10);
        keyField.setText(Main.getPreferences().get("key", ""));
        keyField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLicense();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLicense();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        labelLicenseStatus = new JLabel("No License");
        GridBagConstraints gbc_labelLicenseStatus = new GridBagConstraints();
        gbc_labelLicenseStatus.gridwidth = 2;
        gbc_labelLicenseStatus.insets = new Insets(30, 15, 0, 15);
        gbc_labelLicenseStatus.gridx = 0;
        gbc_labelLicenseStatus.gridy = 3;
        panelBatchLicense.add(labelLicenseStatus, gbc_labelLicenseStatus);

        JScrollPane scrollPane_1 = new JScrollPane();
        tabbedPane.addTab("About", null, scrollPane_1, null);

        txtpnDf = new JTextPane();
        txtpnDf.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                    return;
                }
                if (!java.awt.Desktop.isDesktopSupported()) {
                    return;
                }
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    return;
                }

                try {
                    java.net.URI uri = e.getURL().toURI();
                    desktop.browse(uri);
                } catch (Exception e1) {

                }
            }
        });
        txtpnDf.setContentType("text/html");
        txtpnDf.setEditable(false);
        txtpnDf.setText(
                aboutMsg = "<h1 align=center>" + Version.getAppName() + "</h1>\n\n<p align=center><a href=\"https://pdf.metadata.care/\">https://pdf.metadata.care/</a></p>\n<br>\n<p align=center>If you have suggestions, found bugs or just want to share some idea about it you can write me at : <a href=\"https://pdf.metadata.care/contact/\">https://pdf.metadata.care/contact/</a></p>\n<br>");
        scrollPane_1.setViewportView(txtpnDf);

        JPanel panel_3 = new JPanel();
        GridBagConstraints gbc_panel_3 = new GridBagConstraints();
        gbc_panel_3.insets = new Insets(0, 5, 0, 5);
        gbc_panel_3.fill = GridBagConstraints.BOTH;
        gbc_panel_3.gridx = 0;
        gbc_panel_3.gridy = 1;
        contentPane.add(panel_3, gbc_panel_3);
        panel_3.setLayout(new BorderLayout(0, 0));

        btnClose = new JButton("Close");
        panel_3.add(btnClose, BorderLayout.EAST);

        updateStatusLabel = new JLabel("...");
        panel_3.add(updateStatusLabel, BorderLayout.WEST);
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                save();
            }
        });

        ActionListener onDefaultSaveAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (rdbtnSave.isSelected()) {
                    defaultSaveAction = "save";
                } else if (rdbtnSaveAndRename.isSelected()) {
                    defaultSaveAction = "saveRename";

                } else if (rdbtnSaveAs.isSelected()) {
                    defaultSaveAction = "saveAs";
                }
            }
        };
        rdbtnSave.addActionListener(onDefaultSaveAction);
        rdbtnSaveAndRename.addActionListener(onDefaultSaveAction);
        rdbtnSaveAs.addActionListener(onDefaultSaveAction);
        tcA.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent arg0) {
                showPreview((String) comboBox.getEditor().getItem());
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                showPreview((String) comboBox.getEditor().getItem());
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                showPreview((String) comboBox.getEditor().getItem());
            }
        });
        String defaultSaveAction = prefs.get("defaultSaveAction", "save");
        if (defaultSaveAction.equals("saveRename")) {
            rdbtnSaveAndRename.setSelected(true);
        } else if (defaultSaveAction.equals("saveAs")) {
            rdbtnSaveAndRename.setSelected(true);
        } else {
            rdbtnSave.setSelected(true);
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // lblNewLabel_1
                // 		.setIcon(new ImageIcon(PreferencesWindow.class.getResource("/pmedit/os_integration_hint.png")));

            }
        });

        load();
        refresh();
        contentPane.doLayout();

        if (status.isDone()) {
            showUpdatesStatus(status);
        } else {
            (new Thread(new Runnable() {

                @Override
                public void run() {
                    showUpdatesStatus(status);
                }
            })).start();
        }
        updateLicense();
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    PreferencesWindow frame = new PreferencesWindow(Preferences.userRoot().node("PDFMetadataEditor"),
                            null);
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Future<HttpResponse> checkForUpdates() {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
        HttpGet request = new HttpGet("https://api.github.com/repos/zaro/pdf-metadata-editor/releases?per_page=1");
        Future<HttpResponse> future = httpclient.execute(request, null);
        return future;
    }

    private void showUpdatesStatus(Future<HttpResponse> status) {
        Version.VersionTuple current = Version.get();
        String currentVersionMsg = "<h3 align=center>Version: " + current.getAsString() + "</h3>";
        String versionMsg = "<h3 align=center>Cannot get update information </h3>";
        try {
            HttpResponse response = status.get();
            updateStatusLabel.setText("");
            String lastsVersion = null;
            HttpEntity entity = response.getEntity();

            java.util.List<HashMap<String, Object>> body =  SerDeslUtils.listFromJSON(EntityUtils.toString(entity));
            if (!body.isEmpty()) {
                lastsVersion = (String) (body.get(0)).get("name");
            }
            if (lastsVersion != null) {
                Version.VersionTuple latest = new Version.VersionTuple(lastsVersion);
                if (current.cmp(latest) < 0) {
                    versionMsg = "<h3 align=center>New version available: <a href='https://pdf.metadata.care/download/'>"
                            + latest.getAsString() + "</a> , current: " + current.getAsString() + "</h3>";
                    updateStatusLabel.setText("Newer version available:" + latest.getAsString());
                } else {
                    versionMsg = "<h3 align=center>Version " + current.getAsString() + " is the latest version</h3>";
                }
            }
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            versionMsg += "<h4 align=center>Error: " + e1.getCause().getLocalizedMessage() + "</h4>";
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            txtpnDf.setText(aboutMsg + currentVersionMsg + versionMsg);
        }
    }

    public void save() {
        prefs.putBoolean("onsaveCopyXmpTo", copyXmpToBasic);
        prefs.putBoolean("onsaveCopyBasicTo", copyBasicToXmp);
        if (renameTemplate != null && renameTemplate.length() > 0)
            prefs.put("renameTemplate", renameTemplate);
        else
            prefs.remove("renameTemplate");
        defaultMetadataPane.copyToMetadata(defaultMetadata);
        prefs.put("defaultMetadata", defaultMetadata.toYAML());

        prefs.put("defaultSaveAction", defaultSaveAction);
        if (onSave != null)
            onSave.run();
    }

    public void load() {
        copyBasicToXmp = prefs.getBoolean("onsaveCopyBasicTo", false);
        copyXmpToBasic = prefs.getBoolean("onsaveCopyXmpTo", false);
        renameTemplate = prefs.get("renameTemplate", null);
        String defaultMetadataYAML = prefs.get("defaultMetadata", null);
        if (defaultMetadataYAML != null && defaultMetadataYAML.length() > 0) {
            defaultMetadata.fromYAML(defaultMetadataYAML);
        }
        defaultSaveAction = prefs.get("defaultSaveAction", "save");
    }

    public void refresh() {
        onsaveCopyDocumentTo.setSelected(copyBasicToXmp);
        onsaveCopyXmpTo.setSelected(copyXmpToBasic);

        comboBox.setSelectedItem(renameTemplate);

        defaultMetadataPane.fillFromMetadata(defaultMetadata);
        showPreview(renameTemplate);
    }

    public void showPreview(String template) {
        renameTemplate = template;
        TemplateString ts = new TemplateString(template);

        getPreviewLabel().setText("Preview:" + ts.process(MetadataInfo.getSampleMetadata()));
    }

    public void onSaveAction(Runnable newAction) {
        onSave = newAction;
    }

    protected void updateLicense() {
        String key = keyField.getText();
        String email = emailField.getText();
        if (key.isEmpty() && email.isEmpty()) {
            labelLicenseStatus.setText("No license");
        } else if (BatchMan.maybeHasBatch(key, email)) {
            Main.getPreferences().put("key", keyField.getText());
            Main.getPreferences().put("email", emailField.getText());
            labelLicenseStatus.setText("Valid license");
        } else {
            labelLicenseStatus.setText("Invalid license");
        }

    }

    protected JLabel getPreviewLabel() {
        return lblNewLabel;
    }

    protected JComboBox getRenameTemplateCombo() {
        return comboBox;
    }

    protected JLabel getUpdateStatusLabel() {
        return updateStatusLabel;
    }
	protected JButton getBtnClose() {
		return btnClose;
	}
}
