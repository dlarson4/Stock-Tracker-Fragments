package com.stocktracker.util;

import android.content.Context;
import android.util.Log;

import com.stocktracker.data.Quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import com.stocktracker.R;

public class FormatUtils
{
    private static final String TAG = FormatUtils.class.getSimpleName();

    private static final String STOCK_CHANGE_PERCENT_FORMAT = "%0$.2f%%";

    private FormatUtils()
    {
    }

    public static String formatCurrency(String value)
    {
        if(value == null)
        {
            return "";
        }

        return formatCurrency(new BigDecimal(value));
    }
    
    public static String formatCurrency(BigDecimal bd)
    {
        try
        {
            bd.setScale(3, RoundingMode.HALF_UP);
            DecimalFormat formatter = (DecimalFormat)NumberFormat.getCurrencyInstance();
            formatter.setNegativePrefix("-");
            formatter.setNegativeSuffix("");
            
            return formatter.format(bd.doubleValue());
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Error parsing " + bd, e);
        }
        return "";
    }

    public static String formatPercent(double d)
    {
        return String.format(Locale.getDefault(), STOCK_CHANGE_PERCENT_FORMAT, d * 100);
    }

    public static double getPercentageChange(String valueStr, String changeStr)
    {
        if(valueStr == null || changeStr == null)
        {
            return 0.0;
        }

        try
        {
            double change = Math.abs(Double.parseDouble(changeStr));
            double value = Double.parseDouble(valueStr);
            return change / (value - change);
        }
        catch(NumberFormatException e)
        {
            Log.e(TAG, "Error parsing " + valueStr, e);
        }
        return 0.0;
    }

    /**
     * Basically determine if a value is a negative change, no change, or a positive change.
     * 
     * @param value
     * @return
     */
    public static ChangeType getChangeType(String value)
    {
        if(value == null)
        {
            return ChangeType.NoChange;
        }

        try
        {
            return getChangeType(Double.parseDouble(value));
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Error parsing " + value, e);
        }
        return ChangeType.NoChange;
    }
    
    
    /**
     * Basically determine if a value is a negative change, no change, or a positive change.
     * 
     * @param d
     * @return
     */
    public static ChangeType getChangeType(double d)
    {
        try
        {
            final int changeTypeInt = compareDouble(d);
            final ChangeType c = ChangeType.getById(changeTypeInt);
            return c;
        }
        catch(NumberFormatException e)
        {
            Log.e(TAG, "Error parsing " + d, e);
        }
        return ChangeType.NoChange;
    }
    
    

    // http://stackoverflow.com/questions/3994531/how-to-determine-if-a-number-is-positive-or-negative-in-java#tab-top
    public static int compareDouble(double f)
    {
        if(f != f)
        {
            throw new IllegalArgumentException("NaN");
        }
        if(f == 0)
        {
            return 0;
        }
        f *= Double.POSITIVE_INFINITY;
        if(f == Double.POSITIVE_INFINITY)
        {
            return +1;
        }
        if(f == Double.NEGATIVE_INFINITY)
        {
            return -1;
        }

        throw new IllegalArgumentException("Unfathomed double");
    }

    public enum ChangeType
    {
        NoChange(0), Positive(1), Negative(-1), Unknown(-100);

        private int id;

        ChangeType(int id)
        {
            this.id = id;
        }

        public static ChangeType getById(int lookupId)
        {
            for(ChangeType c : ChangeType.values())
            {
                if(c.id == lookupId)
                {
                    return c;
                }
            }
            return Unknown;
        }
    }

    public static BigDecimal getTotalMarketValue(List<Quote> quoteList)
    {
        if(quoteList == null)
        {
            throw new IllegalArgumentException("quoteList cannot be null");
        }

        BigDecimal totalValue = null;
        for(Quote quote : quoteList)
        {
            BigDecimal price = new BigDecimal(quote.getLastTradePriceOnly());
            BigDecimal quantity = new BigDecimal(quote.getQuantity());
            BigDecimal value = price.multiply(quantity);

            if(totalValue == null)
            {
                totalValue = value;
            }
            else
            {
                totalValue = totalValue.add(value);
            }
        }
        return totalValue;
    }

    /**
     * Calculate the previous market value by adding the 'change' to the 'lastTradePriceOnly' and multiply by 'quantity' for each Quote
     * 
     * @param quoteList
     * @return
     */
    public static BigDecimal getPreviousMarketValue(List<Quote> quoteList)
    {
        if(quoteList == null)
        {
            throw new IllegalArgumentException("quoteList cannot be null");
        }

        BigDecimal yesterdaysTotalValue = null;
        for(Quote quote : quoteList)
        {
            if(quote.getChange() == null) {
                Log.d(TAG, "No 'change' found in Quote, skipping");
                continue;
            }
            BigDecimal price = new BigDecimal(quote.getLastTradePriceOnly());
            BigDecimal quantity = new BigDecimal(quote.getQuantity());
            BigDecimal change = new BigDecimal(quote.getChange());
            BigDecimal yesterdaysValue = price.add(change.negate()).multiply(quantity);
            
            if(yesterdaysTotalValue == null)
            {
                yesterdaysTotalValue = yesterdaysValue;
            }
            else
            {
                yesterdaysTotalValue = yesterdaysTotalValue.add(yesterdaysValue);
            }
        }
        return yesterdaysTotalValue;
    }

    public static BigDecimal getMarketValueChange(final BigDecimal currentMarketValue, final BigDecimal previousMarketValue)
    {
        int diff = currentMarketValue.compareTo(previousMarketValue);
        BigDecimal todaysChange;
        if(diff == 0)
        {
            todaysChange = new BigDecimal(0);
        }
        else if(diff == 1)
        {
            todaysChange = currentMarketValue.subtract(previousMarketValue);
        }
        else
        {
            todaysChange = previousMarketValue.subtract(currentMarketValue);
        }
        return todaysChange;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static String formatMarketValue(BigDecimal b)
    {
        DecimalFormat formatter = (DecimalFormat)NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance(Locale.US));
        
        // http://stackoverflow.com/questions/2056400/format-negative-amount-of-usd-with-a-minus-sign-not-brackets-java#3916098
        String symbol = formatter.getCurrency().getSymbol();
        formatter.setNegativePrefix(symbol + "-");
        formatter.setNegativeSuffix("");
        
        return formatter.format(b);
    }
    
    
    public static String getChangeSymbol(Context context, ChangeType changeType)
    {
        String changeSymbol = "";
        if(changeType == ChangeType.Positive)
        {
            changeSymbol = context.getString(R.string.plus);
        }
        else if(changeType == ChangeType.Negative)
        {
            changeSymbol = context.getString(R.string.minus);
        }
        return changeSymbol;
    }
}
