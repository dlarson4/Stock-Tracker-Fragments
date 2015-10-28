package com.stocktracker.contentprovider.material;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.stocktracker.contentprovider.StockContract;
import com.stocktracker.db.StockDatabaseHelper;
import com.stocktracker.db.StockTable;

import static com.stocktracker.BuildConfig.DEBUG;
import static com.stocktracker.contentprovider.StockContract.AUTHORITY;
import static com.stocktracker.contentprovider.StockContract.BASE_PATH;
import static com.stocktracker.contentprovider.StockContract.CONTENT_URI;

public class StockContentProvider extends ContentProvider {
    private final static String TAG = StockContentProvider.class.getSimpleName();

    private StockDatabaseHelper database;

    // helper constants for use with the UriMatcher
    private static final int STOCKS = 10;
    private static final int STOCK_ID = 20;

    public static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, STOCKS);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", STOCK_ID);
    }

    @Override
    public boolean onCreate() {
        database = new StockDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG) Log.d(TAG, "uri = " + uri);


        checkColumns(projection);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(StockTable.STOCK_TABLE_NAME);

        int uriType = URI_MATCHER.match(uri);

        if (DEBUG) Log.d(TAG, "uriType = " + uriType);


        switch (uriType) {
            case STOCKS:
                break;
            case STOCK_ID:
                queryBuilder.appendWhere(StockTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private void checkColumns(String[] projection) {
        if (projection != null) {
            java.util.HashSet<String> requestedColumns = new java.util.HashSet<String>(java.util.Arrays.asList(projection));
            java.util.HashSet<String> availableColumns = new java.util.HashSet<String>(java.util.Arrays.asList(StockTable.ALL_COLUMNS));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        if (DEBUG) Log.d(TAG, "uri = " + uri);


        switch (URI_MATCHER.match(uri)) {
            case STOCK_ID:
                return StockContract.CONTENT_ITEM_TYPE;
            case STOCKS:
                return StockContract.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (DEBUG) Log.d(TAG, "uri = " + uri);


        int uriType = URI_MATCHER.match(uri);
        long id = 0;
        SQLiteDatabase db = database.getWritableDatabase();

        switch (uriType) {
            case STOCKS:
                id = db.insert(StockTable.STOCK_TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(CONTENT_URI.toString() + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (DEBUG) Log.d(TAG, "uri = " + uri);


        int uriType = URI_MATCHER.match(uri);
        int rowsDeleted = 0;
        SQLiteDatabase db = database.getWritableDatabase();

        switch (uriType) {
            case STOCKS: {
                rowsDeleted = db.delete(StockTable.STOCK_TABLE_NAME, selection, selectionArgs);
                break;
            }
            case STOCK_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(StockTable.STOCK_TABLE_NAME, StockTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(StockTable.STOCK_TABLE_NAME, StockTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (DEBUG) Log.d(TAG, "uri = " + uri);


        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case STOCKS: {
                rowsUpdated = sqlDB.update(StockTable.STOCK_TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case STOCK_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(StockTable.STOCK_TABLE_NAME, values, StockTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(StockTable.STOCK_TABLE_NAME, values, StockTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}
