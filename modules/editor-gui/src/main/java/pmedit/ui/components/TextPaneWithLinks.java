package pmedit.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.OsCheck;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Custom JTextPane component that can be used with IntelliJ GUI Designer.
 * Supports custom background color and placeholder text functionality.
 */
public class TextPaneWithLinks extends JTextPane {
    Map<String, Consumer<String>> actionMap = new HashMap<>();

    // Default constructor required for GUI Designer
    public TextPaneWithLinks() {
        super();
        setupComponent();
    }

    private void setupComponent() {
        setEditable(false);
        setBackground(UIManager.getColor("Panel.background"));
        setContentType("text/html");
        addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                    return;
                }
                if (!java.awt.Desktop.isDesktopSupported()) {
                    return;
                }

                if(e.getURL() == null){
                    // Most probably custom action
                    String action = e.getDescription();
                    for(Map.Entry<String, Consumer<String>> h: actionMap.entrySet()){
                        if(action.startsWith(h.getKey())){
                            h.getValue().accept(action);
                        }
                    }
                    return;
                }
                openURL(e.getURL());
            }
        });
    }

    public String getText() {
        return super.getText();
    }

    public void setText(String text) {
        super.setText(text);
    }

    public void addActionHandler(String actionPrefix, Consumer<String> handler){
        actionMap.put(actionPrefix, handler);
    }

    public static void openURL(String url) {
        try {
            openURL(new URL(url));
        } catch (MalformedURLException e) {
            Logger LOG = LoggerFactory.getLogger("openURL");
            LOG.error("Failed to construct URL from '{}'", url, e);
        }
    }
    public static void openURL(URL url){
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                try{
                    desktop.browse(url.toURI());
                } catch (URISyntaxException | java.io.IOException e) {
                    Logger LOG = LoggerFactory.getLogger("openURL");
                    LOG.error("Failed to construct URI from '{}'", url, e);
                }
            } else {
                // Detect Linux and fallback to xdg-open
                if (OsCheck.isLinux()) {
                    try {
                        Process p = Runtime.getRuntime().exec(new String[] { "xdg-open", url.toString() });
                        p.waitFor();
                        p.destroy();
                    } catch (IOException | InterruptedException e) {
                        Logger LOG = LoggerFactory.getLogger("openURL");
                        LOG.error("Failed to invoke xdg-open with '{}'", url, e);
                    }
                }
            }
    }
}