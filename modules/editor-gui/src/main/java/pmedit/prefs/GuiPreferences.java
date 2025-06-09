package pmedit.prefs;

import com.formdev.flatlaf.FlatLightLaf;

public class GuiPreferences extends Preferences {
    public static String getDefaultLookAndFeelClass(){
        return  FlatLightLaf.class.getName();
    }

    public static String getLookAndFeelClass(){
        return  getInstance().get("LookAndFeel", getDefaultLookAndFeelClass());
    }

    public static boolean isLookAndFeelDark(){
        String s = getLookAndFeelClass();
        if(s.contains("Dark") || s.contains("Darcula")){
            return true;
        }
        return false;
    }

    public static void setLookAndFeelClass(String name){
        getInstance().put("LookAndFeel", name);
    }

}
