package com.stocktracker.util;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.log.Logger;

public class Utils
{
    private static final String CLASS_NAME = Utils.class.getSimpleName();

    public static boolean stockExists(QuoteResponse response)
    {
        if(response == null || response.getQuotes() == null || response.getQuotes().isEmpty())
        {
            return false;
        }

        Quote quote = response.getQuotes().get(00);
        return !quote.getAverageDailyVolume().equals("null") && !quote.getStockExchange().equals("null");
    }

    public static boolean isValidStock(QuoteResponse response)
    {
        if(response == null || response.getQuotes() == null || response.getQuotes().isEmpty())
        {
            return false;
        }

        Quote q = response.getQuotes().get(0);
        return q != null && q.getAverageDailyVolume() != null && q.getStockExchange() != null;
    }
    
    public static boolean isValidQuantity(String quantityStr)
    {
        if(quantityStr == null || quantityStr.trim().length() < 1)
        {
            return false;
        }
        try
        {
            double q = Double.parseDouble(quantityStr);
            return q > 0;
        }
        catch(NumberFormatException e)
        {
            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: Error parsing quantity '%s' to double.", CLASS_NAME, "isValidQuantity", quantityStr);
            }
            return false;
        }
    }
}
