package pmedit.ui;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelpMenuTest {

    @Test
    public void helpMenuContainsMcpEntry() {
        JMenuBar menuBar = new JMenuBar();
        MainWindow.addHelpMenu(menuBar, "help/editor/", e -> {
        });

        JMenu helpMenu = menuBar.getMenu(0);
        assertNotNull(helpMenu);

        boolean found = false;
        for (Component component : helpMenu.getMenuComponents()) {
            if (component instanceof JMenuItem item && "MCP".equals(item.getText())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}
