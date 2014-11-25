package com.stocktracker.stocklist;

import java.math.BigDecimal;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.stocktracker.R;
import com.stocktracker.StockTrackerActivity;
import com.stocktracker.log.Logger;
import com.stocktracker.stocklist.QuoteAdapter.RowType;
import com.stocktracker.util.FormatUtils;

public class MarketChangeListItem implements StockTrackerListItem
{
    private static final Object CLASS_NAME = StockTrackerActivity.class.getSimpleName();
    
    private BigDecimal todaysValue;
    private BigDecimal previousValue;
    
    public MarketChangeListItem(BigDecimal todaysValue, BigDecimal previousValue)
    {
        this.todaysValue = todaysValue;
        this.previousValue = previousValue;
    }
    
    @Override
    public int getViewType()
    {
        return RowType.MARKET_CHANGE_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView)
    {
        View row = convertView;

        if(row == null)
        {
            row = inflater.inflate(R.layout.market_value_change_row, null);
        }
        
        if(todaysValue != null && previousValue != null)
        {
            final BigDecimal todaysChange = todaysValue.subtract(previousValue);
            final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(todaysChange.doubleValue());
            
            int changeColor = inflater.getContext().getResources().getColor(R.color.green);
            if(changeType == FormatUtils.ChangeType.Negative)
            {
                changeColor = inflater.getContext().getResources().getColor(R.color.red);
            }
            
            // change amount
            String todaysChangeFormatted = FormatUtils.formatMarketValue(todaysChange);
            ((TextView)row.findViewById(R.id.total_market_change)).setText(todaysChangeFormatted);
            ((TextView)row.findViewById(R.id.total_market_change)).setTextColor(changeColor);
            
            // change as a percent
            String changeSymbol = FormatUtils.getChangeSymbol(inflater.getContext(), changeType);
            
            if(Logger.isDebugEnabled())
            {
                Logger.debug("%s.%s: previousValue = '%s'", CLASS_NAME, "getView", previousValue);
            }
            
            if(previousValue != null && previousValue.intValue() != 0)
            {
                BigDecimal todaysChangePercent = todaysChange.divide(previousValue, 3);
                String todaysChangePercentFormatted = changeSymbol + FormatUtils.formatPercent(Math.abs(todaysChangePercent.doubleValue()));

                ((TextView) row.findViewById(R.id.total_market_change_percent)).setText(todaysChangePercentFormatted);
                ((TextView) row.findViewById(R.id.total_market_change_percent)).setTextColor(changeColor);
            }
        }
        
        return row;
    }
    
}
