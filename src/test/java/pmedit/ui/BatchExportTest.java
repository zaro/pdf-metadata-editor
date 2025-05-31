package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.*;
import pmedit.CommandDescription;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.serdes.CsvMetadata;
import pmedit.serdes.SerDeslUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pmedit.ui.UiTestHelpers.openFileChooser;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class BatchExportTest {
    java.util.List<FilesTestHelper.PMTuple> initialFiles;
    JFrameOperator topFrame;

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.ui.BatchOperationWindow");
        cr.startApplication();
    }

    @BeforeEach
    void loadFile(TestInfo testInfo) throws Exception {
        if(topFrame == null) {
            topFrame = new JFrameOperator("Batch PDF metadata edit");
        }
        FilesTestHelper.pushTempDir(testInfo.getDisplayName().replaceFirst("\\(.*", ""));
        initialFiles = FilesTestHelper.randomFiles(3);

        new JButtonOperator(topFrame, "Clear").push();
        new JButtonOperator(topFrame, "Add Folder").push();

        openFileChooser("Select Folder to Add", FilesTestHelper.getTempDir().getAbsoluteFile());

    }

    @AfterEach
    void cleanUp() {
        new JButtonOperator(topFrame, "Start Over").push();
        FilesTestHelper.popTempDir();
    }

    void selectCommand(CommandDescription command){
        JComboBoxOperator commandCombo = new JComboBoxOperator(topFrame);
        commandCombo.selectItem(command.description);
    }


    @Test
    public void testExportCsvSingleFile() throws Exception {
        selectCommand(CommandDescription.TO_CSV);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        String outputFile = "exported-metadata";
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Single output file").push();
        new JTextFieldOperator(outPanel).setText(outputFile);
        new JCheckBoxOperator(outPanel, "Use relative paths in output").setSelected(false);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        List<MetadataInfo> mdList = CsvMetadata.readFile(new File(FilesTestHelper.getTempDir(), outputFile + ".csv"));
        for(MetadataInfo csvMd: mdList){
            MetadataInfo md = new MetadataInfo();
            File f = new File(csvMd.file.fullPath);
            md.loadFromPDF(f);
            md.loadPDFFileInfo(f);

            FilesTestHelper.assertEquals(md, csvMd, false, "CSV Exported metadata");
        }
    }

    @Test
    public void testExportCsvPerFile() throws Exception {
        selectCommand(CommandDescription.TO_CSV);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JCheckBoxOperator(outPanel, "Use relative paths in output").setSelected(true);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            List<MetadataInfo> mdList = CsvMetadata.readFile(new File(FilesTestHelper.getTempDir(), tf.md.file.name + ".csv"));
            assertEquals(1, mdList.size());
            MetadataInfo csvMd = mdList.get(0);

            MetadataInfo md = new MetadataInfo();
            File f = new File(FilesTestHelper.getTempDir(), csvMd.file.fullPath);
            md.loadFromPDF(f);
            md.loadPDFFileInfo(f);

            FilesTestHelper.assertEquals(md, csvMd, false, "CSV Exported metadata");
        }
    }

    @Test
    public void testExportJsonSingleFile() throws Exception {
        selectCommand(CommandDescription.TO_JSON);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        String outputFile = "exported-metadata";
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Single output file").push();
        new JTextFieldOperator(outPanel).setText(outputFile);
        new JCheckBoxOperator(outPanel, "Use relative paths in output").setSelected(false);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        List<MetadataInfo> mdList = SerDeslUtils.fromJSONFileAsList(new File(FilesTestHelper.getTempDir(), outputFile + ".json")).stream().map(m -> {
           MetadataInfo md = new MetadataInfo();
           md.fromFlatMap(m);
           return md;
        }).toList();
        for(MetadataInfo jsonMd: mdList){
            MetadataInfo md = new MetadataInfo();
            File f = new File(jsonMd.file.fullPath);
            md.loadFromPDF(f);
            md.loadPDFFileInfo(f);

            FilesTestHelper.assertEquals(md, jsonMd, false, "JSON Exported metadata");
        }
    }

    @Test
    public void testExportJsonPerFile() throws Exception {
        selectCommand(CommandDescription.TO_JSON);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JCheckBoxOperator(outPanel, "Use relative paths in output").setSelected(true);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            List<MetadataInfo> mdList = SerDeslUtils.fromJSONFileAsList(new File(FilesTestHelper.getTempDir(), tf.md.file.name + ".json")).stream().map(m -> {
                MetadataInfo md = new MetadataInfo();
                md.fromFlatMap(m);
                return md;
            }).toList();
            assertEquals(1, mdList.size());
            MetadataInfo jsonMd = mdList.get(0);

            MetadataInfo md = new MetadataInfo();
            File f = new File(FilesTestHelper.getTempDir(), jsonMd.file.fullPath);
            md.loadFromPDF(f);
            md.loadPDFFileInfo(f);

            FilesTestHelper.assertEquals(md, jsonMd, false, "JSON Exported metadata");
        }
    }

    @Test
    public void testExportYamlSingleFile() throws Exception {
        selectCommand(CommandDescription.TO_YAML);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        String outputFile = "exported-metadata";
        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Single output file").push();
        new JTextFieldOperator(outPanel).setText(outputFile);
        new JCheckBoxOperator(outPanel, "Use relative paths in output").setSelected(false);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        List<MetadataInfo> mdList = SerDeslUtils.fromYAMLFileAsList(new File(FilesTestHelper.getTempDir(), outputFile + ".yaml")).stream().map(m -> {
            MetadataInfo md = new MetadataInfo();
            md.fromFlatMap(m);
            return md;
        }).toList();
        for(MetadataInfo yamlMd: mdList){
            MetadataInfo md = new MetadataInfo();
            File f = new File(yamlMd.file.fullPath);
            md.loadFromPDF(f);
            md.loadPDFFileInfo(f);

            FilesTestHelper.assertEquals(md, yamlMd, false, "YAML Exported metadata");
        }
    }

    @Test
    public void testExportYamlPerFile() throws Exception {
        selectCommand(CommandDescription.TO_YAML);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Export parameters");

        ContainerOperator outPanel = UiTestHelpers.getPanelByTitle(parameters, "Output");
        new JRadioButtonOperator(outPanel, "Separate file for each input file").push();
        new JCheckBoxOperator(outPanel, "Use relative paths in output").setSelected(true);


        new JButtonOperator(parameters, "Select all").push();
        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            List<MetadataInfo> mdList = SerDeslUtils.fromYAMLFileAsList(new File(FilesTestHelper.getTempDir(), tf.md.file.name + ".yaml")).stream().map(m -> {
                MetadataInfo md = new MetadataInfo();
                md.fromFlatMap(m);
                return md;
            }).toList();
            assertEquals(1, mdList.size());
            MetadataInfo yamlMd = mdList.get(0);

            MetadataInfo md = new MetadataInfo();
            File f = new File(FilesTestHelper.getTempDir(), yamlMd.file.fullPath);
            md.loadFromPDF(f);
            md.loadPDFFileInfo(f);

            FilesTestHelper.assertEquals(md, yamlMd, false, "YAML Exported metadata");
        }
    }

}
