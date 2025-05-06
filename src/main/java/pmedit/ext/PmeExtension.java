package pmedit.ext;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.xml.XmpParsingException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.MetadataInfo;
import pmedit.preset.PresetValues;
import pmedit.ui.MetadataEditPane;
import pmedit.ui.PreferencesWindow;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public abstract class PmeExtension {
    final static  Logger logger = LoggerFactory.getLogger(PmeExtension.class);
    protected static PmeExtension extensionInstance;

    public static PmeExtension get() {
        if (extensionInstance == null) {
            // Configure Reflections to scan the classpath
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setScanners(new SubTypesScanner(false))
                    .setUrls(ClasspathHelper.forPackage("pmedit.ext"))
            );

            // Get all classes that extend the abstract class
            Set<Class<? extends PmeExtension>> extensions = reflections.getSubTypesOf(PmeExtension.class);
            Class<? extends PmeExtension> extension = extensions.stream().filter(c -> !c.equals(EmptyExtension.class)).findFirst().orElse(EmptyExtension.class);

            logger.info("Found extensions : {}",  extensions);

            try {
                logger.info("Load extension : {}",  extension.getName());

                extensionInstance = extension.newInstance();
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }
        return extensionInstance;
    }

    // Init
    public abstract void init();
    public abstract void initTabs(MetadataEditPane metadataEditor);
    public abstract void initPreferencesTabs(PreferencesWindow preferencesWindow);


    // Preferences
    public abstract void onPreferencesRefresh(PreferencesWindow preferencesWindow);

    // Document load/save
    public abstract void onDocumentReload(PDDocument document, File file, MetadataEditPane metadataEditor) throws XmpParsingException, IOException;
    public abstract void beforeDocumentSave(MetadataEditPane metadataEditor);
    public abstract void onDocumentSave(PDDocument document, File file, MetadataInfo metadataInfo) throws Exception;

    // Preset values
    public abstract <T extends PresetValues> void onLoadPreset(T values);
    public abstract <T extends PresetValues> void onSavePreset(T values);
    public abstract <T extends PresetValues> void onDeletePreset(T values);

    // Pdf Writer Creation
    public abstract PdfWriter createPdfWriter(PDDocument document);

    // SerDes initialization
    public abstract void initSerializer(SimpleModule module);
}
