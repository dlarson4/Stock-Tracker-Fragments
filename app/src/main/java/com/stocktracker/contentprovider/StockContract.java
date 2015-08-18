package com.stocktracker.contentprovider;

import android.content.ContentResolver;
import android.net.Uri;

public final class StockContract {
    public static final String AUTHORITY = "com.stocktracker.contentprovider";

    public static final String BASE_PATH = "stocks";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/stocks";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/stock";
}
