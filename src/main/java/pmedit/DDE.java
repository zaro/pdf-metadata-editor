package pmedit;

import java.util.ArrayList;
import java.util.List;

//import com.pretty_tools.dde.DDEException;
//import com.pretty_tools.dde.server.DDEServer;

import pmedit.CommandLine.ParseError;

public class DDE {
	public static boolean launcherDDE = System.getProperty("launcherDDE") != null;

    private static final String SERVICE = "PdfMetadataEditor";
    private static final String TOPIC = "system";

    public static void init() {
//      TODO : Make it work reliably
//    	if(!OsCheck.isWindows()){
//    		return;
//    	}
//    	if(launcherDDE) {
//    		Main.logLine("DDE.init","Using launger DDE!");
//    		return;
//    	}
//	
//		try {
//		    DDEServer server = new DDEServer(SERVICE) {
//		        @Override
//		        protected boolean isTopicSupported(String topicName)
//		        {
//		            Main.logLine("DDEServer","isTopicSupported(" + topicName + ")");
//		            return TOPIC.equalsIgnoreCase(topicName);
//		        }
//		
//		        @Override
//		        protected boolean isItemSupported(String topic, String item, int uFmt)
//		        {
//		            Main.logLine("DDEServer","isItemSupported(" + topic +"," + item + "," + uFmt + ")");
//		            return true;
//		        }
//		
//		        @Override
//		        protected boolean onExecute(String command)
//		        {
//		            Main.logLine("DDEServer","onExecute(" + command + ")");
//		            if(command.toUpperCase().startsWith("ACTIVATE ")) {
//		            	activate(command.substring(9));
//		            } else {
//		            	execute(command);
//		            }
//		            return true;
//		        }
//		
//		        @Override
//		        protected boolean onPoke(String topic, String item, String data)
//		        {
//		            Main.logLine("DDEServer","onPoke(" + topic + ", " + item + ", " + data + ")");
//		
//		            return true;
//		        }
//		
//		        @Override
//		        protected boolean onPoke(String topic, String item, byte[] data, int uFmt)
//		        {
//		            Main.logLine("DDEServer","onPoke(" +topic + ", " + item + ", " + data + ", " + uFmt + ")");
//		
//		            return false; // we do not support it
//		        }
//		
//		        @Override
//		        protected String onRequest(String topic, String item)
//		        {
//		        	Main.logLine("DDEServer","onRequest(" + topic + ", " + item + ")");
//		
//		            return item;
//		        }
//		
//		        @Override
//		        protected byte[] onRequest(String topic, String item, int uFmt)
//		        {
//		        	Main.logLine("DDEServer","onPoke(" + topic + ", " + item + ", " + uFmt + ")");
//		
//		            return null; // we do not support it
//		        }
//			};
//			server.start();
//		} catch (DDEException e) {
//			e.printStackTrace();
//        	Main.logLine("DDEServer Exception", e.toString());
//		} catch (java.lang.UnsatisfiedLinkError e){
//			e.printStackTrace();
//        	Main.logLine("DDEServer Exception", e.toString());
//		}
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
