package pmedit;

class CommandDescription {
	String name;
	String description;
	String regKey;
	
	public CommandDescription(String command, String name) {
		this.name = command;
		this.regKey = "pme." + command;
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