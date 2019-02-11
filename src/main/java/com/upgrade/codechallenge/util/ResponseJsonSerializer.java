package com.upgrade.codechallenge.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class ResponseJsonSerializer implements JsonSerializer<Response> {
    @Override
    public JsonElement serialize(Response response, Type type, JsonSerializationContext jsonSerializationContext) {
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("error", response.getError());
        json.addProperty("content", response.getContent());
        return json;
    }

}
