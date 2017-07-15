package com.stocktracker.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.stocktracker.data.Quote;
import com.stocktracker.data.Stock;

import java.util.List;

/**
 * Created by dlarson on 5/29/17.
 */
@Dao
public interface QuoteDao {
    @Query("SELECT * FROM quote")
    List<Quote> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Quote quote);

    @Delete
    void delete(Quote quote);

    @Query("SELECT * FROM quote where symbol = :symbol LIMIT 1")
    Quote findByTicker(String symbol);

    @Update
    void update(Quote quote);

    @Query("SELECT * FROM quote")
    LiveData<List<Quote>> getQuotesObservable();
}
