package pmedit.ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.*;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.prefs.Preferences;
import pmedit.ui.components.PdfVersionPicker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalComboBoxIcon;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static pmedit.ui.UiTestHelpers.*;

@DisabledIfEnvironmentVariable(named = "NO_GUI_TESTS", matches = "true")
@EnabledIfSystemProperty(named = "flavour" , matches ="ext-dev")
public class MainWindowEncryptionOptionsTest extends  BaseJemmyTest  {
    FilesTestHelper.PMTuple initialFile;
    static JFrameOperator topFrame;
    static ContainerOperator actionsFrame;
    static ContainerOperator encryptionFrame;
    static JComboBoxOperator versionCombo;
    static JCheckBoxOperator encryptionCheckbox;
    static JButtonOperator encryptionButton;


    Timeouts timeouts = JemmyProperties.getCurrentTimeouts();

    @BeforeAll
    static void setUp() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        ClassReference cr = new ClassReference("pmedit.ui.MainWindow");
        cr.startApplication();
        topFrame = new JFrameOperator("Pdf Metadata Editor");
        actionsFrame = new ContainerOperator(topFrame, 3);;
        encryptionFrame = new ContainerOperator(actionsFrame, new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                if(comp instanceof JPanel p) {
                    if(p.getComponent(0) instanceof JButton b1){
                        if(b1.getText().equals("Encryption")){
                            return true;
                        };
                    }
                    if(p.getComponentCount() > 1 && p.getComponent(1) instanceof JButton b1){
                        if(b1.getText().equals("Encryption")){
                            return true;
                        };
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Pdf Version ComboBox";
            }
        });;

        versionCombo  = new JComboBoxOperator(actionsFrame, new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                if(comp instanceof PdfVersionPicker combo){
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Pdf Version ComboBox";
            }
        });
        encryptionCheckbox  = new JCheckBoxOperator(encryptionFrame, 0);
        encryptionButton = new JButtonOperator(encryptionFrame, "Encryption");
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
        initialFile = randomFiles(1, md -> {
            md.prop.version = 1.7f;
        }).get(0);

        new JButtonOperator(topFrame, "Open PDF").push();
        openFileChooser("Open", initialFile.file);
        checkMetadataPaneValues(topFrame, initialFile.md);

    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        Preferences.clear();
    }

    @Test
    public void testSaveAsVersionFile() throws FileNotFoundException, IOException, Exception {
        versionCombo.setSelectedItem(1.4f);
        JButtonOperator saveBtn =new JButtonOperator(topFrame, "Save");
        assertTrue(saveBtn.isEnabled());
        saveBtn.push();

        MetadataInfo md = FilesTestHelper.load(initialFile.file);
        assertEquals(1.4f, md.prop.version);
    }


    @Test
    public void testEnableEncryption() throws FileNotFoundException, IOException, Exception {
        String password = "pass";
        encryptionCheckbox.push();
        encryptionButton.waitComponentEnabled();
        encryptionButton.push();
        JDialogOperator parameters = new JDialogOperator("Encryption Options");

        new JTextFieldOperator(parameters, 0).setText(password);
        new JTextFieldOperator(parameters, 1).setText(password);

        new JButtonOperator(parameters, "OK").push();


        JButtonOperator saveBtn =new JButtonOperator(topFrame, "Save");
        assertTrue(saveBtn.isEnabled());
        saveBtn.push();

        assertThrowsExactly(org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException.class, () ->{
            FilesTestHelper.load(initialFile.file);
        });
        MetadataInfo md = FilesTestHelper.load(initialFile.file, password);
        assertEquals(password, md.prop.ownerPassword);
    }

}
