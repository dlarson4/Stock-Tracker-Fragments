package com.stocktracker;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
import com.stocktracker.db.AppDatabase;
import com.stocktracker.db.DatabaseCreator;
import com.stocktracker.http.HttpRequest;
import com.stocktracker.http.HttpResponse;
import com.stocktracker.parser.QuoteResponseParser;
import com.stocktracker.util.StringUtil;
import com.stocktracker.util.UrlBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 4/17/17.
 */
public class StockListPresenter implements StockListContract.Presenter {
    private static final String TAG = "StockListPresenter";

    private final Application application;
    private final StockListContract.View view;

    // list of stocks loaded from the database
    private List<Stock> stockList;

    public StockListPresenter(Application application, StockListContract.View view) {
        this.application = application;
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (DEBUG) Log.d(TAG, "start: ");
        updateStockList();
    }

    @Override
    public void onSwipeRefresh() {
        updateStockList();
    }

    @Override
    @Nullable
    public Stock getStock(int selectedIndex) {
        return stockList == null ? null : stockList.get(selectedIndex);
    }

    private AppDatabase getDatabase() {
        return DatabaseCreator.getInstance(application).getDatabase();
    }

    /**
     * Called by the controlling Activity when the stocks should be updated, like when
     * one was added, deleted, or a quantity changed
     */
    private void updateStockList() {
        if (DEBUG) Log.d(TAG, "updateStockList");
        loadStockListFromDatabase();
    }

    private void loadStockListFromDatabase() {
        if (DEBUG) Log.d(TAG, "loadStockListFromDatabase");

        getStocksObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stocks -> {
                    Log.d(TAG, "loadStockListFromDatabase: stocks = " + stocks);
                    this.stockList = stocks;
                    if(!this.stockList.isEmpty()) {
                        retrieveStockQuotesFromWebService();
                    }
                }, throwable -> {
                    if (DEBUG) Log.e(TAG, "", throwable);
                    view.showErrorMessage();
                });
    }

    private void retrieveStockQuotesFromWebService() {
        if (DEBUG) Log.d(TAG, "retrieveStockQuotesFromWebService");

        view.disableSwipe();
        view.showSwipeProgress();
        getStockQuotes();
    }

    private void getStockQuotes() {
        if (DEBUG) Log.d(TAG, "getStockQuotes: ");

        final List<String> stockSymbols = new ArrayList<>();

        Observable.fromIterable(this.stockList)
                .map(stock -> stock.getSymbol())
                .forEach(stockSymbols::add);

        getStockQuoteObservable(stockSymbols)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(response -> {
                    if(response == HttpResponse.INVALID_URL || response == HttpResponse.SERVER_UNAVAILABLE) {
                        return Observable.error(new RuntimeException(response.getStatus().name()));
                    }
                    QuoteResponse quoteResponse = parseResponse(response);
                    if(quoteResponse == null) {
                        return Observable.just(new QuoteResponse.Builder().build());
                    }
                    return Observable.just(quoteResponse);
                })
                .subscribe(quoteResponse -> {
                    if (DEBUG) Log.d(TAG, "accept() quoteResponse = [" + quoteResponse + "]");

                    updateStockListDone(quoteResponse);
                }, throwable -> {
                    if (DEBUG) Log.e(TAG, "accept: ", throwable);
                    view.showErrorMessage();
                });
    }

    /**
     * Called by the handler after refreshed stock list data is retrieved
     *
     * @param quoteResponse Quote response object
     */
    private void updateStockListDone(QuoteResponse quoteResponse) {
        if (quoteResponse == null) {
            view.showErrorMessage();
        } else {
            updateQuoteResponseObjects(quoteResponse);
            if (view != null) {
                List<Quote> quoteList = quoteResponse.getQuotes();
                view.updateStockListDisplay(quoteList);
            }
        }

        if (view != null) {
            view.hideSwipeProgress();
            view.enableSwipe();
        }
    }

    /**
     * Update each Quote object in the quoteList with the quantity from the database
     *
     * @param quotes Quote object
     */
    private void updateQuoteResponseObjects(QuoteResponse quotes) {
        if (quotes != null && !quotes.getLang().isEmpty() && stockList != null && !stockList.isEmpty()) {
            for (Quote q : quotes.getQuotes()) {
                Stock s = getStockBySymbol(stockList, q.getSymbol());
                if (s != null) {
                    if (DEBUG) Log.d(TAG, "Adding quantity '" + s.getQuantity() + "' to quote " + q.getSymbol());

                    q.setQuantity(s.getQuantity());
                    if (DEBUG) Log.d(TAG, "Adding SQLite id '" + s.getId() + "' to quote " + q.getSymbol());
                    q.setId(s.getId());

                    if (DEBUG) Log.d(TAG, "updateQuoteResponseObjects: " + q);
                }
            }
        }
    }

    private Stock getStockBySymbol(List<Stock> stocks, String symbol) {
        for (Stock s : stocks) {
            if (s.getSymbol() != null && s.getSymbol().equalsIgnoreCase(symbol)) {
                return s;
            }
        }
        return null;
    }

    private Observable<HttpResponse> getStockQuoteObservable(final List<String> stockSymbols) {
        return Observable.fromCallable(() -> {
            Map<String, String> stocksQuoteParams = UrlBuilder.getStockQuoteParams(stockSymbols);
            HttpRequest httpRequest = new HttpRequest(UrlBuilder.YQL_URL, stocksQuoteParams);
            return httpRequest.doGet();
        });
    }

    private Observable<List<Stock>> getStocksObservable() {
        return Observable.fromCallable(() -> getDatabase().stockDao().getAll());
    }

    private QuoteResponse parseResponse(HttpResponse response) {
        if (DEBUG) Log.d(TAG, "parseResponse() response = [" + response + "]");

        QuoteResponse quoteResponse = null;

        try {
            String responseData = response.getData();
            if(StringUtil.isBlank(responseData)) {
                if (DEBUG) Log.d(TAG, "parseResponse: response body is blank");
                quoteResponse = new QuoteResponse.Builder().build();
            } else {
                JSONObject json = new JSONObject(responseData);
                if (DEBUG) Log.d(TAG, "parseResponse: json = " + (json == null ? "null" : json.toString(4)));

                quoteResponse = new QuoteResponseParser().parse(json);
                if (DEBUG) Log.d(TAG, "parseResponse: quoteResponse = " + quoteResponse);
            }
        } catch (JSONException e) {
            if (DEBUG) Log.d(TAG, "", e);
        }
        return quoteResponse;
    }
}
