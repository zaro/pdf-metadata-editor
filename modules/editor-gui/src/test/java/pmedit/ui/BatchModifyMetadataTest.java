package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.*;
import pmedit.CommandDescription;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class BatchModifyMetadataTest  extends  BaseJemmyTest {
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
        FilesTestHelper.pushTempDir(testInfo.getDisplayName().replaceFirst("\\(.*", ""));
        initialFiles = FilesTestHelper.randomFiles(3);

    }

    @AfterEach
    void cleanUp() {
        new JButtonOperator(topFrame, "Start Over").push();
        FilesTestHelper.popTempDir();
    }

    void selectCommandAndAddFolder(CommandDescription command){
        JComboBoxOperator commandCombo = new JComboBoxOperator(topFrame);
        commandCombo.selectItem(command.description);

        new JButtonOperator(topFrame, "Add Folder").push();
        openFileChooser("Select Folder to Add", FilesTestHelper.getTempDir().getAbsoluteFile());
        assertEquals(FilesTestHelper.getTempDir().getAbsolutePath(), new JTextPaneOperator(topFrame, 0).getText().stripTrailing());
    }

    @Test
    public void testSetMetadata() throws FileNotFoundException, IOException, Exception {
        selectCommandAndAddFolder(CommandDescription.EDIT);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch set parameters");

        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        md.doc.author = "Don't mind me";
        md.basic.baseURL = "http://example.com";
        md.docEnabled.author = true;
        md.basicEnabled.baseURL = true;
        populateMetadataPaneValues(parameters, md, true);

        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            FilesTestHelper.checkFileHasChangedMetadata(tf, null, md);
        }
    }


    @Test
    public void testClearMetadata() throws FileNotFoundException, IOException, Exception {
        selectCommandAndAddFolder(CommandDescription.CLEAR);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch set parameters");

        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        md.doc.author = null;
        md.basic.baseURL = null;
        md.docEnabled.author = true;
        md.basicEnabled.baseURL = true;
        populateMetadataPaneValues(parameters, md, true);

        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            FilesTestHelper.checkFileHasChangedMetadata(tf, null, md);
        }
    }

    @Test
    public void testClearAll() throws FileNotFoundException, IOException, Exception {
        selectCommandAndAddFolder(CommandDescription.CLEAR);
        new JButtonOperator(topFrame, "Parameters").push();
        JDialogOperator parameters = new JDialogOperator("Batch set parameters");

        MetadataInfo md = new MetadataInfo();
        md.setEnabled(true);
        populateMetadataPaneValues(parameters, md, true);

        parameters.close();
        parameters.waitClosed();

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            // these two are always present in the file
            md.propEnabled.version = false;
            md.propEnabled.compression = false;
            FilesTestHelper.checkFileHasChangedMetadata(tf, null, md);
        }
    }

    @Test
    public void testCopyDocumentToXmp() throws FileNotFoundException, IOException, Exception {
        selectCommandAndAddFolder(CommandDescription.DOC_TO_XMP);

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            MetadataInfo md = new MetadataInfo();
            md.setEnabled(false);
            md.pdf.keywords = tf.md.doc.keywords;
            md.pdf.producer = tf.md.doc.producer;
            md.pdfEnabled.keywords = true;
            md.pdfEnabled.producer = true;

            md.basic.createDate = tf.md.doc.creationDate;
            md.basic.modifyDate = tf.md.doc.modificationDate;
            md.basicEnabled.createDate = true;
            md.basicEnabled.modifyDate = true;

            md.basic.creatorTool = tf.md.doc.creator;
            md.basicEnabled.creatorTool = true;

            md.dc.title = tf.md.doc.title;
            md.dc.description = tf.md.doc.subject;
            md.dc.creators = Arrays.asList(tf.md.doc.author);
            md.dcEnabled.title = true;
            md.dcEnabled.description = true;
            md.dcEnabled.creators = true;

            MetadataInfo saved = FilesTestHelper.load(tf.file);

            saved.copyEnabled(md);

            FilesTestHelper.assertEqualsOnlyEnabledExceptFile(md, saved, "Xmp Fields copied from Doc differ");
        }
    }

    @Test
    public void testCopyXmpToDocument() throws FileNotFoundException, IOException, Exception {
        selectCommandAndAddFolder(CommandDescription.XMP_TO_DOC);

        new JButtonOperator(topFrame, "Begin").push();

        new JTextPaneOperator(topFrame, "Finished successfully!");

        for(FilesTestHelper.PMTuple tf: initialFiles){
            MetadataInfo original = tf.md.clone();
            MetadataInfo md = new MetadataInfo();
            md.setEnabled(false);

            md.doc.keywords = tf.md.pdf.keywords;
            md.doc.producer = tf.md.pdf.producer;
            md.docEnabled.keywords = true;
            md.docEnabled.producer = true;

            md.doc.creationDate = tf.md.basic.createDate;
            md.doc.modificationDate = tf.md.basic.modifyDate;
            md.docEnabled.creationDate = true;
            md.docEnabled.modificationDate = true;


            md.doc.creator = tf.md.basic.creatorTool;
            md.docEnabled.creator = true;

            md.doc.title = tf.md.dc.title;
            md.doc.subject = tf.md.dc.description;

            md.doc.author = tf.md.getString("dc.creators");
            md.docEnabled.title = true;
            md.docEnabled.subject = true;
            md.docEnabled.author = true;

            MetadataInfo saved = FilesTestHelper.load(tf.file);
            saved.copyEnabled(md);

            FilesTestHelper.assertEqualsOnlyEnabledExceptFile(md, saved, "Doc Fields copied from Xmp differ");
        }
    }


}
