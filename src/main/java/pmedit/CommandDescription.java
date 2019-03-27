package pmedit;

public class CommandDescription {
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
			new CommandDescription("fromcsv", "Set metadata from CSV file"),	
	};
	
	public static String helpMessage(int descriptionOffset) {
		StringBuilder sb = new StringBuilder();
		for(CommandDescription cd: batchCommands){
			sb.append("  ");
			sb.append(String.format("%1$-" + descriptionOffset + "s", cd.name));
			sb.append(cd.description);
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public static CommandDescription getBatchCommand(String command){
		for(CommandDescription c: batchCommands){
			if(c.name.equals(command)){
				return c;
			}
		}
		return null;
	}
}