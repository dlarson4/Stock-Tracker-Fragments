package com.stocktracker;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.Stock;
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
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 4/17/17.
 */
public class StockListPresenter implements StockListContract.Presenter {
    private static final String TAG = "StockListPresenter";

    private static final int LOADER_ID = 0;

    private final Context context;
    private final StockListContract.View view;

    // list of stocks loaded from the database
    private List<Stock> stockList;

    // implementation of LoaderManager.LoaderCallbacks, used to load stocks from the database
    private StockLoader stockLoader = null;

    public StockListPresenter(Context context, StockListContract.View view) {
        this.context = context;
        this.view = view;
        stockLoader = new StockLoader(this.context, stockLoaderCallback);
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

    private final StockLoader.StockLoaderCallback stockLoaderCallback = stocks -> {
        if (DEBUG) Log.d(TAG, "onStocksLoadedFromDatabase() stocks = [" + stocks + "]");

        this.stockList = stocks;
        retrieveStockQuotesFromWebService();
    };

    /**
     * Called by the controlling Activity when the stocks should be updated, like when
     * one was added, deleted, or a quantity changed
     */
    private void updateStockList() {
        if (DEBUG) Log.d(TAG, "updateStockList");
        loadStockListFromDatabase();
    }

    /**
     * Start a loader to retrieve the stock list from the database.  (This eventually initiates the process of
     * retrieving quote information from the web service and performing a full refresh)
     */
    private void loadStockListFromDatabase() {
        if (DEBUG) Log.d(TAG, "loadStockListFromDatabase");
        view.getLoaderManager().initLoader(LOADER_ID, null, stockLoader);
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
                .flatMap(new Function<HttpResponse, ObservableSource<QuoteResponse>>() {
                    @Override
                    public ObservableSource<QuoteResponse> apply(HttpResponse response) throws Exception {
                        if(response == HttpResponse.INVALID_URL || response == HttpResponse.SERVER_UNAVAILABLE) {
                            return Observable.error(new RuntimeException(response.getStatus().name()));
                        }
                        return Observable.just(parseResponse(response));
                    }
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

    private QuoteResponse parseResponse(HttpResponse response) {
        if (DEBUG) Log.d(TAG, "parseResponse() response = [" + response + "]");

        QuoteResponse quoteResponse = null;

        try {
            String responseData = response.getData();
            if(StringUtil.isBlank(responseData)) {
                if (DEBUG) Log.d(TAG, "parseResponse: response body is blank");
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
