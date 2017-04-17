package com.stocktracker;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.util.FormatUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockListFragment
        extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        StockLoader.StockLoaderCallback,
        ActionBarCallback.ActionBarListener,
        StockListContract.View {

    private static final String TAG = StockListFragment.class.getSimpleName();
    private static final int LOADER_ID = 0;

    @BindView(R.id.swipe)
    public SwipeRefreshLayout refreshLayout;

    @BindView(R.id.recyclerview)
    public RecyclerView recyclerView;

    @BindView(R.id.fab)
    public FloatingActionButton fab;

    @BindView(R.id.totalMarketValue)
    public TextView totalMarketValueView;

    // implementation of LoaderManager.LoaderCallbacks, used to load stocks from the database
    private StockLoader loaderCallback = null;

    private ActionMode actionMode;
    private final ActionBarCallback actionModeCallback = new ActionBarCallback(this);

    // list of stocks loaded from the database
    private List<Stock> stockList;

    private List<Quote> quoteList;

    private int selectedIndex;

    // callback to Activity
    private StockListListener stockListListener;

    interface StockListListener {
        void addStock();
        void editStock(Quote quote);
        void deleteStock(String symbol, long id);
    }

    public static StockListFragment newInstance() {
        return new StockListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        quoteList = new ArrayList<>();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateStockList();
            }
        });

        loaderCallback = new StockLoader(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        CoordinatorLayout layout = (CoordinatorLayout) inflater.inflate(R.layout.stock_fragment_list, container, false);
        ButterKnife.bind(this, layout);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        final RecyclerView.Adapter mStockAdapter = new StockAdapter(getActivity(), quoteList);
        recyclerView.setAdapter(mStockAdapter);

        refreshLayout.setOnRefreshListener(this);
        recyclerView.setLongClickable(true);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, itemClickListener));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stockListListener.addStock();
            }
        });

        return layout;
    }

    private final RecyclerItemClickListener.OnItemClickListener itemClickListener
            = new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (DEBUG) Log.d(TAG, "onItemClick, position = " + position);
            view.setSelected(false);
        }

        @Override
        public void onItemLongClick(View view, int position) {
            if (DEBUG) Log.d(TAG, "onItemLongClick, position = " + position);
            if (actionMode != null) {
                return;
            }
            selectedIndex = position;

            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            if(toolbar != null) {
                actionMode = toolbar.startActionMode(actionModeCallback);
            }
            recyclerView.getChildAt(selectedIndex).setSelected(true);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            stockListListener = (StockListListener)context;
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
        refreshLayout.setRefreshing(true);
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    @Override
    public void hideSwipeProgress() {
        refreshLayout.setRefreshing(false);
    }

    /**
     * Enables swipe gesture
     */
    @Override
    public void enableSwipe() {
        refreshLayout.setEnabled(true);
    }

    /**
     * Disables swipe gesture. It prevents manual gestures but keeps the option tu show refreshing programmatically.
     */
    private void disableSwipe() {
        refreshLayout.setEnabled(false);
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

        // temporarily disable Refresh button
        if (this.isVisible()) {
            disableSwipe();
        }
        showSwipeProgress();

        new StockListPresenter(this).getStockQuotes(stockList);
        if (DEBUG) Log.d(TAG, "Starting download intent service.");
    }

    @Override
    public void displayQuoteResponse(QuoteResponse quoteResponse) {
        if (DEBUG) Log.d(TAG, "displayQuoteResponse: ");

        updateStockListDone(quoteResponse);
    }

    /**
     * Callback from StockLoader, called when the list of stocks has been retrieved from the database
     */
    @Override
    public void onStocksLoadedFromDatabase(List<Stock> stocks) {
        if (DEBUG) Log.d(TAG, "onStocksLoadedFromDatabase() stocks = [" + stocks + "]");

        this.stockList = stocks;
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
                quoteList = quoteResponse.getQuotes();
                updateStockListDisplay(); //quoteResponse.getQuotes());
            }
        }
    }

    private void showErrorMessage() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.load_error)
                .setTitle(R.string.app_name)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Update each Quote object in the quoteList with the quantity from the database
     *
     * @param quotes Quote object
     */
    private void updateQuoteResponseObjects(QuoteResponse quotes) {
        if (quotes != null && !quotes.getLang().isEmpty() && stockList != null && !stockList.isEmpty()) {
            for (Quote q : quotes.getQuotes()) {
                Stock s = getStockBySymbol(stockList, q.getSymbol());
                if (s != null) {
                    if (DEBUG) Log.d(TAG, "Adding quantity '" + s.getQuantity() + "' to quote " + q.getSymbol());

                    q.setQuantity(s.getQuantity());
                    if (DEBUG) Log.d(TAG, "Adding SQLite id '" + s.getId() + "' to quote " + q.getSymbol());
                    q.setId(s.getId());

                    if (DEBUG) Log.d(TAG, "updateQuoteResponseObjects: " + q);
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
     */
    private void updateStockListDisplay() {
        final RecyclerView.Adapter mStockAdapter = new StockAdapter(getActivity(), quoteList);
        recyclerView.setAdapter(mStockAdapter);

        final BigDecimal marketValue = FormatUtils.getTotalMarketValue(quoteList);
        final BigDecimal previousMarketValue = FormatUtils.getPreviousMarketValue(quoteList);

        if (DEBUG) Log.d(TAG, "Market Total BigDecimal = " + marketValue);
        if (DEBUG) Log.d(TAG, "Previous Market Total BigDecimal = " + previousMarketValue);

        updateMarketValue(marketValue);

        new MarketChangeUiUpdater(getActivity(), marketValue, previousMarketValue).update();
    }

    private void updateMarketValue(BigDecimal marketValue) {
        if (totalMarketValueView != null) {
            totalMarketValueView.setText(FormatUtils.formatMarketValue(marketValue));
        }
    }


    // Action Bar callback
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        recyclerView.getChildAt(selectedIndex).setSelected(false);
        actionMode = null;
    }

    // Action Bar callback
    @Override
    public void onActionItemClicked(MenuItem item) {
        if(!isVisible() || selectedIndex < 0) {
            return;
        }

        final Quote quote = quoteList.get(selectedIndex);
        if (DEBUG) Log.d(TAG, "Selected quote = " + quote);

        recyclerView.clearFocus(); // to remove the highlighted background

        switch(item.getItemId()) {
            case R.id.action_edit: {
                if (DEBUG) Log.d(TAG, "Edit");
                actionMode.finish();
                stockListListener.editStock(quote);
                break;
            }
//            case R.id.action_details: {
//                if (DEBUG) Log.d(TAG, "Details");
//                actionMode.finish();
//                stockListListener.viewStockDetails(quote, quote.getId());
//                break;
//            }
            case R.id.action_delete: {
                if (DEBUG) Log.d(TAG, "Delete");
                actionMode.finish();
                stockListListener.deleteStock(quote.getSymbol(), quote.getId());
                break;
            }
        }
    }
}
