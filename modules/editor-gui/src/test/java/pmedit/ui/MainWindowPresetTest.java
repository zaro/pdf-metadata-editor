package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.*;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.Version;
import pmedit.prefs.Preferences;
import pmedit.preset.PresetStore;
import pmedit.preset.PresetValues;
import pmedit.serdes.SerDeslUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainWindowPresetTest extends  BaseJemmyTest  {
    static  JFrameOperator topFrame;
    static ContainerOperator presetPanel;

    static String presetName;

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.ui.MainWindow");
        cr.startApplication();
        topFrame = new JFrameOperator("Pdf Metadata Editor");
        presetPanel  = getPanelByTitle(topFrame, "Preset Values");
        presetName = "test-preset";
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
    @Order(10)
    public void testSavePreset() throws  Exception {
        for(FilesTestHelper.PMTuple testData : randomFiles(1, e-> {

        })) {
            new JButtonOperator(topFrame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            new TestOut().printLine("Opened: " + testData.file);

            // Populate some data
            MetadataInfo md = new MetadataInfo();
            md.setEnabled(false);
            md.doc.author = "Don't mind me";
            md.basic.baseURL= "http://example.com";
            md.setEnabled("doc.author", true);
            md.setEnabled("basic.baseURL", true);
            populateMetadataPaneValues(topFrame, md);

            JComboBoxOperator presetNameCombo = new  JComboBoxOperator(presetPanel);
            presetNameCombo.typeText(presetName + "\n");
            new JButtonOperator(presetPanel, "Save").push();

            assertArrayEquals(new String[]{presetName}, PresetStore.getPresetNames());
            Map<String, Object> presetData = SerDeslUtils.fromYamlFile(PresetStore.getPresetFile(presetName));
            assertEquals(Version.get().getAsString(), presetData.get("version"));
            assertEquals(Version.getAppName(), presetData.get("app"));
            Map<String, Object> presetMd  = (Map<String, Object>) presetData.get("metadata");
            Map<String, Object> presetMdEnabled  = (Map<String, Object>) presetData.get("metadataEnabled");
            assertNotNull(presetMd);
            assertNotNull(presetMdEnabled);

            Map<String, Object> expected = md.asFlatMap(true);
            assertEquals(expected, presetMd);
            Map<String, Object> expectedEnabled = expected.entrySet().stream()
                    .map(e -> Map.entry(e.getKey(), true))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,       // keyMapper
                            Map.Entry::getValue,      // valueMapper
                            (existing, replacement) -> existing, // mergeFunction (in case of duplicate keys)
                            HashMap::new              // supplier (creates a new HashMap)
                    ));
            assertEquals(expectedEnabled, presetMdEnabled);
        }
    }

    @Test
    @Order(20)
    public void testLoadPreset() throws  Exception {
        for(FilesTestHelper.PMTuple testData : randomFiles(1, e-> {

        })) {
            new JButtonOperator(topFrame, "Open PDF").push();
            openFileChooser("Open", testData.file);
            new TestOut().printLine("Opened: " + testData.file);

            // Load the saved preset from previous test
            JComboBoxOperator presetNameCombo = new JComboBoxOperator(presetPanel);
            presetNameCombo.selectItem(presetName);
            new JButtonOperator(presetPanel, "Load").push();

            MetadataInfo md = new MetadataInfo();
            PresetValues values = PresetStore.loadPreset(presetName);
            md.fromFlatMap(values.metadata);
            md.setEnabled(false);
            for (String k : values.metadataEnabled.keySet()) {
                md.setEnabled(k, values.metadataEnabled.get(k));
            }
            checkMetadataPaneValues(topFrame, md, true);
        }
    }

    @Test
    @Order(30)
    public void testDeletePreset()  {
        // Load the saved preset from previous test
        JComboBoxOperator presetNameCombo = new JComboBoxOperator(presetPanel);
        presetNameCombo.selectItem(presetName);
        new JButtonOperator(presetPanel, "Delete").push();
        assertArrayEquals(new String[]{}, PresetStore.getPresetNames());
        assertEquals(1, presetNameCombo.getItemCount());
    }

}
