package com.stocktracker.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "stock")
public class Stock implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "stock")
    private String symbol;

    @ColumnInfo(name = "quantity")
    private double quantity;

    // just for deleting
    @Ignore
    public Stock(long id) {
        this.id = id;
    }

    public Stock(String symbol, double quantity) {
        super();
        this.symbol = symbol;
        this.quantity = quantity;
    }

    @Ignore
    public Stock(long id, String symbol, double quantity) {
        super();
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stock stock = (Stock) o;

        if (id != stock.id) return false;
        if (Double.compare(stock.quantity, quantity) != 0) return false;
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
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Stock{");
        sb.append("id=").append(id);
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", quantity=").append(quantity);
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
        dest.writeLong(id);
        dest.writeString(symbol);
        dest.writeDouble(quantity);
    }

    public static final Creator<Stock> CREATOR = new Creator<Stock>() {
        public Stock createFromParcel(Parcel in)
        {
            return new Stock(in);
        }

        public Stock[] newArray(int size)
        {
            return new Stock[size];
        }
    };

    private Stock(Parcel in) {
        this.id = in.readLong();
        this.symbol = in.readString();
        this.quantity = in.readDouble();
    }
    
}
