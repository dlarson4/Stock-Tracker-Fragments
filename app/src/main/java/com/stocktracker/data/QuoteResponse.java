package com.stocktracker.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class QuoteResponse implements Parcelable {
    private Date created;
    private String lang;
    private List<Quote> quotes;

    public QuoteResponse(Builder builder) {
        this.created = builder.created;
        this.lang = builder.lang;
        this.quotes = builder.quotes;
    }

    public Date getCreated() {
        return created;
    }

    public String getLang() {
        return lang;
    }

    public List<Quote> getQuotes() {
        // TODO return defensive copy?
        return quotes;
    }

    public static class Builder {
        private Date created;
        private String lang;
        private List<Quote> quotes;

        public Builder created(Date created) {
            this.created = created;
            return this;
        }

        public Builder lang(String lang) {
            this.lang = lang;
            return this;
        }

        public Builder quotes(List<Quote> quotes) {
            this.quotes = quotes;
            return this;
        }

        public QuoteResponse build() {
            return new QuoteResponse(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuoteResponse{");
        sb.append(", created=").append(created);
        sb.append(", lang='").append(lang).append('\'');
        sb.append(", quotes=").append(quotes);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(created.getTime());
        dest.writeString(lang);
        dest.writeList(quotes);
    }

    public static final Creator<QuoteResponse> CREATOR = new Creator<QuoteResponse>() {
        public QuoteResponse createFromParcel(Parcel in) {
            return new QuoteResponse(in);
        }

        public QuoteResponse[] newArray(int size) {
            return new QuoteResponse[size];
        }
    };

    private QuoteResponse(Parcel in) {
        this.created = new Date(in.readLong());
        this.lang = in.readString();

        this.quotes = new java.util.ArrayList<>();
        in.readList(this.quotes, null);
    }

}
