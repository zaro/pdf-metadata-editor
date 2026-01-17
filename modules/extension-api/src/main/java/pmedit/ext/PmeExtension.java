package pmedit.ext;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.xml.XmpParsingException;
import pmedit.MetadataCollection;
import pmedit.preset.PresetValues;
import pmedit.ui.ext.MetadataEditPaneInterface;
import pmedit.ui.ext.PreferencesWindowInterface;
import pmedit.util.HttpResponseCallback;

import java.io.File;
import java.io.IOException;

public abstract class PmeExtension {
    protected static PmeExtension extensionInstance;

    public static PmeExtension get() {
        if (extensionInstance == null) {
            extensionInstance = ExtensionLoader.get();
        }
        return extensionInstance;
    }

    // Configuration methods
    public abstract int priority();
    public abstract boolean hasBatch();
    public abstract boolean giveBatch(String moto, HttpResponseCallback responseCallback);
    public abstract boolean removeBatch(HttpResponseCallback responseCallback);
    public abstract String getBatch();

    // Init
    public abstract void init();
    public abstract void initTabs(MetadataEditPaneInterface metadataEditor);
    public abstract void initPreferencesTabs(PreferencesWindowInterface preferencesWindow);


    // Preferences
    public abstract void onPreferencesRefresh(PreferencesWindowInterface preferencesWindow);
    public abstract void onPreferencesSave(PreferencesWindowInterface preferencesWindow);

    // Document load/save
    public abstract void onDocumentReload(PDDocument document, File file, MetadataEditPaneInterface metadataEditor) throws XmpParsingException, IOException;
    public abstract void beforeDocumentSave(MetadataEditPaneInterface metadataEditor);
    public abstract void onDocumentSave(PDDocument document, File file, MetadataCollection metadataInfo) throws Exception;

    // Preset values
    public abstract <T extends PresetValues> void onLoadPreset(T values);
    public abstract <T extends PresetValues> void onSavePreset(T values);
    public abstract <T extends PresetValues> void onDeletePreset(T values);

    // Pdf Writer Creation
    public abstract PdfWriter newPdfWriter();
    public abstract PdfReader newPdfReader();

    // SerDes initialization
    public abstract void initSerializer(SimpleModule module);
}
