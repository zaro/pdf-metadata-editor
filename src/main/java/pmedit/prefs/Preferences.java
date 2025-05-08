package pmedit.prefs;

import pmedit.serdes.SerDeslUtils;

import java.util.ArrayList;
import java.util.List;

public class Preferences {
    private static java.util.prefs.Preferences _prefs;

    public static java.util.prefs.Preferences getInstance() {
        if (_prefs == null) {
            System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
            _prefs = java.util.prefs.Preferences.userRoot().node("PDFMetadataEditor");
        }
        return _prefs;
    }

    public static String[] getHistoryLines(String tag, String[] defaultValue) {
        var p = getInstance();
        String json = p.get(tag + "History", "");
        if(json == null || json.isEmpty()){
            appendHistoryLines(tag, defaultValue);
            return defaultValue;
        }
        List<String> values  = SerDeslUtils.stringListFromJSON(json);
        return values.toArray(new String[0]);
    }

    public static void appendHistoryLines(String tag, String[] lines) {
        var p = getInstance();
        String json = p.get(tag + "History", "");
        List<String> values;
        if(json == null || json.isEmpty()){
            values = new ArrayList<>() ;
        } else {
            values = SerDeslUtils.stringListFromJSON(json);
        }
        for(String line: lines) {
            if (values.contains(line)) {
                return;
            }
            values.add(line);
        }
        json  = SerDeslUtils.toJSON(false, values);
        p.put(tag + "History", json);
    }

}
