package com.stocktracker.db;

import com.stocktracker.log.Logger;

import android.database.sqlite.SQLiteDatabase;

public class StockTable
{
    private final static String CLASS_NAME = StockTable.class.getSimpleName();
    
    public static final String STOCK_TABLE_NAME = "stock";
    
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_DATE_CREATED = "date_created";
    
    public final static String[] ALL_COLUMNS =
        { StockTable.COLUMN_ID, StockTable.COLUMN_STOCK, StockTable.COLUMN_QUANTITY, StockTable.COLUMN_DATE_CREATED };
    
    private static final String DATABASE_CREATE = "create table " + STOCK_TABLE_NAME + 
            "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_STOCK + " text not null, " +
            COLUMN_QUANTITY + " numeric not null, " +
            COLUMN_DATE_CREATED + " numeric not null" +
            ");";
    
    public static void onCreate(SQLiteDatabase database) 
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Creating SQLLite database as '%s'.", CLASS_NAME, "onCreate", DATABASE_CREATE);
        }
        
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: Upgrading database from version %d to %d", CLASS_NAME, "onUpgrade");
        }
        
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//        onCreate(database);
    }
    
    private StockTable()
    {
    }
}
