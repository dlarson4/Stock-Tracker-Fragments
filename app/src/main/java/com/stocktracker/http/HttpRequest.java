package com.stocktracker.http;

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import static com.stocktracker.BuildConfig.DEBUG;

public class HttpRequest {
    private final static String TAG = HttpRequest.class.getSimpleName();

    private String url = null;
    private Map<String, String> params;

    public HttpRequest(String url, Map<String, String> params) {
        this.url = url;
        this.params = params;
    }

    public HttpResponse doGet() {
        if (DEBUG) Log.d(TAG, "doGet: ");

        try {
            String body = new HttpClient().get(url, params);
            return HttpResponse.createSuccessResponse(body);
        } catch (MalformedURLException e) {
            if (DEBUG) Log.e(TAG, "", e);
            if (DEBUG) Log.d(TAG, "", e);
            return HttpResponse.INVALID_URL;
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "", e);
            if (DEBUG) Log.d(TAG, "", e);
            return HttpResponse.SERVER_UNAVAILABLE;
        }
    }
}
