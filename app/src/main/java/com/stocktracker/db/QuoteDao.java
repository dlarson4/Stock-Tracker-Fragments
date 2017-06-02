package com.stocktracker.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.stocktracker.data.Quote;

import java.util.List;

/**
 * Created by dlarson on 5/29/17.
 */
@Dao
public interface QuoteDao {
    @Query("SELECT * FROM quote")
    List<Quote> getAll();

    @Insert
    void insert(Quote quote);

    @Delete
    void delete(Quote quote);

    @Query("SELECT * FROM quote where symbol = :symbol LIMIT 1")
    Quote findByTicker(String symbol);

    @Update
    void update(Quote quote);
}
