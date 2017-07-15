package com.stocktracker.exception;

/**
 * Created by dlarson on 7/4/17.
 */

public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(String message) {
        super(message);
    }
}
