package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.*;
import pmedit.CommandDescription;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.PDFMetadataEditBatch;
import pmedit.prefs.Preferences;
import pmedit.serdes.CsvMetadata;
import pmedit.serdes.SerDeslUtils;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pmedit.ui.UiTestHelpers.openFileChooser;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class BatchOutputDirTest  extends  BaseJemmyTest {
    List<FilesTestHelper.PMTuple> initialFiles;
    static JFrameOperator topFrame;

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.Main");
        cr.startApplication(new String[]{"batch-gui-cmdline"});
        topFrame = new JFrameOperator("Batch PDF Metadata Process");
    }

    @AfterAll
    static void tearDown() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        topFrame.getOutput().printLine("Disposing window!");
        topFrame.setVisible(false);
        topFrame.dispose();
        topFrame.waitClosed();
        topFrame.waitClosed();

    }

    @BeforeEach
    void loadFile(TestInfo testInfo) throws Exception {
        FilesTestHelper.pushTempDir(testInfo.getDisplayName().replaceFirst("\\(.*", ""));
        initialFiles = FilesTestHelper.randomFiles(3);

        new JButtonOperator(topFrame, "Clear").push();
    }

    @AfterEach
    void cleanUp() {
        new JButtonOperator(topFrame, "Start Over").push();
        FilesTestHelper.popTempDir();
        Preferences.clear();
    }

    void selectCommandAndAddFolder(CommandDescription command){
        JComboBoxOperator commandCombo = new JComboBoxOperator(topFrame);
        commandCombo.selectItem(command.description);
        new JButtonOperator(topFrame, "Add Folder").push();

        openFileChooser("Select Folder to Add", FilesTestHelper.getTempDir().getAbsoluteFile());
    }


    void checkIfLogIsSuccess(File logFile) throws IOException {
        assertTrue(logFile.exists(), "Batch output NOT log found!");
        List<String> lines = Files.readAllLines(logFile.toPath());
        assertTrue(lines.get(lines.size() - 1).contains("SUCCESSFULLY"), "Last log line is not SUCCESS");
    }

    @Test
    public void testExportWithOutputDir() throws Exception {
        selectCommandAndAddFolder(CommandDescription.CLEAR);
        new JButtonOperator(topFrame, "Select").push();
        File outDir = new File(FilesTestHelper.getTempDir(), "output");
        if(!outDir.exists()) {
            outDir.mkdirs();
        }
        openFileChooser("Select Output Folder", outDir.getAbsoluteFile());
        new JCheckBoxOperator(topFrame, "Save to File").changeSelection(false);

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            File outFile = new File(outDir, tf.file.getName());
            assertTrue(outFile.exists(), outFile.getName()  + " is missing in " + outDir);
        }
        assertFalse(new File(outDir, PDFMetadataEditBatch.BATCH_OUTPUT_LOG).exists(), "Unexpected batch output log found!");
    }

    @Test
    public void testExportWithOutputDirAndLog() throws Exception {
        selectCommandAndAddFolder(CommandDescription.CLEAR);
        new JButtonOperator(topFrame, "Select").push();
        File outDir = new File(FilesTestHelper.getTempDir(), "output");
        if(!outDir.exists()) {
            outDir.mkdirs();
        }
        openFileChooser("Select Output Folder", outDir.getAbsoluteFile());
        new JCheckBoxOperator(topFrame, "Save to File").changeSelection(true);

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            File outFile = new File(outDir, tf.file.getName());
            assertTrue(outFile.exists(), outFile.getName()  + " is missing in " + outDir);
        }
        File logFile = new File(outDir, PDFMetadataEditBatch.BATCH_OUTPUT_LOG);
        checkIfLogIsSuccess(logFile);
    }

    @Test
    public void testExportWithLogNoOutputDir() throws Exception {
        selectCommandAndAddFolder(CommandDescription.CLEAR);
        new JCheckBoxOperator(topFrame, "Save to File").changeSelection(true);

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        File logFile = new File(FilesTestHelper.getTempDir(), PDFMetadataEditBatch.BATCH_OUTPUT_LOG);
        checkIfLogIsSuccess(logFile);
    }
}
