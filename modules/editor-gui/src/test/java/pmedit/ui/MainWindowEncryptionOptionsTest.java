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
@SetSystemProperty(key = "junitTest", value = "true")
public class MainWindowEncryptionOptionsTest extends  BaseJemmyTest  {
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
        initialFile = FilesTestHelper.randomFiles(1, md -> {
            md.prop.version = 1.7f;
        }).get(0);

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
    public void testSaveAsVersionFile() throws FileNotFoundException, IOException, Exception {
        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
//        md.prop.encryption = "Don't mind me";
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


}
