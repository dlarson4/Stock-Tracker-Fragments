package com.stocktracker;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stocktracker.api.StockQuoteService;
import com.stocktracker.api.gson.QuoteTypeAdapterFactory;
import com.stocktracker.data.Quote;
import com.stocktracker.data.Resource;
import com.stocktracker.data.Stock;
import com.stocktracker.data.StockQuote;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 6/11/17.
 */
public class StockQuoteViewModel extends ViewModel {
    private static final String TAG = "StockQuoteViewModel";

    private MutableLiveData<Resource<List<StockQuote>>> resource = new MutableLiveData<>();

    public StockQuoteViewModel() {
    }

    public void setStocks(List<Stock> stocks) {
        if (DEBUG) Log.d(TAG, "setStocks() stocks = [" + stocks + "]");

        if(stocks == null || stocks.isEmpty()) {
            return;
        }
        loadQuotes(stocks);
    }

    public MutableLiveData<Resource<List<StockQuote>>> getResource() {
        return resource;
    }

    private void loadQuotes(List<Stock> stocks) {
        if (DEBUG) Log.d(TAG, "loadQuotes() stocks = [" + stocks + "]");

//        Observable.fromIterable(stocks)
//                .map(stock -> stock.getSymbol())
//                .toList()
//                .toObservable()
//                .map(stockSymbols -> UrlBuilder.getStockQuoteParams(stockSymbols))
//                .observeOn(Schedulers.io())
//                .flatMap(params -> {
//                    HttpRequest httpRequest = new HttpRequest(UrlBuilder.YQL_URL, params);
//                    return Observable.just(httpRequest.doGet());
//                })
//                .flatMap(response -> {
//                    Log.d(TAG, "getStockQuotes() response = " + response);
//                    if(response == HttpResponse.INVALID_URL || response == HttpResponse.SERVER_UNAVAILABLE) {
//                        return Observable.error(new QuoteException(HttpResponse.Status.ServerUnavailable));
//                    }
//                    QuoteResponse quoteResponse = parseResponse(response);
//                    if(quoteResponse == null) {
//                        return Observable.just(new QuoteResponse.Builder().build());
//                    }
//                    return Observable.just(quoteResponse);
//                })
//                .flatMap(quoteResponse -> Observable.just(quoteResponseToStockQuotes(quoteResponse, stocks)))
//                .doOnNext(new Consumer<List<StockQuote>>() {
//                    @Override
//                    public void accept(List<StockQuote> stockQuotes) throws Exception {
//                        if(stockQuotes != null && !stockQuotes.isEmpty()) {
//                            int size = stockQuotes.size();
//                            for (int i = 0; i < size; i++) {
//                                Quote quote = new Quote.Builder()
//                                        .symbol(stockQuotes.get(i).symbol())
//                                        .name(stockQuotes.get(i).name())
//                                        .quantity(stockQuotes.get(i).quantity())
//                                        .stockExchange(stockQuotes.get(i).stockExchange())
//                                        .lastTradePriceOnly(stockQuotes.get(i).lastTradePriceOnly())
//                                        .change(stockQuotes.get(i).change())
//                                        .build();
//                                if (DEBUG) Log.d(TAG, "loadQuotes: inserting quote " + quote);
//                                getDatabase().quoteDao().insert(quote);
//                            }
//                        }
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<List<StockQuote>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(List<StockQuote> value) {
//                        if (DEBUG) Log.d(TAG, "onNext() value = [" + value + "]");
//                        updateResult.setValue(QuoteUpdateResult.success());
//                        stockQuotes.setValue(value);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        if (DEBUG) Log.e(TAG, "onError: ", e);
//                        stockQuotes.setValue(stockQuotes.getValue());
//                        if(e instanceof QuoteException) {
//                            updateResult.setValue(QuoteUpdateResult.failure(((QuoteException) e).status));
//                        }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });

        Map<String, String> queryParams = getQueryParams(symbolsAsCommaSeparatedList(stocks));
        getStockQuoteService().quoteList(queryParams).enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                if (DEBUG) Log.d(TAG, "onResponse: response = " + response);
                if (DEBUG) Log.d(TAG, "onResponse: response.body() = " + response.body());
                if (response.isSuccessful()) {
                    List<StockQuote> value = quotesToStockQuotes(response.body(), stocks);
                    resource.setValue(Resource.success(value));

//                    updateResult.setValue(QuoteUpdateResult.success());
//                    stockQuotes.setValue(value);
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                if (DEBUG) Log.e(TAG, "onFailure: ", t);
                resource.setValue(Resource.error(t.getMessage(), resource.getValue().data));

//                if(t instanceof UnknownHostException) {
//                    updateResult.setValue(QuoteUpdateResult.failure(ServerUnavailable));
//                } else {
//                    updateResult.setValue(QuoteUpdateResult.failure(Status.Error));
//                }
            }
        });
    }

    private Map<String, String> getQueryParams(String commaSeparatedSymbols) {
        String selectParam = String.format("select * from yahoo.finance.quote where symbol in (%s)",
                commaSeparatedSymbols);
        Map<String, String> retrofitParams = new HashMap<>();
        retrofitParams.put("q", selectParam);
        retrofitParams.put("format", "json");
        retrofitParams.put("env", "store://datatables.org/alltableswithkeys");
        return retrofitParams;
    }

    private String symbolsAsCommaSeparatedList(List<Stock> stocks) {
        StringBuilder symbolsBuffer = new StringBuilder();
        int size = stocks.size();
        for (int i = 0; i < size; i++) {
            symbolsBuffer.append('"').append(stocks.get(i).getSymbol()).append('"');
            if (i < size - 1) {
                symbolsBuffer.append(",");
            }
        }
        return symbolsBuffer.toString();
    }

    private StockQuoteService getStockQuoteService() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new QuoteTypeAdapterFactory())
                .create();

        return new Retrofit.Builder()
                    .baseUrl("http://query.yahooapis.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(StockQuoteService.class);
    }

    private List<StockQuote> quotesToStockQuotes(List<Quote> quotes, List<Stock> stockList) {
        Map<String, Stock> stockMap = stockMap(stockList);
        List<StockQuote> stockQuotes = Observable.fromIterable(quotes)
                .map(quote -> StockQuote.builder()
                        .change(quote.getChange())
                        .lastTradePriceOnly(quote.getLastTradePriceOnly())
                        .name(quote.getName())
                        .quantity(stockMap.get(quote.getSymbol()).getQuantity())
                        .stockExchange(quote.getStockExchange())
                        .symbol(quote.getSymbol())
                        .build()).toList().blockingGet();
        return (stockQuotes);
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

    /**
     * A creator is used to inject the {@link Application} into the {@link ViewModel}
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application application;

        public Factory(@NonNull Application application) {
            this.application = application;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new StockQuoteViewModel();
        }
    }
}
