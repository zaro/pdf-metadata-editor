package pmedit.prefs;

public class Preferences {
    private static java.util.prefs.Preferences _prefs;

    public static java.util.prefs.Preferences getInstance() {
        if (_prefs == null) {
            System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
            _prefs = java.util.prefs.Preferences.userRoot().node("PDFMetadataEditor");
        }
        return _prefs;
    }
}
