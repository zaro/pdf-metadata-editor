package pmedit.preset;

import java.io.File;

public class PresetStoreSerDes {
    protected <T extends PresetValues> T deserializePreset(File file, Class<T> klass) {
        throw new RuntimeException("PresetStore not initialized! Calling non implemented deserializePreset()");
    }

    protected <T extends PresetValues> void serializePreset(File file, T values) {
        throw new RuntimeException("PresetStore not initialized! Calling non implemented serializePreset()");
    }
}
