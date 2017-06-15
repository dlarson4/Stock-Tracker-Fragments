package com.stocktracker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stocktracker.data.StockQuote;
import com.stocktracker.util.CurrencyUtils;
import com.stocktracker.util.FormatUtils;
import com.stocktracker.util.Utils;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 10/27/15.
 */
public class StockQuoteAdapter extends RecyclerView.Adapter<StockQuoteAdapter.ViewHolder> {
    private static final String TAG = StockQuoteAdapter.class.getSimpleName();

    private List<StockQuote> stockQuotes;

    private final LayoutInflater layoutInflater;
    private final WeakReference<Context> context;

    public StockQuoteAdapter(Context context) {
        this.context = new WeakReference<>(context);
        layoutInflater = LayoutInflater.from(context);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
    }

    public void update(List<StockQuote> stockQuotes) {
        this.stockQuotes = stockQuotes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.stock_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        StockQuote stockQuote = stockQuotes.get(position);

        holder.getStockSymbol().setText(stockQuote.symbol());

        String change = stockQuote.change();

        int changeColor = getChangeColor(stockQuote);
        final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(change);
        final String changeSymbol = FormatUtils.getChangeSymbol(layoutInflater.getContext(), changeType);

        holder.getStockSymbol().setText(stockQuote.symbol());
        holder.getStockName().setText(stockQuote.name());

        String lastTradePriceFormatted = CurrencyUtils.formatCurrency(stockQuote.lastTradePriceOnly());
        holder.getStockPrice().setText(lastTradePriceFormatted);

        boolean isChangeValid = Utils.isValidChangeValue(change);
        if(isChangeValid) {
            String changeFormatted = CurrencyUtils.formatCurrency(change);
            holder.getStockChange().setText(changeFormatted);

            double changePercent = CurrencyUtils.getPercentageChange(
                    stockQuote.lastTradePriceOnly(),
                    change);
            String changePercentFormatted = CurrencyUtils.formatPercent(changePercent);
            holder.getStockChangePercentage().setText(changeSymbol + changePercentFormatted);

            holder.getStockChange().setTextColor(changeColor);
            holder.getStockChangePercentage().setTextColor(changeColor);

        } else {
            if (DEBUG) Log.e(TAG, "onBindViewHolder: invalid change value '" + change + "'");
            holder.getStockChange().setText("-");
            holder.getStockChange().setTextColor(changeColor);
        }


    }

    @Override
    public int getItemCount() {
        return stockQuotes == null ? 0 : stockQuotes.size();
    }

    private int getChangeColor(StockQuote quote) {

        final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(quote.change());

        int colorId;
        if (changeType == FormatUtils.ChangeType.Negative) {
            colorId = R.color.red;
        } else if(changeType == FormatUtils.ChangeType.Positive) {
            colorId = R.color.green;
        } else if(changeType == FormatUtils.ChangeType.NoChange) {
            colorId = R.color.black;
        } else {
            colorId = R.color.holo_gray_light;
        }

        int changeColor;
        if(Utils.hasMarshmallow()) {
            changeColor = layoutInflater.getContext().getResources().getColor(colorId, context.get().getTheme());
        } else {
            changeColor = layoutInflater.getContext().getResources().getColor(colorId);
        }
        return changeColor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView stockSymbol;
        private TextView stockName;
        private TextView stockPrice;
        private TextView stockChange;
        private TextView stockChangePercentage;

        public ViewHolder(View itemView) {
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
}
