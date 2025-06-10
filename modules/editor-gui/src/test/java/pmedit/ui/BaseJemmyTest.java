package pmedit.ui;

import org.junit.jupiter.api.BeforeAll;
import org.netbeans.jemmy.JemmyProperties;

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

        for (String timeoutName : timeoutNames) {
            JemmyProperties.setCurrentTimeout(timeoutName, 10000);
        }
    }
}
