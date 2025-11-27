package pmedit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.util.HttpResponseCallback;

import javax.swing.*;

public class LicenseHttpClientHelper {
    Logger LOG = LoggerFactory.getLogger(LicenseHttpClientHelper.class);


    public void makeAsyncPostRequest(String url, String requestBody,
                                     HttpResponseCallback responseCallback) {
        working = true;

        CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
        httpClient.start();

        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        try {
            request.setEntity(new StringEntity(requestBody));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> responseCallback.onError("Error creating request: " + e.getMessage()));
            try {
                httpClient.close();
            } catch (Exception ex) {
                LOG.error("Failed to send body", ex);
            }
            return;
        }

        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    int statusCode = response.getStatusLine().getStatusCode();

                    SwingUtilities.invokeLater(() -> {
                        responseCallback.onSuccess(statusCode, responseBody);
                        try {
                            httpClient.close();
                        } catch (Exception e) {
                            LOG.error("Failed to close client", e);
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        responseCallback.onError("Error reading response: " + e.getMessage());
                        try {
                            httpClient.close();
                        } catch (Exception ex) {
                            LOG.error("Failed to close client", ex);
                        }
                    });
                }
                working = false;
            }

            @Override
            public void failed(Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    responseCallback.onError("Request failed: " + ex.getMessage());
                    try {
                        httpClient.close();
                    } catch (Exception e) {
                        LOG.error("Failed to close client", e);
                    }
                });
                working = false;

            }

            @Override
            public void cancelled() {
                SwingUtilities.invokeLater(() -> {
                    responseCallback.onError("Request was cancelled");
                    try {
                        httpClient.close();
                    } catch (Exception e) {
                        LOG.error("Failed to close client", e);
                    }
                });
                working = false;

            }
        });
    }

    public void acquireLicense(String requestBody,
                               HttpResponseCallback responseCallback) {
        makeAsyncPostRequest(licenseServerUrl + "acquire/", requestBody, responseCallback);
    }

    public void releaseLicense(String requestBody,
                               HttpResponseCallback responseCallback) {
        makeAsyncPostRequest(licenseServerUrl + "release/", requestBody, responseCallback);
    }

    public boolean isWorking(){ return  working; }

    protected String licenseServerUrl;
    protected boolean working = false;

    public LicenseHttpClientHelper(){
        licenseServerUrl = System.getenv("PME_LICENSE_SERVER_URL");
        if (licenseServerUrl == null || licenseServerUrl.isEmpty()) {
            licenseServerUrl = "http://pdf.metadata.care/api/license/";
        }
        if(!licenseServerUrl.endsWith("/")){
            licenseServerUrl += "/";
        }
    }
}
