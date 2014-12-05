package com.stocktracker;

import java.util.SortedMap;
import java.util.TreeMap;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.stocktracker.data.Quote;
import com.stocktracker.log.Logger;

public class DetailsStockFragment extends Fragment
{
    public static final String TAG = DetailsStockFragment.class.getCanonicalName();
    public static final String KEY_QUOTE = "quote";
    public final static String KEY_ID = "id";

    private static final Object CLASS_NAME = DetailsStockFragment.class.getSimpleName();

    private long id;
    private Quote quote;
    
    private float marginLeft;
    private float marginTop;
    private float marginRight;
    private float marginBottom;
    private TableRow.LayoutParams dividertexTViewParams = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.quote = (Quote) getArguments().getParcelable(DetailsStockFragment.KEY_QUOTE);
        this.id = getArguments().getLong(DetailsStockFragment.KEY_ID);

        return inflater.inflate(R.layout.details_stock_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        marginLeft = getResources().getDimension(R.dimen.details_divider_margin_left);
        marginTop = getResources().getDimension(R.dimen.details_divider_margin_top);
        marginRight = getResources().getDimension(R.dimen.details_divider_margin_right);
        marginBottom = getResources().getDimension(R.dimen.details_divider_margin_bottom);
        
        dividertexTViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 1);
        dividertexTViewParams.weight = 1;
        dividertexTViewParams.setMargins((int) marginLeft, (int) marginTop, (int) marginRight, (int) marginBottom);

        TextView detailsStockSymbol = (TextView) getView().findViewById(R.id.detailsStockSymbol);
        detailsStockSymbol.setText(quote.getSymbol());

        TableLayout table = (TableLayout) getView().findViewById(R.id.detailsTable);
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: table = '%s'", CLASS_NAME, "onActivityCreated", table);
        }

        if(table != null)
        {
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);

            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            rowParams.weight = 1;

            TableLayout tableLayout = new TableLayout(getActivity());
            tableLayout.setLayoutParams(tableParams);

            SortedMap<String, String> map = getDetailsMap();
            for (String key : map.keySet())
            {
                TextView detailNameTextView = new TextView(getActivity());
                detailNameTextView.setLayoutParams(rowParams);
                detailNameTextView.setText(key);

                TextView detailValueTextView = new TextView(getActivity());
                detailValueTextView.setLayoutParams(rowParams);
                detailValueTextView.setText(map.get(key));

                TableRow tableRow = new TableRow(getActivity());
                tableRow.setLayoutParams(tableParams);

                tableRow.addView(detailNameTextView);
                tableRow.addView(detailValueTextView);

                table.addView(tableRow);

                table.addView(createSeparator(tableParams));
            }
        }
    }

    private View createSeparator(TableLayout.LayoutParams tableRowParams)
    {
        TableRow tableRow = new TableRow(getActivity());
        tableRow.setLayoutParams(tableRowParams);

        TextView v = new TextView(getActivity());
        v.setLayoutParams(dividertexTViewParams);
        v.setBackgroundColor(getResources().getColor(R.color.details_divider));

        tableRow.addView(v);

        return tableRow;
    }

    private SortedMap<String, String> getDetailsMap()
    {
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("Average Daily Volume", quote.getAverageDailyVolume());
        map.put("Change", quote.getChange());
        map.put("Days Low", quote.getDaysLow());
        map.put("Days High", quote.getDaysHigh());
        map.put("Year High", quote.getYearHigh());
        map.put("Year Low", quote.getYearLow());
        map.put("Market Capitalization", quote.getMarketCapitalization());
        map.put("Last Trade Price Only", quote.getLastTradePriceOnly());
        map.put("Days Range", quote.getDaysRange());
        map.put("Volume", quote.getVolume());
        map.put("Stock Exchange", quote.getStockExchange());

        return map;
    }
}
