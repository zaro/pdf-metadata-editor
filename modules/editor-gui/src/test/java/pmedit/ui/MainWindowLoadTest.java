package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.*;
import pmedit.*;
import pmedit.prefs.Preferences;
import pmedit.ui.preferences.DefaultsPreferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
public class MainWindowLoadTest  extends  BaseJemmyTest  {
    final int NUM_FILES = 1;
    static  JFrameOperator topFrame;

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
    }

    @BeforeEach
    void loadFile(TestInfo testInfo) throws Exception {

    }

    @AfterEach
    void cleanUp() {
        Preferences.clear();
    }

    @Test
    public void testLoadFile() throws FileNotFoundException, IOException, Exception {
        for(FilesTestHelper.PMTuple testData : FilesTestHelper.randomFiles(NUM_FILES, e-> {
            e.prop.compression = true;
            e.basic.rating = 13; // Make sure th
        })) {
            new JButtonOperator(topFrame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            new TestOut().printLine("Opened: " + testData.file);
            checkMetadataPaneValues(topFrame, testData.md);
        }
    }

    @Test
    public void testPopulateDefaultMetadata() throws FileNotFoundException, IOException, Exception {

        new JButtonOperator(topFrame, "", 1).push();
        JDialogOperator preferences = new JDialogOperator("Preferences");
        JTabbedPaneOperator tabs = new JTabbedPaneOperator(preferences);
        ensureTab(tabs, "Default");
        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        md.doc.author = "Don't mind me";
        md.basic.baseURL= "http://example.com";
        md.setEnabled("doc.author", true);
        md.setEnabled("basic.baseURL", true);
        populateMetadataPaneValues(tabs, md);
        preferences.close();
        preferences.waitClosed();
        MetadataInfo defaultMetadata = DefaultsPreferences.loadDefaultMetadata(Preferences.getInstance());
        FilesTestHelper.assertEqualsAll(defaultMetadata, md, "Default metadata saved differs");
    }

    @Test
    public void testLoadFileWithDefaults() throws FileNotFoundException, IOException, Exception {

        MetadataInfo defaultMetadata = new MetadataInfo();
        defaultMetadata.setEnabled(false);
        defaultMetadata.doc.author = "Don't mind me";
        defaultMetadata.basic.baseURL= "http://example.com";
        defaultMetadata.pdf.keywords = "{doc.title}";
        defaultMetadata.setEnabled("doc.author", true);
        defaultMetadata.setEnabled("basic.baseURL", true);
        defaultMetadata.setEnabled("pdf.keywords", true);


        DefaultsPreferences.saveDefaultMetadata(Preferences.getInstance(), defaultMetadata);

        // Check that it doesn't override already defined fields
        for (FilesTestHelper.PMTuple testData : FilesTestHelper.randomFiles(1)) {
            new JButtonOperator(topFrame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            checkMetadataPaneValues(topFrame, testData.md);
        }

        // Check that it is present in unset fields
        for (FilesTestHelper.PMTuple testData : FilesTestHelper.randomFiles(1, md -> {
            md.setEnabled("doc.author", false);
            md.setEnabled("basic.baseURL", false);
            md.setEnabled("pdf.keywords", false);
        })) {
            new JButtonOperator(topFrame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            MetadataInfo expected = testData.md.clone();
            expected.copyOnlyEnabled(defaultMetadata);
            expected.pdf.keywords = expected.doc.title;
            checkMetadataPaneValues(topFrame, expected);
        }

    }

}
