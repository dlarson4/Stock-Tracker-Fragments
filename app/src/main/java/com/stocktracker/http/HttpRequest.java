package com.stocktracker.http;

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;

import static com.stocktracker.BuildConfig.DEBUG;

public class HttpRequest {
    private final static String TAG = HttpRequest.class.getSimpleName();

    private String url = null;

    public HttpRequest(String url) {
        this.url = url;
    }

    public HttpResponse doGet() {
        if (DEBUG) Log.d(TAG, "doGet: ");

        try {
            String body = new HttpClient().get(url);
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
