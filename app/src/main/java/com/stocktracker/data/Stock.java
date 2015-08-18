package com.stocktracker.data;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.os.Parcel;
import android.os.Parcelable;

public class Stock implements Parcelable {
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

    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public long getDateCreatedMillis() {
        return dateCreatedMillis;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(symbol);
        dest.writeDouble(quantity);
        dest.writeLong(dateCreatedMillis);
    }

    public static final Creator<Stock> CREATOR = new Creator<Stock>() {
        public Stock createFromParcel(Parcel in) {
            return new Stock(in);
        }

        public Stock[] newArray(int size) {
            return new Stock[size];
        }
    };

    private Stock(Parcel in) {
        this.id = in.readLong();
        this.symbol = in.readString();
        this.quantity = in.readDouble();
        this.dateCreatedMillis = in.readLong();
    }

}
