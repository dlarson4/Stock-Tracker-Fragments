package com.stocktracker.stocklist;

import java.math.BigDecimal;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.stocktracker.R;
import com.stocktracker.stocklist.QuoteAdapter.RowType;
import com.stocktracker.util.FormatUtils;

public class MarketTotalListItem implements StockTrackerListItem
{
    private BigDecimal marketTotal;
    
    public MarketTotalListItem(BigDecimal marketTotal)
    {
        this.marketTotal = marketTotal;
    }
    
    @Override
    public int getViewType()
    {
        return RowType.MARKET_TOTAL_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView)
    {
        View row = convertView;

        if(row == null)
        {
            row = inflater.inflate(R.layout.market_value_row, null);
        }
        
        if(marketTotal != null)
        {
            String formatted = FormatUtils.formatMarketValue(marketTotal);
            ((TextView)row.findViewById(R.id.total_market_value)).setText(formatted);
        }
        
        return row;
    }
}
