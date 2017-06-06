package com.stocktracker;

import android.app.DialogFragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.db.AppDatabase;
import com.stocktracker.db.DatabaseCreator;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockTrackerActivity extends AppCompatActivity
        implements AddStockDialogFragment.AddStockDialogListener,
        StockListFragment.StockListListener,
        EditQuantityDialogFragment.EditStockListener,
        LifecycleRegistryOwner {
    private final static String TAG = StockTrackerActivity.class.getSimpleName();

    private DialogFragment addStockDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_tracker);

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

        new StockListPresenter(getApplication(), this, fragment);

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

        insertStock(quoteResponse, quantity);
        getFragmentManager().popBackStack();
    }

    private void insertStock(QuoteResponse parsed, double quantity) {
        if (DEBUG) Log.d(TAG, "Inserting new stock, QuoteResponse = " + parsed);

        Observable
                .fromCallable(() -> {
                    String symbol = parsed.getQuotes().get(0).getSymbol();
                    Stock stock = new Stock(symbol, quantity);
                    getDatabase().stockDao().insert(stock);
                    return stock;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stock -> {
                    if (DEBUG) Log.d(TAG, "Newly created stock = " + stock);
                    showToast(getString(R.string.saved));
                    updateStockList();
                }, throwable -> {
                    Log.e(TAG, "", throwable);
                    showToast(getString(R.string.save_error));
                });
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT);
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
    public void editStock(Stock stock) {
        EditQuantityDialogFragment editQuantityDialogFragment = EditQuantityDialogFragment.newInstance(stock);
        editQuantityDialogFragment.show(getFragmentManager(), EditQuantityDialogFragment.TAG);
    }

    @Override
    public void deleteStock(String symbol, long id) {
        Log.d(TAG, "deleteStock() symbol = [" + symbol + "], id = [" + id + "]");

        Observable
                .fromCallable(() -> {
                    Stock stock = new Stock(id);
                    getDatabase().stockDao().delete(stock);
                    return stock;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stock -> {
                    if (DEBUG) Log.d(TAG, "Stock deleted " + stock);
                    showToast(getString(R.string.deleted));
                    updateStockList();
                }, throwable -> {
                    Log.e(TAG, "", throwable);
                    showToast(getString(R.string.delete_error));
                });
    }

    @Override
    public void updateStockQuantity(String symbol, double quantity, long id) {
        Log.d(TAG, "updateStockQuantity()" +
                " symbol = [" + symbol + "]," +
                " quantity = [" + quantity + "]," +
                " id = [" + id + "]");

        Observable
                .fromCallable(() -> {
                    Stock stock = new Stock(id, symbol, quantity);
                    getDatabase().stockDao().update(stock);
                    return stock;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stock -> {
                    if (DEBUG) Log.d(TAG, "Stock updated " + stock);
                    showToast(getString(R.string.updated));
                    updateStockList();
                }, throwable -> {
                    Log.e(TAG, "", throwable);
                    showToast(getString(R.string.update_error));
                });
    }

    private AppDatabase getDatabase() {
        return DatabaseCreator.getInstance(getApplication()).getDatabase();
    }

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
