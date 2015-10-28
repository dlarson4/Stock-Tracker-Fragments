package com.stocktracker;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;

/**
 * Created by dlarson on 9/5/15.
 */
public class StockChildViewHolder extends ChildViewHolder {
    private TextView mQuantity;
    private TextView mDayHigh;
    private TextView mDayLow;
    private TextView mYearHigh;
    private TextView mYearLow;

    public StockChildViewHolder(View itemView) {
        super(itemView);

        mQuantity = (TextView) itemView.findViewById(R.id.quantity);
        mDayHigh = (TextView) itemView.findViewById(R.id.dayHigh);
        mDayLow = (TextView) itemView.findViewById(R.id.dayLow);
        mYearHigh = (TextView) itemView.findViewById(R.id.yearHigh);
        mYearLow = (TextView) itemView.findViewById(R.id.yearLow);

    }

    public TextView getQuantity() {
        return mQuantity;
    }

    public TextView getDayHigh() {
        return mDayHigh;
    }

    public TextView getDayLow() {
        return mDayLow;
    }

    public TextView getYearHigh() {
        return mYearHigh;
    }

    public TextView getYearLow() {
        return mYearLow;
    }
}
