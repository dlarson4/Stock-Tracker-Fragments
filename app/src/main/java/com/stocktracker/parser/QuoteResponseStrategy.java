package com.stocktracker.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.log.Logger;

public class QuoteResponseStrategy extends ParseStrategy<QuoteResponse> {
    private static final String CLASS_NAME = QuoteResponseStrategy.class.getSimpleName();

    @Override
    public QuoteResponse parse(JSONObject json) {
        if (Logger.isLoggingEnabled()) {
            Logger.debug("%s $s: Parse called.  JSON='%s'", CLASS_NAME, "parse", json);
        }

        try {
            return JsonParser.createResponse(json);
        } catch (JSONException e) {
            Logger.error(e, "%s.%s: Error converting JSON to QuoteResponse.  JSON='%s'.", CLASS_NAME, "parse", json);
        }
        return null;
    }
}
