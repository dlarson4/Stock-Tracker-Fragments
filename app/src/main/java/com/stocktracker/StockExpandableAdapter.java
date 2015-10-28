package com.stocktracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.stocktracker.data.Quote;
import com.stocktracker.util.FormatUtils;

import java.util.List;

import static com.stocktracker.BuildConfig.DEBUG;
/**
 * Created by dlarson on 9/5/15.
 */
public class StockExpandableAdapter extends ExpandableRecyclerAdapter<StockParentViewHolder, StockChildViewHolder> {
    private static final String TAG = StockExpandableAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;

    public StockExpandableAdapter(Context context, List<ParentObject> parentItemList) {
        super(context, parentItemList);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public StockParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.stock_list_item, viewGroup, false);
        return new StockParentViewHolder(view);
    }

    @Override
    public StockChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.stock_details, viewGroup, false);
        return new StockChildViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(StockParentViewHolder stockParentViewHolder, int i, Object parentObject) {
        QuoteListItem stock = (QuoteListItem) parentObject;
        Quote quote = (Quote) stock.getChildObjectList().get(0);

        stockParentViewHolder.getStockSymbol().setText(stock.getStock().getSymbol());

        int changeColor = mInflater.getContext().getResources().getColor(R.color.green);
        final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(quote.getChange());
        final String changeSymbol = FormatUtils.getChangeSymbol(mInflater.getContext(), changeType);

        if (changeType == FormatUtils.ChangeType.Negative) {
            changeColor = mInflater.getContext().getResources().getColor(R.color.red);
        }

        stockParentViewHolder.getStockSymbol().setText(quote.getSymbol());
        stockParentViewHolder.getStockName().setText(quote.getName());

        String lastTradePriceFormatted = FormatUtils.formatCurrency(quote.getLastTradePriceOnly());
        stockParentViewHolder.getStockPrice().setText(lastTradePriceFormatted);

        String changeFormatted = FormatUtils.formatCurrency(quote.getChange());
        stockParentViewHolder.getStockChange().setText(changeFormatted);

        double changePercent = FormatUtils.getPercentageChange(quote.getLastTradePriceOnly(), quote.getChange());
        String changePercentFormatted = FormatUtils.formatPercent(changePercent);
        stockParentViewHolder.getStockChangePercentage().setText(changeSymbol + changePercentFormatted);

        stockParentViewHolder.getStockChange().setTextColor(changeColor);
        stockParentViewHolder.getStockChangePercentage().setTextColor(changeColor);
    }

    @Override
    public void onParentItemClickListener(int position) {
        if(this.mItemList.get(position) instanceof ParentObject) {
            ParentObject parentObject = (ParentObject)this.mItemList.get(position);
            if(DEBUG) Log.d(TAG, "parentObject.getClass() = " + parentObject.getClass());
        }
        super.onParentItemClickListener(position);
    }

    @Override
    public void onBindChildViewHolder(StockChildViewHolder stockChildViewHolder, int i, Object childObject) {
        Quote stockDetail = (Quote) childObject;
        stockChildViewHolder.getDayHigh().setText(stockDetail.getDaysHigh());
        stockChildViewHolder.getDayLow().setText(stockDetail.getDaysLow());
        stockChildViewHolder.getQuantity().setText(String.valueOf(stockDetail.getQuantity()));
        stockChildViewHolder.getYearHigh().setText(stockDetail.getYearHigh());
        stockChildViewHolder.getYearLow().setText(stockDetail.getYearLow());
    }
}
