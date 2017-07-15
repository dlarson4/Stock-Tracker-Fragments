package com.stocktracker.http;

import com.stocktracker.data.Status;

public class HttpResponse {

    public static HttpResponse INVALID_URL = new HttpResponse(Status.InvalidUrl);
    public static HttpResponse SERVER_UNAVAILABLE = new HttpResponse(Status.ServerUnavailable);

    private Status status;
    private String data;

    private HttpResponse(final Status status) {
        this.status = status;
    }

    private HttpResponse(final Status status, final String data) {
        this.status = status;
        this.data = data;
    }

    public static HttpResponse createSuccessResponse(final String content) {
        return new HttpResponse(Status.Success, content);
    }

    public boolean isSuccess() {
        return status == Status.Success;
    }

    public boolean isError() {
        return !isSuccess();
    }

    public String getData() {
        return data;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpResponse{");
        sb.append("status=").append(status);
        sb.append(", data='").append(data).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
