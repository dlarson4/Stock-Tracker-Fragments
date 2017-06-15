package com.stocktracker;

import android.app.Application;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.stocktracker.data.Stock;
import com.stocktracker.db.AppDatabase;
import com.stocktracker.db.DatabaseCreator;

import java.util.List;

/**
 * Created by dlarson on 6/11/17.
 */
public class StockLiveData extends LiveData<List<Stock>> {
    private static final String TAG = "StockLiveData";

    private final Application application;
    private final LifecycleOwner lifecycleOwner;

    public StockLiveData(Application application, LifecycleOwner lifecycleOwner) {
        this.application = application;
        this.lifecycleOwner = lifecycleOwner;
        subscribe();
    }

    private void subscribe() {
        getDatabase().stockDao().getStocksObservable().observe(lifecycleOwner, stocks -> {
            Log.d(TAG, "subscribe() stocks = " + stocks);
            setValue(stocks);
        });
    }

    private AppDatabase getDatabase() {
        return DatabaseCreator.getInstance(application).getDatabase();
    }

}
