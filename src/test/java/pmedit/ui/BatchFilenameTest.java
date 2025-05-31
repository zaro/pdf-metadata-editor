package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.*;
import pmedit.CommandDescription;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.TemplateString;

import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pmedit.ui.UiTestHelpers.openFileChooser;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class BatchFilenameTest {
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
    public void testRename() throws Exception {
        selectCommand(CommandDescription.RENAME);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch rename parameters");

        String template = "{dc.creators}-{dc.title}-{file.size}.pdf";
        JComboBoxOperator renameCombo = new JComboBoxOperator(parameters);
        renameCombo.typeText(template);
        renameCombo.pressKey(KeyEvent.VK_ENTER);
        renameCombo.releaseKey(KeyEvent.VK_ENTER);

        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        TemplateString ts = new TemplateString(template);
        for(Path p: Files.list(FilesTestHelper.getTempDir().toPath()).toList()){
            MetadataInfo md = new MetadataInfo();
            md.loadFromPDF(p.toFile());
            md.loadPDFFileInfo(p.toFile());

            assertEquals(new TemplateString(template).process(md), md.file.nameWithExt, "File name incorrect after rename");
        }
    }

    @Test
    public void testExtract() throws Exception {
        selectCommand(CommandDescription.FROM_FILE_NAME);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Extract parameters");

        String template = "{dc.creators}-{dc.title}";
        JComboBoxOperator renameCombo = new JComboBoxOperator(parameters);
        renameCombo.typeText(template);
        renameCombo.pressKey(KeyEvent.VK_ENTER);
        renameCombo.releaseKey(KeyEvent.VK_ENTER);

        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(Path p: Files.list(FilesTestHelper.getTempDir().toPath()).toList()){
            MetadataInfo md = new MetadataInfo();
            md.loadFromPDF(p.toFile());
            md.loadPDFFileInfo(p.toFile());

            String[] parts = md.file.name.split("-");

            assertEquals(md.dc.creators.get(0),parts[0] + "-" +parts[1],  "Field name incorrect after rename");
            assertEquals(md.dc.title,parts[2] ,  "Field name incorrect after rename");
        }
    }

    @Test
    public void testExtractNonGreedy() throws Exception {
        selectCommand(CommandDescription.FROM_FILE_NAME);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch Extract parameters");

        String template = "{dc.creators?}-{dc.title}";
        JComboBoxOperator renameCombo = new JComboBoxOperator(parameters);
        renameCombo.typeText(template);
        renameCombo.pressKey(KeyEvent.VK_ENTER);
        renameCombo.releaseKey(KeyEvent.VK_ENTER);

        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(Path p: Files.list(FilesTestHelper.getTempDir().toPath()).toList()){
            MetadataInfo md = new MetadataInfo();
            md.loadFromPDF(p.toFile());
            md.loadPDFFileInfo(p.toFile());

            String[] parts = md.file.name.split("-");

            assertEquals(md.dc.creators.get(0),parts[0]  ,  "Field name incorrect after rename");
            assertEquals(md.dc.title,parts[1]  + "-" + parts[2] ,  "Field name incorrect after rename");
        }
    }
}
