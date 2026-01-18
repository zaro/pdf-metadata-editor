package pmedit.preset;

import pmedit.serdes.SerDeslUtils;

import java.io.File;

public class BasicPresetStoreSerDes extends PresetStoreSerDes{
    @Override
    protected <T extends PresetValues> T deserializePreset(File file, Class<T> klass) {
        return SerDeslUtils.fromYamlFile(file,klass);
    }

    @Override
    protected <T extends PresetValues> void serializePreset(File file, T values) {
        SerDeslUtils.toYamlFile(file,values);
    }
}
