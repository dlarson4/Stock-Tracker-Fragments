package com.stocktracker.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Stock implements Parcelable
{
    private long id;
    private String symbol;
    private double quantity;
    private long dateCreatedMillis;

    public Stock(long id, String symbol, double quantity, long dateCreatedMillis) {
        super();
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
        this.dateCreatedMillis = dateCreatedMillis;
    }

    public long getId()
    {
        return id;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public double getQuantity()
    {
        return quantity;
    }

    public long getDateCreatedMillis()
    {
        return dateCreatedMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stock stock = (Stock) o;

        if (id != stock.id) return false;
        if (Double.compare(stock.quantity, quantity) != 0) return false;
        if (dateCreatedMillis != stock.dateCreatedMillis) return false;
        return symbol != null ? symbol.equals(stock.symbol) : stock.symbol == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        temp = Double.doubleToLongBits(quantity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (dateCreatedMillis ^ (dateCreatedMillis >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Stock{");
        sb.append("id=").append(id);
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append(", dateCreatedMillis=").append(dateCreatedMillis);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeString(symbol);
        dest.writeDouble(quantity);
        dest.writeLong(dateCreatedMillis);
    }

    public static final Creator<Stock> CREATOR = new Creator<Stock>()
    {
        public Stock createFromParcel(Parcel in)
        {
            return new Stock(in);
        }

        public Stock[] newArray(int size)
        {
            return new Stock[size];
        }
    };

    private Stock(Parcel in)
    {
        this.id = in.readLong();
        this.symbol = in.readString();
        this.quantity = in.readDouble();
        this.dateCreatedMillis = in.readLong();
    }
    
}
