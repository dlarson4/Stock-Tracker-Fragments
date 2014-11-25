package com.stocktracker;

import java.util.ArrayList;
import java.util.List;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.stocktracker.contentprovider.StockContract;
import com.stocktracker.data.Stock;
import com.stocktracker.db.StockTable;
import com.stocktracker.log.Logger;

public class StockLoader implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final Object CLASS_NAME = StockLoader.class.getSimpleName();
    
    private Context context;
    private StockLoaderCallback callback;
    
    static interface StockLoaderCallback
    {
        void onStocksLoadedFromDatabase(List<Stock> stocks);
    }

    public StockLoader(Context context, StockLoaderCallback callback)
    {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: ", CLASS_NAME, "onCreateLoader");
        }

        final Uri uri = StockContract.CONTENT_URI;
        final String[] projection = StockTable.ALL_COLUMNS;

        return new CursorLoader(context, uri, projection, null, null, StockTable.COLUMN_STOCK);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: ", CLASS_NAME, "onLoadFinished");
        }

        List<Stock> stocks = new ArrayList<Stock>();
        
        if(cursor != null)
        {
            if(cursor.moveToFirst())
            {
                do
                {
                    if(Logger.isDebugEnabled())
                    {
                        Logger.debug("%s.%s: Creating stock", CLASS_NAME, "onLoadFinished");
                    }
                    stocks.add(createStock(cursor));
                } while (cursor.moveToNext());
            }
        }
        
        if(callback != null)
        {
            callback.onStocksLoadedFromDatabase(stocks);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: ", CLASS_NAME, "onLoaderReset");
        }
    }

    private Stock createStock(Cursor cursor)
    {
        long id = cursor.getLong(0);
        String stock = cursor.getString(1);
        double quantity = cursor.getDouble(2);
        long dateCreated = cursor.getLong(3);
        return new Stock(id, stock, quantity, dateCreated);
    }

}
