package com.stocktracker;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;

import java.util.List;

/**
 * Created by dlarson on 4/17/17.
 */

public class StockListContract {
    public interface View {
        void displayQuoteResponse(QuoteResponse quoteResponse);
        void hideSwipeProgress();
        void enableSwipe();

    }

    public interface Presenter {
        void getStockQuotes(final List<Stock> stocks);
    }

}
