package com.stocktracker;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ListFragment;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
//import com.stocktracker.db.StockContentProviderFacade;
import com.stocktracker.log.Logger;
import com.stocktracker.stocklist.DividerListItem;
import com.stocktracker.stocklist.HeaderListItem;
import com.stocktracker.stocklist.MarketChangeListItem;
import com.stocktracker.stocklist.MarketTotalListItem;
import com.stocktracker.stocklist.QuoteAdapter;
import com.stocktracker.stocklist.QuoteListItem;
import com.stocktracker.stocklist.StockTrackerListItem;
import com.stocktracker.util.FormatUtils;

public class StockListFragment extends ListFragment implements ActionBarCallback.ActionBarListener, StockLoader.StockLoaderCallback
{
    private static final int LOADER_ID = 0;
    private static final Object CLASS_NAME = StockListFragment.class.getSimpleName();
    
    private static final String FRAGMENT_LIST_KEY = "fragmentListKey";
    private static final String STOCK_LIST_KEY = "stockListKey";
    
    public static final String TAG = StockListFragment.class.getCanonicalName();
    
    // callback to Activity
    private StockListListener mCallback;
    
    // stock quote data loaded from web service
    private List<Quote> quoteList;
    
    private ActionMode mActionMode;
    private ActionBarCallback mActionModeCallback = new ActionBarCallback(this);
    private int selectedIndex = -1;
    
    // implementation of Handler, used to load stock quote data from web service
    private Handler downloadHandler = null;
    
    // implementation of LoaderManager.LoaderCallbacks, used to load stocks from the database
    private StockLoader loaderCallback = null;
    
    // list of stocks loaded from the database
    private List<Stock> stockList;

    static interface StockListListener
    {
        void editStock(Quote quote);
        void deleteStock(String symbol, long id);
        void viewStockDetails(Quote quote, long id);
        void addClicked(View view);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        
        if(this.quoteList != null)
        {
            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: Saving stock list", CLASS_NAME, "onSaveInstanceState");
            }
            outState.putParcelableArrayList(FRAGMENT_LIST_KEY, (ArrayList<Quote>)this.quoteList);
            outState.putParcelableArrayList(STOCK_LIST_KEY, (ArrayList<Stock>)this.stockList);
        }
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s:", CLASS_NAME, "onAttach");
        }

        try
        {
            mCallback = (StockListListener)activity;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement StockListListener");
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s:", CLASS_NAME, "onCreate");
        }
        
        downloadHandler = new StockDownloadHandler(this);
        loaderCallback = new StockLoader(this.getActivity(), this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s:", CLASS_NAME, "onCreateView");
        }
        
        if(savedInstanceState != null)
        {
            this.quoteList = (List)savedInstanceState.getParcelableArrayList(FRAGMENT_LIST_KEY);
            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: Restored quote list = '%s'", CLASS_NAME, "onCreateView", quoteList);
            }
            
            this.stockList = (List)savedInstanceState.getParcelableArrayList(STOCK_LIST_KEY);
            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: Restored stock list = '%s'", CLASS_NAME, "onCreateView", stockList);
            }
        }
        
        return inflater.inflate(R.layout.stock_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s:", CLASS_NAME, "onActivityCreated");
        }
        
        getListView().setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(Logger.isLoggingEnabled())
                {
                    Logger.debug("%s.%s: onItemLongClick called, position = %d, id = %d", CLASS_NAME, "onItemLongClick", position, id);
                }
                
                StockTrackerListItem listItem = (StockTrackerListItem)getListView().getAdapter().getItem(position);
                
                // Ignore the header, market change, market total, etc.
                if(!(listItem instanceof QuoteListItem))
                {
                    return false;
                }
                
                StockListFragment.this.selectedIndex = position;
                
                if(mActionMode != null)
                {
                    return false;
                }
                
                mActionMode = StockListFragment.this.getActivity().startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });
        
        ImageButton addButtom = (ImageButton)getView().findViewById(R.id.addButton);
        ImageButton refreshButtom = (ImageButton)getView().findViewById(R.id.refreshButton);
        
        addButtom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mCallback.addClicked(view);
            }
        });
        
        refreshButtom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                StockListFragment.this.loadStockListFromDatabase();
            }
        });
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s:", CLASS_NAME, "onResume");
        }
        
        // If we were able to retrieve quote info from the 'savedInstanceState', use that.  Otherwise reload
        if(this.quoteList != null && !this.quoteList.isEmpty())
        {
            updateStockListDisplay(quoteList);
        }
        else
        {
            loadStockListFromDatabase();
        }
    }
    
    /**
     * Callback from StockLoader, called when the list of stocks has been retrieved from the database
     */
    @Override
    public void onStocksLoadedFromDatabase(List<Stock> stocks)
    {
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Stock list loaded.", CLASS_NAME, "onStocksLoadedFromDatabase");
        }
        this.stockList = stocks;
        
        retrieveStockQuotesFromWebService();
    }
    
    /**
     * Start a loader to retrieve the stock list from the database.  (This eventually initiates the process of 
     * retrieving quote information from the web service and peforming a full refresh)
     */
    private void loadStockListFromDatabase()
    {
        getLoaderManager().initLoader(LOADER_ID, null, loaderCallback);
    }
    
    /**
     * Update the ListView with the provided list of Quote objects
     * 
     * @param quoteList
     */
    private void updateStockListDisplay(List<Quote> quoteList)
    {
        if(quoteList == null || quoteList.isEmpty())
        {
            return;
        }
        
        this.quoteList = quoteList;

        ListView stockListView = getListView();

        // wrap each Quote object in a QuoteListItem so the adapter can operate
        // against ListItem objects (one header and one or more quotes)
        List<StockTrackerListItem> wrapQuoteList = wrapQuoteAsListItem(quoteList);

        // add the header, divider, and market value (footer) rows rows
        wrapQuoteList.add(0, new HeaderListItem());
        wrapQuoteList.add(1, new DividerListItem());

        final BigDecimal marketValue = FormatUtils.getTotalMarketValue(quoteList);
        final BigDecimal previousMarketValue = FormatUtils.getPreviousMarketValue(quoteList);
        
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Market Total BigDecimal = '%s'.", CLASS_NAME, "updateStockList", marketValue);
            Logger.debug("%s.%s: Previous Market Total BigDecimal = '%s'.", CLASS_NAME, "updateStockList", previousMarketValue);
        }
        
        wrapQuoteList.add(new MarketTotalListItem(marketValue));
        wrapQuoteList.add(new MarketChangeListItem(marketValue, previousMarketValue));

        final QuoteAdapter adapter = new QuoteAdapter(this.getActivity(), R.layout.stock_row_layout, wrapQuoteList);

        stockListView.setAdapter(adapter);
        
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Done setting adapter", CLASS_NAME, "updateStockList", marketValue);
        }
    }

    private List<StockTrackerListItem> wrapQuoteAsListItem(List<Quote> quotes)
    {
        List<StockTrackerListItem> quoteItems = new ArrayList<StockTrackerListItem>();
        if(quotes != null)
        {
            for (Quote q : quotes)
            {
                quoteItems.add(new QuoteListItem(q));
            }
        }
        return quoteItems;
    }
    
    private void disableRefreshButton()
    {
        if(this.isVisible())
        {
            ImageButton refreshButton = (ImageButton) getView().findViewById(R.id.refreshButton);
            refreshButton.setImageResource(R.drawable.refresh_holo_light_disabled);
            refreshButton.setEnabled(false);
        }
    }

    private void enableRefreshButton()
    {
        if(this.isVisible())
        {
            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: Enabling refresh button.", CLASS_NAME, "enableRefreshButton", "run");
            }

            ImageButton refreshButton = (ImageButton) getView().findViewById(R.id.refreshButton);
            refreshButton.setImageResource(R.drawable.refresh_holo_light);
            refreshButton.setEnabled(true);
        }
    }
    
    // Action Bar callback
    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        getListView().clearFocus();
        mActionMode = null;
    }

    // Action Bar callback
    @Override
    public void onActionItemClicked(MenuItem item)
    {
        if(!isVisible() || selectedIndex < 0)
        {
            return;
        }
        
        StockTrackerListItem listItem = (StockTrackerListItem)getListView().getAdapter().getItem(this.selectedIndex);
        
        // Ignore the header, market change, market total, etc.
        if(!(listItem instanceof QuoteListItem))
        {
            mActionMode.finish();
            return;
        }
        
        Quote quote = ((QuoteListItem)listItem).getQuote();
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Selected quote = '%s'", CLASS_NAME, "onActionItemClicked", quote);
        }
        
        getListView().clearFocus(); // to remove the highlighted background
        
        switch(item.getItemId())
        {
            case R.id.action_edit:
            {
                if(Logger.isLoggingEnabled())
                {
                    Logger.debug("%s.%s: Edit", CLASS_NAME, "onActionItemClicked");
                }
                mActionMode.finish();
                mCallback.editStock(quote);
                break;
            }
            case R.id.action_details:
            {
                if(Logger.isLoggingEnabled())
                {
                    Logger.debug("%s.%s: Details", CLASS_NAME, "onActionItemClicked");
                }
                mActionMode.finish();
                mCallback.viewStockDetails(quote, quote.getId());
                break;
            }
            case R.id.action_delete:
            {
                if(Logger.isLoggingEnabled())
                {
                    Logger.debug("%s.%s: Delete", CLASS_NAME, "onActionItemClicked");
                }
                mActionMode.finish();
                mCallback.deleteStock(quote.getSymbol(), quote.getId());
                break;
            }
        }
    }
    
    /**
     * Called by the controlling Activity when the stocks should be updated, like when
     * one was added, deleted, or a quantity changed
     */
    void updateStockList()
    {
        loadStockListFromDatabase();
    }
    
    private void retrieveStockQuotesFromWebService()
    {
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: ", CLASS_NAME, "retrieveStockQuotesFromWebService");
        }
        
        final String url = UrlBuilder.buildAllStocksQuoteUrl(this.stockList);

        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: buildAllStocksQuoteUrl() returned '%s'.", CLASS_NAME, "retrieveStockQuotesFromWebService", url);
        }
        if(url != null)
        {
            // temporarily disable Refresh button
            if(this.isVisible())
            {
                disableRefreshButton();
            }
    
            displayProgressDialog();
            
            Intent intent = DownloadIntentService.createIntent(this.getActivity(), Uri.parse(url), downloadHandler, null, 0);
            
            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: Starting download intent service.", CLASS_NAME, "retrieveStockQuotesFromWebService");
            }
            
            this.getActivity().startService(intent);
        }
    }
    
    
    /**
     * A constructor that gets a weak reference to the enclosing class. We do this to avoid memory leaks during Java
     * Garbage Collection.
     * 
     * @see https://groups.google.com/forum/#!msg/android-developers/1aPZXZG6kWk/lIYDavGYn5UJ
     */
    private static class StockDownloadHandler extends Handler
    {
        // Allows Fragment to be garbage collected properly
        private WeakReference<StockListFragment> mFragment;

        public StockDownloadHandler(StockListFragment activity)
        {
            mFragment = new WeakReference<StockListFragment>(activity);
        }

        @Override
        public void handleMessage(Message message)
        {
            StockListFragment fragment = mFragment.get();
            
            // check if the StockListFragment is gone
            if(fragment == null)
            {
                return;
            }

            QuoteResponse quoteResponse = DownloadIntentService.getQuoteResponse(message);
            fragment.dismissProgressDialog();
            fragment.updateStockListDone(quoteResponse);
            fragment.enableRefreshButton();
        }
    }
    
    private void displayProgressDialog()
    {
        if(this.isVisible())
        {
            ImageView progress = (ImageView)getView().findViewById(R.id.imgProgress);
            if(progress != null)
            {
                progress.setVisibility(android.view.View.VISIBLE);
                final AnimationDrawable frameAnimation = (AnimationDrawable)progress.getDrawable();
                frameAnimation.setCallback(progress);
                frameAnimation.setVisible(true, true);
            }
        }
    }

    private void dismissProgressDialog()
    {
        if(this.isVisible())
        {
            ImageView progress = (ImageView) getView().findViewById(R.id.imgProgress);
            if(progress != null)
            {
                progress.setVisibility(android.view.View.INVISIBLE);
                final AnimationDrawable frameAnimation = (AnimationDrawable) progress.getDrawable();
                frameAnimation.setVisible(false, false);
            }
        }
    }
    
    /**
     * Called by the handler after refreshed stock list data is retrieved
     * 
     * @param quoteResponse
     */
    private void updateStockListDone(QuoteResponse quoteResponse)
    {
        if(quoteResponse == null)
        {
            showErrorMessage();
        }
        else
        {
            if(quoteResponse != null)
            {
                updateQuoteResponseObjects(quoteResponse);
                if(this.isVisible())
                {
                    updateStockListDisplay(quoteResponse.getQuotes());
                }
            }
        }
    }
    
    private void showErrorMessage()
    {
        new Builder(this.getActivity()).setMessage(R.string.load_error).setTitle(R.string.app_name).setPositiveButton(android.R.string.ok, null).show();
    }
    
    
    /**
     * Update each Quote object in the quoteList with the quantity from the database
     * 
     * @param quotes
     */
    private void updateQuoteResponseObjects(QuoteResponse quotes)
    {
        if(quotes != null && !quotes.getLang().isEmpty() && stockList != null && !stockList.isEmpty())
        {
            for (Quote q : quotes.getQuotes())
            {
                Stock s = getStockBySymbol(stockList, q.getSymbol());
                if(s != null)
                {
                    if(Logger.isLoggingEnabled())
                    {
                        Logger.debug("%s.%s: Adding quantity to Quote for symbol '%s'", CLASS_NAME, "updateQuoteResponseObjects", q.getSymbol());
                    }
                    q.setQuantity(s.getQuantity());
                    
                    if(Logger.isLoggingEnabled())
                    {
                        Logger.debug("%s.%s: Adding SQLite id to Quote for symbol '%s'", CLASS_NAME, "updateQuoteResponseObjects", q.getSymbol());
                    }
                    q.setId(s.getId());
                }
            }
        }
    }
    
    private Stock getStockBySymbol(List<Stock> stocks, String symbol)
    {
        for (Stock s : stocks)
        {
            if(s.getSymbol() != null && s.getSymbol().equalsIgnoreCase(symbol))
            {
                return s;
            }
        }
        return null;
    }
    
}
