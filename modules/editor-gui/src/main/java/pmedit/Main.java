package pmedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.CommandLine.ParseError;
import pmedit.prefs.GuiPreferences;
import pmedit.prefs.LocalDataDir;
import pmedit.prefs.Preferences;
import pmedit.ui.BatchOperationWindow;
import pmedit.ui.MainWindow;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class Main {
    static {
        java.util.logging.Logger.getLogger("org.apache").setLevel(java.util.logging.Level.FINE);
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("logLevel","debug");
        boolean isCli = isCli();
        if (System.getProperty("devLog") == null) {
            System.setProperty("devLog", isCli ? "" : devLogValue());
        }
        System.setProperty("logFileName", LocalDataDir.getAppDataDir() + "log.txt");
        if (!isCli) {
            System.out.println("Logfile location:" + LocalDataDir.getAppDataDir() + "log.txt");
        }
    }
    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    protected static int batchGuiCounter = 0;
    static BlockingQueue<CommandLine> cmdQueue = new LinkedBlockingDeque<CommandLine>();
    static Map<String, BatchOperationWindow> batchInstances = new HashMap<String, BatchOperationWindow>();
    static List<MainWindow> editorInstances = new ArrayList<MainWindow>();

    public static boolean isCli() {
        return System.getProperty("noGui") != null;
    }


    private static String  devLogValue() {
        return System.getProperty("devLog", "");
    }

    public static String getBatchGuiCommand() {
        return "batch-gui-" + batchGuiCounter++;
    }

    // this must be swing worker
    public static void makeBatchWindow(final String commandName, final CommandDescription command, final List<String> fileList) {
        LOG.info("makeBatchWindow: {}", commandName);
        BatchOperationWindow bs = new BatchOperationWindow(command);
        if(fileList!= null) {
            bs.appendFiles(FileList.fileList(fileList));
        }
        bs.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                batchInstances.remove(commandName);
                maybeExit();
            }
        });
        batchInstances.put(commandName, bs);
        bs.setVisible(true);
    }

    public static void makeEditorWindow(String file) {
        LOG.info("open editor: {}", file);
        final MainWindow window = new MainWindow(file);
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                LOG.info("Received windowClosing for {}", window);
                editorInstances.remove(window);
                maybeExit();
            }
        });
        editorInstances.add(window);
    }

    protected static void executeCommandSwingWorker(final CommandLine cmdLine) {
        LOG.info("executeCommandSwingWorker: {}", cmdLine.toString());

        if (cmdLine.hasCommand()) {
            try {
                BatchOperationWindow bs = batchInstances.get(cmdLine.command.name);
                if (bs != null) {
                    bs.appendFiles(FileList.fileList(cmdLine.fileList));
                } else {
                    makeBatchWindow(cmdLine.command.name, cmdLine.command, cmdLine.fileList);
                }
            } catch (Exception e) {
                LOG.error("executeCommandSwingWorker", e);
            }
            return;
        }
        if (cmdLine.batchGui) {
            makeBatchWindow(getBatchGuiCommand(), null, cmdLine.fileList);
            return;
        }
        List<String> files = new ArrayList<String>(cmdLine.fileList);
        if (files.size() == 0) {
            files.add(null);
        }
        for (final String file : files) {
            try {
                String fileAbsPath = file != null ? new File(file).getAbsolutePath() : null;

                // If we have file, and a single open empty window, load the file in it
                if (fileAbsPath != null && editorInstances.size() == 1 && editorInstances.get(0).getCurrentFile() == null) {
                    LOG.info("executeCommand Found empty editor, using it to load : {}", fileAbsPath);
                    editorInstances.get(0).loadFile(fileAbsPath);
                    // If it is only one file to load, there is nothing more to do
                    if(files.size() == 1) {
                        return;
                    } else {
                        continue;
                    }
                }

                LOG.info("executeCommand fileName: {}", fileAbsPath);
                for (MainWindow window : editorInstances) {
                    File wFile = window.getCurrentFile();
                    boolean matched = (fileAbsPath == null && wFile == null) || (wFile != null && wFile.getAbsolutePath().equals(fileAbsPath));
                    LOG.info("check {} -> matched={}", wFile != null ? wFile.getAbsolutePath() : null, matched);
                    if (matched) {
                        if (window.getState() == JFrame.ICONIFIED) {
                            window.setState(JFrame.NORMAL);
                        }
                        window.toFront();
                        window.repaint();
                        window.reloadFile();
                        return;
                    }
                }
                makeEditorWindow(file);
            } catch (Exception e) {
                LOG.error("executeCommandSwingWorker", e);
                maybeExit();
            }
        }
    }

    public static void executeCommand(final CommandLine cmdLine) {
        LOG.debug("executeCommand: {}", cmdLine.toString());

        try {
            cmdQueue.put(cmdLine);
        } catch (InterruptedException e) {
            LOG.error("executeCommand",e);
        }
    }

    public static void maybeExit() {
        LOG.info("maybeExit() batchInstances={}, editorInstances={}, cmdQueue={}", batchInstances.size(), editorInstances.size(), cmdQueue.size());
        if (batchInstances.isEmpty() && editorInstances.isEmpty() && cmdQueue.isEmpty()) {
            LOG.info("No instances left, exiting...");
            System.exit(0);
        }
    }

    public static int numWindows() {
        return batchInstances.size() + editorInstances.size();
    }


    public static void main(final String[] args) {
        CommandLine cmdLine = null;
        try {
            cmdLine = CommandLine.parse(args);
        } catch (ParseError e) {
            LOG.error("CommandLine.ParseError", e);
            System.err.println(e.toString());
            return;
        }
        LOG.info("Parsed command line: {}", cmdLine);
        if (cmdLine.noGui) {
            MainCli.main(cmdLine);
            return;
        }

        if(OsCheck.isMacOs()){
            System.setProperty( "apple.laf.useScreenMenuBar", "true" );
            System.setProperty( "apple.awt.application.name", Version.getAppName() );
            System.setProperty( "apple.awt.application.appearance", "system" );
        }

        String lafClass = GuiPreferences.getLookAndFeelClass();
        try {
            UIManager.setLookAndFeel(lafClass);
        } catch (UnsupportedLookAndFeelException| ClassNotFoundException| InstantiationException | IllegalAccessException e) {
            LOG.error("UIManager.setLookAndFeel", e);
        }

        if( OsCheck.isLinux()  && lafClass.startsWith("com.formdev.flatlaf")) {
            // enable custom window decorations
            JFrame.setDefaultLookAndFeelDecorated( true );
            JDialog.setDefaultLookAndFeelDecorated( true );
        }

        if (OsCheck.isWindows() && WindowsSingletonApplication.isAlreadyRunning()) {
            LOG.info("WindowsSingletonApplication.isAlreadyRunning() = true");
        }

        executeCommand(cmdLine);
        if (OsCheck.isWindows()) {
            DDE.init();
            LOG.info("DDE: DONE");
        }
        CommandsExecutor commandsExecutor = new CommandsExecutor();
        commandsExecutor.execute();

        // Wait for at least on windows to open up, or the program
        // terminates without showing anything
        try {
            while (numWindows() == 0) {
                try {
                    commandsExecutor.get(50, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                }
            }
        } catch (InterruptedException| ExecutionException e) {
            LOG.error("main", e);
        }
    }


    static class CommandsExecutor extends SwingWorker<Void, CommandLine> {
        CommandsExecutor() {
            //initialize
        }

        @Override
        public Void doInBackground() {
            while (true) {
                CommandLine cmdLine;
                try {
                    cmdLine = cmdQueue.take();
                    LOG.info("publish: {}", cmdLine.toString());

                    publish(cmdLine);
                } catch (InterruptedException e) {
                    LOG.error("doInBackground", e);
                }
            }
        }

        @Override
        protected void process(List<CommandLine> chunks) {
            for (CommandLine cmdLine : chunks) {
                executeCommandSwingWorker(cmdLine);
            }
        }
    }
}
