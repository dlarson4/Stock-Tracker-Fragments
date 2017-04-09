package com.stocktracker.http;

import android.util.Log;

import com.stocktracker.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 4/8/17.
 */
public class HttpClient {
    private static final String TAG = "HttpClient";

    public String get(String url) throws IOException {
        if (DEBUG) Log.d(TAG, "get() url = [" + url + "]");

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            if (DEBUG) Log.d(TAG, "get: response code = " + response.code());
            if(response.isSuccessful()) {
                return getResponseBody(response.body().byteStream());
            } else {
                return null;
            }
        }
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private String getResponseBody(InputStream stream) throws IOException {
        InputStream input = new BufferedInputStream(stream);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOUtils.copy(input, output);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
