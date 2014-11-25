package com.stocktracker.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stocktracker.contentprovider.StockContract;
import com.stocktracker.data.Stock;
import com.stocktracker.log.Logger;

/**
 * Facade for the content provider defined in <code>StockContract</code>
 * @author dlarson
 *
 */
public class StockContentProviderFacade
{
    private final static String CLASS_NAME = StockContentProviderFacade.class.getSimpleName();
    
    private Context context;

    public StockContentProviderFacade(Context context)
    {
        this.context = context;
    }

    public Stock insert(String stock, double quantity)
    {
        ContentValues values = new ContentValues();
        values.put(StockTable.COLUMN_STOCK, stock);
        values.put(StockTable.COLUMN_QUANTITY, quantity);
        values.put(StockTable.COLUMN_DATE_CREATED, System.currentTimeMillis());
        
        final Uri uri = StockContract.CONTENT_URI;
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Uri for content provider = '%s'", CLASS_NAME, "insert", uri);
        }
        
        Uri newRowUri = context.getContentResolver().insert(uri, values);
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: newRowUri = '%s'", CLASS_NAME, "insert", newRowUri);
        }
        
        Cursor cursor = context.getContentResolver().query(newRowUri, StockTable.ALL_COLUMNS, null, null, null);
        if(cursor != null && cursor.moveToFirst())
        {
            Stock newStock = createStock(cursor);
            close(cursor);
            return newStock;
        }
        
        return null;
    }

    public int delete(long id)
    {
        final Uri uri = Uri.parse(StockContract.CONTENT_URI.toString() + "/" + id);
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Uri for content provider = '%s'", CLASS_NAME, "delete", uri);
        }
        
        int rowsDeleted = context.getContentResolver().delete(uri, null, null);
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: rowsDeleted = '%d'", CLASS_NAME, "delete", rowsDeleted);
        }
        
        return rowsDeleted;
    }
    
    public boolean isDuplicate(String stock)
    {
        final String[] projection =  { StockTable.COLUMN_STOCK };
        final String selection = StockTable.COLUMN_STOCK + " = UPPER(?)";
        final String[] selectionArgs = {stock};
        
        final Uri uri = StockContract.CONTENT_URI;
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Uri for content provider = '%s'", CLASS_NAME, "isDuplicate", uri);
        }

        Cursor cursor = null;
        try
        {
            if(Logger.isDebugEnabled())
            {
                Logger.debug("%s.%s: About to get cursor", CLASS_NAME, "isDuplicate");
            }
            
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            
            return (cursor != null && cursor.moveToFirst());
        }
        finally
        {
            close(cursor);
        }
    }

    public List<Stock> getStocks()
    {
        List<Stock> list = new ArrayList<Stock>();

        final String[] projection = StockTable.ALL_COLUMNS;
        final Uri uri = StockContract.CONTENT_URI;
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Uri for content provider = '%s'", CLASS_NAME, "getStocks", uri);
        }

        Cursor cursor = null;
        try
        {
            if(Logger.isDebugEnabled())
            {
                Logger.debug("%s.%s: About to get cursor", CLASS_NAME, "getStocks");
            }
            
            cursor = context.getContentResolver().query(uri, projection, null, null, StockTable.COLUMN_STOCK);
            
            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        list.add(createStock(cursor));
                    } while (cursor.moveToNext());
                }
            }
            return list;
        }
        finally
        {
            close(cursor);
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
    
    private void close(Cursor cursor)
    {
        if(cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }
    }

    public int update(long id, double quantity)
    {
        ContentValues values = new ContentValues();
        values.put(StockTable.COLUMN_QUANTITY, quantity);
        
        final Uri uri = Uri.parse(StockContract.CONTENT_URI.toString() + "/" + id);
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Uri for content provider = '%s'", CLASS_NAME, "update", uri);
        }
        
        int rowsUpdated = context.getContentResolver().update(uri, values, null, null);
        return rowsUpdated;
    }

}
