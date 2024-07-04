package org.example.utils;

import cn.hutool.json.JSON;
import cn.hutool.json.serialize.JSONDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class InstantSerializer implements JsonSerializer<Instant>, JSONDeserializer<Instant> {
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    @Override
    public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(instant.toString());
    }

    @Override
    public Instant deserialize(JSON json) {
        return Instant.from(fmt.parse(json.toString()));
    }
}
