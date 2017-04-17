package com.stocktracker;

import android.util.Log;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.http.HttpRequest;
import com.stocktracker.http.HttpResponse;
import com.stocktracker.parser.QuoteResponseParser;
import com.stocktracker.util.StringUtil;
import com.stocktracker.util.UrlBuilder;

import org.json.JSONException;
import org.json.JSONObject;

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

public class AddStockPresenter {
    private static final String TAG = "AddStockPresenter";

    private final AddStockContract.View view;

    public AddStockPresenter(AddStockContract.View view) {
        this.view = view;
    }


    public void getStockQuote(String stockSymbol, double quantity) {

        getStockQuoteObservable(stockSymbol)
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

                    view.quoteLoaded(quoteResponse, quantity);
                }, throwable -> {
                    if (DEBUG) Log.e(TAG, "accept: ", throwable);

                    view.quoteLoaded(null, quantity);
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
                if (DEBUG) Log.d(TAG, "parseResponse: json = " + json);

                quoteResponse = new QuoteResponseParser().parse(json);
                if (DEBUG) Log.d(TAG, "parseResponse: quoteResponse = " + quoteResponse);
            }
        } catch (JSONException e) {
            if (DEBUG) Log.d(TAG, "", e);
        }

        return quoteResponse;
    }

    Observable<HttpResponse> getStockQuoteObservable(final String stockSymbol) {
        return Observable.fromCallable(() -> {
            Map<String, String> stocksQuoteParams = UrlBuilder.getStockQuoteParams(stockSymbol);
            HttpRequest httpRequest = new HttpRequest(UrlBuilder.YQL_URL, stocksQuoteParams);
            return httpRequest.doGet();
        });
    }

}
