package com.stocktracker.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.stocktracker.data.Quote;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

/**
 * Created by dlarson on 5/29/17.
 */
@RunWith(AndroidJUnit4.class)
public class QuoteDaoTest {

    private AppDatabase db;
    private QuoteDao quoteDao;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        quoteDao = db.quoteDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void writeAndReadQuote() throws Exception {
        String name = "SPDR S&P 500";
        String symbol = "spy";
        Quote quote = createQuote(symbol, name);

        quoteDao.insert(quote);
        Quote spy = quoteDao.findByTicker(symbol);
        assertNotNull(spy);
        assertTrue(spy.getName().equals(name));
        assertTrue(spy.getSymbol().equals(symbol));
    }

    @Test
    public void deleteQuote() throws Exception {
        String name = "SPDR S&P 500";
        String symbol = "spy";
        Quote quote = createQuote(symbol, name);

        quoteDao.insert(quote);
        Quote spy = quoteDao.findByTicker(symbol);
        assertNotNull(spy);
        assertTrue(spy.getName().equals(name));
        assertTrue(spy.getSymbol().equals(symbol));

        quoteDao.delete(quote);

        List<Quote> quotes = quoteDao.getAll();
        assertTrue(quotes.isEmpty());
    }

    private Quote createQuote(String symbol, String name) {
        return new Quote.Builder()
                .symbol(symbol)
                .averageDailyVolume("77920096")
                .change("-0.05")
                .daysLow("241.45")
                .daysHigh("241.90")
                .yearLow("198.65")
                .yearHigh("242.08")
                .marketCapitalization("null")
                .lastTradePriceOnly("241.71")
                .daysRange("241.45 - 241.90")
                .name(name)
                .volume("46629905")
                .stockExchange("PCX")
                .build();
    }


}