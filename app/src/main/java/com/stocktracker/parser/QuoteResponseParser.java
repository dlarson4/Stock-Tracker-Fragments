package com.stocktracker.parser;

import android.util.Log;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.stocktracker.BuildConfig.DEBUG;

public class QuoteResponseParser {
    private final static String TAG = QuoteResponseParser.class.getSimpleName();

    private static final String YQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public QuoteResponse parse(JSONObject json) {
        if (DEBUG) Log.d(TAG, "parse: ");
        return createResponse(json);
    }

    private QuoteResponse createResponse(JSONObject json)  {
        if (json == null) {
            return null;
        }

        JSONObject queryObj = json.optJSONObject("query");
        if (queryObj != null) {
            JSONObject resultsObj = queryObj.optJSONObject("results");
            if (resultsObj != null) {
                JSONArray quoteArr = getQuoteArray(resultsObj);
                if (quoteArr != null) {
                    List<Quote> quotes = new ArrayList<>();
                    for (int i = 0; i < quoteArr.length(); i++) {
                        try {
                            Quote quote = createQuote(quoteArr.getJSONObject(i));
                            quotes.add(quote);
                        } catch (Throwable t) {
                            if (DEBUG) Log.d(TAG, "Error parsing Quote object from JSON " + json);
                        }
                    }

                    return new QuoteResponse.Builder()
                            .created(parseCreatedDate(queryObj.optString("created")))
                            .lang(queryObj.optString("lang"))
                            .quotes(quotes)
                            .build();
                }
            }
        }
        return null;
    }

    private JSONArray getQuoteArray(JSONObject json) {
        if (json.has("quote")) {
            Object quote = json.opt("quote");
            if (quote instanceof JSONObject) {
                JSONArray arr = new JSONArray();
                arr.put(quote);
                return arr;
            } else if (quote instanceof JSONArray) {
                return (JSONArray) quote;
            }
        }
        return null;
    }

    private Date parseCreatedDate(String json) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(YQL_DATE_FORMAT, Locale.getDefault());
            return format.parse(json.replaceAll("Z$", "+0000"));
        } catch (ParseException e) {
            if (DEBUG) Log.d(TAG, "Error parsing created date " + json);
        }
        return null;
    }

    private Quote createQuote(final JSONObject json) {
        Quote.Builder builder = new Quote.Builder();
        builder.symbol(json.optString("Symbol"));
        builder.averageDailyVolume(json.optString("AverageDailyVolume"));
        builder.change(json.optString("Change"));
        builder.daysLow(json.optString("DaysLow"));
        builder.daysHigh(json.optString("DaysHigh"));
        builder.yearLow(json.optString("YearLow"));
        builder.yearHigh(json.optString("YearHigh"));
        builder.marketCapitalization(json.optString("MarketCapitalization"));
        builder.lastTradePriceOnly(json.optString("LastTradePriceOnly"));
        builder.daysRange(json.optString("DaysRange"));
        builder.name(json.optString("Name"));
        builder.volume(json.optString("Volume"));
        builder.stockExchange(json.optString("StockExchange"));

        Quote quote = builder.build();

        if (DEBUG) Log.d(TAG, "Final quote object: " + quote);
        return quote;
    }
}
