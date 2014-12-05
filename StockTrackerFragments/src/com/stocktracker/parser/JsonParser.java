package com.stocktracker.parser;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.log.Logger;

public class JsonParser
{
    private static final String CLASS_NAME = JsonParser.class.getSimpleName();
    
    private static final Class<?>[] SINGLE_STRING_PARAM_TYPE = { java.lang.String.class };

    private static final String[] QUOTE_FIELDS = { "Symbol", "AverageDailyVolume", "Change", "DaysLow", "DaysHigh", "YearLow", "YearHigh", "MarketCapitalization",
            "LastTradePriceOnly", "DaysRange", "Name", "Volume", "StockExchange" };
    
    private static final String YQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private JsonParser()
    {
    }

    @SuppressLint("SimpleDateFormat")
    public static QuoteResponse createResponse(JSONObject json) throws JSONException
    {
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Converting JSON to QuoteResponse.  JSON='%s'.", CLASS_NAME, "createResponse", json);
        }
        
        if(json == null)
        {
            return null;
        }

        QuoteResponse response = new QuoteResponse();
        JSONObject queryObj = json.getJSONObject("query");
        if(queryObj != null)
        {
            response.setCount(queryObj.getInt("count"));
            response.setLang(queryObj.getString("lang"));
            response.setCreated(parseCreatedDate(queryObj.getString("created")));

            JSONObject resultsObj = queryObj.getJSONObject("results");
            if(resultsObj != null)
            {
                JSONArray quoteArr = getQuoteArray(resultsObj);
                if(quoteArr != null)
                {
                    List<Quote> quotes = new ArrayList<Quote>();
                    for(int i = 0; i < quoteArr.length(); i++)
                    {
                        try
                        {
                            Quote quote = createQuote(quoteArr.getJSONObject(i));
                            quotes.add(quote);
                        }
                        catch(Throwable t)
                        {
                            Logger.error("%s.%s: Error parsing Quote object from JSON '%s'", CLASS_NAME, "createResponse", json);
                        }
                    }
                    response.setQuotes(quotes);
                }
            }
        }

        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Returning response '%s'", CLASS_NAME, "createResponse", response);
        }

        return response;
    }

    private static JSONArray getQuoteArray(JSONObject json) throws JSONException
    {
        if(json.has("quote"))
        {
            Object quote = json.get("quote");
            if(quote instanceof JSONObject)
            {
                JSONArray arr = new JSONArray();
                arr.put((JSONObject)quote);
                return arr;
            }
            else if(quote instanceof JSONArray)
            {
                return (JSONArray)quote;
            }
        }
        return null;
    }

    private static Date parseCreatedDate(String json)
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(YQL_DATE_FORMAT, Locale.getDefault());
            Date date = format.parse(json.replaceAll("Z$", "+0000"));
            return date;
        }
        catch(ParseException e)
        {
            Logger.debug("%s.%s: Error parsing created date '%s'", CLASS_NAME, "parseCreatedDate", json);
        }
        return null;
    }

    private static Quote createQuote(final JSONObject json)
    {
        Quote quote = new Quote();

        for(String field : QUOTE_FIELDS)
        {
            if(json.has(field) && !json.isNull(field))
            {
                final String methodName = "set" + field;
                java.lang.reflect.Method method = null;
                try
                {
                    if(Logger.isLoggingEnabled())
                    {
                        Logger.debug("%s.%s: Attempting to access method '%s'", CLASS_NAME, "parseCreatedDate", methodName);
                    }
                    
                    method = quote.getClass().getMethod(methodName, SINGLE_STRING_PARAM_TYPE);
                }
                catch(SecurityException e)
                {
                    Logger.error(e, "%s.%s: Error accessing method '%s' on Quote object", CLASS_NAME, "createQuote", field);
                }
                catch(NoSuchMethodException e)
                {
                    Logger.error(e, "%s.%s: Error accessing method '%s' on Quote object", CLASS_NAME, "createQuote", field);
                }

                try
                {
                    if(method == null)
                    {
                        if(Logger.isLoggingEnabled())
                        {
                            Logger.debug("%s.%s: Unable to access method '%s'", CLASS_NAME, "createQuote", methodName);
                        }
                    }
                    else
                    {
                        String value = json.getString(field);
                        if(Logger.isLoggingEnabled())
                        {
                            Logger.debug("%s.%s: Invoking method '%s' with value '%s'", CLASS_NAME, "createQuote", methodName, value);
                        }
                        method.invoke(quote, value);
                    }
                }
                catch(IllegalArgumentException e)
                {
                    Logger.error(e, "%s.%s: Error invoking method '%s' on Quote object", CLASS_NAME, "createQuote", field);
                }
                catch(IllegalAccessException e)
                {
                    Logger.error(e, "%s.%s: Error invoking method '%s' on Quote object", CLASS_NAME, "createQuote", field);
                }
                catch(InvocationTargetException e)
                {
                    Logger.error(e, "%s.%s: Error invoking method '%s' on Quote object", CLASS_NAME, "createQuote", field);
                }
                catch(JSONException e)
                {
                    Logger.error(e, "%s.%s: Error invoking method '%s' on Quote object", CLASS_NAME, "createQuote", field);
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

        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Final quote object: '%s'", CLASS_NAME, "createQuote", quote);
        }
        return quote;
    }

}
