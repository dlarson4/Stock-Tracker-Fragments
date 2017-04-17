package com.stocktracker.util;

import android.os.Build;
import android.util.Log;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;

import static com.stocktracker.BuildConfig.DEBUG;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static boolean isValidStock(QuoteResponse response) {
        if (response == null || response.getQuotes() == null || response.getQuotes().isEmpty()) {
            return false;
        }

        Quote q = response.getQuotes().get(0);
        if(q == null) {
            return false;
        }

        if(StringUtil.isBlank(q.getLastTradePriceOnly()) || "null".equals(q.getLastTradePriceOnly())) {
            return false;
        }

        if(StringUtil.isBlank(q.getStockExchange()) || "null".equals(q.getStockExchange())) {
            return false;
        }
        return true;
    }

    public static boolean isValidQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().length() < 1) {
            return false;
        }
        try {
            double q = Double.parseDouble(quantityStr);
            return q > 0;
        } catch (NumberFormatException e) {
            if(DEBUG) Log.d(TAG, "Error parsing quantity " + quantityStr + " to double.");
            return false;
        }
    }

    public static boolean isValidChangeValue(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().length() < 1) {
            return false;
        }
        try {
            double q = Float.parseFloat(quantityStr);
            return true;
        } catch (NumberFormatException e) {
            if(DEBUG) Log.d(TAG, "Error parsing change " + quantityStr + " to float.");
            return false;
        }
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


}
