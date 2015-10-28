package com.stocktracker.parser;

import android.util.Log;

import com.stocktracker.data.QuoteResponse;

import org.json.JSONException;
import org.json.JSONObject;

import static com.stocktracker.BuildConfig.DEBUG;

public class QuoteResponseStrategy extends ParseStrategy<QuoteResponse> {
    private final static String TAG = QuoteResponseStrategy.class.getSimpleName();

    @Override
    public QuoteResponse parse(JSONObject json) {
        if (DEBUG) Log.d(TAG, "Parse called.  JSON=" + json);

        try {
            return JsonParser.createResponse(json);
        } catch (JSONException e) {
            if (DEBUG) Log.d(TAG, "Error converting JSON to QuoteResponse.  JSON=" + json);
        }
        return null;
    }
}
