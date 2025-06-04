package pmedit.ui.util;

import pmedit.prefs.Preferences;

public class PersistentToggleAction extends ToggleAction{
    protected String preferencesKey;
    public PersistentToggleAction(String name, String prefKey) {
        super(name, Preferences.getInstance().getBoolean(prefKey, false));
        preferencesKey = prefKey;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        Preferences.getInstance().putBoolean(preferencesKey, selected);
    }
}
