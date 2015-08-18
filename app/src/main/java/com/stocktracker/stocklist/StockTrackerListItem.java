package com.stocktracker.stocklist;

import android.view.LayoutInflater;
import android.view.View;

public interface StockTrackerListItem {
    public int getViewType();

    public View getView(LayoutInflater inflater, View convertView);
}
