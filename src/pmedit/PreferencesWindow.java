package pmedit;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import javax.swing.UIManager;
import javax.swing.JComboBox;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.DefaultComboBoxModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

public class PreferencesWindow extends JDialog {

	private JPanel contentPane;
	public MetadataEditPane defaultMetadataPane;
	
	public boolean copyBasicToXmp;
	public boolean copyXmpToBasic;
	public String  renameTemplate;
	public String  defaultSaveAction;
	MetadataInfo defaultMetadata;
	final Preferences prefs;
	
	Runnable onSave;
	
	protected boolean isWindows;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PreferencesWindow frame = new PreferencesWindow(Preferences.userRoot().node("PDFMetadataEditor"), null);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * @wbp.parser.constructor
	 */
	public PreferencesWindow(final Preferences prefs,MetadataInfo defaultMetadata) {
		this(prefs, defaultMetadata, null);
	}
	/**
	 * Create the frame.
	 */
	public PreferencesWindow(final Preferences prefs,MetadataInfo defaultMetadata, final Frame owner) {
		super(owner, true);
		isWindows = System.getProperty("os.name").startsWith("Windows");
		this.prefs = prefs;
		if(defaultMetadata != null){
			this.defaultMetadata =  defaultMetadata;
		} else {
			this.defaultMetadata = new MetadataInfo();
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				save();
				if(onSave != null){
					onSave.run();
				}
			}
		});
		setTitle("Preferences");
		setBounds(100, 100, 640, 480);
		setMinimumSize(new Dimension(640, 480));
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[grow][]", "[grow][][]"));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, "cell 0 0 2 1,grow");
		
		JPanel panelGeneral = new JPanel();
		tabbedPane.addTab("General", null, panelGeneral, null);
		panelGeneral.setLayout(new MigLayout("", "[grow]", "[][]"));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "On Save ...", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel_1.setLayout(new MigLayout("", "[]", "[][]"));
		
		onsaveCopyBasicTo = new JCheckBox("Copy Basic To XMP");
		onsaveCopyBasicTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (onsaveCopyBasicTo.isSelected()) {
					onsaveCopyXmpTo.setSelected(false);
				}
				copyBasicToXmp = onsaveCopyBasicTo.isSelected();
				copyXmpToBasic = onsaveCopyXmpTo.isSelected();
			}
		});
		panel_1.add(onsaveCopyBasicTo, "cell 0 0,alignx left,aligny top");
		onsaveCopyBasicTo.setSelected(false);
		
		onsaveCopyXmpTo = new JCheckBox("Copy XMP To Basic");
		onsaveCopyXmpTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (onsaveCopyXmpTo.isSelected()) {
					onsaveCopyBasicTo.setSelected(false);
				}
				copyBasicToXmp = onsaveCopyBasicTo.isSelected();
				copyXmpToBasic = onsaveCopyXmpTo.isSelected();
			}
		});
		panel_1.add(onsaveCopyXmpTo, "cell 0 1");
		onsaveCopyXmpTo.setSelected(false);
		panelGeneral.add(panel_1, "flowx,cell 0 0,alignx left,aligny top");

		onsaveCopyXmpTo.setSelected(prefs.getBoolean("onsaveCopyXmpTo", false));
		onsaveCopyBasicTo.setSelected(prefs.getBoolean("onsaveCopyBasicTo",
				false));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Rename template", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panelGeneral.add(panel, "cell 0 1,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[][][]"));
		Font font = new Font("Monospaced", Font.PLAIN, 14);
		
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
		txtpnAaa.setText("Supported fields:<br>\n<pre>\n<i>{basic.title}</i>      <i>{basic.producer}</i> \n<i>{basic.author}</i>     <i>{basic.trapped}</i> \n<i>{basic.subject}</i>    <i>{basic.creationDate}</i> \n<i>{basic.keywords}</i>   <i>{basic.modificationDate}</i> \n<i>{basic.creator}</i> \n\n<i>{xmpBasic.creatorTool}</i>   <i>{xmpBasic.identifiers}</i> \n<i>{xmpBasic.baseURL}</i>       <i>{xmpBasic.advisories}</i> \n<i>{xmpBasic.label}</i>         <i>{xmpBasic.modifyDate}</i> \n<i>{xmpBasic.nickname}</i>      <i>{xmpBasic.createDate}</i> \n<i>{xmpBasic.rating}</i>        <i>{xmpBasic.metadataDate}</i> \n<i>{xmpBasic.title}</i> \n\n<i>{xmpPdf.keywords}</i> \n<i>{xmpPdf.pdfVersion}</i> \n<i>{xmpPdf.producer}</i> \n\n<i>{xmpDc.title}</i>         <i>{xmpDc.creators}</i> \n<i>{xmpDc.coverage}</i>      <i>{xmpDc.contributors}</i> \n<i>{xmpDc.description}</i>   <i>{xmpDc.languages}</i> \n<i>{xmpDc.dates}</i>         <i>{xmpDc.publishers}</i> \n<i>{xmpDc.format}</i>        <i>{xmpDc.relationships}</i> \n<i>{xmpDc.identifier}</i>    <i>{xmpDc.subjects}</i> \n<i>{xmpDc.rights}</i>        <i>{xmpDc.types}</i> \n<i>{xmpDc.source}</i> \n</pre>");
		txtpnAaa.setFont(UIManager.getFont("TextPane.font"));
		txtpnAaa.setCaretPosition(0);
		
		comboBox = new JComboBox();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showPreview((String)getRenameTemplateCombo().getModel().getSelectedItem());
			}
		});
		comboBox.setEditable(true);
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"", "{basic.author} - {basic.title}.pdf", "{basic.author} - {basic.creationDate}.pdf"}));
		panel.add(comboBox, "cell 0 0,growx");
		
		JPanel saveActionPanel = new JPanel();
		saveActionPanel.setBorder(new TitledBorder(null, "Default save action", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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

		ActionListener onDefaultSaveAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnSave.isSelected()){
					defaultSaveAction = "save";
				} else  if(rdbtnSaveAndRename.isSelected()) {
					defaultSaveAction = "saveRename";
					
				} else  if(rdbtnSaveAs.isSelected()){
					defaultSaveAction = "saveAs";
				}
			}
		}; 
		rdbtnSave.addActionListener(onDefaultSaveAction);
		rdbtnSaveAndRename.addActionListener(onDefaultSaveAction);
		rdbtnSaveAs.addActionListener(onDefaultSaveAction);

		saveActionPanel.add(rdbtnSaveAndRename, "cell 0 0,alignx left,aligny top");
		
		saveActionPanel.add(rdbtnSaveAs, "cell 1 0,aligny top");
		final JTextComponent tcA = (JTextComponent) comboBox.getEditor().getEditorComponent();
		tcA.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				showPreview((String)comboBox.getEditor().getItem());
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				showPreview((String)comboBox.getEditor().getItem());
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				showPreview((String)comboBox.getEditor().getItem());
			} 
		});
		String defaultSaveAction = prefs.get("defaultSaveAction", "save");
		if( defaultSaveAction.equals("saveRename")) {
			rdbtnSaveAndRename.setSelected(true);
		} else if( defaultSaveAction.equals("saveAs")) {
				rdbtnSaveAndRename.setSelected(true);
		} else {
			rdbtnSave.setSelected(true);
		}
		
		JPanel panelDefaults = new JPanel();
		tabbedPane.addTab("Defaults", null, panelDefaults, null);
		panelDefaults.setLayout(new MigLayout("", "[grow,fill]", "[]"));
		
		JTextArea lblDefineHereDefault = new JTextArea("Define here default values for the fields you would like prefilled if not set in the PDF document ");
		lblDefineHereDefault.setBackground(UIManager.getColor("Panel.background"));
		lblDefineHereDefault.setWrapStyleWord(true);
		lblDefineHereDefault.setEditable(false);
		lblDefineHereDefault.setLineWrap(true);
		panelDefaults.add(lblDefineHereDefault, "cell 0 0,growx");
		
		defaultMetadataPane = new MetadataEditPane();
		panelDefaults.add(defaultMetadataPane.tabbedaPane, "cell 0 1,grow");
		
		JPanel panelOsIntegration = new JPanel();
		tabbedPane.addTab("Os Integration", null, panelOsIntegration, null);
		panelOsIntegration.setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Explorer context menu (Windows only)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelOsIntegration.add(panel_2, "cell 0 0,grow");
		panel_2.setLayout(new MigLayout("", "[][]", "[growprio 50,grow][growprio 50,grow]"));
		
		JButton btnRegister = new JButton("Add to context menu");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					WindowsRegisterContextMenu.register();
				} catch (Exception e1) {
//					StringWriter sw = new StringWriter();
//					PrintWriter pw = new PrintWriter(sw);
//					e1.printStackTrace(pw);
//					JOptionPane.showMessageDialog(owner,
//							"Failed to register context menu:\n" + e1.toString() +"\n" +sw.toString());
					JOptionPane.showMessageDialog(owner,
					"Failed to register context menu:\n" + e1.toString());
					e1.printStackTrace();
				}

			}
		});
		panel_2.add(btnRegister, "cell 0 0,growx,aligny center");
		
		JButton btnUnregister = new JButton("Remove from context menu");
		btnUnregister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WindowsRegisterContextMenu.unregister();
			}
		});
		
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setIcon(new ImageIcon(PreferencesWindow.class.getResource("/pmedit/os_integration_hint.png")));
		panel_2.add(lblNewLabel_1, "cell 1 0 1 2");
		
		panel_2.add(btnUnregister, "cell 0 1,growx,aligny center");
		
		btnRegister.setEnabled(isWindows);
		btnUnregister.setEnabled(isWindows);

		JScrollPane scrollPane_1 = new JScrollPane();
		tabbedPane.addTab("About", null, scrollPane_1, null);
		
		JTextPane txtpnDf = new JTextPane();
		txtpnDf.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if( e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
					return;
				}
		        if( !java.awt.Desktop.isDesktopSupported() ) {
		        	return;
		        }   
		        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
		        if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
		        	return;
		        }
	        
	            try {
	                java.net.URI uri = e.getURL().toURI();
	                desktop.browse( uri );
	            }catch(Exception e1){
	            	
	            }
			}
		});
		txtpnDf.setContentType("text/html");
		txtpnDf.setEditable(false);
		txtpnDf.setText("<h1 align=center>PDF Metadata editor</h1>\n\n<p align=center><a href=\"http://broken-by.me/pdf-metadata-editor/\">http://broken-by.me/pdf-metadata-editor/</a></p>\n<br>\n<p>This progam is free for use but <a href=\"http://broken-by.me/pdf-metadata-editor/#donate\">donations</a> are always welcome:)</p>\n<br>\n<p>For support & questions write to <b>zarrro@gmail.com</b></p>");
		scrollPane_1.setViewportView(txtpnDf);
		
		JButton btnClose = new JButton("Close");
		btnClose.setSelectedIcon(new ImageIcon(PreferencesWindow.class.getResource("/pmedit/pdf-metadata-edit.png")));
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				save();
			}
		});
		contentPane.add(btnClose, "cell 1 1");
		
		load();
		refresh();
		
	}
	
	public void save(){
		prefs.putBoolean("onsaveCopyXmpTo", copyXmpToBasic);
		prefs.putBoolean("onsaveCopyBasicTo", copyBasicToXmp);
		if(renameTemplate != null && renameTemplate.length() > 0)
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
		copyXmpToBasic = prefs.getBoolean("onsaveCopyXmpTo"  , false);
		renameTemplate = prefs.get("renameTemplate", null);
		String defaultMetadataYAML = prefs.get("defaultMetadata", null);
		if(defaultMetadataYAML != null && defaultMetadataYAML.length() > 0) {
			defaultMetadata.fromYAML(defaultMetadataYAML);
		}
		defaultSaveAction = prefs.get("defaultSaveAction", "save");
	}
	
	public void refresh() {
		onsaveCopyBasicTo.setSelected(copyBasicToXmp);
		onsaveCopyXmpTo.setSelected(copyXmpToBasic);
		
		comboBox.setSelectedItem(renameTemplate);
		
		defaultMetadataPane.fillFromMetadata(defaultMetadata);
		showPreview(renameTemplate);
	}
	
	public void showPreview(String template){
		renameTemplate = template;
		TemplateString ts = new TemplateString(template);
		
		getPreviewLabel().setText("Preview:" + ts.process(MetadataInfo.getSampleMetadata()));
	}
	
	public void onSaveAction(Runnable newAction){
		onSave = newAction;
	}
	
	private String desc = "";
	private JLabel lblNewLabel;
	private JComboBox comboBox;
	private JCheckBox onsaveCopyBasicTo;
	private JCheckBox onsaveCopyXmpTo;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	protected JLabel getPreviewLabel() {
		return lblNewLabel;
	}
	protected JComboBox getRenameTemplateCombo() {
		return comboBox;
	}
}
