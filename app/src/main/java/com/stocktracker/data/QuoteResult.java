package com.stocktracker.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by dlarson on 7/5/17.
 */
public class QuoteResult {

    @SerializedName("query")
    public Query query;

    @Override
    public String toString() {
        return "QuoteResult{" +
                "query=" + query +
                '}';
    }

    public static class Query {
        @SerializedName("count")
        public int count;
        @SerializedName("created")
        public Date created;
        @SerializedName("lang")
        public Locale lang;
        @SerializedName("results")
        public Results results;

        @Override
        public String toString() {
            return "Query{" +
                    "count=" + count +
                    ", created=" + created +
                    ", lang=" + lang +
                    ", results=" + results +
                    '}';
        }
    }

    public static class Results {
        @SerializedName("quote")
        public List<Quote> quotes;

        @Override
        public String toString() {
            return "Results{" +
                    "quotes=" + quotes +
                    '}';
        }
    }
}
