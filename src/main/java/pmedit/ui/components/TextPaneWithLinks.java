package pmedit.ui.components;

import pmedit.OsCheck;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URISyntaxException;
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
                try {
                    java.net.URI uri = e.getURL().toURI();
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                            desktop.browse(uri);
                    } else {
                        // Detect Linux and fallback to xdgo-open
                        if (OsCheck.isLinux()) {
                            try {
                                Process p = Runtime.getRuntime().exec(new String[] { "xdg-open", e.getURL().toString() });
                                p.waitFor();
                                p.destroy();
                            } catch (Exception e1) {

                            }
                        }
                    }
                } catch (URISyntaxException | java.io.IOException e1) {

                }

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
}