package com.stocktracker.parser;

import org.json.JSONObject;

public abstract class ParseStrategy<T> {
    public abstract T parse(JSONObject json);
}
