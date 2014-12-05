package com.stocktracker.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.stocktracker.log.Logger;

public class HttpRequestWrapper
{
    private final static String CLASS_NAME = HttpRequestWrapper.class.getSimpleName();
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
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Performing HTTP GET of URL '%s'.", CLASS_NAME, "execute", this.url);
        }

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
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Done.", CLASS_NAME, "execute");
        }
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

            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: HTTP status code = '%d', response message = '%s'.", CLASS_NAME, "doHttpGet", responseCode, responseMessage);
            }
        }
        catch(MalformedURLException e)
        {
            Logger.error(e, "%s.%s: Invalid URL '%s'.", CLASS_NAME, "doHttpGet", url);
            return HttpTaskResponse.INVALID_URL;
        }
        catch(IOException e)
        {
            Logger.error(e, "%s.%s: I/O error communicating with server at URL = '%s'.", CLASS_NAME, "doHttpGet", url);
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
            Logger.error(e, "%s.%s: Error reading server response.", CLASS_NAME, "doHttpGet", url);
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
