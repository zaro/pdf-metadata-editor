package pmedit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainCliTest {

    @Test
    public void onlyTheServerUsesTheMcpLog() {
        assertTrue(MainCli.isMcpServe(new String[]{"--mcp"}));
        assertTrue(MainCli.isMcpServe(new String[]{"--mcp-root", "/tmp", "-mcp"}));

        assertFalse(MainCli.isMcpServe(new String[]{"--help-mcp"}));
        assertFalse(MainCli.isMcpServe(new String[]{"--mcp-root", "/tmp", "file.pdf"}));
        assertFalse(MainCli.isMcpServe(new String[]{"--showLicense"}));
        assertFalse(MainCli.isMcpServe(new String[0]));
    }
}
