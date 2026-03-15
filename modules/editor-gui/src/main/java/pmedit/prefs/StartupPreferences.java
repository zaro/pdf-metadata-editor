package pmedit.prefs;

import pmedit.serdes.SerDeslUtils;

import java.io.File;

public class StartupPreferences {
    public static String FILENAME = "startup.json";

    public static File getFile(){
        return new File(LocalDataDir.getAppDataDir(), FILENAME);
    }
    public static StartupPreferences load(){
        File f =getFile();
        if(f.exists()){

            return SerDeslUtils.fromJsonFile(getFile(), StartupPreferences.class);
        }
        return new StartupPreferences();
    }

    public void store(){
        SerDeslUtils.toJSONFile(getFile(), this);
    }

    public String uiScale = "default";

}
