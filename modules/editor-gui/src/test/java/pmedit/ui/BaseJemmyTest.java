package pmedit.ui;

import org.junit.jupiter.api.BeforeAll;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import pmedit.prefs.LocalDataDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class BaseJemmyTest {
    @BeforeAll
    static void setupJemmyTimeouts() {
        setFastTimeouts();
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
        try {
            File logDir = new File(LocalDataDir.getAppDataDir()).getParentFile();
            File jemmyLogFile = new File(logDir, "jemmy.log");
            PrintStream out = new PrintStream(new FileOutputStream(jemmyLogFile));

            JemmyProperties.setCurrentOutput(new TestOut(null, out, out));
        } catch (FileNotFoundException e){
          throw  new RuntimeException(e);
        }
    }
}
