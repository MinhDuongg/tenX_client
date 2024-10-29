package com.example.spring_boot.model;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class InstantDeserializer implements JsonDeserializer<Instant>, JsonSerializer<Instant> {
    @Override
    public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }

        if (json.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .appendOffset("+HHmm", "+0000")
                        .toFormatter();

                LocalDateTime dateTime = LocalDateTime.parse(jsonPrimitive.getAsString(), formatter);
                return dateTime.atOffset(ZoneOffset.of("+0000")).toInstant();
            }

            throw new JsonParseException("Instant is a non-string primitive type.");
        }

        throw new JsonParseException("Instant is a non-primitive type.");
    }

    @Override
    public JsonElement serialize(Instant src, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(src.toString());
    }
}
