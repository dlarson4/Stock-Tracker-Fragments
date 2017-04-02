package com.stocktracker.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static com.stocktracker.BuildConfig.DEBUG;

public class StockTable {
    private final static String TAG = StockTable.class.getSimpleName();

    public static final String STOCK_TABLE_NAME = "stock";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_DATE_CREATED = "date_created";

    public final static String[] ALL_COLUMNS =
            {StockTable.COLUMN_ID, StockTable.COLUMN_STOCK, StockTable.COLUMN_QUANTITY, StockTable.COLUMN_DATE_CREATED};

    private static final String DATABASE_CREATE = "create table " + STOCK_TABLE_NAME +
            "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_STOCK + " text not null, " +
            COLUMN_QUANTITY + " numeric not null, " +
            COLUMN_DATE_CREATED + " numeric not null" +
            ");";


    public static void onCreate(SQLiteDatabase database) {
        if (DEBUG) Log.d(TAG, "Creating SQLLite database as " + DATABASE_CREATE);

        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (DEBUG) Log.d(TAG, "Upgrading database from version %d to %d");

//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//        onCreate(database);
    }

    private StockTable() {
    }
}
