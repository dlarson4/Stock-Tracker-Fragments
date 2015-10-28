package com.stocktracker.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

public class QuoteResponse implements Parcelable
{
    private int count;
    private Date created;
    private String lang;
    private List<Quote> quotes;

    public QuoteResponse()
    {
    }
    
    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public String getLang()
    {
        return lang;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
    }

    public List<Quote> getQuotes()
    {
        return quotes;
    }

    public void setQuotes(List<Quote> quotes)
    {
        this.quotes = quotes;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(count);
        dest.writeLong(created.getTime());
        dest.writeString(lang);
        dest.writeList(quotes);
    }

    public static final Creator<QuoteResponse> CREATOR = new Creator<QuoteResponse>()
    {
        public QuoteResponse createFromParcel(Parcel in)
        {
            return new QuoteResponse(in);
        }

        public QuoteResponse[] newArray(int size)
        {
            return new QuoteResponse[size];
        }
    };

    private QuoteResponse(Parcel in)
    {
        this.count = in.readInt();
        this.created = new Date(in.readLong());
        this.lang = in.readString();
        
        this.quotes = new java.util.ArrayList<Quote>();
        in.readList(this.quotes, null);
    }

}
