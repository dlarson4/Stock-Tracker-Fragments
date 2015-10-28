package com.stocktracker;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

/**
 * Created by dlarson on 9/5/15.
 */
public class StockParentViewHolder extends ParentViewHolder {

    private TextView stockSymbol;
    private TextView stockName;
    private TextView stockPrice;
    private TextView stockChange;
    private TextView stockChangePercentage;

    public StockParentViewHolder(View itemView) {
        super(itemView);

        stockSymbol = (TextView) itemView.findViewById(R.id.stockSymbol);
        stockName = (TextView) itemView.findViewById(R.id.stockName);
        stockPrice = (TextView) itemView.findViewById(R.id.stockPrice);
        stockChange = (TextView) itemView.findViewById(R.id.stockChange);
        stockChangePercentage = (TextView) itemView.findViewById(R.id.stockChangePercentage);
    }

    public TextView getStockSymbol() {
        return stockSymbol;
    }

    public TextView getStockName() {
        return stockName;
    }

    public TextView getStockPrice() {
        return stockPrice;
    }

    public TextView getStockChange() {
        return stockChange;
    }

    public TextView getStockChangePercentage() {
        return stockChangePercentage;
    }
}
