package pmedit.ext;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.xml.XmpParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.MetadataCollection;
import pmedit.preset.PresetValues;
import pmedit.ui.ext.MetadataEditPaneInterface;
import pmedit.ui.ext.PreferencesWindowInterface;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class PmeExtension {
    final static  Logger LOG = LoggerFactory.getLogger(PmeExtension.class);
    protected static PmeExtension extensionInstance;

    //All providers
    public static List<PmeExtension> providers() {
        List<PmeExtension> services = new ArrayList<>();
        ServiceLoader<PmeExtension> loader = ServiceLoader.load(PmeExtension.class);
        loader.forEach(services::add);
        return services;
    }

    public static PmeExtension get() {
        if (extensionInstance == null) {
            ServiceLoader<PmeExtension> loader = ServiceLoader.load(PmeExtension.class);
            Iterator<PmeExtension> it = loader.iterator();
            if (it.hasNext()) {
                extensionInstance = it.next();
            } else {
                RuntimeException e = new RuntimeException("Failed to find any configured extensions! Program is in non functional state!");
                LOG.error("PmeExtension.get()", e);
                throw e;
            }
        }
        return extensionInstance;
    }

    // Init
    public abstract void init();
    public abstract void initTabs(MetadataEditPaneInterface metadataEditor);
    public abstract void initPreferencesTabs(PreferencesWindowInterface preferencesWindow);


    // Preferences
    public abstract void onPreferencesRefresh(PreferencesWindowInterface preferencesWindow);

    // Document load/save
    public abstract void onDocumentReload(PDDocument document, File file, MetadataEditPaneInterface metadataEditor) throws XmpParsingException, IOException;
    public abstract void beforeDocumentSave(MetadataEditPaneInterface metadataEditor);
    public abstract void onDocumentSave(PDDocument document, File file, MetadataCollection metadataInfo) throws Exception;

    // Preset values
    public abstract <T extends PresetValues> void onLoadPreset(T values);
    public abstract <T extends PresetValues> void onSavePreset(T values);
    public abstract <T extends PresetValues> void onDeletePreset(T values);

    // Pdf Writer Creation
    public abstract PdfWriter createPdfWriter(PDDocument document);

    // SerDes initialization
    public abstract void initSerializer(SimpleModule module);
}
