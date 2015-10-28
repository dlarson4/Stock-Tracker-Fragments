package com.stocktracker.http;


import android.util.Log;

import com.stocktracker.DownloadIntentService;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static com.stocktracker.BuildConfig.DEBUG;

public class HttpRequestWrapper
{
    private final static String TAG = HttpRequestWrapper.class.getSimpleName();

    private final static int HTTP_TIMEOUT_CONNECTION = 10000;
    private final static int SOCKET_TIMEOUT_CONNECTION = 10000;

    private String url = null;
    private HttpTaskResponse response;

    public HttpRequestWrapper(String url)
    {
        this.url = url;
    }

    public void execute()
    {
        if (DEBUG) Log.d(TAG, "Performing HTTP GET of URL " + this.url);


        HttpURLConnection urlConnection = null;
        try
        {
            response = doHttpGet(urlConnection);
        }
        finally
        {
            if(urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }
        if (DEBUG) Log.d(TAG, "Done.");
    }

    public HttpTaskResponse getResponse()
    {
        return this.response;
    }

    private HttpTaskResponse doHttpGet(HttpURLConnection urlConnection)
    {
        int responseCode = 0;
        try
        {
            urlConnection = (HttpURLConnection)new URL(url).openConnection();
            urlConnection.setConnectTimeout(HTTP_TIMEOUT_CONNECTION);
            urlConnection.setReadTimeout(SOCKET_TIMEOUT_CONNECTION);

            String responseMessage = urlConnection.getResponseMessage();
            responseCode = urlConnection.getResponseCode();

            if (DEBUG) Log.d(TAG, "HTTP status code = " + responseCode + ", response message = " + responseMessage);

        }
        catch(MalformedURLException e)
        {
            if (DEBUG) Log.d(TAG, "Invalid URL " + url);
            return HttpTaskResponse.INVALID_URL;
        }
        catch(IOException e)
        {
            if (DEBUG) Log.d(TAG, "I/O error communicating with server at URL = " + url);
            return HttpTaskResponse.SERVER_UNAVAILABLE;
        }

        if(responseCode != HttpTaskResponse.HTTP_OK)
        {
            return HttpTaskResponse.INTERNAL_SERVER_ERROR;
        }

        try
        {
            final String body = getResponseBody(urlConnection.getInputStream());
            return HttpTaskResponse.createSuccessResponse(body);
        }
        catch(IOException e)
        {
            if (DEBUG) Log.d(TAG, "Error reading server response.");
            return HttpTaskResponse.ERROR_READING_RESPONSE;
        }
    }

    private String getResponseBody(InputStream stream) throws IOException
    {
        InputStream input = new BufferedInputStream(stream);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOUtils.copy(input, output);
        return new String(output.toByteArray(), "UTF-8");
    }
}
