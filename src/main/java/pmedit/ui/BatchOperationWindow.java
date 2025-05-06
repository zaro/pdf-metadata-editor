package pmedit.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import pmedit.*;
import pmedit.prefs.Preferences;
import pmedit.ui.components.TextPaneWithLinks;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BatchOperationWindow extends JFrame {
    public JPanel contentPane;
    public JComboBox<CommandDescription> selectedBatchOperation;
    public JButton btnParameters;
    public JTextPane fileList;
    public JTextPane statusText;
    public JScrollPane statusScrollPane;
    public TextPaneWithLinks txtpnnoBatchLicense;
    public JButton btnCancel;
    public JButton btnAction;

    //
    final static String LAST_USED_COMMAND_KEY = "lastUsedBatchCommand";
    List<File> batchFileList = new ArrayList<File>();
    boolean hasErrors = false;
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
        setContentPane(contentPane);

        btnParameters.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createBatchParametersWindow();
            }
        });

        selectedBatchOperation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createBatchParametersWindowButton();
            }
        });


        Style estyle = statusText.addStyle("ERROR", null);

        txtpnnoBatchLicense.setText("<p align=center>No batch license. In order to use batch operations please get a license from <a href='" + Constants.batchLicenseUrl + "'>" + Constants.batchLicenseUrl + "<a></p>");

        btnCancel.addActionListener(closeWindowActionListener);
        btnAction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runBatch();
            }
        });

        if (command != null) {
            selectedBatchOperation.setModel(new DefaultComboBoxModel<CommandDescription>(new CommandDescription[]{command}));
        } else {
            selectedBatchOperation.setModel(new DefaultComboBoxModel<CommandDescription>(CommandDescription.batchCommands));
            String lastUsedCommand = Preferences.getInstance().get(LAST_USED_COMMAND_KEY, null);
            if (lastUsedCommand != null) {
                CommandDescription lastCommand = CommandDescription.getBatchCommand(lastUsedCommand);
                if (lastCommand != null) {
                    selectedBatchOperation.setSelectedItem(lastCommand);
                }
            }
        }
        StyleConstants.setForeground(estyle, Color.red);

        createBatchParametersWindowButton();


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
        if (!BatchMan.hasBatch()) {
            btnAction.setEnabled(false);
            txtpnnoBatchLicense.setVisible(true);
        } else {
            btnAction.setEnabled(true);
            getContentPane().remove(txtpnnoBatchLicense);
            txtpnnoBatchLicense = null;
        }

        URL imgURL = MainWindow.class
                .getResource("pdf-metadata-edit.png");
        ImageIcon icoImg = new ImageIcon(imgURL);
        setIconImage(icoImg.getImage());
    }

    public static void clearActionListeners(AbstractButton btn) {
        for (ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }
    }

    public void append(String s) {
        try {
            Document doc = statusText.getDocument();
            doc.insertString(doc.getLength(), s, null);
            statusScrollPane.getVerticalScrollBar().setValue(statusScrollPane.getVerticalScrollBar().getMaximum());
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    public void appendError(String s) {
        hasErrors = true;
        try {
            StyledDocument doc = statusText.getStyledDocument();
            doc.insertString(doc.getLength(), s, statusText.getStyle("ERROR"));
            statusScrollPane.getVerticalScrollBar().setValue(statusScrollPane.getVerticalScrollBar().getMaximum());
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    public void appendError(Throwable e) {
        hasErrors = true;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString(); // stack trace as a string
            StyledDocument doc = statusText.getStyledDocument();
            doc.insertString(doc.getLength(), sw.toString(), statusText.getStyle("ERROR"));
            statusScrollPane.getVerticalScrollBar().setValue(statusScrollPane.getVerticalScrollBar().getMaximum());
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
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
                        exc.printStackTrace();
                    }
                }
                batchFileList.addAll(files);
            }
        });
    }

    public void runBatch() {
        final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());
        Preferences.getInstance().put(LAST_USED_COMMAND_KEY, command.name);

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
                batch.runCommand(command, batchFileList, actionStatus);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    appendError(e);
                } catch (ExecutionException e) {
                    appendError(e);
                }
                onDone();
            }
        }).execute();
    }

    void onDone() {
        try {
            append("------\n");
            if (hasErrors) {
                appendError("Done (with Errors)\n");
            } else {
                append("Done");
            }
            clearActionListeners(btnAction);
            btnAction.setText("Close");
            btnAction.addActionListener(closeWindowActionListener);
            btnCancel.setVisible(false);
            FileDrop.remove(this);
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

        if (parametersWindow != null) {
            parametersWindow.setVisible(false);
            parametersWindow.dispose();
            parametersWindow = null;
        }

        BatchOperationParameters params = getBatchParameters(command);

        if (command.is("clear")) {
            parametersWindow = new BatchParametersClear(params, this);
        }
        if (command.is("edit")) {
            parametersWindow = new BatchParametersEdit(params, this);
        }
        if (command.is("rename")) {
            parametersWindow = new BatchParametersRename(params, this);
        }
        if (parametersWindow != null) {
            parametersWindow.setModal(true);
            parametersWindow.setVisible(true);
        }

    }

    public void createBatchParametersWindowButton() {
        final CommandDescription command = ((CommandDescription) selectedBatchOperation.getSelectedItem());

        btnParameters.setEnabled(command.is("clear") || command.is("rename") || command.is("edit"));
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
        final JLabel label1 = new JLabel();
        label1.setText("Status");
        contentPane.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusScrollPane = new JScrollPane();
        contentPane.add(statusScrollPane, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        statusText = new JTextPane();
        statusText.setEditable(false);
        statusScrollPane.setViewportView(statusText);
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fileList = new JTextPane();
        fileList.setEditable(false);
        fileList.setText("Drop files here to batch process them ...");
        scrollPane1.setViewportView(fileList);
        txtpnnoBatchLicense = new TextPaneWithLinks();
        contentPane.add(txtpnnoBatchLicense, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        contentPane.add(btnCancel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAction = new JButton();
        btnAction.setText("Begin");
        contentPane.add(btnAction, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
    }

    abstract class Worker extends SwingWorker<Void, FileOpResult> {
        @Override
        protected void process(List<FileOpResult> chunks) {
            for (BatchOperationWindow.FileOpResult chunk : chunks) {
                if (chunk.error) {
                    appendError(chunk.filename + " -> " + chunk.message + "\n");
                } else {
                    append(chunk.filename + " -> " + chunk.message + "\n");
                }
            }
        }
    }
}
