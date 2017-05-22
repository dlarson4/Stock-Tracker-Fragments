package com.stocktracker.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Quote implements Parcelable {
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
    
    private Quote(Quote.Builder builder) {
        symbol = builder.symbol;
        averageDailyVolume = builder.averageDailyVolume;
        change = builder.change;
        daysLow = builder.daysLow;
        daysHigh = builder.daysHigh;
        yearLow = builder.yearLow;
        yearHigh = builder.yearHigh;
        marketCapitalization = builder.marketCapitalization;
        lastTradePriceOnly = builder.lastTradePriceOnly;
        daysRange = builder.daysRange;
        name = builder.name;
        volume = builder.volume;
        stockExchange = builder.stockExchange;
    }
    
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getChange() {
        return change;
    }

    public String getLastTradePriceOnly() {
        return lastTradePriceOnly;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStockExchange()
    {
        return stockExchange;
    }

    public double getQuantity()
    {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quote quote = (Quote) o;

        if (Double.compare(quote.quantity, quantity) != 0) return false;
        if (id != quote.id) return false;
        if (symbol != null ? !symbol.equals(quote.symbol) : quote.symbol != null) return false;
        if (averageDailyVolume != null ? !averageDailyVolume.equals(quote.averageDailyVolume) : quote.averageDailyVolume != null)
            return false;
        if (change != null ? !change.equals(quote.change) : quote.change != null) return false;
        if (daysLow != null ? !daysLow.equals(quote.daysLow) : quote.daysLow != null) return false;
        if (daysHigh != null ? !daysHigh.equals(quote.daysHigh) : quote.daysHigh != null) return false;
        if (yearLow != null ? !yearLow.equals(quote.yearLow) : quote.yearLow != null) return false;
        if (yearHigh != null ? !yearHigh.equals(quote.yearHigh) : quote.yearHigh != null) return false;
        if (marketCapitalization != null ? !marketCapitalization.equals(quote.marketCapitalization) : quote.marketCapitalization != null)
            return false;
        if (lastTradePriceOnly != null ? !lastTradePriceOnly.equals(quote.lastTradePriceOnly) : quote.lastTradePriceOnly != null)
            return false;
        if (daysRange != null ? !daysRange.equals(quote.daysRange) : quote.daysRange != null) return false;
        if (name != null ? !name.equals(quote.name) : quote.name != null) return false;
        if (volume != null ? !volume.equals(quote.volume) : quote.volume != null) return false;
        return stockExchange != null ? stockExchange.equals(quote.stockExchange) : quote.stockExchange == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + (averageDailyVolume != null ? averageDailyVolume.hashCode() : 0);
        result = 31 * result + (change != null ? change.hashCode() : 0);
        result = 31 * result + (daysLow != null ? daysLow.hashCode() : 0);
        result = 31 * result + (daysHigh != null ? daysHigh.hashCode() : 0);
        result = 31 * result + (yearLow != null ? yearLow.hashCode() : 0);
        result = 31 * result + (yearHigh != null ? yearHigh.hashCode() : 0);
        result = 31 * result + (marketCapitalization != null ? marketCapitalization.hashCode() : 0);
        result = 31 * result + (lastTradePriceOnly != null ? lastTradePriceOnly.hashCode() : 0);
        result = 31 * result + (daysRange != null ? daysRange.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        result = 31 * result + (stockExchange != null ? stockExchange.hashCode() : 0);
        temp = Double.doubleToLongBits(quantity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Quote{");
        sb.append("symbol='").append(symbol).append('\'');
        sb.append(", averageDailyVolume='").append(averageDailyVolume).append('\'');
        sb.append(", change='").append(change).append('\'');
        sb.append(", daysLow='").append(daysLow).append('\'');
        sb.append(", daysHigh='").append(daysHigh).append('\'');
        sb.append(", yearLow='").append(yearLow).append('\'');
        sb.append(", yearHigh='").append(yearHigh).append('\'');
        sb.append(", marketCapitalization='").append(marketCapitalization).append('\'');
        sb.append(", lastTradePriceOnly='").append(lastTradePriceOnly).append('\'');
        sb.append(", daysRange='").append(daysRange).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", volume='").append(volume).append('\'');
        sb.append(", stockExchange='").append(stockExchange).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append(", id=").append(id);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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

    public static final Creator<Quote> CREATOR = new Creator<Quote>() {
        public Quote createFromParcel(Parcel in) {
            return new Quote(in);
        }

        public Quote[] newArray(int size) {
            return new Quote[size];
        }
    };

    private Quote(Parcel in) {
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

    public static class Builder {
        private String symbol;
        private String name;
        private String change;
        private String averageDailyVolume;
        private String daysLow;
        private String daysHigh;
        private String yearLow;
        private String yearHigh;
        private String lastTradePriceOnly;
        private String daysRange;
        private String marketCapitalization;
        private String volume;

        private String stockExchange;
        // populated from the database, not the web service
        private double quantity;
        // SQLite id
        private long id;

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder change(String change) {
            this.change = change;
            return this;
        }

        public Builder daysLow(String daysLow) {
            this.daysLow = daysLow;
            return this;
        }

        public Builder daysHigh(String daysHigh) {
            this.daysHigh = daysHigh;
            return this;
        }

        public Builder yearLow(String yearLow) {
            this.yearLow = yearLow;
            return this;
        }

        public Builder yearHigh(String yearHigh) {
            this.yearHigh = yearHigh;
            return this;
        }

        public Builder lastTradePriceOnly(String lastTradePriceOnly) {
            this.lastTradePriceOnly = lastTradePriceOnly;
            return this;
        }

        public Builder daysRange(String daysRange) {
            this.daysRange = daysRange;
            return this;
        }

        public Builder stockExchange(String stockExchange) {
            this.stockExchange = stockExchange;
            return this;
        }

        public Builder quantity(double quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder averageDailyVolume(String averageDailyVolume) {
            this.averageDailyVolume = averageDailyVolume;
            return this;
        }

        public Builder marketCapitalization(String marketCapitalization) {
            this.marketCapitalization = marketCapitalization;
            return this;
        }

        public Builder volume(String volume) {
            this.volume = volume;
            return this;
        }


        public Quote build() {
            return new Quote(this);
        }
    }
}
