package com.stocktracker.data;

import com.stocktracker.http.HttpResponse;

/**
 * Created by dlarson on 6/14/17.
 */
public class QuoteUpdateResult {
    final HttpResponse.Status status;

    public QuoteUpdateResult(
            HttpResponse.Status status) {
        this.status = status;
    }

    public static QuoteUpdateResult failure(HttpResponse.Status status) {
        return new QuoteUpdateResult(status);
    }

    public static QuoteUpdateResult success() {
        return new QuoteUpdateResult(HttpResponse.Status.Success);
    }

    public boolean isSuccess() {
        return status == HttpResponse.Status.Success;
    }

    public HttpResponse.Status getStatus() {
        return status;
    }
}
