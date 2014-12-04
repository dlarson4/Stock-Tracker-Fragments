package com.stocktracker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.db.StockContentProviderFacade;
import com.stocktracker.log.Logger;

public class StockTrackerActivity extends Activity implements AddStockFragment.AddStockListener, StockListFragment.StockListListener, EditStockFragment.EditStockListener
{
    private static final Object CLASS_NAME = StockTrackerActivity.class.getSimpleName();
    
    private static final String FRAGMENT_TAGS[] = {StockListFragment.TAG, AddStockFragment.TAG, DetailsStockFragment.TAG, EditStockFragment.TAG};
    
    // Used to save the current fragment tag in 'onSaveInstanceState'
    private static final String FRAGMENT_TAG = "FragmentTag";
    
    // populated from the 'savedInstanceState' Bundle
    private String savedFragmentTag = null;
    
    private StockContentProviderFacade dao;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: ", CLASS_NAME, "onCreate");
        }
        
        setContentView(R.layout.activity_stock_tracker);
        
        dao = new StockContentProviderFacade(this);
        
        if(savedInstanceState != null)
        {
            savedFragmentTag = savedInstanceState.getString(FRAGMENT_TAG);
                    
            if(Logger.isDebugEnabled())
            {
                Logger.debug("%s.%s: savedFragmentTag = '%s'", CLASS_NAME, "onCreate", savedFragmentTag);
            }
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        
        final String currentFragmentTag = getCurrentFragmentTag();
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: currentFragmentTag = '%s'", CLASS_NAME, "onSaveInstanceState", currentFragmentTag);
        }
        
        if(currentFragmentTag != null)
        {
            outState.putString(FRAGMENT_TAG, currentFragmentTag);
        }
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: savedFragmentTag = '%s'", CLASS_NAME, "onResume", savedFragmentTag);
        }
        
        if(savedFragmentTag == null)
        {
            getFragmentManager().beginTransaction().replace(R.id.container, new StockListFragment(), StockListFragment.TAG).commit();
            getFragmentManager().executePendingTransactions();
            
            StockListFragment slf = getStockListFragment();
            if(slf != null)
            {
                slf.updateStockList();
            }
        }
    }
    
    /**
     * Get the current fragment's tag or null
     * @return
     */
    private String getCurrentFragmentTag()
    {
        Fragment f = null;
        for(String tag : FRAGMENT_TAGS)
        {
            f = getFragmentManager().findFragmentByTag(tag);
            
            if(Logger.isDebugEnabled())
            {
                Logger.debug("%s.%s: Fragment tag found '%s', isVisible? = '%b'", CLASS_NAME, "getCurrentFragmentTag", tag, (f == null ? false : f.isVisible()));
            }
            
            if(f != null && f.isVisible())
            {
                return tag;
            }
        }
        return null;
    }
    
    /**
     * Retrieve the StockListFragment from the fragment manager or null if it's not currently 
     * held by the fragment manager
     * @return
     */
    private StockListFragment getStockListFragment()
    {
        Fragment f = getFragmentManager().findFragmentByTag(StockListFragment.TAG);
        if(f instanceof StockListFragment)
        {
            return (StockListFragment)f;
        }
        return null;
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.stock_tracker, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if(id == R.id.action_settings)
//        {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void addClicked(View view)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Adding stock", CLASS_NAME, "addClicked");
        }
        
        replaceFragment(new AddStockFragment(), AddStockFragment.TAG);
    }
    
    @Override
    public void editStock(Quote quote)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Edit", CLASS_NAME, "editStock");
        }
        replaceFragment(new EditStockFragment(quote), EditStockFragment.TAG);
    }
    
    @Override
    public void deleteStock(String symbol, long id)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Delete", CLASS_NAME, "deleteStock");
        }
        deleteStock(id);
    }
    
    @Override
    public void viewStockDetails(Quote quote, long id)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Details", CLASS_NAME, "viewStockDetails");
        }
        
        Bundle bundle = new Bundle();
        bundle.putParcelable(DetailsStockFragment.KEY_QUOTE, quote);
        bundle.putLong(DetailsStockFragment.KEY_ID, id);
        DetailsStockFragment fragment = new DetailsStockFragment();
        fragment.setArguments(bundle);
        replaceFragment(fragment, DetailsStockFragment.TAG);
        
        //replaceFragment(new DetailsStockFragment(quote, id), DetailsStockFragment.TAG);
    }
    
    private void replaceFragment(android.app.Fragment fragment, String tag)
    {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
        getFragmentManager().executePendingTransactions();
    }
    
    private void deleteStock(long id)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Deleting stock '%d'.", CLASS_NAME, "deleteStock", id);
        }

        dao.delete(id); // TODO should be made asynchronous
        updateStockList();
    }
    
    private void updateStockList()
    {
        StockListFragment slf = getStockListFragment();
        if(slf != null)
        {
            slf.updateStockList();
        }
    }

    private void hideKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) 
        {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    
    private void insertStockSymbol(QuoteResponse parsed, double quantity)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Inserting new stock, QuoteResponse = '%s'", CLASS_NAME, "insertStockSymbol", parsed);
        }
        
        Stock newStock = dao.insert(parsed.getQuotes().get(0).getSymbol(), quantity); // TODO should be made asynchronous

        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Newly created stock = '%s'", CLASS_NAME, "insertStockSymbol", newStock);
        }

        Toast toast = Toast.makeText(getBaseContext(), getString(R.string.saved), Toast.LENGTH_SHORT);
        toast.show();
    }
    
    @Override
    public void saveNewStock(QuoteResponse quoteResponse, double quantity)
    {
      if(Logger.isDebugEnabled())
      {
          Logger.debug("%s.%s: Saving new stock", CLASS_NAME, "saveNewStock");
      }
        
        insertStockSymbol(quoteResponse, quantity);
        
        // go back to the stock list fragment
        getFragmentManager().popBackStack();
        
        // for when the keyboard doesn't close on its own
        hideKeyboard();
        
        updateStockList();
    }

    @Override
    public void cancelAddNewStock()
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: BackStackEntryCount = '%d'", CLASS_NAME, "cancelAddNewStock", getFragmentManager().getBackStackEntryCount());
        }
        goToPreviousFragment();
    }

    @Override
    public void updateStockQuantity(String symbol, double quantity, long id)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Updating stock '%s' to quantity '%f'.", CLASS_NAME, "updateStock", symbol, quantity);
        }
        
        int rowsUpdated = dao.update(id, quantity); // TODO should be made asynchronous
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: rowsUpdated = '%d'.", CLASS_NAME, "updateStock", rowsUpdated);
        }
        
        getFragmentManager().popBackStack();
        updateStockList();
    }

    @Override
    public void cancelUpdateStock()
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: BackStackEntryCount = '%d'", CLASS_NAME, "cancelUpdateStock", getFragmentManager().getBackStackEntryCount());
        }
        goToPreviousFragment();
    }
    
    private void goToPreviousFragment()
    {
        getFragmentManager().popBackStack();
        getFragmentManager().executePendingTransactions();
    }
}
