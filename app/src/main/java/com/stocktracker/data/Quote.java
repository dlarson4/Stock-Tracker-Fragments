package com.stocktracker.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Quote implements Parcelable
{
    private String symbol;
    private String averageDailyVolume;
    private String change;
    private String daysLow;
    private String daysHigh;
    private String yearLow;
    private String yearHigh;
    private String marketCapitalization;
    private String lastTradePriceOnly;
    private String daysRange;
    private String name;
    private String volume;
    private String stockExchange;

    // populated from the database, not the web service
    private double quantity;

    // SQLite id
    private long id;
    
    public Quote()
    {
    }
    
    public String getSymbol()
    {
        return symbol;
    }

    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

    public String getAverageDailyVolume()
    {
        return averageDailyVolume;
    }

    public void setAverageDailyVolume(String averageDailyVolume)
    {
        this.averageDailyVolume = averageDailyVolume;
    }

    public String getChange()
    {
        return change;
    }

    public void setChange(String change)
    {
        this.change = change;
    }

    public String getDaysLow()
    {
        return daysLow;
    }

    public void setDaysLow(String daysLow)
    {
        this.daysLow = daysLow;
    }

    public String getDaysHigh()
    {
        return daysHigh;
    }

    public void setDaysHigh(String daysHigh)
    {
        this.daysHigh = daysHigh;
    }

    public String getYearLow()
    {
        return yearLow;
    }

    public void setYearLow(String yearLow)
    {
        this.yearLow = yearLow;
    }

    public String getYearHigh()
    {
        return yearHigh;
    }

    public void setYearHigh(String yearHigh)
    {
        this.yearHigh = yearHigh;
    }

    public String getMarketCapitalization()
    {
        return marketCapitalization;
    }

    public void setMarketCapitalization(String marketCapitalization)
    {
        this.marketCapitalization = marketCapitalization;
    }

    public String getLastTradePriceOnly()
    {
        return lastTradePriceOnly;
    }

    public void setLastTradePriceOnly(String lastTradePriceOnly)
    {
        this.lastTradePriceOnly = lastTradePriceOnly;
    }

    public String getDaysRange()
    {
        return daysRange;
    }

    public void setDaysRange(String daysRange)
    {
        this.daysRange = daysRange;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVolume()
    {
        return volume;
    }

    public void setVolume(String volume)
    {
        this.volume = volume;
    }

    public String getStockExchange()
    {
        return stockExchange;
    }

    public void setStockExchange(String stockExchange)
    {
        this.stockExchange = stockExchange;
    }

    public double getQuantity()
    {
        return quantity;
    }

    public void setQuantity(double quantity)
    {
        this.quantity = quantity;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(symbol);
        dest.writeString(averageDailyVolume);
        dest.writeString(change);
        dest.writeString(daysLow);
        dest.writeString(daysHigh);
        dest.writeString(yearLow);
        dest.writeString(yearHigh);
        dest.writeString(marketCapitalization);
        dest.writeString(lastTradePriceOnly);
        dest.writeString(daysRange);
        dest.writeString(name);
        dest.writeString(volume);
        dest.writeString(stockExchange);
        dest.writeDouble(quantity);
        dest.writeLong(id);
    }

    public static final Creator<Quote> CREATOR = new Creator<Quote>()
    {
        public Quote createFromParcel(Parcel in)
        {
            return new Quote(in);
        }

        public Quote[] newArray(int size)
        {
            return new Quote[size];
        }
    };

    private Quote(Parcel in)
    {
        this.symbol = in.readString();
        this.averageDailyVolume = in.readString();
        this.change = in.readString();
        this.daysLow = in.readString();
        this.daysHigh = in.readString();
        this.yearLow = in.readString();
        this.yearHigh = in.readString();
        this.marketCapitalization = in.readString();
        this.lastTradePriceOnly = in.readString();
        this.daysRange = in.readString();
        this.name = in.readString();
        this.volume = in.readString();
        this.stockExchange = in.readString();
        this.quantity = in.readDouble();
        this.id = in.readLong();
    }


    public static class QuoteBuilder {
        private String symbol;
        private String name;
        private String change;
        private String daysLow;
        private String daysHigh;
        private String yearLow;
        private String yearHigh;
        private String lastTradePriceOnly;
        private String daysRange;
        private String stockExchange;
        // populated from the database, not the web service
        private double quantity;
        // SQLite id
        private long id;

        private QuoteBuilder() {
        }

        public static QuoteBuilder aQuote() {
            return new QuoteBuilder();
        }
        public QuoteBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public QuoteBuilder name(String name) {
            this.name = name;
            return this;
        }

        public QuoteBuilder change(String change) {
            this.change = change;
            return this;
        }

        public QuoteBuilder daysLow(String daysLow) {
            this.daysLow = daysLow;
            return this;
        }

        public QuoteBuilder daysHigh(String daysHigh) {
            this.daysHigh = daysHigh;
            return this;
        }

        public QuoteBuilder yearLow(String yearLow) {
            this.yearLow = yearLow;
            return this;
        }

        public QuoteBuilder yearHigh(String yearHigh) {
            this.yearHigh = yearHigh;
            return this;
        }

        public QuoteBuilder lastTradePriceOnly(String lastTradePriceOnly) {
            this.lastTradePriceOnly = lastTradePriceOnly;
            return this;
        }

        public QuoteBuilder daysRange(String daysRange) {
            this.daysRange = daysRange;
            return this;
        }

        public QuoteBuilder stockExchange(String stockExchange) {
            this.stockExchange = stockExchange;
            return this;
        }

        public QuoteBuilder quantity(double quantity) {
            this.quantity = quantity;
            return this;
        }

        public QuoteBuilder id(long id) {
            this.id = id;
            return this;
        }

//        public QuoteBuilder but() {
//            return aQuote().change(change).daysLow(daysLow).daysHigh(daysHigh).yearLow(yearLow).yearHigh(yearHigh).lastTradePriceOnly(mlastTradePriceOnly).daysRange(mdaysRange).stockExchange(mstockExchange).quantity(mquantity).id(mid);
//        }

        public Quote build() {
            Quote quote = new Quote();
            quote.setChange(change);
            quote.setDaysLow(daysLow);
            quote.setDaysHigh(daysHigh);
            quote.setYearLow(yearLow);
            quote.setYearHigh(yearHigh);
            quote.setLastTradePriceOnly(lastTradePriceOnly);
            quote.setDaysRange(daysRange);
            quote.setStockExchange(stockExchange);
            quote.setQuantity(quantity);
            quote.setId(id);
            quote.setSymbol(symbol);
            quote.setName(name);
            return quote;
        }
    }
}
