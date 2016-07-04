package pmedit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CommandLine {
	public static class ParseError extends Exception{

		public ParseError(String string) {
			super(string);
		}
	}
	
	static Set<String> validMdNames = new MetadataInfo().asFlatMap().keySet();
	public List<String> fileList = new ArrayList<String>();
	
	public boolean noGui = System.getProperty("noGui") != null;
	public CommandDescription command;
	public BatchOperationParameters params = new BatchOperationParameters();
	public boolean batchGui = false;
	
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
			} else {
				throw new ParseError("Invalid option: " + arg);
			}
			++i;
		}
		return i;
	}
    private static final SimpleDateFormat isoDateFormat[] = new SimpleDateFormat[]{
    		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
      		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
      		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
      		new SimpleDateFormat("yyyy-MM-dd"),
    };
  
	public static Object mdFieldFromString(String id, Class<?> type, String value) throws ParseError{
		if(type.equals(String.class)){
			return value;
		}
		if(type.equals(Calendar.class) || id.equals("xmpDc.dates")){
			Date d = null;
			for(SimpleDateFormat df:isoDateFormat){
				try {
					d = df.parse(value);
				} catch (ParseException e) {
				}
			}
			if(d != null){
				  Calendar cal = Calendar.getInstance();
				  cal.setTime(d);
				  return cal;
			}
			throw new ParseError("Invalid date format: "+ value);
		}
		if(type.equals(Integer.class)){
			try{
				return Integer.parseInt(value);
			}catch(NumberFormatException e){
				throw new ParseError("Invalid inreger format: "+ value);
			}
		}
		if(type.equals(List.class)){
			return value;
		}
		return value;
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
					String value = arg.substring(eqIndex+1);
					cmdLine.params.metadata.setAppend(id, mdFieldFromString(id,cmdLine.params.metadata.getFieldType(id), value));
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
