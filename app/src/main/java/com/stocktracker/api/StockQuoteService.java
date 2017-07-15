package com.stocktracker.api;

import android.arch.lifecycle.LiveData;

import com.stocktracker.data.Quote;
import com.stocktracker.data.QuoteResult;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by dlarson on 7/5/17.
 */
public interface StockQuoteService {
    @GET("/v1/public/yql")
    Call<List<Quote>> quoteList(@QueryMap(encoded=true) Map<String, String> options);
}
