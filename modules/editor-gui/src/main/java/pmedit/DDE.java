package pmedit;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ddeml;
import com.sun.jna.platform.win32.Ddeml.HCONV;
import com.sun.jna.platform.win32.Ddeml.HSZ;
import com.sun.jna.platform.win32.DdemlUtil.ConnectHandler;
import com.sun.jna.platform.win32.DdemlUtil.ExecuteHandler;
import com.sun.jna.platform.win32.DdemlUtil.StandaloneDdeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.CommandLine.ParseError;

import java.util.ArrayList;
import java.util.List;

public class DDE {
    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String serviceName = Version.getAppName();
    private static final String topicName = "System";
    public static boolean launcherDDE = System.getProperty("launcherDDE") != null;
    static DdeEvent handler;
    private static StandaloneDdeClient server = null;

    public static void init() {
        LOG.info("DDE::init start");

        server = new StandaloneDdeClient() {

            private final ConnectHandler connectHandler = new ConnectHandler() {
                public boolean onConnect(int transactionType, HSZ topic, HSZ service, Ddeml.CONVCONTEXT convcontext, boolean sameInstance) {
                    LOG.info("DDE::Connect handler connect topic '{}' {}\n", queryString(topic), topicName.equals(queryString(topic)));
                    return topicName.equals(queryString(topic));
                }
            };

            private final ExecuteHandler executeHandler = new ExecuteHandler() {
                public int onExecute(int transactionType, HCONV hconv, HSZ topic, Ddeml.HDDEDATA commandStringData) {
                    Pointer[] pointer = new Pointer[]{accessData(commandStringData, null)};
                    try {
                        String commandString = pointer[0].getWideString(0);
                        LOG.info("DDE::onExecute topic[{}] {}\n", queryString(topic), commandString);
                        if (queryString(topic).equals(topicName)) {
                            execute(commandString);
                            return Ddeml.DDE_FACK;
                        }
                    } finally {
                        synchronized (pointer) {
                            unaccessData(commandStringData);
                        }
                    }
                    return Ddeml.DDE_FNOTPROCESSED;
                }
            };

            {
                registerConnectHandler(connectHandler);
                registerExecuteHandler(executeHandler);
                this.initialize(Ddeml.APPCMD_FILTERINITS
                        | Ddeml.CBF_SKIP_ALLNOTIFICATIONS
                );
            }
        };

        server.nameService(serviceName, Ddeml.DNS_REGISTER);
        LOG.info("DDE Registered service name '{}'\n", serviceName);
        LOG.info("DDE: init done.");
    }

    private static void close() {
        if (server != null) {
            try {
                server.close();
            } catch (Exception ex) {
            }
        }
    }

    public static void addHandler(DdeEvent newHandler) {
        handler = newHandler;
    }

    public static void execute(String command) {
        LOG.info("DDE execute: {}", command);
        if (handler != null) {
            handler.ddeExecute(DDE.splitCommand(command));
        } else {
            try {
                Main.executeCommand(CommandLine.parse(DDE.splitCommand(command)));
            } catch (ParseError e) {
                LOG.error("execute", e);
            }
        }
    }

    public static void activate(String command) {
        LOG.info("DDE activate: {}", command);
        if (handler != null) {
            handler.ddeActivate(DDE.splitCommand(command));
        } else {
            try {
                Main.executeCommand(CommandLine.parse(DDE.splitCommand(command)));
            } catch (ParseError e) {
                LOG.error("activate", e);
            }
        }
    }

    public static List<String> splitCommand(String input) {
        int pos = 0;
        ArrayList<String> rval = new ArrayList<String>();
        while (pos < input.length()) {
            while (Character.isWhitespace(input.codePointAt(pos))) {
                pos += Character.charCount(input.codePointAt(pos));
            }
            if (input.codePointAt(pos) == '"') {
                pos += Character.charCount(input.codePointAt(pos));
                int endPos = input.indexOf('"', pos);
                if (endPos == -1) {
                    endPos = input.length();
                }
                rval.add(input.substring(pos, endPos));
                pos = endPos + 1;
            } else {
                int endPos = pos;
                while (endPos < input.length() && !Character.isWhitespace(input.codePointAt(endPos))) {
                    endPos += Character.charCount(input.codePointAt(endPos));
                }
                rval.add(input.substring(pos, endPos));
                pos = endPos;
            }
        }
        return rval;
    }

}
