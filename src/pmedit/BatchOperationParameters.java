package pmedit;

import java.util.prefs.Preferences;

public class BatchOperationParameters {
	public MetadataInfo metadata = new MetadataInfo();
	public String renameTemplate;

	public void storeForCommand(CommandDescription command){
		if(command.is("edit")){
			return;
		}
		Preferences cmdPrefs = Main.getPreferences().node("batchParams").node(command.name);
		cmdPrefs.put("md", metadata.asPersistenceString());
		if(renameTemplate != null) {
			cmdPrefs.put("rt", renameTemplate);
		}
	}
	
	public static BatchOperationParameters loadForCommand(CommandDescription command){
		BatchOperationParameters params = new BatchOperationParameters();
		if(command.is("edit")){
			String defaultMetadataYAML = Main.getPreferences().get("defaultMetadata", null);
			if (defaultMetadataYAML != null && defaultMetadataYAML.length() > 0) {
				MetadataInfo editMetadata = new MetadataInfo();
				editMetadata.fromYAML(defaultMetadataYAML);
				editMetadata.enableOnlyNonNull();
				params.metadata = editMetadata;
				
			}			
		} else {
			Preferences cmdPrefs = Main.getPreferences().node("batchParams").node(command.name);
			String mdP = cmdPrefs.get( "md", null);
			if(mdP != null && mdP.length() > 0){
				params.metadata = MetadataInfo.fromPersistenceString(mdP);
			}
			params.renameTemplate = cmdPrefs.get("rt", null);
			if( params.renameTemplate == null && command.is("rename")){
				params.renameTemplate = Main.getPreferences().get("renameTemplate", null);
			}
		}
		
		return params;
	}
	
}
