package com.stocktracker.util;

import android.content.Context;
import android.util.Log;

import com.stocktracker.R;
import com.stocktracker.data.Quote;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static com.stocktracker.BuildConfig.DEBUG;

public class FormatUtils {
    private static final String TAG = FormatUtils.class.getSimpleName();

    private FormatUtils() {
        throw new IllegalArgumentException();
    }

    /**
     * Basically determine if a value is a negative change, no change, or a positive change.
     */
    public static ChangeType getChangeType(String value) {
        if (value == null) {
            return ChangeType.NoChange;
        }

        try {
            return getChangeType(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing '" + value + "'", e);
        }
        return ChangeType.NoChange;
    }

    /**
     * Basically determine if a value is a negative change, no change, or a positive change.
     */
    public static ChangeType getChangeType(double d) {
        try {
            final int changeTypeInt = compareDouble(d);
            return ChangeType.getById(changeTypeInt);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing " + d, e);
        }
        return ChangeType.NoChange;
    }


    // http://stackoverflow.com/questions/3994531/how-to-determine-if-a-number-is-positive-or-negative-in-java#tab-top
    public static int compareDouble(double f) {
        if (f != f) {
            throw new IllegalArgumentException("NaN");
        }
        if (f == 0) {
            return 0;
        }
        f *= Double.POSITIVE_INFINITY;
        if (f == Double.POSITIVE_INFINITY) {
            return +1;
        }
        if (f == Double.NEGATIVE_INFINITY) {
            return -1;
        }

        throw new IllegalArgumentException("Unfathomed double");
    }

    public enum ChangeType {
        NoChange(0), Positive(1), Negative(-1), Unknown(-100);

        private int id;

        ChangeType(int id) {
            this.id = id;
        }

        public static ChangeType getById(int lookupId) {
            for (ChangeType c : ChangeType.values()) {
                if (c.id == lookupId) {
                    return c;
                }
            }
            return Unknown;
        }
    }

    public static String formatMarketValue(BigDecimal b) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance(Locale.US));

        // http://stackoverflow.com/questions/2056400/format-negative-amount-of-usd-with-a-minus-sign-not-brackets-java#3916098
        String symbol = formatter.getCurrency().getSymbol();
        formatter.setNegativePrefix(symbol + "-");
        formatter.setNegativeSuffix("");

        return formatter.format(b);
    }


    public static String getChangeSymbol(Context context, ChangeType changeType) {
        String changeSymbol = "";
        if (changeType == ChangeType.Positive) {
            changeSymbol = context.getString(R.string.plus);
        } else if (changeType == ChangeType.Negative) {
            changeSymbol = context.getString(R.string.minus);
        }
        return changeSymbol;
    }
}
