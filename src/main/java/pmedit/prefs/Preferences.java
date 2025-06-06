package pmedit.prefs;

import com.formdev.flatlaf.FlatLightLaf;
import pmedit.serdes.SerDeslUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

public class Preferences {
    private static java.util.prefs.Preferences _prefs;

    public static java.util.prefs.Preferences getInstance() {
        if (_prefs == null) {
            System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
            _prefs = java.util.prefs.Preferences.userRoot().node("PDFMetadataEditor");
        }
        return _prefs;
    }

    public static void clear(){
        try {
            getInstance().clear();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
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

    public static String getLookAndFeelClass(){
        return  getInstance().get("LookAndFeel", FlatLightLaf.class.getName());
    }

    public static void setLookAndFeelClass(String name){
        getInstance().put("LookAndFeel", name);
    }

    public record MotoBoto(String moto, long timeMs){
    }

    public static MotoBoto getMotoBoto(){
        Path f = FileSystems.getDefault().getPath(LocalDataDir.getAppDataDir(), "lic");
        String moto = "";
        long timeMs = 0;
        try {
            timeMs = Files.readAttributes(f, BasicFileAttributes.class).creationTime().toMillis();
            moto = Files.readString(f);
        } catch (IOException e) {
        }
        return new MotoBoto(moto, timeMs);
    }

    public static void setMotoBoto(String moto){
        Path f = FileSystems.getDefault().getPath(LocalDataDir.getAppDataDir(), "lic");
        MotoBoto exist = getMotoBoto();
        try {
            if(exist.moto.isEmpty() || !exist.moto.equals(moto)) {
                Files.writeString(f, moto);
            }
        } catch (IOException e) {
        }
    }

    public static void removeMotoBoto(){
        Path f = FileSystems.getDefault().getPath(LocalDataDir.getAppDataDir(), "lic");
        try {
            Files.delete(f);
        } catch (IOException e) {
        }
    }
}
