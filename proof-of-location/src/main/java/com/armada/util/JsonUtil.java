package com.armada.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class JsonUtil {
    public static JsonObject parseJson(String data) {
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(data);
        return json;
    }
}
