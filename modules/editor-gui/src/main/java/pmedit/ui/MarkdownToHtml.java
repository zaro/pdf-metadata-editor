package pmedit.ui;

import javax.swing.UIManager;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the small Markdown subset used by the MCP help into HTML.
 *
 * <p>Supported: headings, fenced code blocks, inline links, bold ({@code **})
 * and italic ({@code *}). Everything else is rendered as escaped text.</p>
 */
public final class MarkdownToHtml {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC = Pattern.compile("\\*([^*\\n]+?)\\*");
    private static final Color DEFAULT_BACKGROUND = new Color(0xf2f2f2);

    private MarkdownToHtml() {
    }

    public static String toHtml(String markdown) {
        return toHtml(markdown, UIManager.getColor("Panel.background"));
    }

    public static String toHtml(String markdown, Color baseBackground) {
        String source = markdown == null ? "" : markdown;
        String codeBackground = toHex(codeBlockBackground(baseBackground));
        StringBuilder body = new StringBuilder();
        StringBuilder paragraph = new StringBuilder();
        StringBuilder code = new StringBuilder();
        boolean inCode = false;

        for (String line : source.split("\\R", -1)) {
            if (line.startsWith("```")) {
                flushParagraph(body, paragraph);
                if (inCode) {
                    appendCodeBlock(body, code.toString(), codeBackground);
                    code.setLength(0);
                    inCode = false;
                } else {
                    inCode = true;
                }
                continue;
            }

            if (inCode) {
                code.append(line).append('\n');
                continue;
            }

            Matcher heading = HEADING.matcher(line);
            if (heading.matches()) {
                flushParagraph(body, paragraph);
                int level = heading.group(1).length();
                body.append("<h").append(level).append('>')
                        .append(inline(escape(heading.group(2))))
                        .append("</h").append(level).append('>');
            } else if (line.isBlank()) {
                flushParagraph(body, paragraph);
            } else {
                if (paragraph.length() > 0) {
                    paragraph.append("<br>");
                }
                paragraph.append(inline(escape(line)));
            }
        }

        flushParagraph(body, paragraph);
        if (inCode) {
            appendCodeBlock(body, code.toString(), codeBackground);
        }

        return "<html><head><style>"
                + "body { font-family: monospace; font-size: 14px; }"
                + "a { color: #1155cc; }"
                + "</style></head><body>"
                + body
                + "</body></html>";
    }

    static Color codeBlockBackground(Color baseBackground) {
        Color base = baseBackground == null ? DEFAULT_BACKGROUND : baseBackground;
        double luminance = (0.299 * base.getRed()
                + 0.587 * base.getGreen()
                + 0.114 * base.getBlue()) / 255.0;
        boolean dark = luminance < 0.5;
        double amount = dark ? 0.12 : 0.07;
        int target = dark ? 255 : 0;
        return new Color(
                blend(base.getRed(), target, amount),
                blend(base.getGreen(), target, amount),
                blend(base.getBlue(), target, amount));
    }

    static String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static int blend(int channel, int target, double amount) {
        return (int) Math.round(channel + (target - channel) * amount);
    }

    private static void appendCodeBlock(StringBuilder body, String code, String background) {
        body.append("<pre style=\"background-color: ")
                .append(background)
                .append("; padding: 6px;\">")
                .append(escape(code))
                .append("</pre>");
    }

    private static void flushParagraph(StringBuilder body, StringBuilder paragraph) {
        if (paragraph.length() == 0) {
            return;
        }
        body.append("<p>").append(paragraph).append("</p>");
        paragraph.setLength(0);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String inline(String value) {
        return emphasis(linkify(value));
    }

    private static String emphasis(String value) {
        return wrap(ITALIC, wrap(BOLD, value, "<b>", "</b>"), "<i>", "</i>");
    }

    private static String wrap(Pattern pattern, String value, String open, String close) {
        Matcher matcher = pattern.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result,
                    Matcher.quoteReplacement(open + matcher.group(1) + close));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String linkify(String value) {
        Matcher matcher = LINK.matcher(value);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String text = matcher.group(1);
            String url = matcher.group(2);
            String replacement = isSafeUrl(url)
                    ? "<a href=\"" + url + "\">" + text + "</a>"
                    : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean isSafeUrl(String url) {
        return url.startsWith("http://")
                || url.startsWith("https://")
                || url.startsWith("mailto:");
    }
}
