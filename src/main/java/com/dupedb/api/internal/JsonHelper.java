package com.dupedb.api.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Internal JSON serialization helper using Gson with snake_case naming policy.
 * Maps between snake_case JSON fields and camelCase Java record components.
 */
public final class JsonHelper {
    private static final Gson GSON = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(boolean.class, new LenientBooleanAdapter())
        .registerTypeAdapter(Boolean.class, new LenientBooleanAdapter())
        .create();

    /**
     * Handles boolean fields that may arrive as 0/1 numbers from the database.
     */
    private static class LenientBooleanAdapter extends TypeAdapter<Boolean> {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value);
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NUMBER) {
                return in.nextInt() != 0;
            }
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return false;
            }
            return in.nextBoolean();
        }
    }

    private JsonHelper() {}

    /**
     * Deserializes a JSON string into an object of the given class.
     */
    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Deserializes a JSON string into an object of the given generic type.
     * Use with {@code TypeToken} for parameterized types like {@code List<Exploit>}.
     */
    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Serializes an object to a JSON string.
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
}
