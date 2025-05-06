package pmedit.preset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.ext.PmeExtension;
import pmedit.prefs.LocalDataDir;
import pmedit.serdes.SerDeslUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class PresetStore {
    final static Logger logger = LoggerFactory.getLogger(PresetStore.class);
    static final File presetsDir = new File(LocalDataDir.getAppDataDir(), "presets");

    protected static Class<? extends PresetValues> presetValuesClass = PresetValues.class;

    public static Class<? extends PresetValues> getPresetValuesClass() {
        return presetValuesClass;
    }

    public static void setPresetValuesClass(Class<? extends PresetValues> presetValuesClass) {
        PresetStore.presetValuesClass = presetValuesClass;
    }

    public static <T extends PresetValues> T getPresetValuesInstance() {
        try {
            return (T) presetValuesClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected static File getPresetFile(String name ){
        return new File(presetsDir, name + ".yaml");
    }

    public static <T extends PresetValues> T loadPreset(String name){
        final File presetFile = getPresetFile(name);
        if(!presetFile.exists()){
            return null;
        }
        logger.info("Loading: " + presetFile);
        return (T) SerDeslUtils.fromYamlFile(presetFile, presetValuesClass);
    }

    public static <T extends PresetValues> void savePreset(String name, T values){
        if(!presetsDir.exists()){
            presetsDir.mkdirs();
        }
        File presetFile = getPresetFile(name);
        SerDeslUtils.toYamlFile(presetFile, values);
        logger.info("Saved: " + presetFile);
    }

    public static void deletePreset(String name){

        final File presetFile = getPresetFile(name);
        if(presetExists(name)) {
            presetFile.delete();
            logger.info("Deleted: " + presetFile);
        }
    }

    public static String[] getPresetNames(){
        if(!presetsDir.exists()){
            return new String[]{};
        }
        return Arrays.stream(Objects.requireNonNull(presetsDir.list((dir, name) -> name.endsWith(".yaml")))).map(e -> e.substring(0, e.length()- 5)).toArray(String[]::new);
    }

    public static boolean presetExists(String name){
        if(name == null || name.isEmpty()){
            return false;
        }
        final File presetFile = getPresetFile(name);
        return presetFile.exists();
    }
}
