package com.stocktracker;

import android.app.Application;
import android.arch.lifecycle.LifecycleOwner;
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
    private final LifecycleOwner lifecycleOwner;
    private final StockListContract.View view;

    // list of stocks loaded from the database
    private List<Stock> stockList;

    public StockListPresenter(
            Application application,
            LifecycleOwner lifecycleOwner,
            StockListContract.View view) {
        this.application = application;
        this.lifecycleOwner = lifecycleOwner;
        this.view = view;
        view.setPresenter(this);
        setupStockSubscription();
    }

    private void setupStockSubscription() {
        getDatabase().stockDao().getStocksObservable().observe(lifecycleOwner, stocks -> {
            Log.d(TAG, "setupStockSubscription() stocks = " + stocks);
            this.stockList = stocks;
            if(!this.stockList.isEmpty()) {
                getStockQuotes();
            }
        });
    }

    @Override
    public void start() {
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
        getStockQuotes();
    }

//    final static class SubmitUiModel {
//        final boolean inProgress;
//        final boolean success;
//        final String errorMessage;
//        final HttpResponse httpResponse;
//
//        public SubmitUiModel(
//                boolean inProgress,
//                boolean success,
//                String errorMessage,
//                HttpResponse httpResponse) {
//            this.inProgress = inProgress;
//            this.success = success;
//            this.errorMessage = errorMessage;
//            this.httpResponse = httpResponse;
//        }
//
//        static SubmitUiModel inProgress() {
//            return new SubmitUiModel(true, false, null, null);
//        }
//
//        static SubmitUiModel success(HttpResponse httpResponse) {
//            return new SubmitUiModel(false, true, null, httpResponse);
//        }
//
//        static SubmitUiModel failure(String errorMessage) {
//            return new SubmitUiModel(false, false, errorMessage, null);
//        }
//    }

    private void getStockQuotes() {
        if (DEBUG) Log.d(TAG, "getStockQuotes: ");

        final List<String> stockSymbols = new ArrayList<>();

        Observable.fromIterable(this.stockList)
                .map(stock -> stock.getSymbol())
                .forEach(stockSymbols::add);

        view.disableSwipe();
        view.showSwipeProgress();

//        Observable.just(stockSymbols)
//                .flatMap( symbols -> get(symbols)
//                        .map(httpResponse -> SubmitUiModel.success(httpResponse))
//                        .onErrorReturn(t -> SubmitUiModel.failure(t.getMessage()))
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribeOn(Schedulers.io())
//                        .startWith(SubmitUiModel.inProgress()))
//                .subscribe(model -> {
//                    view.setSwipeEnabled(!model.inProgress);
//                    view.showSwipeProgress(model.inProgress);
//                    if(!model.inProgress) {
//                        if(model.success) {
//                            responseReceived(model.httpResponse);
//                        } else {
//                            view.showErrorMessage(application.getString(R.string.no_server_response));
//                        }
//                    }
//                }, throwable -> {
//                    Log.e(TAG, "getStockQuotes:error ", throwable);
//                    view.showErrorMessage(throwable.getMessage());
//                });

        getStockQuoteObservable(stockSymbols)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(httpResponse -> view.showSwipeProgress(true))
                .flatMap(response -> {
                    Log.d(TAG, "getStockQuotes() response = " + response);

                    if(response == HttpResponse.INVALID_URL || response == HttpResponse.SERVER_UNAVAILABLE) {
                        return Observable.error(new RuntimeException(application.getString(R.string.unable_to_contact_server)));
                    }
                    QuoteResponse quoteResponse = parseResponse(response);
                    if(quoteResponse == null) {
                        return Observable.just(new QuoteResponse.Builder().build());
                    }
                    return Observable.just(quoteResponse);
                }).doFinally(() -> {
                    Log.d(TAG, "doFinally() called");
                    if (view != null) {
                        view.showSwipeProgress(false);
                        view.enableSwipe();
                    }
                })
                .subscribe(quoteResponse -> {
                    if (DEBUG) Log.d(TAG, "accept() quoteResponse = [" + quoteResponse + "]");

                    updateStockListDone(quoteResponse);
                }, throwable -> {
                    if (DEBUG) Log.e(TAG, "accept: ", throwable);
                    view.showErrorMessage(throwable.getMessage());
                });
    }

//    private Observable<HttpResponse> get(List<String> symbols) {
//            Map<String, String> stocksQuoteParams = UrlBuilder.getStockQuoteParams(symbols);
//            HttpRequest httpRequest = new HttpRequest(UrlBuilder.YQL_URL, stocksQuoteParams);
//            return Observable.just(httpRequest.doGet());
//    }
//
//    private void responseReceived(HttpResponse response) {
//        if(response == HttpResponse.INVALID_URL || response == HttpResponse.SERVER_UNAVAILABLE) {
//            view.showErrorMessage(application.getString(R.string.unable_to_contact_server));
//        } else {
//            updateStockListDone(parseResponse(response));
//        }
//    }

    /**
     * Called by the handler after refreshed stock list data is retrieved
     *
     * @param quoteResponse Quote response object
     */
    private void updateStockListDone(QuoteResponse quoteResponse) {
        if (quoteResponse == null) {
            view.showErrorMessage("No response from server.");
        } else {
            updateQuoteResponseObjects(quoteResponse);
            if (view != null) {
                List<Quote> quoteList = quoteResponse.getQuotes();
                view.updateStockListDisplay(quoteList);
            }
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
