package com.stocktracker;

import com.stocktracker.data.QuoteResponse;

/**
 * Created by dlarson on 4/17/17.
 */

public class AddStockContract {
    public interface View {
        void quoteLoaded(QuoteResponse quoteResponse, double quantity);
    }
}
