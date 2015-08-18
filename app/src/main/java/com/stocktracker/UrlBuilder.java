package com.stocktracker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.stocktracker.data.Stock;
import com.stocktracker.log.Logger;

// TODO: combine buildAllStocksQuoteUrl and buildQuoteUrl, they're basically the same
public class UrlBuilder {
    // Console for debugging
    // https://developer.yahoo.com/yql/console/


    private final static String YQL_BASE_URL = "http://query.yahooapis.com/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
    private final static String YQL_STOCK_INFO_QUERY = "select * from yahoo.finance.quote where symbol in (";
    private final static String YQL_STOCK_EXISTS_QUERY = "select * from yahoo.finance.quote where symbol = \"%1$s\"";

    public static String buildAllStocksQuoteUrl(List<Stock> stocks) {
        if (stocks != null && !stocks.isEmpty()) {
            StockInfoQueryBuilder builder = new StockInfoQueryBuilder();
            for (Stock stock : stocks) {
                builder.stock(stock.getSymbol());
            }
            return builder.build();
        }
        return null;
    }

    public static String buildQuoteUrl(String stock) {
        StockExistsQueryBuilder builder = new StockExistsQueryBuilder(stock);
        return builder.build();
    }

    /**
     * Build a Yahoo Finance API query for stock quote info
     */
    private static class StockInfoQueryBuilder {
        private final static String CLASS_NAME = StockInfoQueryBuilder.class.getSimpleName();

        private List<String> stockSymbols;

        public StockInfoQueryBuilder() {
            stockSymbols = new ArrayList<String>();
        }

        public StockInfoQueryBuilder stock(String stock) {
            stockSymbols.add(stock);
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder(200);
            sb.append(YQL_BASE_URL).append("&q=");

            StringBuilder query = new StringBuilder(YQL_STOCK_INFO_QUERY);
            final int size = stockSymbols.size();
            for (int i = 0; i < size; i++) {
                query.append('"').append(stockSymbols.get(i)).append('"');
                if (i < size - 1) {
                    query.append(',');
                }
            }
            query.append(')');

            try {
                sb.append(URLEncoder.encode(query.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Logger.error(e, "%s.%s: Error URL-encoding query '%s'", CLASS_NAME, "build", query.toString());
            }
            return sb.toString();
        }
    }

    /**
     * Build a Yahoo Finance API query to check if a stock symbol exists
     */
    private static class StockExistsQueryBuilder {
        private final static String CLASS_NAME = UrlBuilder.class.getSimpleName();

        private String stockSymbol;

        public StockExistsQueryBuilder(String stockSymbol) {
            this.stockSymbol = stockSymbol;
        }

        public String build() {
            StringBuilder sb = new StringBuilder(150);
            sb.append(YQL_BASE_URL).append("&q=");

            String query = String.format(YQL_STOCK_EXISTS_QUERY, this.stockSymbol);

            if (Logger.isLoggingEnabled()) {
                Logger.debug("%s.%s: query = '%s'", CLASS_NAME, "build", query.toString());
            }

            try {
                sb.append(URLEncoder.encode(query.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Logger.error(e, "%s.%s: Error URL-encoding query '%s'", CLASS_NAME, "build", query.toString());
            }
            return sb.toString();
        }
    }
}
