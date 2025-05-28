package pmedit.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.*;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.prefs.Preferences;
import pmedit.ui.preferences.DefaultsPreferences;

import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalScrollButton;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class MainWindowSaveTest {
    FilesTestHelper.PMTuple initialFile;
    JFrameOperator topFrame;
    Timeouts timeouts = JemmyProperties.getCurrentTimeouts();

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.ui.MainWindow");
        cr.startApplication();
    }

    @BeforeEach
    void loadFile() throws Exception {
        if(topFrame == null) {
            topFrame = new JFrameOperator("Pdf Metadata Editor");
        }
        initialFile = FilesTestHelper.randomFiles(1).get(0);

        new JButtonOperator(topFrame, "Open PDF").push();
        openFileChooser("Open", initialFile.file);
        checkMetadataPaneValues(topFrame, initialFile.md);

    }

    @AfterEach
    void cleanUp() {
        Preferences.clear();
    }

    @Test
    public void testSaveFile() throws FileNotFoundException, IOException, Exception {
        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        md.doc.author = "Don't mind me";
        md.basic.baseURL = "http://example.com";
        md.docEnabled.author = true;
        md.basicEnabled.baseURL = true;
        populateMetadataPaneValues(topFrame, md);
        new JButtonOperator(topFrame, "Save").push();
        MetadataInfo saved = new MetadataInfo();
        saved.loadFromPDF(initialFile.file);
        saved.docEnabled.author = false;
        saved.basicEnabled.baseURL = false;
        assertTrue(saved.isEquivalent(initialFile.md, true), "Non edited metadata differs");
        saved.setEnabled(false);
        saved.docEnabled.author = true;
        saved.basicEnabled.baseURL = true;
        assertTrue(saved.isEquivalent(md, true), "Edited metadata differs");
    }


    @Test
    public void testSaveAsFile() throws FileNotFoundException, IOException, Exception {
        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        md.doc.author = "Don't mind me";
        md.basic.baseURL = "http://example.com";
        md.docEnabled.author = true;
        md.basicEnabled.baseURL = true;
        populateMetadataPaneValues(topFrame, md);

       new ComponentOperator(
                topFrame,
                new JButtonOperator.JButtonFinder() {
                    public boolean checkComponent(Component comp) {
                        return super.checkComponent(comp) && comp instanceof BasicArrowButton;
                    }
                }).clickMouse();

        JPopupMenuOperator menuOp = new JPopupMenuOperator();
        menuOp.pushMenuNoBlock("Save As");
        File savedAs = new File(initialFile.file.getParentFile(), initialFile.file.getName() + ".saveAs.pdf");
        saveFileChooser("Save As", savedAs);

        MetadataInfo saved = new MetadataInfo();
        saved.loadFromPDF(initialFile.file);
        assertTrue(saved.isEquivalent(initialFile.md), "Original file metadata differs");

        saved.loadFromPDF(savedAs);
        saved.docEnabled.author = false;
        saved.basicEnabled.baseURL = false;
        assertTrue(saved.isEquivalent(initialFile.md, true), "Non edited metadata differs");
        saved.setEnabled(false);
        saved.docEnabled.author = true;
        saved.basicEnabled.baseURL = true;
        assertTrue(saved.isEquivalent(md, true), "Edited metadata differs");
    }

    @Test
    public void testSaveRename() throws FileNotFoundException, IOException, Exception {

        new JButtonOperator(topFrame, "", 1).push();
        JDialogOperator preferences = new JDialogOperator("Preferences");
        JTabbedPaneOperator tabs = new JTabbedPaneOperator(preferences);
        ensureTab(tabs, "General");
        JComboBoxOperator renameCombo = new JComboBoxOperator(preferences);
        renameCombo.typeText("{doc.author}-{dc.title}-{file.size}.pdf");
        renameCombo.pressKey(KeyEvent.VK_ENTER);
        renameCombo.releaseKey(KeyEvent.VK_ENTER);
        preferences.close();
        preferences.waitClosed();


        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        md.doc.author = "Don't mind me";
        for(int i=md.doc.author.length(); i<initialFile.md.doc.author.length(); i++){
            md.doc.author += "_";
        }
        md.basic.baseURL = "http://example.com";
        for(int i=md.basic.baseURL.length(); i<initialFile.md.basic.baseURL.length(); i++){
            md.basic.baseURL += "_";
        }
        md.docEnabled.author = true;
        md.basicEnabled.baseURL = true;
        populateMetadataPaneValues(topFrame, md);

        new ComponentOperator(
                topFrame,
                new JButtonOperator.JButtonFinder() {
                    public boolean checkComponent(Component comp) {
                        return super.checkComponent(comp) && comp instanceof BasicArrowButton;
                    }
                }).clickMouse();

        JPopupMenuOperator menuOp = new JPopupMenuOperator();
        menuOp.pushMenu("Save & rename");
        String fileName = md.doc.author.toString() + "-" + initialFile.md.dc.title.toString() + "-" + initialFile.md.file.size + ".pdf";
        File savedAs = new File(initialFile.file.getParentFile(), fileName);


        MetadataInfo saved = new MetadataInfo();
        saved.loadFromPDF(initialFile.file);
        assertTrue(saved.isEquivalent(initialFile.md), "Original file metadata differs");

        saved.loadFromPDF(savedAs);
        saved.docEnabled.author = false;
        saved.basicEnabled.baseURL = false;
        assertTrue(saved.isEquivalent(initialFile.md, true), "Non edited metadata differs");
        saved.setEnabled(false);
        saved.docEnabled.author = true;
        saved.basicEnabled.baseURL = true;
        assertTrue(saved.isEquivalent(md, true), "Edited metadata differs");
    }

}
