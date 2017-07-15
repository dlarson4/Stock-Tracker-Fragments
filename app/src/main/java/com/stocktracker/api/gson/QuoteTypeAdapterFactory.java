package com.stocktracker.api.gson;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.stocktracker.data.Quote;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by dlarson on 7/7/17.
 */
public class QuoteTypeAdapterFactory implements TypeAdapterFactory {

    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {

        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

        return new TypeAdapter<T>() {

            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            public T read(JsonReader in) throws IOException {
                JsonElement jsonElement = elementAdapter.read(in);
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (jsonObject.has("query") && jsonObject.get("query").isJsonObject()) {
                        JsonObject query = jsonObject.getAsJsonObject("query");
                        if(query.has("results") && query.get("results").isJsonObject()) {
                            JsonObject results = query.getAsJsonObject("results");
                            if(results.has("quote") && results.get("quote").isJsonArray()) {
                                JsonArray quote = results.getAsJsonArray("quote");
                                Type collectionType = new TypeToken<Collection<Quote>>(){}.getType();
                                return gson.fromJson(quote, collectionType);
                            }
                        }
                    }
                }
                return delegate.fromJsonTree(jsonElement);
            }
        }.nullSafe();
    }
}