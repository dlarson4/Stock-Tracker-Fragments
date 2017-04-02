package com.stocktracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.stocktracker.contentprovider.StockContract;
import com.stocktracker.data.Stock;

import java.util.ArrayList;
import java.util.List;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Facade for the content provider defined in <code>StockContract</code>
 *
 * @author dlarson
 */
public class StockContentProviderFacade {
    private final static String TAG = StockContentProviderFacade.class.getSimpleName();

    private Context context;

    public StockContentProviderFacade(Context context) {
        this.context = context;
    }

    public Stock insert(String stock, double quantity) {
        if (DEBUG) Log.d(TAG, "insert");
        ContentValues values = new ContentValues();
        values.put(StockTable.COLUMN_STOCK, stock);
        values.put(StockTable.COLUMN_QUANTITY, quantity);
        values.put(StockTable.COLUMN_DATE_CREATED, System.currentTimeMillis());

        final Uri uri = StockContract.CONTENT_URI;

        if (DEBUG) Log.d(TAG, "Uri for content provider = " + uri);

        Uri newRowUri = context.getContentResolver().insert(uri, values);

        if (DEBUG) Log.d(TAG, "newRowUri = " + newRowUri);

        if(newRowUri != null) {
            Cursor cursor = context.getContentResolver().query(newRowUri, StockTable.ALL_COLUMNS, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                Stock newStock = createStock(cursor);
                close(cursor);
                return newStock;
            }
        }

        return null;
    }

    public int delete(long id) {
        final Uri uri = Uri.parse(StockContract.CONTENT_URI.toString() + "/" + id);

        if (DEBUG) Log.d(TAG, "Uri for content provider = " + uri);

        int rowsDeleted = context.getContentResolver().delete(uri, null, null);

        if (DEBUG) Log.d(TAG, "rowsDeleted = " + rowsDeleted);

        return rowsDeleted;
    }

    public boolean isDuplicate(String stock) {
        final String[] projection = {StockTable.COLUMN_STOCK};
        final String selectionClause = StockTable.COLUMN_STOCK + " = ?";
        final String[] selectionArgs = {stock};

        final Uri uri = StockContract.CONTENT_URI;

        if (DEBUG) Log.d(TAG, "Uri for content provider = " + uri);

        Cursor cursor = null;
        try {
            if (DEBUG) Log.d(TAG, "About to get cursor");
            cursor = context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);
            return (cursor != null && cursor.moveToFirst());
        } finally {
            close(cursor);
        }
    }

    public List<Stock> getStocks() {
        List<Stock> list = new ArrayList<>();

        final String[] projection = StockTable.ALL_COLUMNS;
        final Uri uri = StockContract.CONTENT_URI;

        if (DEBUG) Log.d(TAG, "Uri for content provider = " + uri);


        Cursor cursor = null;
        try {
            if (DEBUG) Log.d(TAG, "About to get cursor");

            cursor = context.getContentResolver().query(uri, projection, null, null, StockTable.COLUMN_STOCK);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        list.add(createStock(cursor));
                    } while (cursor.moveToNext());
                }
            }
            return list;
        } finally {
            close(cursor);
        }
    }

    private Stock createStock(Cursor cursor) {
        long id = cursor.getLong(0);
        String stock = cursor.getString(1);
        double quantity = cursor.getDouble(2);
        long dateCreated = cursor.getLong(3);
        return new Stock(id, stock, quantity, dateCreated);
    }

    private void close(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public int update(long id, double quantity) {
        ContentValues values = new ContentValues();
        values.put(StockTable.COLUMN_QUANTITY, quantity);

        final Uri uri = Uri.parse(StockContract.CONTENT_URI.toString() + "/" + id);

        if (DEBUG) Log.d(TAG, "Uri for content provider = " + uri);

        int rowsUpdated = context.getContentResolver().update(uri, values, null, null);
        return rowsUpdated;
    }

}
