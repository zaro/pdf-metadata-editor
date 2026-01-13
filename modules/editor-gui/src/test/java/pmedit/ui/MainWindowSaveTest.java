package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.*;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.prefs.Preferences;

import javax.swing.*;
import javax.swing.plaf.metal.MetalComboBoxIcon;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
public class MainWindowSaveTest  extends  BaseJemmyTest  {
    FilesTestHelper.PMTuple initialFile;
    static JFrameOperator topFrame;
    Timeouts timeouts = JemmyProperties.getCurrentTimeouts();

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.ui.MainWindow");
        cr.startApplication();
        topFrame = new JFrameOperator("Pdf Metadata Editor");
    }

    @AfterAll
    static void tearDown() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        topFrame.getOutput().printLine("Disposing window!");
        topFrame.setVisible(false);
        topFrame.dispose();
        topFrame.waitClosed();

    }

    @BeforeEach
    void loadFile(TestInfo testInfo) throws Exception {
        FilesTestHelper.pushTempDir(testInfo.getDisplayName().replaceFirst("\\(.*", ""));
        initialFile = FilesTestHelper.randomFiles(1).get(0);

        new JButtonOperator(topFrame, "Open PDF").push();
        openFileChooser("Open", initialFile.file);
        checkMetadataPaneValues(topFrame, initialFile.md);

    }

    @AfterEach
    void cleanUp() {
        Preferences.clear();
        FilesTestHelper.popTempDir();
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
        JButtonOperator saveBtn =new JButtonOperator(topFrame, "Save");
        assertTrue(saveBtn.isEnabled());
        saveBtn.push();

        MetadataInfo saved = FilesTestHelper.load(initialFile.file);

        saved.docEnabled.author = false;
        saved.basicEnabled.baseURL = false;
        FilesTestHelper.assertEqualsOnlyEnabledExceptFile(initialFile.md, saved, "Non edited metadata differs");
        saved.setEnabled(false);
        saved.docEnabled.author = true;
        saved.basicEnabled.baseURL = true;
        FilesTestHelper.assertEqualsOnlyEnabledExceptFile(md, saved,  "Edited metadata differs");
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
                        boolean r = super.checkComponent(comp);
                        if(r && comp instanceof JButton b){
                            r = b.getIcon() instanceof MetalComboBoxIcon;
                        }
                        return r;
                    }
                }).clickMouse();

        JPopupMenuOperator menuOp = new JPopupMenuOperator();
        menuOp.pushMenuNoBlock("Save As");
        File savedAs = new File(initialFile.file.getParentFile(), initialFile.file.getName() + ".saveAs.pdf");
        saveFileChooser("Save As", savedAs);

        FilesTestHelper.checkFileHasChangedMetadata(initialFile, savedAs, md);
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
                        boolean r = super.checkComponent(comp);
                        if(r && comp instanceof JButton b){
                            r = b.getIcon() instanceof MetalComboBoxIcon;
                        }
                        return r;
                    }
                }).clickMouse();

        JPopupMenuOperator menuOp = new JPopupMenuOperator();
        menuOp.pushMenu("Save & rename");
        String fileName = md.doc.author.toString() + "-" + initialFile.md.dc.title.toString() + "-" + initialFile.md.file.size + ".pdf";
        File savedAs = new File(initialFile.file.getParentFile(), fileName);


        FilesTestHelper.checkFileHasChangedMetadata(initialFile, savedAs, md);

    }

}
