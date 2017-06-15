package com.stocktracker;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.data.QuoteUpdateResult;
import com.stocktracker.data.Stock;
import com.stocktracker.data.StockQuote;
import com.stocktracker.http.HttpRequest;
import com.stocktracker.http.HttpResponse;
import com.stocktracker.parser.QuoteResponseParser;
import com.stocktracker.util.StringUtil;
import com.stocktracker.util.UrlBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 6/11/17.
 */
public class StockQuoteViewModel extends ViewModel {
    private static final String TAG = "StockQuoteViewModel";

    private MutableLiveData<List<StockQuote>> stockQuotes = new MutableLiveData<>();
    private MutableLiveData<QuoteUpdateResult> updateResult = new MutableLiveData<QuoteUpdateResult>();

    public void setStocks(List<Stock> stocks) {
        if (DEBUG) Log.d(TAG, "setStocks() stocks = [" + stocks + "]");

        if(stocks == null || stocks.isEmpty()) {
            return;
        }
        loadQuotes(stocks);
    }

    public MutableLiveData<List<StockQuote>> getStockQuotes() {
        return stockQuotes;
    }

    public MutableLiveData<QuoteUpdateResult> getUpdateResult() {
        return updateResult;
    }

    private void loadQuotes(List<Stock> stocks) {
        if (DEBUG) Log.d(TAG, "loadQuotes() stocks = [" + stocks + "]");

        Observable.fromIterable(stocks)
                .map(stock -> stock.getSymbol())
                .toList()
                .toObservable()
                .map(stockSymbols -> UrlBuilder.getStockQuoteParams(stockSymbols))
                .observeOn(Schedulers.io())
                .flatMap(params -> {
                    HttpRequest httpRequest = new HttpRequest(UrlBuilder.YQL_URL, params);
                    return Observable.just(httpRequest.doGet());
                })
                .flatMap(response -> {
                    Log.d(TAG, "getStockQuotes() response = " + response);
                    if(response == HttpResponse.INVALID_URL || response == HttpResponse.SERVER_UNAVAILABLE) {
                        return Observable.error(new QuoteException(HttpResponse.Status.ServerUnavailable));
                    }
                    QuoteResponse quoteResponse = parseResponse(response);
                    if(quoteResponse == null) {
                        return Observable.just(new QuoteResponse.Builder().build());
                    }
                    return Observable.just(quoteResponse);
                })
                .flatMap(quoteResponse -> Observable.just(quoteResponseToStockQuotes(quoteResponse, stocks)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<StockQuote>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<StockQuote> value) {
                        if (DEBUG) Log.d(TAG, "onNext() value = [" + value + "]");
                        updateResult.setValue(QuoteUpdateResult.success());
                        stockQuotes.setValue(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DEBUG) Log.e(TAG, "onError: ", e);
                        stockQuotes.setValue(stockQuotes.getValue());
                        if(e instanceof QuoteException) {
                            updateResult.setValue(QuoteUpdateResult.failure(((QuoteException) e).status));
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private class QuoteException extends Exception {
        HttpResponse.Status status;
        QuoteException(HttpResponse.Status status) {
            this.status = status;
        }
    }

    private List<StockQuote> quoteResponseToStockQuotes(
            QuoteResponse quoteResponse,
            List<Stock> stockList) {

        Map<String, Stock> stockMap = stockMap(stockList);

        List<StockQuote> stockQuoteList = Observable
                .fromIterable(quoteResponse.getQuotes())
                .map(quote -> StockQuote.builder()
                        .change(quote.getChange())
                        .lastTradePriceOnly(quote.getLastTradePriceOnly())
                        .name(quote.getName())
                        .quantity(stockMap.get(quote.getSymbol()).getQuantity())
                        .stockExchange(quote.getStockExchange())
                        .symbol(quote.getSymbol())
                        .build())
                .toList().blockingGet();

        return (stockQuoteList);
    }

    private Map<String, Stock> stockMap(List<Stock> stocks) {
        if(stocks == null || stocks.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Stock> stockMap = new HashMap<>();
        int size = stocks.size();
        for (int i = 0; i < size; i++) {
            stockMap.put(stocks.get(i).getSymbol(), stocks.get(i));
        }
        return stockMap;
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
