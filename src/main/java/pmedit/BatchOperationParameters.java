package pmedit;

import pmedit.ui.RenameTemplateOptions;

import java.util.prefs.Preferences;

public class BatchOperationParameters {
    public static  final String DEFAULT_OUTPUT_FILENAME = "metadata-export";
    public MetadataInfo metadata = new MetadataInfo();
    public String renameTemplate;
    public String extractTemplate;
    public String outputFile = DEFAULT_OUTPUT_FILENAME;
    public Boolean useRelativePaths;
    public Boolean perFileExport;

    public boolean isSingleFileExport(){
        return perFileExport == null || !perFileExport;
    }

    public boolean shouldUseRelativePaths(){
        return useRelativePaths == null || useRelativePaths;
    }

    public static BatchOperationParameters loadForCommand(CommandDescription command) {
        BatchOperationParameters params = new BatchOperationParameters();
        if (command.is("edit")) {
            String defaultMetadataYAML = pmedit.prefs.Preferences.getInstance().get("defaultMetadata", null);
            if (defaultMetadataYAML != null && defaultMetadataYAML.length() > 0) {
                MetadataInfo editMetadata = new MetadataInfo();
                editMetadata.fromYAML(defaultMetadataYAML);
                editMetadata.enableOnlyNonNull();
                params.metadata = editMetadata;

            }
        } else {
            Preferences cmdPrefs = pmedit.prefs.Preferences.getInstance().node("batchParams").node(command.name);
            String mdP = cmdPrefs.get("md", null);
            if (mdP != null && mdP.length() > 0) {
                params.metadata = MetadataInfo.fromPersistenceString(mdP);
            }
            params.renameTemplate = cmdPrefs.get("rt", null);

            if (params.renameTemplate == null && command.is("rename")) {
                // If not set previously, try to initialise it from teh general preferences
                params.renameTemplate = pmedit.prefs.Preferences.getInstance().get(RenameTemplateOptions.RENAME_TEMPLATE_KEY, null);
            }
            params.extractTemplate = cmdPrefs.get("et", null);
            params.outputFile = cmdPrefs.get("of", DEFAULT_OUTPUT_FILENAME);
            String rp = cmdPrefs.get("rp", null);
            if(rp != null){
                params.useRelativePaths = rp.equals("true");
            }
            String pfe = cmdPrefs.get("pfe", null);
            if(pfe != null){
                params.perFileExport = pfe.equals("true");
            }
        }

        return params;
    }

    public void storeForCommand(CommandDescription command) {
        if (command.is("edit")) {
            return;
        }
        Preferences cmdPrefs = pmedit.prefs.Preferences.getInstance().node("batchParams").node(command.name);
        cmdPrefs.put("md", metadata.asPersistenceString());
        if (renameTemplate != null) {
            cmdPrefs.put("rt", renameTemplate);
        }
        if (extractTemplate != null) {
            cmdPrefs.put("et", extractTemplate);
        }
        if(outputFile != null){
            cmdPrefs.put("of", outputFile);
        }
        if(useRelativePaths != null){
            cmdPrefs.put("rp", useRelativePaths.toString());
        }
        if(perFileExport != null){
            cmdPrefs.put("pfe", perFileExport.toString());
        }
    }

}
