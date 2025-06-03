package pmedit.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.*;
import pmedit.prefs.Preferences;
import pmedit.serdes.SerDeslUtils;
import pmedit.ui.components.TextPaneWithLinks;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BatchOperationWindow extends JFrame {
    Logger LOG = LoggerFactory.getLogger(BatchOperationWindow.class);
    public JPanel contentPane;
    public JComboBox<CommandDescription> selectedBatchOperation;
    public JButton btnParameters;
    public JTextPane fileList;
    public JTable statusTable;
    public JScrollPane statusScrollPane;
    public TextPaneWithLinks statusSummary;
    public JButton btnCancel;
    public JButton btnAction;
    public JTextField outputDirField;
    public JButton selectOutputDir;
    public JButton addFolderButton;
    public JButton addFileButton;
    public JButton clearFileList;
    public JCheckBox persistFileList;

    //
    final static String LAST_USED_COMMAND_KEY = "lastUsedBatchCommand";
    final static String BATCH_FILES_LIST_KEY = "BatchFileList";
    final static String BATCH_OUTPUT_DIR_KEY = "BatchOutputDir";
    final static String PERSIST_BATCH_FILES_KEY = "PersistBatchFileList";

    List<File> batchFileList = new ArrayList<File>();
    private FileOpResultTableModel tableModel;
    private final ActionListener closeWindowActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            dispatchEvent(new WindowEvent(BatchOperationWindow.this, WindowEvent.WINDOW_CLOSING));
        }
    };
    private BatchParametersWindow parametersWindow;
    private final Map<String, BatchOperationParameters> batchParameters = new HashMap<String, BatchOperationParameters>();

    public BatchOperationWindow(CommandDescription command) {
        setTitle("Batch PDF metadata edit");
        setBounds(100, 100, 640, 480);
        setMinimumSize(new Dimension(640, 480));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(contentPane);
        tableModel = new FileOpResultTableModel();
        statusTable.setModel(tableModel);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        btnParameters.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createBatchParametersWindow();
            }
        });

        selectedBatchOperation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object selected = selectedBatchOperation.getSelectedItem();
                if (selected instanceof CommandDescription cd) {
                    if (cd.isGroup()) {
                        selectedBatchOperation.setSelectedIndex(-1); // Deselect if separator was clicked
                    }
                    enableBatchParametersWindowButton();
                }
            }
        });

        btnCancel.addActionListener(closeWindowActionListener);

        selectedBatchOperation.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof CommandDescription cd) {
                    if (cd.groupName != null) {
                        // Make separator item look different
                        label.setText("--- " + cd.groupName); // Clear the text
                        label.setEnabled(false); // Make it non-selectable
                    }
                }
                return label;
            }
        });
        selectedBatchOperation.setMaximumRowCount(20);
        if (command != null) {
            selectedBatchOperation.setModel(new DefaultComboBoxModel<CommandDescription>(new CommandDescription[]{command}));
        } else {
            selectedBatchOperation.setModel(new DefaultComboBoxModel<CommandDescription>(CommandDescription.batchCommandsGuiMenu));
            String lastUsedCommand = Preferences.getInstance().get(LAST_USED_COMMAND_KEY, null);
            if (lastUsedCommand != null) {
                CommandDescription lastCommand = CommandDescription.getBatchCommand(lastUsedCommand);
                if (lastCommand != null) {
                    selectedBatchOperation.setSelectedItem(lastCommand);
                }
            }
        }

        enableBatchParametersWindowButton();

        // Add menu
        buildMenu();

        selectOutputDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectOutputDirAction();
            }
        });

        addFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFileAction();
            }
        });
        addFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDirAction();
            }
        });
        clearFileList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearInputFiles();
            }
        });
        persistFileList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences.getInstance().putBoolean(PERSIST_BATCH_FILES_KEY, persistFileList.isSelected());
            }
        });
        persistFileList.setSelected(Preferences.getInstance().getBoolean(PERSIST_BATCH_FILES_KEY, false));

        new FileDrop(this, new FileDrop.Listener() {
            public void filesDropped(File[] files, Point where) {
                getGlassPane().setVisible(false);
                repaint();
                appendFiles(Arrays.asList(files));
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
            }
        });
        setGlassPane(new FileDropMessage());
        reset(true);

        URL imgURL = MainWindow.class
                .getResource("pdf-metadata-edit.png");
        ImageIcon icoImg = new ImageIcon(imgURL);
        setIconImage(icoImg.getImage());

        tableModel.addTableModelListener(e -> {
            if (e.getType() != TableModelEvent.DELETE) {
                SwingUtilities.invokeLater(() -> {
                    adjustRowHeights(e.getFirstRow(), e.getLastRow(), statusScrollPane.getViewport().getWidth());
                });
            }
        });
    }

    protected void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu inputMenu = new JMenu("Input");

        JMenuItem addFile = new JMenuItem("Add File");
        JMenuItem addDir = new JMenuItem("Add Dir");

        addFile.addActionListener(e -> {
            addFileAction();
        });

        addDir.addActionListener(e -> {
            addDirAction();
        });

        addFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        addDir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
        inputMenu.add(addFile);
        inputMenu.add(addDir);

        JMenu outputMenu = new JMenu("Output");
        JMenuItem selectOutDir = new JMenuItem("Select output dir");
        selectOutDir.addActionListener(e -> {
            selectOutputDirAction();
        });
        selectOutDir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));

        outputMenu.add(selectOutDir);

        menuBar.add(inputMenu);
        menuBar.add(outputMenu);
        this.setJMenuBar(menuBar);
    }

    protected void selectOutputDirAction() {
        DirChooser fc = new DirChooser("Output");
        fc.setDialogTitle("Select Output Folder");
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            outputDirField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    protected void addFileAction() {
        final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());
        if (command == null || command.isGroup()) {
            return;
        }
        FileChooser fc = new FileChooser(command.inputFileExtensions);
        fc.setDialogTitle("Select File to Add");
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            appendFiles(Collections.singletonList(fc.getSelectedFile()));
        }
    }

    protected void addDirAction() {
        DirChooser fc = new DirChooser();
        fc.setDialogTitle("Select Folder to Add");
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            appendFiles(Collections.singletonList(fc.getSelectedFile()));
        }
    }

    protected void clearInputFiles() {
        batchFileList.clear();
        Document doc = fileList.getDocument();
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
        }
    }

    protected void initInputFiles() {
        batchFileList.clear();
        if (persistFileList.isSelected()) {
            String json = Preferences.getInstance().get(BATCH_FILES_LIST_KEY, "");
            if (!json.isEmpty()) {
                for (String f : SerDeslUtils.stringListFromJSON(json)) {
                    batchFileList.add(new File(f));
                }
            }
            outputDirField.setText(Preferences.getInstance().get(BATCH_OUTPUT_DIR_KEY, ""));
        }
        Document doc = fileList.getDocument();
        try {
            doc.remove(0, doc.getLength());
            for (File file : batchFileList) {
                doc.insertString(doc.getLength(), file.getAbsolutePath() + "\n", null);
            }
        } catch (BadLocationException e) {
        }
    }

    protected void persistInputFiles() {
        if (persistFileList.isSelected()) {
            List<String> files = batchFileList.stream().map(File::getAbsolutePath).toList();
            Preferences.getInstance().put(BATCH_FILES_LIST_KEY, SerDeslUtils.toJSON(false, files));
            Preferences.getInstance().put(BATCH_OUTPUT_DIR_KEY, outputDirField.getText());
        }
    }

    protected void reset(boolean fullReset) {
        if (!fullReset) {
            boolean onlyOutput = hasErrors() || persistFileList.isSelected();
            if (!onlyOutput) {
                clearInputFiles();
            }
        } else {
            initInputFiles();
        }


        tableModel.clearResults();

        clearActionListeners(btnAction);
        btnAction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runBatch();
            }
        });
        btnAction.setText("Begin");
        if (!BatchMan.hasBatch()) {
            btnAction.setEnabled(false);
            statusSummary.setVisible(true);
            statusSummary.setText("<p align=center>No batch license. In order to use batch operations please get a license from <a href='" + Constants.batchLicenseUrl + "'>" + Constants.batchLicenseUrl + "<a></p>");
        } else {
            btnAction.setEnabled(true);
            statusSummary.setVisible(false);
            statusSummary.setText("");
        }

        // important that it happens here, so that the renderer cache is reset!
        statusTable.getColumnModel().getColumn(1).setCellRenderer(new MessageCellRenderer());

        SwingUtilities.invokeLater(() -> {
            int stWidth = statusScrollPane.getViewport().getWidth();

            final int columnCount = statusTable.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = statusTable.getColumnModel().getColumn(i);
                column.setPreferredWidth((int) (stWidth / columnCount));
            }
        });

    }

    public static void clearActionListeners(AbstractButton btn) {
        for (ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }
    }

    private void adjustRowHeights(int startRow, int endRow, int viewportWidth) {
        JTable table = statusTable;
        for (int row = startRow; row <= endRow; row++) {
            int rowHeight = 25; // Default minimum height
            int totalWidth = 0;
            for (int column = 0; column < table.getColumnCount(); column++) {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                Dimension d = comp.getPreferredSize();
                rowHeight = Math.max(rowHeight, d.height);
                TableColumn c = table.getColumnModel().getColumn(column);
                c.setPreferredWidth(d.width);
                totalWidth += d.width;
            }

            if (totalWidth < viewportWidth) {
                // Distribute extra width proportionally
                double scale = (double) viewportWidth / totalWidth;
                for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                    TableColumn column = table.getColumnModel().getColumn(i);
                    column.setPreferredWidth((int) (column.getPreferredWidth() * scale));
                }
            }

            table.setRowHeight(row, rowHeight);
        }
    }

    public void appendError(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tableModel.addFileOpResult(new FileOpResult("", sw.toString(), true));
        LOG.error("Exception in FileOpResult:", e);
    }

    public void appendFiles(final List<File> files) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                if (batchFileList.isEmpty() && files.size() > 0) {
                    Document doc = fileList.getDocument();
                    try {
                        doc.remove(0, doc.getLength());
                    } catch (BadLocationException e) {
                    }
                }
                for (File file : files) {
                    try {
                        Document doc = fileList.getDocument();
                        doc.insertString(doc.getLength(), file.getAbsolutePath() + "\n", null);
                    } catch (BadLocationException exc) {
                        LOG.error("appendFiles", exc);
                    }
                }
                batchFileList.addAll(files);
            }
        });
    }

    protected boolean hasErrors() {
        boolean hasErr = false;
        for (FileOpResult o : tableModel.data) {
            hasErr = hasErr || o.error;
        }
        return hasErr;
    }

    public void runBatch() {
        final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());
        Preferences.getInstance().put(LAST_USED_COMMAND_KEY, command.name);
        persistInputFiles();

        (new BatchOperationWindow.Worker() {
            final ActionStatus actionStatus = new ActionStatus() {
                @Override
                public void addStatus(String filename, String message) {
                    publish(new BatchOperationWindow.FileOpResult(filename, message, false));
                }

                @Override
                public void addError(String filename, String error) {
                    publish(new BatchOperationWindow.FileOpResult(filename, error, true));
                }

            };

            @Override
            protected Void doInBackground() throws Exception {
                BatchOperationParameters params = getBatchParameters(command);
                params.storeForCommand(command);

                PDFMetadataEditBatch batch = new PDFMetadataEditBatch(params);
                String outputDirS = outputDirField.getText();
                File outputDir = outputDirS != null && !outputDirS.isEmpty() ? new File(outputDirS) : null;
                batch.runCommand(command, batchFileList, outputDir, actionStatus);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    appendError(e);
                } catch (ExecutionException e) {
                    appendError(e.getCause());
                }
                onDone();
            }
        }).execute();
    }

    void onDone() {
        try {
            btnCancel.setText("Close");
            clearActionListeners(btnAction);
            btnAction.setText("Start Over");
            btnAction.addActionListener(l -> {
                reset(false);
            });

            if (tableModel.data.isEmpty()) {
                statusSummary.setText("<p align=center style='color:red;'>No PDF files found in selected input files/folders!</p>\n");
            } else if (hasErrors()) {
                statusSummary.setText("<p align=center style='color:red;'>There were some errors processing files...</p>\n");
            } else {
                statusSummary.setText("<p align=center style='color:green;'>Finished successfully!</p>");
            }
            statusSummary.setVisible(true);
        } catch (Exception ignore) {
        }
    }

    protected BatchOperationParameters getBatchParameters(CommandDescription command) {
        BatchOperationParameters params = batchParameters.get(command.name);
        if (params == null) {
            params = BatchOperationParameters.loadForCommand(command);
            batchParameters.put(command.name, params);
        }
        return params;
    }

    public void createBatchParametersWindow() {
        final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());
        if (command.isGroup()) {
            return;
        }

        if (parametersWindow != null) {
            parametersWindow.setVisible(false);
            parametersWindow.dispose();
            parametersWindow = null;
        }

        BatchOperationParameters params = getBatchParameters(command);

        if (command.is(CommandDescription.CLEAR)) {
            parametersWindow = new BatchParametersClear(params, this);
        }
        if (command.is(CommandDescription.EDIT)) {
            parametersWindow = new BatchParametersEdit(params, this);
        }
        if (command.is(CommandDescription.RENAME)) {
            parametersWindow = new BatchParametersRename(params, this);
        }
        if (command.is(CommandDescription.FROM_FILE_NAME)) {
            parametersWindow = new BatchParametersExtract(params, this);
        }
        if (command.isInGroup(CommandDescription.EXPORT_GROUP)) {
            parametersWindow = new BatchParametersExport(params, this, command);
        }
        if (parametersWindow != null) {
            parametersWindow.setModal(true);
            parametersWindow.setVisible(true);
        }

    }

    public void enableBatchParametersWindowButton() {
        final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());
        if (command == null) {
            btnParameters.setEnabled(false);
            addFileButton.setEnabled(false);
            addFolderButton.setEnabled(false);
        } else {
            btnParameters.setEnabled(command.is(CommandDescription.CLEAR) || command.is(CommandDescription.EDIT) || command.isInGroup(CommandDescription.FILE_OPERATIONS_GROUP) || command.isInGroup(CommandDescription.EXPORT_GROUP));
            addFileButton.setEnabled(!command.isGroup());
            addFolderButton.setEnabled(!command.isGroup());
        }
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
        contentPane.setLayout(new GridLayoutManager(6, 2, new Insets(10, 10, 10, 10), -1, -1));
        selectedBatchOperation = new JComboBox();
        contentPane.add(selectedBatchOperation, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnParameters = new JButton();
        btnParameters.setText("Parameters");
        contentPane.add(btnParameters, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusSummary = new TextPaneWithLinks();
        statusSummary.setText("<html>\n  <head>\n    \n  </head>\n  <body>\n  </body>\n</html>\n");
        contentPane.add(statusSummary, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        contentPane.add(btnCancel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAction = new JButton();
        btnAction.setText("Begin");
        contentPane.add(btnAction, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Input Files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fileList = new JTextPane();
        fileList.setEditable(false);
        fileList.setText("Drop files here to batch process them ...");
        scrollPane1.setViewportView(fileList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addFolderButton = new JButton();
        addFolderButton.setText("Add Folder");
        panel2.add(addFolderButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        addFileButton = new JButton();
        addFileButton.setText("Add File");
        panel2.add(addFileButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clearFileList = new JButton();
        clearFileList.setText("Clear");
        panel2.add(clearFileList, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        persistFileList = new JCheckBox();
        persistFileList.setText("Keep file list");
        panel2.add(persistFileList, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Output", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        statusScrollPane = new JScrollPane();
        panel3.add(statusScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        statusTable = new JTable();
        statusScrollPane.setViewportView(statusTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(null, "Output Folder", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        outputDirField = new JTextField();
        panel4.add(outputDirField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        selectOutputDir = new JButton();
        selectOutputDir.setText("Select");
        panel4.add(selectOutputDir, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    static class FileOpResult {
        String filename;
        String message;
        boolean error;

        public FileOpResult(String filename, String message, boolean error) {
            this.filename = filename;
            this.message = message;
            this.error = error;
        }

        @Override
        public String toString() {
            return message; // tab-delimited format for table data
        }
    }

    abstract class Worker extends SwingWorker<Void, FileOpResult> {
        @Override
        protected void process(List<FileOpResult> chunks) {
            for (BatchOperationWindow.FileOpResult chunk : chunks) {
                tableModel.addFileOpResult(chunk);
            }
        }
    }


    // Table model class
    static class FileOpResultTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Filename", "Message"};
        private final List<FileOpResult> data = new ArrayList<>();

        public void addFileOpResult(FileOpResult result) {
            data.add(result);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        public void clearResults() {
            int size = data.size();
            data.clear();
            if (size > 0) {
                fireTableRowsDeleted(0, size - 1);
            }
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            FileOpResult result = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return result.filename;
                case 1:
                    return result;  // Return the entire object for the renderer
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return FileOpResult.class;
                default:
                    return Object.class;
            }
        }
    }

    // Custom cell renderer for the message column
    static class MessageCellRenderer extends JTextArea implements TableCellRenderer {
        private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

        public MessageCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Set JTextArea properties from the adaptee
            setFont(adaptee.getFont());

            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());

                if (value instanceof FileOpResult) {
                    FileOpResult result = (FileOpResult) value;
                    if (result.error) {
                        setForeground(Color.RED);
                    } else {
                        setForeground(table.getForeground());
                    }
                }
            }

            // Set the text
            if (value instanceof FileOpResult) {
                FileOpResult result = (FileOpResult) value;
                setText(result.message);
            } else {
                setText("");
            }

            // Calculate preferred size based on text content
            int lineHeight = getFontMetrics(getFont()).getHeight();
            int lineCount = getLineCount();

            // Set preferred size
            Dimension d = getPreferredSize();
            d.height = lineHeight * lineCount;
            setPreferredSize(d);

            return this;
        }
    }

    public static void main(String[] args) {
        new BatchOperationWindow(null).setVisible(true);
    }
}
