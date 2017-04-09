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
import com.stocktracker.http.HttpRequest;
import com.stocktracker.http.HttpResponse;
import com.stocktracker.parser.QuoteResponseParser;
import com.stocktracker.util.StringUtil;

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

    public DownloadIntentService() {
        super(CLASS_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onHandleIntent: ");

        final String url = intent.getData().toString();
        if (url == null) {
            return;
        }

        HttpResponse response = new HttpRequest(url).doGet();
        if(response.isSuccess()) {
            parseResponse(response, intent);
        } else {
            sendMessage(intent, null);
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

    private void parseResponse(HttpResponse response, Intent intent) {
        if (DEBUG) Log.d(TAG, "parseResponse: ");

        QuoteResponse quoteResponse = null;

        try {
            String responseData = response.getData();
            if(StringUtil.isBlank(responseData)) {
                if (DEBUG) Log.d(TAG, "parseResponse: response body is blank");
            } else {
                JSONObject json = new JSONObject(responseData);
                if (DEBUG) Log.d(TAG, "parseResponse: json = " + json);

                quoteResponse = new QuoteResponseParser().parse(json);
                if (DEBUG) Log.d(TAG, "parseResponse: quoteResponse = " + quoteResponse);
            }
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

}
