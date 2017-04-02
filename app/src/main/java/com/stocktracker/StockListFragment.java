package com.stocktracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.util.FormatUtils;
import com.stocktracker.util.UrlBuilder;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, StockLoader.StockLoaderCallback, ActionBarCallback.ActionBarListener {

    private static final String TAG = StockListFragment.class.getSimpleName();
    private static final int LOADER_ID = 0;

    @BindView(R.id.swipe)
    public SwipeRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview)
    public RecyclerView mRecyclerView;

    @BindView(R.id.fab)
    public FloatingActionButton fab;

    // implementation of LoaderManager.LoaderCallbacks, used to load stocks from the database
    private StockLoader mLoaderCallback = null;

    private android.view.ActionMode mActionMode;
    private final ActionBarCallback mActionModeCallback = new ActionBarCallback(this);

    // implementation of Handler, used to load stock quote data from web service
    private Handler downloadHandler = null;

    // list of stocks loaded from the database
    private List<Stock> mStockList;

    private List<Quote> mQuoteList;

    private int mSelectedIndex;

    // callback to Activity
    private StockListListener mCallback;

    interface StockListListener {
        void addStock();
        void editStock(Quote quote);
        void deleteStock(String symbol, long id);
        void viewStockDetails(Quote quote, long id);
    }

    public static StockListFragment newInstance() {
        StockListFragment stockListFragment = new StockListFragment();
        return stockListFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        mQuoteList = new ArrayList<>();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateStockList();
            }
        });

        downloadHandler = new StockDownloadHandler(this);
        mLoaderCallback = new StockLoader(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        CoordinatorLayout layout = (CoordinatorLayout) inflater.inflate(R.layout.stock_fragment_list, container, false);
        ButterKnife.bind(this, layout);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        final RecyclerView.Adapter mStockAdapter = new StockAdapter(getActivity(), mQuoteList);
        mRecyclerView.setAdapter(mStockAdapter);

        mRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLongClickable(true);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (DEBUG) Log.d(TAG, "onItemClick, position = " + position);
                view.setSelected(false);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (DEBUG) Log.d(TAG, "onItemLongClick, position = " + position);
                if (mActionMode != null) {
                    return;
                }
                mSelectedIndex = position;

                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                if(toolbar != null) {
                    mActionMode = toolbar.startActionMode(mActionModeCallback);
                }
                mRecyclerView.getChildAt(mSelectedIndex).setSelected(true);
            }
        }));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.addStock();
            }
        });

        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (StockListListener)context;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement StockListListener");
        }
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
        getLoaderManager().initLoader(LOADER_ID, null, mLoaderCallback);
    }

    private void retrieveStockQuotesFromWebService() {
        if (DEBUG) Log.d(TAG, "retrieveStockQuotesFromWebService");

        final String url = UrlBuilder.buildAllStocksQuoteUrl(this.mStockList);

        if (DEBUG) Log.d(TAG, "buildAllStocksQuoteUrl() returned " + url);

        if(url == null) {
            hideSwipeProgress();
        } else {
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
    private static class StockDownloadHandler extends Handler {
        // Allows Fragment to be garbage collected properly
        private final WeakReference<StockListFragment> mFragment;

        public StockDownloadHandler(StockListFragment activity) {
            mFragment = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            StockListFragment fragment = mFragment.get();

            // check if the StockListFragment is gone
            if (fragment == null) {
                return;
            }

            QuoteResponse quoteResponse = DownloadIntentService.getQuoteResponse(message);

            mFragment.get().hideSwipeProgress();
            mFragment.get().updateStockListDone(quoteResponse);
            mFragment.get().enableSwipe();
        }
    }

    /**
     * Callback from StockLoader, called when the list of stocks has been retrieved from the database
     */
    @Override
    public void onStocksLoadedFromDatabase(List<Stock> stocks) {
        if (DEBUG) Log.d(TAG, "onStocksLoadedFromDatabase");

        this.mStockList = stocks;
        retrieveStockQuotesFromWebService();
    }

    /**
     * Called by the handler after refreshed stock list data is retrieved
     *
     * @param quoteResponse Quote response object
     */
    private void updateStockListDone(QuoteResponse quoteResponse) {
        if (quoteResponse == null) {
            showErrorMessage();
        } else {
            updateQuoteResponseObjects(quoteResponse);
            if (this.isVisible()) {
                mQuoteList = quoteResponse.getQuotes();
                updateStockListDisplay(); //quoteResponse.getQuotes());
            }
        }
    }

    private void showErrorMessage() {
        new AlertDialog.Builder(this.getActivity()).setMessage(R.string.load_error).setTitle(R.string.app_name).setPositiveButton(android.R.string.ok, null).show();
    }

    /**
     * Update each Quote object in the quoteList with the quantity from the database
     *
     * @param quotes Quote object
     */
    private void updateQuoteResponseObjects(QuoteResponse quotes) {
        if (quotes != null && !quotes.getLang().isEmpty() && mStockList != null && !mStockList.isEmpty()) {
            for (Quote q : quotes.getQuotes()) {
                Stock s = getStockBySymbol(mStockList, q.getSymbol());
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
     */
    private void updateStockListDisplay() {
        final RecyclerView.Adapter mStockAdapter = new StockAdapter(getActivity(), mQuoteList);
        mRecyclerView.setAdapter(mStockAdapter);

        final BigDecimal marketValue = FormatUtils.getTotalMarketValue(mQuoteList);
        final BigDecimal previousMarketValue = FormatUtils.getPreviousMarketValue(mQuoteList);

        if (DEBUG) Log.d(TAG, "Market Total BigDecimal = " + marketValue);
        if (DEBUG) Log.d(TAG, "Previous Market Total BigDecimal = " + previousMarketValue);

        new MarketTotalUiUpdater(getActivity(), marketValue).update();
        new MarketChangeUiUpdater(getActivity(), marketValue, previousMarketValue).update();
    }


    // Action Bar callback
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mRecyclerView.getChildAt(mSelectedIndex).setSelected(false);
        mActionMode = null;
    }

    // Action Bar callback
    @Override
    public void onActionItemClicked(MenuItem item) {
        if(!isVisible() || mSelectedIndex < 0) {
            return;
        }

        final Quote quote = mQuoteList.get(mSelectedIndex);
        if (DEBUG) Log.d(TAG, "Selected quote = " + quote);

        mRecyclerView.clearFocus(); // to remove the highlighted background

        switch(item.getItemId()) {
            case R.id.action_edit: {
                if (DEBUG) Log.d(TAG, "Edit");
                mActionMode.finish();
                mCallback.editStock(quote);
                break;
            }
//            case R.id.action_details: {
//                if (DEBUG) Log.d(TAG, "Details");
//                mActionMode.finish();
//                mCallback.viewStockDetails(quote, quote.getId());
//                break;
//            }
            case R.id.action_delete: {
                if (DEBUG) Log.d(TAG, "Delete");
                mActionMode.finish();
                mCallback.deleteStock(quote.getSymbol(), quote.getId());
                break;
            }
        }
    }
}
