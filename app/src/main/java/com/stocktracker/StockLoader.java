package com.stocktracker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.stocktracker.contentprovider.StockContract;
import com.stocktracker.data.Stock;
import com.stocktracker.db.StockTable;

import java.util.ArrayList;
import java.util.List;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StockLoader.class.getSimpleName();

    private Context context;
    private StockLoaderCallback callback;

    interface StockLoaderCallback {
        void onStocksLoadedFromDatabase(List<Stock> stocks);
    }

    public StockLoader(Context context, StockLoaderCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (DEBUG) Log.d(TAG, "onCreateLoader");

        final Uri uri = StockContract.CONTENT_URI;
        final String[] projection = StockTable.ALL_COLUMNS;

        return new CursorLoader(context, uri, projection, null, null, StockTable.COLUMN_STOCK);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (DEBUG) Log.d(TAG, "onLoadFinished");

        List<Stock> stocks = new ArrayList<Stock>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (DEBUG) Log.d(TAG, "Creating stock");

                    stocks.add(createStock(cursor));
                } while (cursor.moveToNext());
            }
        }

        if (callback != null) {
            callback.onStocksLoadedFromDatabase(stocks);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (DEBUG) Log.d(TAG, "onLoaderReset");
    }

    private Stock createStock(Cursor cursor) {
        long id = cursor.getLong(0);
        String stock = cursor.getString(1);
        double quantity = cursor.getDouble(2);
        long dateCreated = cursor.getLong(3);
        return new Stock(id, stock, quantity, dateCreated);
    }

}
