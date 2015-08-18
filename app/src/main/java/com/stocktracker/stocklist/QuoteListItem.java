package com.stocktracker.stocklist;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.stocktracker.R;
import com.stocktracker.data.Quote;
import com.stocktracker.stocklist.QuoteAdapter.RowType;
import com.stocktracker.util.FormatUtils;

public class QuoteListItem implements StockTrackerListItem {
    private Quote quote;

    public QuoteListItem(Quote quote) {
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }

    @Override
    public int getViewType() {
        return RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View stockRow = convertView;

        if (stockRow == null) {
            stockRow = inflater.inflate(R.layout.stock_row_layout, null);
        }

        //stockRow.setTag(R.string.stock_row_view_tag_id, quote.getSymbol());
        stockRow.setTag(R.string.stock_row_view_tag_id, quote);
        stockRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });

        int changeColor = inflater.getContext().getResources().getColor(R.color.green);
        final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(quote.getChange());
        final String changeSymbol = FormatUtils.getChangeSymbol(inflater.getContext(), changeType);

        if (changeType == FormatUtils.ChangeType.Negative) {
            changeColor = inflater.getContext().getResources().getColor(R.color.red);
        }

        ((TextView) stockRow.findViewById(R.id.stock_symbol_textview)).setText(quote.getSymbol());
        ((TextView) stockRow.findViewById(R.id.stock_name_textview)).setText(quote.getName());

        String lastTradePriceFormatted = FormatUtils.formatCurrency(quote.getLastTradePriceOnly());
        ((TextView) stockRow.findViewById(R.id.stock_price_textview)).setText(lastTradePriceFormatted);

        ((TextView) stockRow.findViewById(R.id.stock_quantity_textview)).setText(String.valueOf(quote.getQuantity()));

        String changeFormatted = FormatUtils.formatCurrency(quote.getChange());
        ((TextView) stockRow.findViewById(R.id.stock_change_textview)).setText(changeFormatted);

        double changePercent = FormatUtils.getPercentageChange(quote.getLastTradePriceOnly(), quote.getChange());
        String changePercentFormatted = FormatUtils.formatPercent(changePercent);
        ((TextView) stockRow.findViewById(R.id.stock_change_percentage_textview)).setText(changeSymbol + changePercentFormatted);

        ((TextView) stockRow.findViewById(R.id.stock_change_textview)).setTextColor(changeColor);
        ((TextView) stockRow.findViewById(R.id.stock_change_percentage_textview)).setTextColor(changeColor);

        return stockRow;
    }

}
