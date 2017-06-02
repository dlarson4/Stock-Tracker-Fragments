package com.stocktracker;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
        ActionBarCallback.ActionBarListener,
        StockListContract.View {

    private static final String TAG = StockListFragment.class.getSimpleName();

    @BindView(R.id.swipe)
    public SwipeRefreshLayout refreshLayout;

    @BindView(R.id.recyclerview)
    public RecyclerView recyclerView;

    @BindView(R.id.fab)
    public FloatingActionButton fab;

    @BindView(R.id.totalMarketValue)
    public TextView totalMarketValueView;

    private ActionMode actionMode;
    private final ActionBarCallback actionModeCallback = new ActionBarCallback(this);

    private int selectedIndex;

    // callback to Activity
    private StockListListener stockListListener;

    private StockListContract.Presenter presenter;

    private CoordinatorLayout mainView;

    interface StockListListener {
        void addStock();
        void editStock(Stock stock);
        void deleteStock(String symbol, long id);
    }

    public static StockListFragment newInstance() {
        return new StockListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainView = (CoordinatorLayout) inflater.inflate(R.layout.stock_fragment_list, container, false);
        ButterKnife.bind(this, mainView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));

        final RecyclerView.Adapter mStockAdapter = new StockAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(mStockAdapter);

        refreshLayout.setOnRefreshListener(this);
        recyclerView.setLongClickable(true);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, itemClickListener));

        fab.setOnClickListener(view -> stockListListener.addStock());

        return mainView;
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume: ");
        super.onResume();
        presenter.start();
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

            Toolbar toolbar = ButterKnife.findById(getActivity(), R.id.toolbar);
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
        presenter.onSwipeRefresh();
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    @Override
    public void showSwipeProgress() {
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

    @Override
    public void setPresenter(StockListContract.Presenter stockListPresenter) {
        this.presenter = stockListPresenter;
    }

    /**
     * Disables swipe gesture. It prevents manual gestures but keeps the option tu show refreshing programmatically.
     */
    @Override
    public void disableSwipe() {
        refreshLayout.setEnabled(false);
    }

    /**
     * Called by the controlling Activity when the stocks should be updated, like when
     * one was added, deleted, or a quantity changed
     */
    void updateStockList() {
        if (DEBUG) Log.d(TAG, "updateStockList");
        presenter.onSwipeRefresh(); // TODO temporary hack until more refactoring is done
    }

    @Override
    public void showErrorMessage() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.load_error)
                .setTitle(R.string.app_name)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Update the ListView with the provided list of Quote objects
     */
    @Override
    public void updateStockListDisplay(final List<Quote> quoteList) {
        if(quoteList == null) {
            Log.e(TAG, "updateStockListDisplay: quoteList is null");
            return;
        }

        final RecyclerView.Adapter stockAdapter = new StockAdapter(getContext(), quoteList);
        recyclerView.setAdapter(stockAdapter);

        final BigDecimal marketValue = FormatUtils.getTotalMarketValue(quoteList);
        final BigDecimal previousMarketValue = FormatUtils.getPreviousMarketValue(quoteList);

        if (DEBUG) Log.d(TAG, "Market Total BigDecimal = " + marketValue);
        if (DEBUG) Log.d(TAG, "Previous Market Total BigDecimal = " + previousMarketValue);

        updateMarketValue(marketValue);

        updateMarketValue(marketValue, previousMarketValue);
    }

    private void updateMarketValue(final BigDecimal todaysValue, final BigDecimal previousValue) {
        if (todaysValue != null && previousValue != null) {
            final BigDecimal todaysChange = todaysValue.subtract(previousValue);
            final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(todaysChange.doubleValue());

            final TextView marketValueChangeView = ButterKnife.findById(mainView, R.id.marketValueChange);
            final TextView totalMarketChangePercentView = ButterKnife.findById(mainView, R.id.totalMarketChangePercent);

            int changeColor = getChangeColor(changeType);

            // change amount
            final String todaysChangeFormatted = FormatUtils.formatMarketValue(todaysChange);
            marketValueChangeView.setText(todaysChangeFormatted);
            marketValueChangeView.setTextColor(changeColor);

            // change as a percent
            String changeSymbol = FormatUtils.getChangeSymbol(getContext(), changeType);

            if (DEBUG) Log.d(TAG, "previousValue = " + previousValue);

            if (previousValue != null && previousValue.intValue() != 0) {
                BigDecimal todaysChangePercent = todaysChange.divide(previousValue, 3);
                String todaysChangePercentFormatted = FormatUtils.formatPercent(Math.abs(todaysChangePercent.doubleValue()));

                totalMarketChangePercentView.setText(changeSymbol + todaysChangePercentFormatted);
                totalMarketChangePercentView.setTextColor(changeColor);
            }
        }
    }

    private int getChangeColor(FormatUtils.ChangeType changeType) {
        int changeColor;
        if (changeType == FormatUtils.ChangeType.Negative) {
            changeColor = ActivityCompat.getColor(getContext(), R.color.red);
        } else {
            changeColor = ActivityCompat.getColor(getContext(), R.color.green);
        }
        return changeColor;
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

        Stock stock = presenter.getStock(selectedIndex);
        if (DEBUG) Log.d(TAG, "Selected stock = " + stock);

        recyclerView.clearFocus(); // to remove the highlighted background

        switch(item.getItemId()) {
            case R.id.action_edit: {
                if (DEBUG) Log.d(TAG, "Edit");
                actionMode.finish();
                stockListListener.editStock(stock);
                break;
            }
            case R.id.action_delete: {
                if (DEBUG) Log.d(TAG, "Delete");
                actionMode.finish();
                stockListListener.deleteStock(stock.getSymbol(), stock.getId());
                break;
            }
        }
    }
}
