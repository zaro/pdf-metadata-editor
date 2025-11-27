package pmedit.ext;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.xml.XmpParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.BatchMan;
import pmedit.LicenseHttpClientHelper;
import pmedit.MetadataCollection;
import pmedit.OsCheck;
import pmedit.preset.PresetValues;
import pmedit.serdes.SerDeslUtils;
import pmedit.ui.ext.MetadataEditPaneInterface;
import pmedit.ui.ext.PreferencesWindowInterface;
import pmedit.ui.preferences.LicensePreferences;
import pmedit.util.HttpResponseCallback;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.prefs.Preferences;

public class ProExtension extends BasicExtension{
    Logger LOG = LoggerFactory.getLogger(ProExtension.class);

    public LicensePreferences licenseForm;
    final Preferences prefs = pmedit.prefs.Preferences.getInstance();

    protected LicenseHttpClientHelper licenseClient;


    @Override
    public int priority(){
        return  1000000;
    }

    @Override
    public boolean hasBatch(){
        return BatchMan.hasBatch();
    }

    @Override
    public boolean giveBatch(String moto, HttpResponseCallback responseCallback){
        if(licenseClient == null) {
            licenseClient = new LicenseHttpClientHelper();
        }
        if (licenseClient.isWorking()) {
            LOG.warn("License request already in progress: {}", moto);
            return false;
        }
        String[] parts =moto.split(",");
        if(parts.length!= 2){
            LOG.warn("License request invalid: {}", moto);
            return  false;
        }
        String licensedTo = parts[0];
        String licenseId = parts[1];

        String requestBody = ("{\n" +
                "    \"licenseId\": \"" + licenseId + "\",\n" +
                "    \"email\": \"" + licensedTo + "\",\n" +
                "    \"deviceId\": \"" + OsCheck.getComputerName() + "\"\n" +
                "}");
        LOG.info("Requesting license: {}", moto);
        licenseClient.acquireLicense(requestBody,
                new HttpResponseCallback() {
                    @Override
                    public void onSuccess(int statusCode, String responseBody) {
                        LOG.debug("Got license response: {} - {}", statusCode, responseBody);

                        Map<String, Object> map =  SerDeslUtils.fromJSON(responseBody);
                        if(statusCode != 200){
                            String error = (String) map.get("error");
                            if(error == null){
                                error= responseBody;
                            }
                            if(responseCallback != null){
                                responseCallback.onError(error);
                            }
                            return;
                        }
                        String licenseToken = (String) map.get("licenseToken");
                        BatchMan.giveBatch(licenseToken);
                        LOG.info("Got license token: {}", licenseToken != null ? licenseToken.substring(0, 20) : licenseToken);
                        if(responseCallback != null){
                            responseCallback.onSuccess(statusCode, responseBody);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        LOG.error("Failed to get  license token, error: {}", errorMessage);

                        if(responseCallback != null){
                            responseCallback.onError(errorMessage);
                        }
                    }
                });
        return true;
    }

    @Override
    public boolean removeBatch( HttpResponseCallback responseCallback){
        if(licenseClient == null) {
            licenseClient = new LicenseHttpClientHelper();
        }
        if (licenseClient.isWorking()) {
            LOG.warn("License operation already in progress");
            return false;
        }

        String licensedTo = pmedit.prefs.Preferences.getInstance().get("licensedTo", "");
        String licenseId = pmedit.prefs.Preferences.getInstance().get("licenseId", "");
        String deviceId= OsCheck.getComputerName();
        if(licenseId.isEmpty() || licensedTo.isEmpty()){
            LOG.error("Cannot release license: licensedTo={} licenseId={}", licensedTo, licenseId);
            return false;
        }


        String requestBody = ("{\n" +
                "    \"licenseId\": \"" + licenseId + "\",\n" +
                "    \"email\": \"" + licensedTo + "\",\n" +
                "    \"deviceId\": \"" + OsCheck.getComputerName() + "\"\n" +
                "}");
        LOG.info("Releasing license: licensedTo={} licenseId={} deviceId={}", licensedTo, licenseId, deviceId);
        licenseClient.releaseLicense(requestBody,
                new HttpResponseCallback() {
                    @Override
                    public void onSuccess(int statusCode, String responseBody) {
                        LOG.debug("Got release response: {} - {}", statusCode, responseBody);

                        BatchMan.giveBatch(null);
                        if(responseCallback != null){
                            responseCallback.onSuccess(statusCode, responseBody);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        LOG.error("Failed to get  license token, error: {}", errorMessage);

                        if(responseCallback != null){
                            responseCallback.onError(errorMessage);
                        }
                    }
                });
        return true;
    }

    @Override
    public String getBatch(){
        return BatchMan.getBatch().licensedTo();
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
