package com.stocktracker.stocklist;

import android.view.LayoutInflater;
import android.view.View;

import com.stocktracker.R;
import com.stocktracker.stocklist.QuoteAdapter.RowType;

public class DividerListItem implements StockTrackerListItem
{
    @Override
    public int getViewType()
    {
        return RowType.DIVIDER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView)
    {
        View divider = convertView;

        if(divider == null)
        {
            divider = inflater.inflate(R.layout.divider_line, null);
        }
        
        return divider;
    }
}
