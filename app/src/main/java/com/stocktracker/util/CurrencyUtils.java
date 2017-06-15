package com.stocktracker.util;

import android.util.Log;

import com.stocktracker.data.StockQuote;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 6/11/17.
 */

public class CurrencyUtils {
    private static final String TAG = "CurrencyUtils";

    private CurrencyUtils() {
        throw new IllegalArgumentException();
    }

    public static BigDecimal getTotalMarketValue(List<StockQuote> quoteList) {
        if (quoteList == null) {
            throw new IllegalArgumentException("quoteList cannot be null");
        }

        BigDecimal totalValue = null;
        for (StockQuote quote : quoteList) {
            BigDecimal price = new BigDecimal(quote.lastTradePriceOnly());
            BigDecimal quantity = new BigDecimal(quote.quantity());
            BigDecimal value = price.multiply(quantity);

            if (totalValue == null) {
                totalValue = value;
            } else {
                totalValue = totalValue.add(value);
            }
        }
        return totalValue;
    }

    /**
     * Calculate the previous market value by adding the 'change' to the 'lastTradePriceOnly' and multiply by 'quantity' for each Quote
     */
    public static BigDecimal getPreviousMarketValue(List<StockQuote> quoteList) {
        if (quoteList == null) {
            throw new IllegalArgumentException("quoteList cannot be null");
        }

        BigDecimal yesterdaysTotalValue = null;
        for (StockQuote quote : quoteList) {
            if (!Utils.isValidChangeValue(quote.change())) {
                Log.d(TAG, "getPreviousMarketValue: no change value for " + quote.symbol());
                continue;
            }
            BigDecimal price = new BigDecimal(quote.lastTradePriceOnly());
            BigDecimal quantity = new BigDecimal(quote.quantity());
            BigDecimal change = new BigDecimal(quote.change());
            BigDecimal yesterdaysValue = price.add(change.negate()).multiply(quantity);

            if (DEBUG) Log.d(TAG, "getPreviousMarketValue: " + quote.symbol()
                    + ": price=" + price
                    + ", quantity=" + quantity
                    + ", change=" + change
                    + ", yesterdaysValue=" + yesterdaysValue );

            if (yesterdaysTotalValue == null) {
                yesterdaysTotalValue = yesterdaysValue;
            } else {
                yesterdaysTotalValue = yesterdaysTotalValue.add(yesterdaysValue);
            }
        }
        return yesterdaysTotalValue;
    }

    public static String formatCurrency(String value) {
        if (value == null || !Utils.isValidChangeValue(value)) {
            return "";
        }

        return formatCurrency(new BigDecimal(value));
    }

    public static String formatCurrency(BigDecimal bd) {
        try {
//            bd.setScale(3, RoundingMode.HALF_UP);
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance();
            formatter.setNegativePrefix("-");
            formatter.setNegativeSuffix("");

            return formatter.format(bd.doubleValue());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing " + bd, e);
        }
        return "";
    }

    public static double getPercentageChange(String valueStr, String changeStr) {
        if (valueStr == null || changeStr == null) {
            return 0.0;
        }

        try {
            double change = Math.abs(Double.parseDouble(changeStr));
            double value = Double.parseDouble(valueStr);
            return change / (value - change);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing '" + valueStr + "'", e);
        }
        return 0.0;
    }

    public static String formatPercent(double d) {
//        if (DEBUG) Log.d(TAG, "formatPercent() d = [" + d + "]");

        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(2);
        String formatted = defaultFormat.format(d);

//        if (DEBUG) Log.d(TAG, "formatPercent() formatted: " + formatted);
        return formatted;
    }
}
