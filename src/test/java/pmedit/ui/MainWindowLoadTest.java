package pmedit.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.*;
import pmedit.*;
import pmedit.prefs.Preferences;
import pmedit.ui.preferences.DefaultsPreferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class MainWindowLoadTest {
    final int NUM_FILES = 1;

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.ui.MainWindow");
        cr.startApplication();
    }

    @AfterEach
    void cleanUp() {
        Preferences.clear();
    }

    @Test
    public void testLoadFile() throws FileNotFoundException, IOException, Exception {
        JFrameOperator frame = new JFrameOperator("Pdf Metadata Editor");
        for(FilesTestHelper.PMTuple testData : FilesTestHelper.randomFiles(NUM_FILES)) {
            new JButtonOperator(frame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            checkMetadataPaneValues(frame, testData.md);
        }
    }

    @Test
    public void testPopulateDefaultMetadata() throws FileNotFoundException, IOException, Exception {
        JFrameOperator frame = new JFrameOperator("Pdf Metadata Editor");

        new JButtonOperator(frame, "", 1).push();
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
        assertTrue(defaultMetadata.isEquivalent(md), "Default metadata saved differs");
    }

    @Test
    public void testLoadFileWithDefaults() throws FileNotFoundException, IOException, Exception {
        JFrameOperator frame = new JFrameOperator("Pdf Metadata Editor");

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
            new JButtonOperator(frame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            checkMetadataPaneValues(frame, testData.md);
        }

        // Check that it is present in unset fields
        for (FilesTestHelper.PMTuple testData : FilesTestHelper.randomFiles(1, md -> {
            md.setEnabled("doc.author", false);
            md.setEnabled("basic.baseURL", false);
            md.setEnabled("pdf.keywords", false);
        })) {
            new JButtonOperator(frame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            MetadataInfo check = testData.md.clone();
            check.copyIfEnabled(defaultMetadata);
            check.pdf.keywords = check.doc.title;
            checkMetadataPaneValues(frame, check);
        }

    }

}
