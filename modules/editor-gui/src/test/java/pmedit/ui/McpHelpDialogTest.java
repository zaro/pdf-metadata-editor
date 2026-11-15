package pmedit.ui;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpHelpDialogTest {

    @Test
    public void copiesWholeTextWhenNothingIsSelected() {
        JTextArea area = new JTextArea("hello\nworld");

        assertEquals("hello\nworld", McpHelpDialog.textToCopy(area));
    }

    @Test
    public void copiesOnlyTheSelectedText() {
        JTextArea area = new JTextArea("hello world");
        area.select(6, 11);

        assertEquals("world", McpHelpDialog.textToCopy(area));
    }

    @Test
    public void copiesMultiLineSelection() {
        JTextArea area = new JTextArea("one\ntwo\nthree");
        area.select(4, 7);

        assertEquals("two", McpHelpDialog.textToCopy(area));
    }

    @Test
    public void copiesWholeTextForZeroLengthSelection() {
        JTextArea area = new JTextArea("hello");
        area.setCaretPosition(2);
        area.select(2, 2);

        assertEquals("hello", McpHelpDialog.textToCopy(area));
    }

    @Test
    public void copiesRenderedTextFromHtmlPane() throws Exception {
        JTextPane pane = htmlPane();

        String text = McpHelpDialog.textToCopy(pane);

        assertTrue(text.contains("Title"));
        assertTrue(text.contains("Hello world"));
        assertFalse(text.contains("<h1>"));
    }

    @Test
    public void copiesSelectionFromHtmlPane() throws Exception {
        JTextPane pane = htmlPane();
        String document = pane.getDocument().getText(0, pane.getDocument().getLength());
        int start = document.indexOf("Hello");

        pane.select(start, start + 5);

        assertEquals("Hello", McpHelpDialog.textToCopy(pane));
    }

    private static JTextPane htmlPane() {
        JTextPane pane = new JTextPane();
        pane.setContentType("text/html");
        pane.setText("<html><body><h1>Title</h1><p>Hello world</p></body></html>");
        return pane;
    }
}
