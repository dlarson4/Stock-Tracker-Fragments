package com.stocktracker;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.db.StockContentProviderFacade;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockTrackerActivity extends AppCompatActivity
        implements AddStockDialogFragment.AddStockDialogListener,
        StockListFragment.StockListListener,
        EditQuantityDialogFragment.EditStockListener {
    private final static String TAG = StockTrackerActivity.class.getSimpleName();

    private DialogFragment addStockDialog;

    private StockContentProviderFacade dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_tracker);

        dao = new StockContentProviderFacade(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }

        StockListFragment fragment = (StockListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);

        if(fragment == null) {
            fragment = StockListFragment.newInstance();
        }

        new StockListPresenter(fragment);

        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    @Override
    public void addStock() {
        addStockDialog = new AddStockDialogFragment();
        addStockDialog.show(getFragmentManager(), AddStockDialogFragment.TAG);
    }

    @Override
    public void saveNewStock(QuoteResponse quoteResponse, double quantity) {
        if (DEBUG) Log.d(TAG, "saveNewStock");

        if (addStockDialog != null) {
            addStockDialog.dismiss();
        }

        if (DEBUG) Log.d(TAG, "Saving new stock");

        insertStockSymbol(quoteResponse, quantity);

        // go back to the stock list fragment
        getFragmentManager().popBackStack();

        updateStockList();
    }

    private void insertStockSymbol(QuoteResponse parsed, double quantity) {
        if (DEBUG) Log.d(TAG, "Inserting new stock, QuoteResponse = " + parsed);

        Stock newStock = dao.insert(parsed.getQuotes().get(0).getSymbol(), quantity);

        if (DEBUG) Log.d(TAG, "Newly created stock = " + newStock);

        Toast toast = Toast.makeText(getBaseContext(), getString(R.string.saved), Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Retrieve the StockListFragment from the fragment manager or null if it's not currently
     * held by the fragment manager
     *
     * @return The StockListFragment or null if not found
     */
    private StockListFragment getStockListFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (f instanceof StockListFragment) {
            return (StockListFragment) f;
        }
        return null;
    }

    private void updateStockList() {
        StockListFragment stockListFragment = getStockListFragment();
        if (stockListFragment != null) {
            stockListFragment.updateStockList();
        }
    }

    @Override
    public void editStock(Quote quote) {
        EditQuantityDialogFragment editQuantityDialogFragment = EditQuantityDialogFragment.newInstance(quote);
        editQuantityDialogFragment.show(getFragmentManager(), EditQuantityDialogFragment.TAG);
    }

    @Override
    public void deleteStock(String symbol, long id) {
        if (DEBUG) Log.d(TAG, "deleteStock, symbol = " + symbol + ", id = " + id);
        dao.delete(id); // TODO should be made asynchronous
        updateStockList();
    }

    @Override
    public void updateStockQuantity(String symbol, double quantity, long id) {
        if (DEBUG) Log.d(TAG, "updateStockQuantity, symbol = " + symbol + ", id = " + id + ", quantity " + quantity);
        dao.update(id, quantity); // TODO should be made asynchronous
        updateStockList();
    }
}
