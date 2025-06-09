package pmedit.preset;

import pmedit.Version;

import java.util.Map;

public class PresetValues {
    public String version = Version.get().getAsString();
    public String app = Version.getAppName();
    public Map<String, Object> metadata;
    public Map<String, Boolean> metadataEnabled;

}
