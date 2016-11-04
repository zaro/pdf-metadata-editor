package pmedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CommandLine {
	public static class ParseError extends Exception{

		public ParseError(String string) {
			super(string);
		}
	}
	
	public static String mdFieldsHelpMessage(int lineLen, boolean markReadOnly){
		return mdFieldsHelpMessage(lineLen, "  ", "", markReadOnly);
	}
	public static String mdFieldsHelpMessage(int lineLen, String pre, String post, boolean markReadOnly){
		int maxLen=0;
		for(String s: validMdNames){
			int additionalLength =  ( markReadOnly && !MetadataInfo.keyIsWritable(s) ? 1 : 0);
			if(s.length() + additionalLength > maxLen){
				maxLen = s.length() +  post.length() + additionalLength;
			}
		}
		int ll = 0;
		StringBuilder sb = new StringBuilder();
		for(String s: validMdNames){
			sb.append(pre);
			sb.append(String.format("%1$-" + maxLen + "s",s + ( markReadOnly && !MetadataInfo.keyIsWritable(s) ? "*" : "") + post ));
			ll += maxLen + pre.length();
			if(ll >= lineLen){
				sb.append('\n');
				ll=0;
			}
		}
		if( ll != 0 ){
			sb.append('\n');			
		}
		return sb.toString();
	}
	
	static Set<String> validMdNames = new LinkedHashSet<String>(MetadataInfo.keys());
	public List<String> fileList = new ArrayList<String>();
	
	public boolean noGui = System.getProperty("noGui") != null;
	public CommandDescription command;
	public BatchOperationParameters params = new BatchOperationParameters();
	public boolean batchGui = false;
	public boolean showHelp = false;
	public String licenseEmail;
	public String licenseKey;
	
	public CommandLine(){
	}
	public CommandLine(List<String> fileList){
		this.fileList = fileList;
	}
	public CommandLine(List<String> fileList, boolean batchGui){
		this.fileList = fileList;
		this.batchGui = batchGui; 
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("CommandLine(");
		sb.append("noGui="); sb.append(noGui) ; sb.append(", ");
		sb.append("batchGui="); sb.append(batchGui) ; sb.append(", ");
		sb.append("showHelp="); sb.append(showHelp) ; sb.append(", ");
		sb.append("command="); sb.append(command != null ? command.name: "") ; sb.append(", ");
		sb.append("files=[");
		Iterator<String> it = fileList.iterator();
		while(it.hasNext()){
			sb.append(it.next()) ;
			if(it.hasNext()){
				sb.append(", ");
			}
		}
		sb.append("])");
		return sb.toString();
	}
	
	public boolean hasCommand(){
		return command != null;
	}

	protected static int processOptions(int startIndex, List<String> args, CommandLine cmdLine) throws ParseError{
		int i = startIndex;
		while( (i < args.size()) && args.get(i).startsWith("-") ){
			String arg = args.get(i).startsWith("--")? args.get(i).substring(2) : args.get(i).substring(1);
			if(arg.equalsIgnoreCase("nogui") || arg.equalsIgnoreCase("console") ){
				cmdLine.noGui = true;
			} else if(arg.equalsIgnoreCase("rt") || arg.equalsIgnoreCase("renameTemplate") ){
				if( i+1 < args.size() ){
					cmdLine.params.renameTemplate = args.get(i+1);
					++i;
				} else {
					throw new ParseError("Missing argument for renameTemplate");
				}
			} else if(arg.equalsIgnoreCase("license") ){
				if( i+1 < args.size() ){
					String ek = args.get(i+1).trim();
					int comaPos = ek.indexOf(",");
					if( comaPos > 0){
						cmdLine.licenseEmail = ek.substring(0, comaPos);
						cmdLine.licenseKey = ek.substring(comaPos+1);
					} else {
						throw new ParseError("Invalid argument for --license, must be in format '--license email,key' , found:" + ek);
					}
					++i;
				} else {
					throw new ParseError("Missing argument for license");
				}
			} else if(arg.equalsIgnoreCase("h") || arg.equalsIgnoreCase("help") ){
				cmdLine.showHelp = true;
			} else {
				throw new ParseError("Invalid option: " + arg);
			}
			++i;
		}
		return i;
	}
  
	
	public static CommandLine parse(String[] args) throws ParseError{
		return parse(Arrays.asList(args));
	}
	
	public static CommandLine parse(List<String> args) throws ParseError{
		CommandLine cmdLine = new CommandLine();
		cmdLine.params.metadata.setEnabled(false);
		int i = processOptions(0, args, cmdLine);
		if(i < args.size()){
			cmdLine.command = CommandDescription.getBatchCommand(args.get(i));
			if( cmdLine.command != null) {
				++i;
			} else if(args.get(i).matches("^batch-gui-\\w+$")){
				cmdLine.batchGui = true;
				++i;
			}
		}
		while(i < args.size() ){
			String arg = args.get(i);
			boolean enable = true;
			if(arg.charAt(0) == '!'){
				enable = false;
				arg=arg.substring(1);
			}
			int eqIndex = arg.indexOf("=");
			if(eqIndex >= 0){
				String id = arg.substring(0, eqIndex);
				if(validMdNames.contains(id)){
					String value = arg.substring(eqIndex+1).trim();
					cmdLine.params.metadata.setAppendFromString(id, value);
					cmdLine.params.metadata.setEnabled(id, enable);
				}
			} else if(validMdNames.contains(arg)){
				cmdLine.params.metadata.setEnabled(arg, enable);
			} else if(arg.equals("all")){
				cmdLine.params.metadata.setEnabled(true);
			} else if(arg.equals("none")){
				cmdLine.params.metadata.setEnabled(false);
			} else if(args.get(i).equals("--") ){
				++i;
				break;
			} else {
				break;
			}
			++i;
		}
		
		while(i<args.size()){
			cmdLine.fileList.add(args.get(i));
			++i;
		}
		return cmdLine;
	}

}
