package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.*;
import pmedit.CommandDescription;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.prefs.Preferences;

import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@SetSystemProperty(key = "junitTest", value = "true")
public class BatchModifyMetadataTest {
    java.util.List<FilesTestHelper.PMTuple> initialFiles;
    JFrameOperator topFrame;
    Timeouts timeouts = JemmyProperties.getCurrentTimeouts();

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
        FilesTestHelper.popTempDir();
    }

    void selectCommand(CommandDescription command){
        JComboBoxOperator commandCombo = new JComboBoxOperator(topFrame);
        commandCombo.selectItem(command.description);
    }

    @Test
    public void testSetMetadata() throws FileNotFoundException, IOException, Exception {
        selectCommand(CommandDescription.EDIT);
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
        selectCommand(CommandDescription.CLEAR);
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
        selectCommand(CommandDescription.CLEAR);
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
            FilesTestHelper.checkFileHasChangedMetadata(tf, null, md);
        }
    }

    @Test
    public void testCopyDocumentToXmp() throws FileNotFoundException, IOException, Exception {
        selectCommand(CommandDescription.DOC_TO_XMP);

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

            MetadataInfo saved = new MetadataInfo();
            saved.loadFromPDF(tf.file);
            saved.copyEnabled(md);

            FilesTestHelper.assertEquals(md, saved, true, "Xmp Fields copied from Doc differ");
        }
    }

    @Test
    public void testCopyXmpToDocument() throws FileNotFoundException, IOException, Exception {
        selectCommand(CommandDescription.XMP_TO_DOC);

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

            MetadataInfo saved = new MetadataInfo();
            saved.loadFromPDF(tf.file);
            saved.copyEnabled(md);

            FilesTestHelper.assertEquals(md, saved, true, "Doc Fields copied from Xmp differ");
        }
    }


}
