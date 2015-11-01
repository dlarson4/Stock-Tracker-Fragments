package com.stocktracker;

import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.db.StockContentProviderFacade;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockTrackerActivity extends AppCompatActivity implements AddStockDialogFragment.AddStockDialogListener, StockListFragment.StockListListener {
    private final static String TAG = StockTrackerActivity.class.getSimpleName();

    private static final String FRAGMENT_TAGS[] = {StockListFragment.TAG, AddStockDialogFragment.TAG};

    private DrawerLayout mDrawerLayout;
    private DialogFragment mAddStockDialog;

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
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStock(view);
            }
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            StockListFragment fragment = new StockListFragment();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        }
    }

    private void addStock(View view) {
        mAddStockDialog = new AddStockDialogFragment();
        mAddStockDialog.show(getFragmentManager(), AddStockDialogFragment.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public void saveNewStock(QuoteResponse quoteResponse, double quantity) {
        if (DEBUG) Log.d(TAG, "saveNewStock");

        if (mAddStockDialog != null) {
            mAddStockDialog.dismiss();
        }

        if (DEBUG) Log.d(TAG, "Saving new stock");

        insertStockSymbol(quoteResponse, quantity);

        // go back to the stock list fragment
        getFragmentManager().popBackStack();

        updateStockList();
    }

    private void insertStockSymbol(QuoteResponse parsed, double quantity) {
        if (DEBUG) Log.d(TAG, "Inserting new stock, QuoteResponse = " + parsed);

        Stock newStock = dao.insert(parsed.getQuotes().get(0).getSymbol(), quantity); // TODO should be made asynchronous

        if (DEBUG) Log.d(TAG, "Newly created stock = " + newStock);

        Toast toast = Toast.makeText(getBaseContext(), getString(R.string.saved), Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Get the current fragment's tag or null
     *
     * @return The current fragment's tag
     */
    private String getCurrentFragmentTag() {
        Fragment f;
        for (String tag : FRAGMENT_TAGS) {
            f = getSupportFragmentManager().findFragmentByTag(tag);

            if (DEBUG) Log.d(TAG, "Fragment tag found '%s', isVisible? = " + (f != null && f.isVisible()));

            if (f != null && f.isVisible()) {
                return tag;
            }
        }
        return null;
    }

    /**
     * Retrieve the StockListFragment from the fragment manager or null if it's not currently
     * held by the fragment manager
     *
     * @return The StockListFragment or null if not found
     */
    private StockListFragment getStockListFragment() {
        Fragment f = getSupportFragmentManager().findFragmentByTag(StockListFragment.TAG);
        if (f instanceof StockListFragment) {
            return (StockListFragment) f;
        }
        return null;
    }

    private void updateStockList() {
        StockListFragment slf = getStockListFragment();
        if (slf != null) {
            slf.updateStockList();
        }
    }

    @Override
    public void editStock(Quote quote) {

    }

    @Override
    public void deleteStock(String symbol, long id) {

    }

    @Override
    public void viewStockDetails(Quote quote, long id) {

    }
}
