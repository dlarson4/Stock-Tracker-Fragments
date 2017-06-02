package com.stocktracker.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.stocktracker.data.Stock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by dlarson on 5/29/17.
 */
public class StockDaoTest {

    private AppDatabase db;
    private StockDao stockDao;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        stockDao = db.stockDao();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void writeAndReadStock() throws Exception {
        String symbol = "spy";
        Stock stock = createStock(symbol);

        stockDao.insert(stock);
        Stock spy = stockDao.findByTicker(symbol);
        assertNotNull(spy);
        assertTrue(spy.getSymbol().equals(symbol));
    }

    @Test
    public void deleteStock() throws Exception {
        String symbol = "spy";
        Stock stock = createStock(symbol);

        stockDao.insert(stock);
        Stock spy = stockDao.findByTicker(symbol);
        assertNotNull(spy);
        assertTrue(spy.getSymbol().equals(symbol));

        stockDao.delete(stock);

        List<Stock> stocks = stockDao.getAll();
        assertTrue(stocks.isEmpty());
    }

    private Stock createStock(String symbol) {
        return new Stock(System.currentTimeMillis(), symbol, 12.34);
    }

}