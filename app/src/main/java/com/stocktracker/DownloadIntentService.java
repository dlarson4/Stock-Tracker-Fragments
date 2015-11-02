package com.stocktracker;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.http.HttpRequestWrapper;
import com.stocktracker.http.HttpTaskResponse;
import com.stocktracker.parser.QuoteResponseStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import static com.stocktracker.BuildConfig.DEBUG;

public class DownloadIntentService extends IntentService {
    private final static String TAG = DownloadIntentService.class.getSimpleName();

    private static final String WHAT = "WHAT";
    private static final String EXTRAS = "extras";
    public static final String MESSENGER = "MESSENGER";
    private static final String QUOTE_RESPONSE = "QUOTE_RESPONSE";
    private static final String CLASS_NAME = DownloadIntentService.class.getSimpleName();
    private static final int MAX_DEBUG_CONTENT_LENGTH = 50000;

    public DownloadIntentService() {
        super(CLASS_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String url = intent.getData().toString();

        if (DEBUG) Log.d(TAG, "Download URI = " + url);

        if (url == null) {
            return;
        }

        HttpRequestWrapper httpRequest = new HttpRequestWrapper(url);
        httpRequest.execute();
        HttpTaskResponse response = httpRequest.getResponse();

        logResponse(response);

        if (response.isError()) {
            if (DEBUG) Log.d(TAG, "Server did not respond.");
            sendMessage(intent, null);
        } else {
            parseResponse(response, intent);
        }
    }

    private void sendMessage(Intent intent, QuoteResponse quoteResponse) {
        Messenger messenger = (Messenger) intent.getExtras().get(MESSENGER);

        Message message = makeReplyMessage(quoteResponse, intent.getExtras());
        message.what = intent.getExtras().getInt(WHAT);

        try {
            // Send the message/response back to the handler
            messenger.send(message);
        } catch (RemoteException e) {
            if (DEBUG) Log.d(TAG, "Error sending Messenger response from DownloadIntentService.");
        }
    }

    private Message makeReplyMessage(QuoteResponse quoteResponse, Bundle bundle) {
        Message message = Message.obtain();

        // Return the result to indicate whether the download succeeded or failed
        message.arg1 = quoteResponse == null ? Activity.RESULT_CANCELED : Activity.RESULT_OK;

        Bundle data = new Bundle();
        data.putBundle(EXTRAS, bundle);
        data.putParcelable(QUOTE_RESPONSE, quoteResponse);

        message.setData(data);
        return message;
    }

    private void parseResponse(HttpTaskResponse response, Intent intent) {
        //Messenger messenger = (Messenger) intent.getExtras().get(MESSENGER);
        QuoteResponse quoteResponse = null;

        JSONObject json;
        try {
            json = new JSONObject(response.getData());
            log(json);

            QuoteResponseStrategy parser = new QuoteResponseStrategy();
            quoteResponse = parser.parse(json);

            if (DEBUG) Log.d(TAG, "Parsed JSON = " + String.valueOf(quoteResponse));

        } catch (JSONException e) {
            if (DEBUG) Log.d(TAG, "%s.%s: Failed to parse JSON from result.");
        }

        sendMessage(intent, quoteResponse);
    }

    public static QuoteResponse getQuoteResponse(Message message) {
        Bundle data = message.getData();

        // Extract the pathname from the Bundle.
        QuoteResponse quoteResponse = data.getParcelable(QUOTE_RESPONSE);

        // Check to see if the download succeeded.
        if (message.arg1 != Activity.RESULT_OK || quoteResponse == null) {
            return null;
        } else {
            return quoteResponse;
        }
    }

    public static Bundle getExtras(Message message) {
        Bundle data = message.getData();
        return data.getBundle(EXTRAS);
    }

    public static int getWhat(Message message) {
        Bundle data = message.getData();
        return data.getInt(WHAT);
    }

    public static Intent createIntent(Context context, Uri uri, Handler downloadHandler, Bundle extras, int what) {
        Intent intent = new Intent(context, DownloadIntentService.class);
        intent.setData(uri);
        intent.putExtra(MESSENGER, new Messenger(downloadHandler));
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.putExtra(WHAT, what);
        return intent;
    }

    private void logResponse(HttpTaskResponse response) {
        if (DEBUG) Log.d(TAG, "HTTP response = " + response);

    }

    private void log(JSONObject json) {
        if (DEBUG) {
            if (json.length() < MAX_DEBUG_CONTENT_LENGTH) {
                try {
                    if (DEBUG) Log.d(TAG, "Parsed JSON: " + json.toString(4));
                } catch (JSONException e) {
                    if (DEBUG) Log.d(TAG, "Failed to parse JSON from result.");
                }
            } else {
                if (DEBUG) Log.d(TAG, "Parsed JSON is too big to display (length = " + json.length() + ")");
            }
        }
    }


}
