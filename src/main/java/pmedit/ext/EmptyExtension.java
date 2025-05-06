package pmedit.ext;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.xml.XmpParsingException;
import pmedit.MetadataInfo;
import pmedit.preset.PresetValues;
import pmedit.ui.MetadataEditPane;
import pmedit.ui.PreferencesWindow;

import java.io.File;
import java.io.IOException;

public class EmptyExtension extends PmeExtension{
    @Override
    public void init() {

    }

    @Override
    public void initTabs(MetadataEditPane metadataEditor){

    }

    @Override
    public void initPreferencesTabs(PreferencesWindow preferencesWindow) {

    }


    @Override
    public void onDocumentReload(PDDocument document, File file, MetadataEditPane metadataEditor) throws XmpParsingException, IOException {

    }

    @Override
    public void beforeDocumentSave(MetadataEditPane metadataEditor) {

    }

    @Override
    public void onDocumentSave(PDDocument document, File file, MetadataInfo metadataInfo) {

    }

    @Override
    public <T extends PresetValues> void onLoadPreset(T values) {

    }

    @Override
    public <T extends PresetValues> void onSavePreset(T values) {

    }

    @Override
    public <T extends PresetValues> void onDeletePreset(T values) {

    }

    @Override
    public void onPreferencesRefresh(PreferencesWindow preferencesWindow) {

    }

    @Override
    public PdfWriter createPdfWriter(PDDocument document) {
        return new PdfWriter(document);
    }

    @Override
    public void initSerializer(SimpleModule module){

    }
}
