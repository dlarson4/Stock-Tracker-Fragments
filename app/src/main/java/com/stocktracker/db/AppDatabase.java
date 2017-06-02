package com.stocktracker.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.stocktracker.data.Quote;
import com.stocktracker.data.Stock;

/**
 * Created by dlarson on 5/29/17.
 */
@Database(entities = {Stock.class, Quote.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract StockDao stockDao();
    public abstract QuoteDao quoteDao();
}
