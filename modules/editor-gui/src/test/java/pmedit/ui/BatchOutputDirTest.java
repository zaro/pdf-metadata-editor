package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.*;
import pmedit.CommandDescription;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.PDFMetadataEditBatch;
import pmedit.prefs.Preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pmedit.ui.UiTestHelpers.openFileChooser;
import static pmedit.ui.UiTestHelpers.populateMetadataPaneValues;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@EnabledIfSystemProperty(named = "flavour" , matches ="pro")
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
        initialFiles = randomFiles(3);

        new JButtonOperator(topFrame, "Clear").push();
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        new JButtonOperator(topFrame, "Start Over").push();
//        pushButtonNoDelay(topFrame, "Start Over");

        Preferences.clear();
    }

    void selectCommandAndAddFolder(CommandDescription command){
        JComboBoxOperator commandCombo = new JComboBoxOperator(topFrame);
        commandCombo.selectItem(command.description);
        new JButtonOperator(topFrame, "Add Folder").push();

        openFileChooser("Select Folder to Add", getTempDir().getAbsoluteFile());
    }


    void checkIfLogIsSuccess(File logFile) throws IOException {
        assertTrue(logFile.exists(), "Batch output NOT log found: "+ logFile.getAbsolutePath());
        List<String> lines = Files.readAllLines(logFile.toPath());
        assertTrue(lines.get(lines.size() - 1).contains("SUCCESSFULLY"), "Last log line is not SUCCESS");
    }

    @Test
    public void testSetMetadataWithOutputDirAndLog() throws FileNotFoundException, IOException, Exception {
        selectCommandAndAddFolder(CommandDescription.EDIT);
        new JButtonOperator(topFrame, "Select").push();
        File outDir = new File(getTempDir(), "output");
        if(!outDir.exists()) {
            outDir.mkdirs();
        }
        openFileChooser("Select Output Folder", outDir.getAbsoluteFile());
        new JCheckBoxOperator(topFrame, "Save to File").changeSelection(true);

        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch set parameters");

        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        md.doc.author = "Don't mind me";
        md.basic.baseURL = "http://example.com";
        md.docEnabled.author = true;
        md.basicEnabled.baseURL = true;
        populateMetadataPaneValues(parameters, md, false);

        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            File outFile = new File(outDir, tf.file.getName());
            assertTrue(outFile.exists(), outFile.getName()  + " is missing in " + outDir);
            FilesTestHelper.checkFileHasChangedMetadata(tf, outFile, md);
        }
        File logFile = new File(outDir, PDFMetadataEditBatch.BATCH_OUTPUT_LOG);
        checkIfLogIsSuccess(logFile);
        // check there are no extra files in output dir
        assertEquals(initialFiles.size() + 1, outDir.listFiles().length, "output dir contains unexpected files");
    }

    @Test
    public void testExportWithOutputDir() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_YAML);
        new JButtonOperator(topFrame, "Select").push();
        File outDir = new File(getTempDir(), "output");
        if(!outDir.exists()) {
            outDir.mkdirs();
        }
        openFileChooser("Select Output Folder", outDir.getAbsoluteFile());
        new JCheckBoxOperator(topFrame, "Save to File").changeSelection(false);

        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            File outFile = new File(outDir, tf.file.getName().replaceAll("\\.pdf$", ".yaml"));
            assertTrue(outFile.exists(), outFile.getName()  + " is missing in " + outDir);
        }
        assertFalse(new File(outDir, PDFMetadataEditBatch.BATCH_OUTPUT_LOG).exists(), "Unexpected batch output log found!");
    }

    @Test
    public void testExportWithOutputDirAndLog() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_CSV);
        new JButtonOperator(topFrame, "Select").push();
        File outDir = new File(getTempDir(), "output");
        if(!outDir.exists()) {
            outDir.mkdirs();
        }
        openFileChooser("Select Output Folder", outDir.getAbsoluteFile());
        new JCheckBoxOperator(topFrame, "Save to File").changeSelection(true);

        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            File outFile = new File(outDir, tf.file.getName().replaceAll("\\.pdf$", ".csv"));
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

        File logFile = new File(getTempDir(), PDFMetadataEditBatch.BATCH_OUTPUT_LOG);
        checkIfLogIsSuccess(logFile);
    }
}
