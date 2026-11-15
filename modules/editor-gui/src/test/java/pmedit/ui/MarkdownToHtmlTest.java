package pmedit.ui;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkdownToHtmlTest {

    @Test
    public void convertsHeadings() {
        String html = MarkdownToHtml.toHtml("# Title\n\n## Sub\n\n### Detail");

        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<h2>Sub</h2>"));
        assertTrue(html.contains("<h3>Detail</h3>"));
    }

    @Test
    public void convertsLinks() {
        String html = MarkdownToHtml.toHtml("[MCP help](https://pdf.metadata.care/help/)");

        assertTrue(html.contains("<a href=\"https://pdf.metadata.care/help/\">MCP help</a>"));
    }

    @Test
    public void leavesUnsafeLinksAsText() {
        String html = MarkdownToHtml.toHtml("[x](javascript:alert(1))");

        assertFalse(html.contains("<a "));
        assertTrue(html.contains("[x](javascript:alert(1))"));
    }

    @Test
    public void convertsFencedCodeBlocks() {
        Color base = new Color(0xf2f2f2);
        String background = MarkdownToHtml.toHex(MarkdownToHtml.codeBlockBackground(base));
        String html = MarkdownToHtml.toHtml("```json\n{\"a\":\"<b>\"}\n```", base);

        assertTrue(html.contains("<pre style=\"background-color: " + background + "; padding: 6px;\">"));
        assertTrue(html.contains("{\"a\":\"&lt;b&gt;\"}"));
    }

    @Test
    public void doesNotConvertMarkdownInsideCodeBlocks() {
        Color base = new Color(0xf2f2f2);
        String html = MarkdownToHtml.toHtml("```\n# not a heading\n[x](https://example.com)\n```", base);

        assertTrue(html.contains("<pre style=\"background-color: #"));
        assertTrue(html.contains("# not a heading"));
        assertTrue(html.contains("[x](https://example.com)"));
        assertFalse(html.contains("<h1>"));
        assertFalse(html.contains("<a "));
    }

    @Test
    public void escapesHtml() {
        String html = MarkdownToHtml.toHtml("A <tag> & value");

        assertTrue(html.contains("A &lt;tag&gt; &amp; value"));
    }

    @Test
    public void closesUnclosedCodeBlock() {
        Color base = new Color(0xf2f2f2);
        String html = MarkdownToHtml.toHtml("```sh\necho hi", base);

        assertTrue(html.contains("<pre style=\"background-color: #"));
        assertTrue(html.contains("echo hi"));
    }

    @Test
    public void lightThemeUsesDarkerCodeBackground() {
        Color base = new Color(0xf2f2f2);
        Color code = MarkdownToHtml.codeBlockBackground(base);

        assertTrue(code.getRed() < base.getRed());
        assertTrue(code.getRed() > 0);
    }

    @Test
    public void darkThemeUsesLighterCodeBackground() {
        Color base = new Color(0x2b2b2b);
        Color code = MarkdownToHtml.codeBlockBackground(base);

        assertTrue(code.getRed() > base.getRed());
        assertTrue(code.getRed() < 255);
    }

    @Test
    public void missingBaseFallsBackToLightCodeBackground() {
        Color code = MarkdownToHtml.codeBlockBackground(null);

        assertTrue(code.getRed() > 0);
        assertTrue(code.getRed() < 255);
    }
}
