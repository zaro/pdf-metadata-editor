package pmedit.ui;

import pmedit.CommandLine;
import pmedit.ui.components.DateTimePicker;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TextFieldContextMenu {
    private final JTextComponent textComponent;
    private final Consumer<JComponent> resetHandler;
    private JPopupMenu popupMenu;

    public TextFieldContextMenu(JComponent component, Consumer<JComponent> resetHandler) {
        if (component instanceof JTextComponent tc) {
            textComponent = tc;
        } else if (component instanceof DateTimePicker dtp) {
            textComponent = dtp.getTextComponent();
        } else {
            throw new RuntimeException("Trying to create menu on unsupported component: " + component.getClass().getName());
        }
        this.resetHandler = resetHandler;
    }
    public TextFieldContextMenu(JComponent component) {
        this(component, null);
    }

    public TextFieldContextMenu createContextMenu() {

        popupMenu = new JPopupMenu();

        // Create the Reset action using the provided handler
        Action resetAction = new AbstractAction("Reset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(resetHandler != null ) {
                    resetHandler.accept(textComponent);
                }
            }
        };
        resetAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl R"));

        // Create menu items
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem pasteItem = new JMenuItem("Paste");
        JMenuItem resetItem = new JMenuItem(resetAction);

        // Set up standard actions
        copyItem.addActionListener(e -> textComponent.copy());
        cutItem.addActionListener(e -> textComponent.cut());
        pasteItem.addActionListener(e -> textComponent.paste());

        // Set accelerators for standard actions
        copyItem.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        cutItem.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        pasteItem.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));

        if(resetHandler == null){
            resetItem.setEnabled(false);
        }

        // Add items to menu
        popupMenu.add(copyItem);
        popupMenu.add(cutItem);
        popupMenu.add(pasteItem);
        popupMenu.add(resetItem);

        // Add mouse listener for popup trigger
        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // Register keyboard shortcut
        String resetKey = "reset-text";
        InputMap inputMap = textComponent.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textComponent.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ctrl R"), resetKey);
        actionMap.put(resetKey, resetAction);

        return this;
    }

    public TextFieldContextMenu addTemplatePlaceholders() {
        Map<String, List<String>> fields = CommandLine.mdFieldsGrouped();
        JMenu subMenu = new JMenu("Template Variable");

        for (var e : fields.entrySet()) {
            JMenu group = new JMenu(e.getKey());
            for (var v : e.getValue()) {
                JMenuItem item = new JMenuItem(v);
                item.addActionListener(a -> textComponent.replaceSelection("{"+v+"}"));
                group.add(item);
            }
            subMenu.add(group);
        }

        popupMenu.add(subMenu);
        return this;
    }
}