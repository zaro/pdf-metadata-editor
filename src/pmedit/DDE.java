package pmedit;

import java.util.ArrayList;
import java.util.List;

public class DDE {
	
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
    		Main.executeCommand(DDE.splitCommand(command));
    	}
    }

    public static void activate(String command) {
    	Main.logLine("DDE activate:", command);
    	if( handler != null){
    		handler.ddeActivate(DDE.splitCommand(command));
    		return;
    	} else {
    		Main.executeCommand(DDE.splitCommand(command));
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
