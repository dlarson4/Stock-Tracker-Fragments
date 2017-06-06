package com.stocktracker;

import android.support.v4.app.LoaderManager;

import com.stocktracker.data.Quote;
import com.stocktracker.data.Stock;

import java.util.List;

/**
 * Created by dlarson on 4/17/17.
 */

public class StockListContract {
    public interface View {
        void enableSwipe();
        void setPresenter(StockListContract.Presenter stockListPresenter);
        void showSwipeProgress();
        void disableSwipe();
        void showErrorMessage(String message);
        void updateStockListDisplay(List<Quote> quoteList);
        void showSwipeProgress(boolean inProgress);
    }

    public interface Presenter {
        void start();
        void onSwipeRefresh();
        Stock getStock(int selectedIndex);
    }

}
