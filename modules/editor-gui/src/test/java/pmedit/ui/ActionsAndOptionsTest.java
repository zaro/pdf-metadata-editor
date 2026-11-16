package pmedit.ui;

import org.junit.jupiter.api.Test;
import pmedit.MetadataInfo;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionsAndOptionsTest {

    @Test
    public void startsWithMcpPromo() {
        ActionsAndOptions ui = new ActionsAndOptions();

        assertTrue(ui.isMcpPromoActive());
        assertTrue(ui.successfullyLoadedButton.isEnabled());
        assertTrue(ui.successfullyLoadedButton.getText().contains(ActionsAndOptions.MCP_PROMO_TEXT));
        assertFalse(ui.isPromoTimerRunning());
    }

    @Test
    public void cleanLoadShowsSuccessThenRevertsToPromo() {
        ActionsAndOptions ui = new ActionsAndOptions();

        ui.attachMetadataInfo(new MetadataInfo());

        assertEquals("Successfully Loaded", ui.successfullyLoadedButton.getText());
        assertFalse(ui.successfullyLoadedButton.isEnabled());
        assertFalse(ui.isMcpPromoActive());
        assertTrue(ui.isPromoTimerRunning());
        assertTrue(ActionsAndOptions.nextPromoDelay() > 0);

        ui.showMcpPromo();

        assertTrue(ui.isMcpPromoActive());
        assertTrue(ui.successfullyLoadedButton.isEnabled());
        assertTrue(ui.successfullyLoadedButton.getText().contains(ActionsAndOptions.MCP_PROMO_TEXT));
        assertFalse(ui.isPromoTimerRunning());
    }

    @Test
    public void warningsKeepTheWarningButton() {
        ActionsAndOptions ui = new ActionsAndOptions();
        MetadataInfo warned = new MetadataInfo();
        warned.warnings.add(new MetadataInfo.Warning(MetadataInfo.Warning.Type.NORMAL, "warning", null));

        ui.attachMetadataInfo(warned);

        assertEquals("1 Warning(s)", ui.successfullyLoadedButton.getText());
        assertTrue(ui.successfullyLoadedButton.isEnabled());
        assertFalse(ui.isMcpPromoActive());
        assertFalse(ui.isPromoTimerRunning());
    }

    @Test
    public void cleanLoadAfterWarningsArmsThePromoTimer() {
        ActionsAndOptions ui = new ActionsAndOptions();
        MetadataInfo warned = new MetadataInfo();
        warned.warnings.add(new MetadataInfo.Warning(MetadataInfo.Warning.Type.CRITICAL, "broken", null));

        ui.attachMetadataInfo(warned);
        ui.attachMetadataInfo(new MetadataInfo());

        assertEquals("Successfully Loaded", ui.successfullyLoadedButton.getText());
        assertTrue(ui.isPromoTimerRunning());
        assertFalse(ui.isMcpPromoActive());
    }

    @Test
    public void resetLoadStatusReturnsToPromo() {
        ActionsAndOptions ui = new ActionsAndOptions();
        ui.attachMetadataInfo(new MetadataInfo());

        ui.resetLoadStatus();

        assertTrue(ui.isMcpPromoActive());
        assertFalse(ui.isPromoTimerRunning());
    }

    @Test
    public void promoDelayStaysInRange() {
        for (int i = 0; i < 200; i++) {
            int delay = ActionsAndOptions.nextPromoDelay();

            assertTrue(delay >= 8_000, "delay " + delay);
            assertTrue(delay <= 16_000, "delay " + delay);
        }
    }

    @Test
    public void statusButtonPaintsNoBackground() {
        ActionsAndOptions ui = new ActionsAndOptions();

        assertFalse(ui.successfullyLoadedButton.isContentAreaFilled());
        assertFalse(ui.successfullyLoadedButton.isOpaque());
        assertFalse(ui.successfullyLoadedButton.isBorderPainted());
    }

    @Test
    public void rolloverHighlightsTheStatusText() {
        ActionsAndOptions ui = new ActionsAndOptions();
        Color base = ui.successfullyLoadedButton.getForeground();

        ui.successfullyLoadedButton.getModel().setRollover(true);

        Color highlighted = ui.successfullyLoadedButton.getForeground();
        assertNotEquals(base, highlighted);
        assertEquals(ActionsAndOptions.highlight(base), highlighted);

        ui.successfullyLoadedButton.getModel().setRollover(false);

        assertEquals(base, ui.successfullyLoadedButton.getForeground());
    }
}
