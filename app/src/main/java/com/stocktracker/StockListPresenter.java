package com.stocktracker;

import android.support.annotation.Nullable;
import android.util.Log;

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

    private final StockListContract.View view;

    public StockListPresenter(StockListContract.View view) {
        this.view = view;
    }

    @Nullable
    public void getStockQuotes(final List<Stock>  stocks) {
        if (DEBUG) Log.d(TAG, "getStockQuotes: ");

        final List<String> stockSymbols = new ArrayList<>();
        Observable.fromIterable(stocks)
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

                    view.hideSwipeProgress();
                    view.displayQuoteResponse(quoteResponse);
                    view.enableSwipe();
                }, throwable -> {
                    if (DEBUG) Log.e(TAG, "accept: ", throwable);

                    view.hideSwipeProgress();
                    view.displayQuoteResponse(null);
                    view.enableSwipe();
                });
    }

    Observable<HttpResponse> getStockQuoteObservable(final List<String> stockSymbols) {
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
