package com.stocktracker.stocklist;

import android.view.LayoutInflater;
import android.view.View;

import com.stocktracker.R;
import com.stocktracker.stocklist.QuoteAdapter.RowType;

public class HeaderListItem implements StockTrackerListItem
{
    @Override
    public int getViewType()
    {
        return RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView)
    {
        View headerRow = convertView;

        if(headerRow == null)
        {
            headerRow = inflater.inflate(R.layout.header_row_layout, null);
        }
        
        return headerRow;
    }
}
