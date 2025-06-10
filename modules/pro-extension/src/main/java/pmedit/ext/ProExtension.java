package pmedit.ext;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.xml.XmpParsingException;
import pmedit.BatchMan;
import pmedit.MetadataCollection;
import pmedit.preset.PresetValues;
import pmedit.ui.ext.MetadataEditPaneInterface;
import pmedit.ui.ext.PreferencesWindowInterface;
import pmedit.ui.preferences.LicensePreferences;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class ProExtension extends BasicExtension{
    public LicensePreferences licenseForm;
    final Preferences prefs = pmedit.prefs.Preferences.getInstance();

    @Override
    public int priority(){
        return  1000000;
    }

    @Override
    public boolean hasBatch(){
        return BatchMan.hasBatch();
    }

    @Override
    public boolean giveBatch(String moto){
        return BatchMan.giveBatch(moto);
    }

    @Override
    public String getBatch(){
        return BatchMan.getBatch();
    }


    @Override
    public void init() {

    }

    @Override
    public void initTabs(MetadataEditPaneInterface metadataEditor){

    }

    @Override
    public void initPreferencesTabs(PreferencesWindowInterface preferencesWindow) {
        licenseForm = new LicensePreferences();
        preferencesWindow.addPreferencesTab("About", "License", licenseForm.$$$getRootComponent$$$());
        licenseForm.init(prefs);

    }


    @Override
    public void onDocumentReload(PDDocument document, File file, MetadataEditPaneInterface metadataEditor) throws XmpParsingException, IOException {

    }

    @Override
    public void beforeDocumentSave(MetadataEditPaneInterface metadataEditor) {

    }

    @Override
    public void onDocumentSave(PDDocument document, File file, MetadataCollection metadataInfo) {

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
    public void onPreferencesRefresh(PreferencesWindowInterface preferencesWindow) {
        licenseForm.refresh();
    }
    @Override
    public void onPreferencesSave(PreferencesWindowInterface preferencesWindow){
        licenseForm.save(prefs);
    }


    @Override
    public ProPdfWriter newPdfWriter() {
        return new ProPdfWriter();
    }


}
