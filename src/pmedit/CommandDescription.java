package pmedit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class CommandDescription {
	protected static int regKeyCount = 1;
	
	String name;
	String description;
	String regKey;
	
	protected CommandDescription(String command, String name) {
		this.name = command;
		this.regKey = "pme."  + (regKeyCount++) + command;
		this.description = name;
	}
	public String toString(){
		return description;
	}
	
	public boolean is(String command){
		return this.name.equals(command);
	}

	public static final CommandDescription[] batchCommands = {
			new CommandDescription("edit", "Set metadata"),	
			new CommandDescription("clear", "Clear metadata"),	
			new CommandDescription("rename", "Rename files from metadata"),	
			new CommandDescription("tojson", "Extract metadata as JSON"),	
			new CommandDescription("toyaml", "Extract metadata as YAML/Text"),	
	};
	
	public static CommandDescription getBatchCommand(String command){
		for(CommandDescription c: batchCommands){
			if(c.name.equals(command)){
				return c;
			}
		}
		return null;
	}
}