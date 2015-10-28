package com.stocktracker.parser;

import android.annotation.SuppressLint;
import android.util.Log;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.stocktracker.BuildConfig.DEBUG;

public class JsonParser {
    private final static String TAG = JsonParser.class.getSimpleName();

    private static final Class<?>[] SINGLE_STRING_PARAM_TYPE = {String.class};

    private static final String[] QUOTE_FIELDS = {"Symbol", "AverageDailyVolume", "Change", "DaysLow", "DaysHigh", "YearLow", "YearHigh", "MarketCapitalization",
            "LastTradePriceOnly", "DaysRange", "Name", "Volume", "StockExchange"};

    private static final String YQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private JsonParser() {
    }

    @SuppressLint("SimpleDateFormat")
    public static QuoteResponse createResponse(JSONObject json) throws JSONException {
        if (DEBUG) Log.d(TAG, "Converting JSON to QuoteResponse.  JSON=" + json);

        if (json == null) {
            return null;
        }

        QuoteResponse response = new QuoteResponse();
        JSONObject queryObj = json.getJSONObject("query");
        if (queryObj != null) {
            response.setCount(queryObj.getInt("count"));
            response.setLang(queryObj.getString("lang"));
            response.setCreated(parseCreatedDate(queryObj.getString("created")));

            JSONObject resultsObj = queryObj.getJSONObject("results");
            if (resultsObj != null) {
                JSONArray quoteArr = getQuoteArray(resultsObj);
                if (quoteArr != null) {
                    List<Quote> quotes = new ArrayList<Quote>();
                    for (int i = 0; i < quoteArr.length(); i++) {
                        try {
                            Quote quote = createQuote(quoteArr.getJSONObject(i));
                            quotes.add(quote);
                        } catch (Throwable t) {
                            if (DEBUG) Log.d(TAG, "Error parsing Quote object from JSON " + json);
                        }
                    }
                    response.setQuotes(quotes);
                }
            }
        }

        if (DEBUG) Log.d(TAG, "Returning response " + response);
        return response;
    }

    private static JSONArray getQuoteArray(JSONObject json) throws JSONException {
        if (json.has("quote")) {
            Object quote = json.get("quote");
            if (quote instanceof JSONObject) {
                JSONArray arr = new JSONArray();
                arr.put((JSONObject) quote);
                return arr;
            } else if (quote instanceof JSONArray) {
                return (JSONArray) quote;
            }
        }
        return null;
    }

    private static Date parseCreatedDate(String json) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(YQL_DATE_FORMAT, Locale.getDefault());
            Date date = format.parse(json.replaceAll("Z$", "+0000"));
            return date;
        } catch (ParseException e) {
            if (DEBUG) Log.d(TAG, "Error parsing created date " + json);
        }
        return null;
    }

    private static Quote createQuote(final JSONObject json) {
        Quote quote = new Quote();

        for (String field : QUOTE_FIELDS) {
            if (json.has(field) && !json.isNull(field)) {
                final String methodName = "set" + field;
                java.lang.reflect.Method method = null;
                try {
                    if (DEBUG) Log.d(TAG, "Attempting to access method " + methodName);


                    method = quote.getClass().getMethod(methodName, SINGLE_STRING_PARAM_TYPE);
                } catch (SecurityException | NoSuchMethodException e) {
                    if (DEBUG) Log.d(TAG, "Error accessing method " + field + " on Quote object");
                }

                try {
                    if (method == null) {
                        if (DEBUG) Log.d(TAG, "Unable to access method " + methodName);

                    } else {
                        String value = json.getString(field);
                        if (DEBUG) Log.d(TAG, "Invoking method " + methodName + "  with value " + value);

                        method.invoke(quote, value);
                    }
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | JSONException e) {
                    if (DEBUG) Log.d(TAG, "Error invoking method " + field + " on Quote object");
                }
            }
        }

//        quote.setSymbol(json.getString("Symbol"));
//        quote.setAverageDailyVolume(json.getString("AverageDailyVolume"));
//        quote.setChange(json.getString("Change"));
//        quote.setDaysLow(json.getString("DaysLow"));
//        quote.setDaysHigh(json.getString("DaysHigh"));
//        quote.setYearLow(json.getString("YearLow"));
//        quote.setYearHigh(json.getString("YearHigh"));
//        quote.setMarketCapitalization(json.getString("MarketCapitalization"));
//        quote.setLastTradePriceOnly(json.getString("LastTradePriceOnly"));
//        quote.setDaysRange(json.getString("DaysRange"));
//        quote.setName(json.getString("Name"));
//        quote.setVolume(json.getString("Volume"));
//        quote.setStockExchange(json.getString("StockExchange"));

        if (DEBUG) Log.d(TAG, "Final quote object: " + quote);
        return quote;
    }

}
