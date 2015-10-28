package com.stocktracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.util.UrlBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, StockLoader.StockLoaderCallback {

    static final String TAG = StockListFragment.class.getSimpleName();
    private static final int LOADER_ID = 0;

    private static final String FRAGMENT_LIST_KEY = "fragmentListKey";
    private static final String STOCK_LIST_KEY = "stockListKey";

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;

    // implementation of LoaderManager.LoaderCallbacks, used to load stocks from the database
    private StockLoader loaderCallback = null;

    // implementation of Handler, used to load stock quote data from web service
    private Handler downloadHandler = null;

    // list of stocks loaded from the database
    private List<Stock> stockList;

    // stock quote data loaded from web service and saved between configuration changes
    private List<ParentObject> parentObjectList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.d(TAG, "onCreate");

        parentObjectList = new ArrayList<>();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateStockList();
            }
        });

        downloadHandler = new StockDownloadHandler(this);
        loaderCallback = new StockLoader(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.stock_fragment_list, container, false);
        mRecyclerView = (RecyclerView) relativeLayout.findViewById(R.id.recyclerview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        StockExpandableAdapter mStockExpandableAdapter = new StockExpandableAdapter(getActivity().getBaseContext(), parentObjectList);
        mStockExpandableAdapter.setParentClickableViewAnimationDefaultDuration();
        mStockExpandableAdapter.setParentAndIconExpandOnClick(true);
        mRecyclerView.setAdapter(mStockExpandableAdapter);

        mRefreshLayout = (SwipeRefreshLayout) relativeLayout.findViewById(R.id.swipe);
        mRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (DEBUG) Log.d(TAG, "onItemClick, position = " + position);
                if(view.isSelected()) {
                    view.setSelected(false);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (DEBUG) Log.d(TAG, "onItemLongClick, position = " + position);
                StockParentViewHolder viewHolder = (StockParentViewHolder)mRecyclerView.findViewHolderForLayoutPosition(position);
                if(!viewHolder.isExpanded()) {
                    view.setSelected(true);
                }
            }
        }));

        return relativeLayout;
    }

    @Override
    public void onRefresh() {
        if (DEBUG) Log.d(TAG, "onRefresh");
        disableSwipe();
        updateStockList();
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    private void showSwipeProgress() {
        mRefreshLayout.setRefreshing(true);
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    private void hideSwipeProgress() {
        mRefreshLayout.setRefreshing(false);
    }

    /**
     * Enables swipe gesture
     */
    private void enableSwipe() {
        mRefreshLayout.setEnabled(true);
    }

    /**
     * Disables swipe gesture. It prevents manual gestures but keeps the option tu show refreshing programmatically.
     */
    private void disableSwipe() {
        mRefreshLayout.setEnabled(false);
    }

    /**
     * Called by the controlling Activity when the stocks should be updated, like when
     * one was added, deleted, or a quantity changed
     */
    void updateStockList() {
        if (DEBUG) Log.d(TAG, "updateStockList");
        loadStockListFromDatabase();
    }

    /**
     * Start a loader to retrieve the stock list from the database.  (This eventually initiates the process of
     * retrieving quote information from the web service and performing a full refresh)
     */
    private void loadStockListFromDatabase() {
        if (DEBUG) Log.d(TAG, "loadStockListFromDatabase");
        getLoaderManager().initLoader(LOADER_ID, null, loaderCallback);
    }

    private void retrieveStockQuotesFromWebService() {
        if (DEBUG) Log.d(TAG, "retrieveStockQuotesFromWebService");

        final String url = UrlBuilder.buildAllStocksQuoteUrl(this.stockList);

        if (DEBUG) Log.d(TAG, "buildAllStocksQuoteUrl() returned " + url);

        if (url != null) {
            // temporarily disable Refresh button
            if (this.isVisible()) {
                disableSwipe();
            }

            showSwipeProgress();

            Intent intent = DownloadIntentService.createIntent(this.getActivity(), Uri.parse(url), downloadHandler, null, 0);

            if (DEBUG) Log.d(TAG, "Starting download intent service.");


            this.getActivity().startService(intent);
        }
    }

    /**
     * A constructor that gets a weak reference to the enclosing class. We do this to avoid memory leaks during Java
     * Garbage Collection.
     * <p/>
     * groups.google.com/forum/#!msg/android-developers/1aPZXZG6kWk/lIYDavGYn5UJ
     */
    private class StockDownloadHandler extends Handler {
        // Allows Fragment to be garbage collected properly
        private WeakReference<StockListFragment> mFragment;

        public StockDownloadHandler(StockListFragment activity) {
            mFragment = new WeakReference<StockListFragment>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            StockListFragment fragment = mFragment.get();

            // check if the StockListFragment is gone
            if (fragment == null) {
                return;
            }

            QuoteResponse quoteResponse = DownloadIntentService.getQuoteResponse(message);

            hideSwipeProgress();
//            fragment.dismissProgressDialog();
            fragment.updateStockListDone(quoteResponse);
//            fragment.enableRefreshButton();
            enableSwipe();
        }
    }

    /**
     * Callback from StockLoader, called when the list of stocks has been retrieved from the database
     */
    @Override
    public void onStocksLoadedFromDatabase(List<Stock> stocks) {
        if (DEBUG) Log.d(TAG, "onStocksLoadedFromDatabase");

        this.stockList = stocks;
        retrieveStockQuotesFromWebService();
    }

    /**
     * Called by the handler after refreshed stock list data is retrieved
     *
     * @param quoteResponse
     */
    private void updateStockListDone(QuoteResponse quoteResponse) {
        if (quoteResponse == null) {
            showErrorMessage();
        } else {
            if (quoteResponse != null) {
                updateQuoteResponseObjects(quoteResponse);
                if (this.isVisible()) {
                    updateStockListDisplay(quoteResponse.getQuotes());
                }
            }
        }
    }

    private void showErrorMessage() {
        new AlertDialog.Builder(this.getActivity()).setMessage(R.string.load_error).setTitle(R.string.app_name).setPositiveButton(android.R.string.ok, null).show();
    }

    /**
     * Update each Quote object in the quoteList with the quantity from the database
     *
     * @param quotes
     */
    private void updateQuoteResponseObjects(QuoteResponse quotes) {
        if (quotes != null && !quotes.getLang().isEmpty() && stockList != null && !stockList.isEmpty()) {
            for (Quote q : quotes.getQuotes()) {
                Stock s = getStockBySymbol(stockList, q.getSymbol());
                if (s != null) {
                    if (DEBUG) Log.d(TAG, "Adding quantity to Quote for symbol " + q.getSymbol());

                    q.setQuantity(s.getQuantity());

                    if (DEBUG) Log.d(TAG, "Adding SQLite id to Quote for symbol " + q.getSymbol());

                    q.setId(s.getId());
                }
            }
        }
    }

    private Stock getStockBySymbol(List<Stock> stocks, String symbol) {
        for (Stock s : stocks) {
            if (s.getSymbol() != null && s.getSymbol().equalsIgnoreCase(symbol)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Update the ListView with the provided list of Quote objects
     *
     * @param quoteList
     */
    private void updateStockListDisplay(List<Quote> quoteList)
    {
        parentObjectList = getStockListItems(quoteList);

        StockExpandableAdapter mStockExpandableAdapter = new StockExpandableAdapter(getActivity().getBaseContext(), parentObjectList);
        mStockExpandableAdapter.setParentClickableViewAnimationDefaultDuration();
        mStockExpandableAdapter.setParentAndIconExpandOnClick(true);
        mRecyclerView.setAdapter(mStockExpandableAdapter);
    }

    private List<ParentObject> getStockListItems(List<Quote> quoteList) {
        List<ParentObject> list = new ArrayList<>();
        if(quoteList == null || quoteList.isEmpty()) {
            return list;
        }

        for(Quote quote : quoteList) {
            Stock s = new Stock(quote.getId(), quote.getSymbol(), Double.valueOf(quote.getLastTradePriceOnly()), System.currentTimeMillis());
            QuoteListItem q = new QuoteListItem(s, Arrays.<Object>asList(Quote.QuoteBuilder.aQuote()
                    .change(quote.getChange())
                    .stockExchange(quote.getStockExchange())
                    .lastTradePriceOnly(quote.getLastTradePriceOnly())
                    .symbol(quote.getSymbol())
                    .name(quote.getName())
                    .daysLow(quote.getDaysLow())
                    .daysHigh(quote.getDaysHigh())
                    .quantity(quote.getQuantity())
                    .yearHigh(quote.getYearHigh())
                    .yearLow(quote.getYearLow()).build()));
            list.add(q);
        }

        return list;
    }

}
