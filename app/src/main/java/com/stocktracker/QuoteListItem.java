package com.stocktracker;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.stocktracker.data.Stock;

import java.util.List;

/**
 * Created by dlarson on 9/5/15.
 */
public class QuoteListItem implements ParentObject {

    private Stock mStock;
    private List<Object> details;

    public QuoteListItem(Stock stock, List<Object> details) {
        this.mStock = stock;
        this.details = details;
    }
    @Override
    public List<Object> getChildObjectList() {
        return details;
    }

    @Override
    public void setChildObjectList(List<Object> list) {
        this.details = list;
    }

    public Stock getStock() {
        return mStock;
    }
}
