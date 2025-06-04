package pmedit.ui.util;

import pmedit.prefs.Preferences;

import javax.swing.*;

public class PersistentRadioAction extends RadioAction{
    String preferencesKey;

    public PersistentRadioAction(String name, ButtonGroup group, String preferencesKey) {
        super(name, group, Preferences.getInstance().getBoolean(preferencesKey, false));
        this.preferencesKey = preferencesKey;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        Preferences.getInstance().putBoolean(preferencesKey, selected);
    }

}
