package com.stocktracker.util;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stocktracker.BuildConfig.DEBUG;

public class UrlBuilder {
    private final static String TAG = UrlBuilder.class.getSimpleName();

    // Console for debugging
    // https://developer.yahoo.com/yql/console/

    public final static String YQL_URL = "http://query.yahooapis.com/v1/public/yql";
    private final static String YQL_STOCK_INFO_QUERY = "select * from yahoo.finance.quote where symbol in (";

    private static Map<String, String> baseParams = new HashMap<>();

    static {

        baseParams.put("format", "json");
        baseParams.put("env", "store://datatables.org/alltableswithkeys");
    }

    public static Map<String, String> getStockQuoteParams(List<String> stockSymbols) {
        if(stockSymbols == null || stockSymbols.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        String query = buildQuery(YQL_STOCK_INFO_QUERY, stockSymbols);
        if (DEBUG) Log.d(TAG, "getStockQuoteParams: query = " + query);

        Map<String, String> params = new HashMap<>();
        params.putAll(baseParams);
        params.put("q", query);

        return params;
    }

    public static Map<String, String> getStockQuoteParams(String stockSymbol) {
        return getStockQuoteParams(Arrays.asList(stockSymbol));
    }

    private static String buildQuery(String baseQuery, List<String> stockSymbols) {
        StringBuilder query = new StringBuilder(baseQuery);
        final int size = stockSymbols.size();
        for (int i = 0; i < size; i++) {
            query.append('"').append(stockSymbols.get(i)).append('"');
            if (i < size - 1) {
                query.append(',');
            }
        }
        query.append(')');
        return query.toString();
    }
}
