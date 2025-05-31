package pmedit.ui;

import org.apache.xmpbox.xml.XmpParsingException;
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pmedit.ui.UiTestHelpers.delay;
import static pmedit.ui.UiTestHelpers.openFileChooser;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class BatchImportTest {
    List<FilesTestHelper.PMTuple> initialFiles;
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

    List<MetadataInfo> getExpectedMetadata(){
        List<MetadataInfo> expected  = initialFiles.stream().map(e -> {
            MetadataInfo m = e.md.clone();
            m.doc.title = "Changed";
            Calendar now = Calendar.getInstance();
            Calendar now10 = Calendar.getInstance();
            now10.add(Calendar.SECOND, 10);
            m.dc.dates = List.of(now, now10);
            return m;
        }).toList();
        return  expected;
    }

    void processAndCheck(List<MetadataInfo> expected, File file) throws XmpParsingException, IOException {
        if(file.isDirectory()) {
            new JButtonOperator(topFrame, "Add Folder").push();

            openFileChooser("Select Folder to Add", file);
        }else{
            new JButtonOperator(topFrame, "Add File").push();

            openFileChooser("Select File to Add", file);
        }

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(MetadataInfo ed: expected){
            MetadataInfo md = new MetadataInfo();
            File f  = new File(ed.file.fullPath);
            f = f.isAbsolute() ? f : new File(FilesTestHelper.getTempDir(), ed.file.fullPath);
            md.loadFromPDF(f);
            md.loadPDFFileInfo(f);

            FilesTestHelper.assertEquals(md, ed, false, "Imported metadata");
        }
    }

    @Test
    public void testImportCsvSingleFile() throws Exception {
        selectCommand(CommandDescription.FROM_CSV);
        List<MetadataInfo> expected  = getExpectedMetadata();

        File csvFile = new File(FilesTestHelper.getTempDir(), "import.csv");
        CsvMetadata.writeFile(csvFile, (List) expected.stream().map(e ->e.asFlatStringMap(false)).toList());

        processAndCheck(expected, csvFile);
    }

    @Test
    public void testImportCsvPerFile() throws Exception {
        selectCommand(CommandDescription.FROM_CSV);
        List<MetadataInfo> expected  = getExpectedMetadata();

        for(MetadataInfo md: expected) {
            File csvFile = new File(FilesTestHelper.getTempDir(), md.file.name + ".csv");
            CsvMetadata.writeFile(csvFile, List.of(md.asFlatStringMap(false)));
        }

        processAndCheck(expected, FilesTestHelper.getTempDir());

    }

    @Test
    public void testImportJsonSingleFile() throws Exception {
        selectCommand(CommandDescription.FROM_JSON);
        List<MetadataInfo> expected  = getExpectedMetadata();

        File jsonFile = new File(FilesTestHelper.getTempDir(), "import.json");
        SerDeslUtils.toJSONFile(jsonFile, expected.stream().map(e ->e.asFlatMap(false)).toList());

        processAndCheck(expected, jsonFile);

    }

    @Test
    public void testImportJsonPerFile() throws Exception {
        selectCommand(CommandDescription.FROM_JSON);
        List<MetadataInfo> expected  = getExpectedMetadata();

        for(MetadataInfo md: expected) {
            File jsonFile = new File(FilesTestHelper.getTempDir(), md.file.name + ".json");
            SerDeslUtils.toJSONFile(jsonFile, md.asFlatMap(false));
        }

        processAndCheck(expected, FilesTestHelper.getTempDir());
    }

    @Test
    public void testImportYamlSingleFile() throws Exception {
        selectCommand(CommandDescription.FROM_YAML);
        List<MetadataInfo> expected  = getExpectedMetadata();

        File yamlFile = new File(FilesTestHelper.getTempDir(), "import.yml");
        SerDeslUtils.toJSONFile(yamlFile, expected.stream().map(e ->e.asFlatMap(false)).toList());

        processAndCheck(expected, yamlFile);
    }

    @Test
    public void testImportYamlPerFile() throws Exception {
        selectCommand(CommandDescription.FROM_YAML);
        List<MetadataInfo> expected  = getExpectedMetadata();

        for(MetadataInfo md: expected) {
            File yamlFile = new File(FilesTestHelper.getTempDir(), md.file.name + ".yaml");
            SerDeslUtils.toJSONFile(yamlFile, md.asFlatMap(false));
        }

        processAndCheck(expected, FilesTestHelper.getTempDir());
    }

}
