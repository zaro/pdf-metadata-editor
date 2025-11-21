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

public class BasicExtension extends PmeExtension{
    @Override
    public int priority(){
        return  0;
    }

    @Override
    public boolean hasBatch(){
        return false;
    }

    @Override
    public boolean giveBatch(String moto, HttpResponseCallback responseCallback){
        return false;
    }

    @Override
    public boolean removeBatch(HttpResponseCallback responseCallback){ return  false; }

    @Override
    public String getBatch(){
        return "n/a";
    }

    @Override
    public void init() {

    }

    @Override
    public void initTabs(MetadataEditPaneInterface metadataEditor){

    }

    @Override
    public void initPreferencesTabs(PreferencesWindowInterface preferencesWindow) {

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

    }

    @Override
    public void onPreferencesSave(PreferencesWindowInterface preferencesWindow) {

    }

    @Override
    public BasicPdfWriter newPdfWriter() {
        return new BasicPdfWriter();
    }

    @Override
    public BasicPdfReader newPdfReader() {
        return new BasicPdfReader();
    }

    @Override
    public void initSerializer(SimpleModule module){

    }
}
