package pmedit;

public class CommandDescription {
    // File extensions
    public static final String[] PDF_FILE_EXTENSIONS = new String[]{"pdf"};
    public static final String[] CSV_FILE_EXTENSIONS = new String[]{"csv"};
    public static final String[] JSON_FILE_EXTENSIONS = new String[]{"json"};
    public static final String[] YAML_FILE_EXTENSIONS = new String[]{"yaml", "yml"};

    // Groups
    public static final CommandDescription MODIFY_METADATA_GROUP = new CommandDescription("Modify Metadata");
    public static final CommandDescription FILE_OPERATIONS_GROUP = new CommandDescription("File name operations");
    public static final CommandDescription EXPORT_GROUP = new CommandDescription("Export metadata");
    public static final CommandDescription IMPORT_GROUP = new CommandDescription("Import metadata");

    // Commands
    public static final CommandDescription EDIT = new CommandDescription("edit", "Set metadata", MODIFY_METADATA_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription CLEAR = new CommandDescription("clear", "Clear metadata", MODIFY_METADATA_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription RENAME = new CommandDescription("rename", "Rename files from metadata", FILE_OPERATIONS_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription FROM_FILE_NAME = new CommandDescription("fromfilename", "Set metadata from file names", FILE_OPERATIONS_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription FROM_JSON = new CommandDescription("fromjson", "Set metadata from JSON",IMPORT_GROUP, JSON_FILE_EXTENSIONS);
    public static final CommandDescription TO_JSON = new CommandDescription("tojson", "Export metadata as JSON",  EXPORT_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription FROM_YAML = new CommandDescription("fromyaml", "Set metadata from YAML",IMPORT_GROUP, YAML_FILE_EXTENSIONS);
    public static final CommandDescription TO_YAML = new CommandDescription("toyaml", "Export metadata as YAML",  EXPORT_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription FROM_CSV = new CommandDescription("fromcsv", "Set metadata from CSV file",IMPORT_GROUP, CSV_FILE_EXTENSIONS);
    public static final CommandDescription TO_CSV = new CommandDescription("tocsv", "Export metadata as CSV file",  EXPORT_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription XMP_TO_DOC = new CommandDescription("xmptodoc", "Copy XMP to Document metadata", MODIFY_METADATA_GROUP, PDF_FILE_EXTENSIONS);
    public static final CommandDescription DOC_TO_XMP = new CommandDescription("doctoxmp", "Copy Document to XMP metadata", MODIFY_METADATA_GROUP, PDF_FILE_EXTENSIONS);

    public static final CommandDescription[] batchCommands = {
            EDIT,
            CLEAR,
            RENAME,
            FROM_FILE_NAME,
            FROM_JSON,
            TO_JSON,
            FROM_YAML,
            TO_YAML,
            FROM_CSV,
            TO_CSV,
            XMP_TO_DOC,
            DOC_TO_XMP,
    };

    public static final CommandDescription[] batchCommandsGuiMenu = {
            MODIFY_METADATA_GROUP,
            EDIT,
            CLEAR,
            XMP_TO_DOC,
            DOC_TO_XMP,
            FILE_OPERATIONS_GROUP,
            RENAME,
            FROM_FILE_NAME,
            EXPORT_GROUP,
            TO_CSV,
            TO_JSON,
            TO_YAML,
            IMPORT_GROUP,
            FROM_CSV,
            FROM_JSON,
            FROM_YAML,
    };

    protected static int regKeyCount = 1;
    public String name;
    public String description;
    public String groupName;
    public String inGroup;
    public String[] inputFileExtensions;
    String regKey;

    protected CommandDescription(String command, String name, CommandDescription group, String[] inputFileExtensions) {
        this.name = command;
        this.regKey = "pme." + (regKeyCount++) + command;
        this.description = name;
        this.inGroup = group.groupName;
        this.inputFileExtensions = inputFileExtensions;
    }

    protected CommandDescription(String group) {
        this.groupName = group;
        this.name = "GROUP:" + group;
    }

    public static String helpMessage(int descriptionOffset) {
        StringBuilder sb = new StringBuilder();
        for (CommandDescription cd : batchCommands) {
            sb.append("  ");
            sb.append(String.format("%1$-" + descriptionOffset + "s", cd.name));
            sb.append(cd.description);
            sb.append('\n');
        }
        return sb.toString();
    }

    public static CommandDescription getBatchCommand(String command) {
        for (CommandDescription c : batchCommands) {
            if (c.name.equals(command)) {
                return c;
            }
        }
        return null;
    }

    public String toString() {
        return description;
    }

    public boolean is(String command) {
        return this.name != null  && this.name.equals(command);
    }
    public boolean is(CommandDescription o) {
        return is(o.name);
    }

    public boolean isGroup(){
        return groupName != null;
    }
    public boolean isInGroup(String g){
        return (inGroup != null && inGroup.equals(g)) || (inGroup == null && g == null);
    }
    public boolean isInGroup(CommandDescription o){
        return isInGroup(o.groupName);
    }
}