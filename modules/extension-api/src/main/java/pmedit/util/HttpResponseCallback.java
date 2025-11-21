package pmedit.util;

public interface HttpResponseCallback {
    void onSuccess(int statusCode, String responseBody);
    void onError(String errorMessage);
}