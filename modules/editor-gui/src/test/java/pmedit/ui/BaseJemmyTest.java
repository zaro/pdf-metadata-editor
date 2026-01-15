package pmedit.ui;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import pmedit.BaseTest;
import pmedit.FilesTestHelper;
import pmedit.prefs.LocalDataDir;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class BaseJemmyTest extends BaseTest {
    @BeforeAll
    static void setupJemmy(TestInfo testInfo) {
        System.setProperty("junitTest", "true");
        setFastTimeouts();
        String className=testInfo.getTestClass().get().getSimpleName();
        pushTempDirRoot(className);
        setJemmyLog(getTempDirRoot());
    }

    @AfterAll
    static void setupJemmyTimeouts(TestInfo testInfo) {
        popTempDirRoot();
    }

    @BeforeEach
    void setUp(TestInfo testInfo) throws Exception {
        pushTempDir(testInfo.getTestMethod().get().getName());
        setJemmyLog(getTempDir());
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        popTempDir();
    }

    private static void setFastTimeouts() {
        String[] timeoutNames = {
                "ComponentOperator.WaitComponentTimeout",
                "ComponentOperator.WaitStateTimeout",
                "DialogWaiter.WaitDialogTimeout",
                "FrameWaiter.WaitFrameTimeout",
                "WindowWaiter.WaitWindowTimeout",
                "Waiter.WaitingTime",
                "ComponentOperator.WaitComponentTimeout",
                "ComponentOperator.WaitStateTimeout",
                "DialogWaiter.WaitDialogTimeout",
                "FrameWaiter.WaitFrameTimeout",
                "WindowWaiter.WaitWindowTimeout",
                "Waiter.WaitingTime",
                // Add these popup-specific timeouts:
                "JPopupMenuOperator.WaitPopupTimeout",
                "JComboBoxOperator.WaitListTimeout",
                "JMenuOperator.WaitPopupTimeout",
                "AbstractButtonOperator.PushTimeout",
                "ComponentOperator.WaitComponentEnabledTimeout"
        };
        int jemmyTimeoutMs = 10000;
        try {
            jemmyTimeoutMs = Integer.parseInt(System.getenv("JEMMY_TIMEOUT"));
        } catch (Exception ignored){}
        for (String timeoutName : timeoutNames) {
            JemmyProperties.setCurrentTimeout(timeoutName, jemmyTimeoutMs);
        }
    }

    public static void setJemmyLog(File logDir){
        try {
            File jemmyLogFile = new File(logDir, "jemmy.log");
            PrintStream out = new PrintStream(new FileOutputStream(jemmyLogFile));

            JemmyProperties.setCurrentOutput(new TestOut(null, out, out));
        } catch (FileNotFoundException e){
          throw  new RuntimeException(e);
        }
    }

    public JButton findButtonNoDelay(JFrameOperator frame, String buttonText) {
        ComponentChooser chooser = new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                if (comp instanceof JButton) {
                    JButton button = (JButton) comp;
                    return button.getText().equals(buttonText) && button.isVisible();
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "JButton with text: " + buttonText;
            }
        };

        return (JButton) frame.findSubComponent(chooser);
    }

    public void pushButtonNoDelay(JFrameOperator frame, String buttonText){
        JButton btn  = findButtonNoDelay(frame, buttonText);
        if(btn != null) {
            new JButtonOperator(btn).push();
        }
    }
}
