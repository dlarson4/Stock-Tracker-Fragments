package com.stocktracker.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.stocktracker.data.Stock;

import java.util.List;

/**
 * Created by dlarson on 5/29/17.
 */
@Dao
public interface StockDao {
    @Query("SELECT * FROM stock")
    List<Stock> getAll();

    @Query("SELECT * FROM stock")
    LiveData<List<Stock>> getStocksObservable();

    @Insert
    void insert(Stock stock);

    @Delete
    void delete(Stock stock);

    @Query("SELECT * FROM stock where stock = :symbol LIMIT 1")
    Stock findByTicker(String symbol);

    @Update
    void update(Stock stock);
}
