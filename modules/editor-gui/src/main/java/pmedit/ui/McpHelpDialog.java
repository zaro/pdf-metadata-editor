package pmedit.ui;

import pmedit.mcp.McpHelpText;
import pmedit.ui.components.TextPaneWithLinks;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

/**
 * Help → MCP configuration dialog.
 */
public final class McpHelpDialog extends JDialog {

    private final JTabbedPane tabs;
    private final TextPaneWithLinks configurationText;
    private final TextPaneWithLinks optionsText;

    private McpHelpDialog(Window owner) {
        super(owner, "MCP Configuration", ModalityType.APPLICATION_MODAL);

        configurationText = createTextPane(McpHelpText.text());
        optionsText = createTextPane(McpHelpText.optionsText());

        tabs = new JTabbedPane();
        tabs.addTab("Configuration", scrollPane(configurationText));
        tabs.addTab("Options", scrollPane(optionsText));

        JButton copy = new JButton("Copy");
        copy.setToolTipText("Copy selected text, or all text if nothing is selected");
        copy.addActionListener(e -> copyActiveTab());

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(copy);
        buttons.add(close);

        JPanel content = new JPanel(new BorderLayout());
        content.add(tabs, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(760, 500));
        pack();
        setSize(Math.max(getWidth(), 1000), Math.max(getHeight(), 760));
        setLocationRelativeTo(owner);

        getRootPane().setDefaultButton(close);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static void show(Window owner) {
        new McpHelpDialog(owner).setVisible(true);
    }

    private static TextPaneWithLinks createTextPane(String markdown) {
        TextPaneWithLinks text = new TextPaneWithLinks();
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        text.setText(MarkdownToHtml.toHtml(markdown, text.getBackground()));
        text.setCaretPosition(0);
        text.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return text;
    }

    private static JScrollPane scrollPane(JTextComponent text) {
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(950, 620));
        return scroll;
    }

    private void copyActiveTab() {
        JTextComponent active = tabs.getSelectedIndex() == 1 ? optionsText : configurationText;
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(textToCopy(active)), null);
    }

    /**
     * The text the Copy button should place on the clipboard: the current
     * selection when one exists, otherwise the rendered text of the whole tab.
     */
    static String textToCopy(JTextComponent area) {
        String selected = area.getSelectedText();
        if (selected != null && !selected.isEmpty()) {
            return selected;
        }
        try {
            return area.getDocument().getText(0, area.getDocument().getLength());
        } catch (BadLocationException e) {
            return area.getText();
        }
    }
}
