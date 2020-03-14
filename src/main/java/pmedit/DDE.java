package pmedit;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;

import com.sun.jna.platform.win32.Ddeml;
import com.sun.jna.platform.win32.Ddeml.CONVINFO;
import com.sun.jna.platform.win32.Ddeml.HCONV;
import com.sun.jna.platform.win32.Ddeml.HDDEDATA;
import com.sun.jna.platform.win32.Ddeml.HSZ;

import com.sun.jna.platform.win32.DdemlUtil.StandaloneDdeClient;
import com.sun.jna.platform.win32.DdemlUtil.ConnectHandler;
import com.sun.jna.platform.win32.DdemlUtil.ExecuteHandler;

import pmedit.CommandLine.ParseError;

public class DDE {
	public static boolean launcherDDE = System.getProperty("launcherDDE") != null;

    private static final String serviceName = Version.getAppName();
	private static final String topicName = "System";
	private static StandaloneDdeClient server = null;


    public static void init() {
		System.out.println("DDE::init start");

		server = new StandaloneDdeClient() {

			private final ConnectHandler connectHandler = new ConnectHandler() {
				public boolean onConnect(int transactionType, HSZ topic, HSZ service, Ddeml.CONVCONTEXT convcontext, boolean sameInstance) {
					System.out.printf("DDE::Connect handler connect topic '%s' %b\n", queryString(topic), topicName.equals(queryString(topic)));
					return topicName.equals(queryString(topic));
				}
			};

			private final ExecuteHandler executeHandler = new ExecuteHandler() {
				public int onExecute(int transactionType, HCONV hconv, HSZ topic, Ddeml.HDDEDATA commandStringData) {
					Pointer[] pointer = new Pointer[] { accessData(commandStringData, null) };
					try {
						String commandString = pointer[0].getWideString(0);
						System.out.printf("DDE::onExecute topic[%s] %s\n", queryString(topic), commandString);
						if(queryString(topic).equals(topicName)) {
							execute(commandString);
							return Ddeml.DDE_FACK;
						}
					} finally {
						synchronized(pointer) {
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
		System.out.printf("DDE Registered service name '%s'\n", serviceName);

	}
	
	private static void close() {
        if (server != null) {
            try {
                server.close();
            } catch (Exception ex) {
            }
        }
    }

	
	static DdeEvent handler;
	
	public static void addHandler(DdeEvent newHandler){
		handler = newHandler;
	}
	
    public static void execute(String command) {
    	Main.logLine("DDE execute:", command);
    	if( handler != null){
    		handler.ddeExecute(DDE.splitCommand(command));
    		return;
    	} else {
    		try {
				Main.executeCommand(CommandLine.parse(DDE.splitCommand(command)));
			} catch (ParseError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    public static void activate(String command) {
    	Main.logLine("DDE activate:", command);
    	if( handler != null){
    		handler.ddeActivate(DDE.splitCommand(command));
    		return;
    	} else {
    		try {
				Main.executeCommand(CommandLine.parse(DDE.splitCommand(command)));
			} catch (ParseError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

	public static List<String> splitCommand(String input){
		int pos = 0;
		ArrayList<String> rval = new ArrayList<String>();
		while(pos < input.length()){
			while(Character.isWhitespace(input.codePointAt(pos))){
				pos += Character.charCount(input.codePointAt(pos));
			}
			if(input.codePointAt(pos) == '"'){
				pos += Character.charCount(input.codePointAt(pos));
				int endPos = input.indexOf('"', pos);
				if( endPos == -1){
					endPos = input.length();
				}
				rval.add(input.substring(pos, endPos));
				pos = endPos + 1;
			} else {
				int endPos = pos;
				while(endPos < input.length() && !Character.isWhitespace(input.codePointAt(endPos))){
					endPos += Character.charCount(input.codePointAt(endPos));
				}
				rval.add(input.substring(pos, endPos));
				pos = endPos;
			}
		}
		return rval;
	}
    
}
