package com.stocktracker.stocklist;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class QuoteAdapter extends ArrayAdapter<StockTrackerListItem>
{
    private LayoutInflater mInflater;
    
    public enum RowType
    {
        LIST_ITEM, HEADER_ITEM, DIVIDER_ITEM, MARKET_TOTAL_ITEM, MARKET_CHANGE_ITEM;
    }

    public QuoteAdapter(Context context, int resource, List<StockTrackerListItem> quotes)
    {
        super(context, resource, quotes);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount()
    {
        return RowType.values().length;
    }

    @Override
    public int getItemViewType(int position)
    {
        return getItem(position).getViewType();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getItem(position).getView(mInflater, convertView);
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

}
