package pmedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class RunCmd {

    public record Result(int status, String out, String err, Exception e, List<String> outLines, List<String> errLines){
        public boolean ok(){
            return status == 0;
        }
        public String errorText() {
            return e != null ? e.getMessage() : err;
        }
    }

    public static String[] tokenizeCommand(String command){
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();
        return cmdarray;
    }

    public static Result execCmd(String cmd, String[] additionalArgs) {
        String[] cmdArray = tokenizeCommand(cmd);
        String[] fullCmd = Arrays.copyOf(cmdArray, cmdArray.length + additionalArgs.length);
        System.arraycopy(additionalArgs, 0, fullCmd, cmdArray.length, additionalArgs.length);
        return execCmd(fullCmd);
    }

    public static Result execCmd(String cmd) {
        return execCmd(new String[]{cmd});
    }

    public static Result execCmd(String[] cmd){
        final Logger LOG = LoggerFactory.getLogger(RunCmd.class);

        String[] cmdWithArgs =cmd;
        if(OsCheck.isWindows()){
            String[] fullCmd = new String[3];
            fullCmd[0] = "cmd.exe";
            fullCmd[1] = "/C";
            List<String> s  =new ArrayList<>();
            for(int i = 0; i < cmd.length; i++){
                s.add("\"" + cmd[i] + "\"");
            }
            fullCmd[2]= "\"" + String.join(" ",s) + "\"";
            cmdWithArgs = fullCmd;
        }

        Runtime rt = Runtime.getRuntime();
        try {
            LOG.info("exec: {}", Arrays.toString(cmdWithArgs));
            Process proc = rt.exec(cmdWithArgs);

            //String[] commands = {"system.exe", "-get t"};

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));


            // Read the output from the command:
            List<String> outLines = new ArrayList<>();
            List<String> errLines = new ArrayList<>();
            String s;
            while ((s = stdInput.readLine()) != null) {
                LOG.trace("stdin: {}", s);
                outLines.add(s);
            }

            // Read any errors from the attempted command:
            while ((s = stdError.readLine()) != null) {
                LOG.trace("stderr: {}", s);
                errLines.add(s);
            }

            int exitVal = proc.waitFor();
            return new Result(exitVal, String.join(System.lineSeparator(),outLines), String.join(System.lineSeparator(),errLines),  null, outLines, errLines);
        }catch (IOException | InterruptedException e){
            return new Result(-1, null, null, e, null, null);
        }
    }
}
