package com.stocktracker.data;


import com.google.auto.value.AutoValue;

/**
 * Created by dlarson on 6/11/17.
 */
@AutoValue
public abstract class StockQuote {
    public abstract String symbol();
    public abstract String name();
    public abstract double quantity();
    public abstract String stockExchange();
    public abstract String lastTradePriceOnly();
    public abstract String change();

    public static Builder builder() {
        return new AutoValue_StockQuote.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder symbol(String symbol);
        public abstract Builder name(String name);
        public abstract Builder quantity(double quantity);
        public abstract Builder stockExchange(String stockExchange);
        public abstract Builder lastTradePriceOnly(String lastTradePriceOnly);
        public abstract Builder change(String change);
        public abstract StockQuote build();
    }

}
