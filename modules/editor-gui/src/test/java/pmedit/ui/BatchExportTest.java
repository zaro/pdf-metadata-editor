package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.*;
import pmedit.CommandDescription;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.ext.PmeExtension;
import pmedit.serdes.CsvMetadata;
import pmedit.serdes.SerDeslUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pmedit.ui.UiTestHelpers.openFileChooser;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@EnabledIfSystemProperty(named = "flavour" , matches ="pro")
public class BatchExportTest extends  BaseJemmyTest {
    java.util.List<FilesTestHelper.PMTuple> initialFiles;
    static JFrameOperator topFrame;

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.ui.BatchOperationWindow");
        cr.startApplication();
        topFrame = new JFrameOperator("Batch PDF Metadata Process");
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
        if(topFrame == null) {
            topFrame = new JFrameOperator("Batch PDF Metadata Process");
        }
        initialFiles = randomFiles(3);

        new JButtonOperator(topFrame, "Clear").push();


    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        new JButtonOperator(topFrame, "Start Over").push();
    }

    void selectCommandAndAddFolder(CommandDescription command){
        JComboBoxOperator commandCombo = new JComboBoxOperator(topFrame);
        commandCombo.selectItem(command.description);
        new JButtonOperator(topFrame, "Add Folder").push();

        openFileChooser("Select Folder to Add", getTempDir().getAbsoluteFile());
        assertEquals(getTempDir().getAbsolutePath(), new JTextPaneOperator(topFrame, 0).getText().stripTrailing());
    }


    @Test
    public void testExportCsvSingleFile() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_CSV);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        String outputFile = "exported-metadata";
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Single output file").push();
        new JTextFieldOperator(outPanel).setText(outputFile);
        new JCheckBoxOperator(outPanel, "Use relative paths in output").changeSelection(false);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        List<MetadataInfo> mdList = CsvMetadata.readFile(new File(getTempDir(), outputFile + ".csv"));
        for(MetadataInfo csvMd: mdList){
            File f = new File(csvMd.file.fullPath);
            MetadataInfo md = FilesTestHelper.load(f);



            FilesTestHelper.assertEqualsAllExceptFileProps(md, csvMd, "CSV Exported metadata");
        }
    }

    @Test
    public void testExportCsvPerFile() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_CSV);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JCheckBoxOperator(outPanel, "Use relative paths in output").changeSelection(true);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            List<MetadataInfo> mdList = CsvMetadata.readFile(new File(getTempDir(), tf.md.file.name + ".csv"));
            assertEquals(1, mdList.size());
            MetadataInfo csvMd = mdList.get(0);

            File f = new File(getTempDir(), csvMd.file.fullPath);
            MetadataInfo md = FilesTestHelper.load(f);


            FilesTestHelper.assertEqualsAllExceptFileProps(md, csvMd, "CSV Exported metadata");
        }
    }

    @Test
    public void testExportJsonSingleFile() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_JSON);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        String outputFile = "exported-metadata";
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Single output file").push();
        new JTextFieldOperator(outPanel).setText(outputFile);
        new JCheckBoxOperator(outPanel, "Use relative paths in output").changeSelection(false);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        List<MetadataInfo> mdList = SerDeslUtils.fromJSONFileAsList(new File(getTempDir(), outputFile + ".json")).stream().map(m -> {
           MetadataInfo md = new MetadataInfo();
           md.fromFlatMap(m);
           return md;
        }).toList();
        for(MetadataInfo jsonMd: mdList){
            File f = new File(jsonMd.file.fullPath);
            MetadataInfo md = FilesTestHelper.load(f);


            FilesTestHelper.assertEqualsAll(md, jsonMd, "JSON Exported metadata");
        }
    }

    @Test
    public void testExportJsonPerFile() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_JSON);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JCheckBoxOperator(outPanel, "Use relative paths in output").changeSelection(true);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            List<MetadataInfo> mdList = SerDeslUtils.fromJSONFileAsList(new File(getTempDir(), tf.md.file.name + ".json")).stream().map(m -> {
                MetadataInfo md = new MetadataInfo();
                md.fromFlatMap(m);
                return md;
            }).toList();
            assertEquals(1, mdList.size());
            MetadataInfo jsonMd = mdList.get(0);

            File f = new File(getTempDir(), jsonMd.file.fullPath);
            MetadataInfo md = FilesTestHelper.load(f);


            FilesTestHelper.assertEqualsAllExceptFileProps(md, jsonMd, "JSON Exported metadata");
        }
    }

    @Test
    public void testExportYamlSingleFile() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_YAML);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        String outputFile = "exported-metadata";
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Single output file").push();
        new JTextFieldOperator(outPanel).setText(outputFile);
        new JCheckBoxOperator(outPanel, "Use relative paths in output").changeSelection(false);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        List<MetadataInfo> mdList = SerDeslUtils.fromYAMLFileAsList(new File(getTempDir(), outputFile + ".yaml")).stream().map(m -> {
            MetadataInfo md = new MetadataInfo();
            md.fromFlatMap(m);
            return md;
        }).toList();
        for(MetadataInfo yamlMd: mdList){
            File f = new File(yamlMd.file.fullPath);
            MetadataInfo md = FilesTestHelper.load(f);


            FilesTestHelper.assertEqualsAll(md, yamlMd, "YAML Exported metadata");
        }
    }

    @Test
    public void testExportYamlPerFile() throws Exception {
        selectCommandAndAddFolder(CommandDescription.TO_YAML);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JCheckBoxOperator(outPanel, "Use relative paths in output").changeSelection(true);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            List<MetadataInfo> mdList = SerDeslUtils.fromYAMLFileAsList(new File(getTempDir(), tf.md.file.name + ".yaml")).stream().map(m -> {
                MetadataInfo md = new MetadataInfo();
                md.fromFlatMap(m);
                return md;
            }).toList();
            assertEquals(1, mdList.size());
            MetadataInfo yamlMd = mdList.get(0);

            File f = new File(getTempDir(), yamlMd.file.fullPath);
            MetadataInfo md = FilesTestHelper.load(f);


            FilesTestHelper.assertEqualsAllExceptFileProps(md, yamlMd, "YAML Exported metadata");
        }
    }

}
