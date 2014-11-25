package com.stocktracker.data;

import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class TotalMarketValueDataPoint implements Comparable<TotalMarketValueDataPoint>
{
    public Calendar calendar;
    public BigDecimal totalMarketValue;
    
    public TotalMarketValueDataPoint(Calendar calendar, BigDecimal totalMarketValue)
    {
        super();
        this.calendar = calendar;
        this.totalMarketValue = totalMarketValue;
    }

    public Calendar getCalendar()
    {
        return calendar;
    }

    public BigDecimal getTotalMarketValue()
    {
        return totalMarketValue;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Sort by date
     */
    @Override
    public int compareTo(TotalMarketValueDataPoint other)
    {
        return this.calendar.compareTo(other.getCalendar());
    }
}