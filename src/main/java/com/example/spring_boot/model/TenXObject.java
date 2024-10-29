package com.example.spring_boot.model;

import com.example.spring_boot.net.ApiResource;
import com.example.spring_boot.net.TenXResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Instant;

import com.example.spring_boot.net.TenXResponseGetter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class TenXObject implements TenXObjectInterface {
    public static final Gson PRETTY_PRINT_GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Instant.class, new InstantSerializer())
                    .create();


    private transient TenXResponse lastResponse;

    private transient JsonObject rawJsonObject;

    @Override
    public String toString() {
        return String.format(
                "<%s@%s id=%s> JSON: %s",
                this.getClass().getName(),
                System.identityHashCode(this),
                this.getIdString(),
                PRETTY_PRINT_GSON.toJson(this));
    }

    public JsonObject getRawJsonObject() {
        // Lazily initialize this the first time the getter is called.
        if ((this.rawJsonObject == null) && (this.getLastResponse() != null)) {
            this.rawJsonObject =
                    ApiResource.INTERNAL_GSON.fromJson(this.getLastResponse().body(), JsonObject.class);
        }

        return this.rawJsonObject;
    }

    public String toJson() {
        return PRETTY_PRINT_GSON.toJson(this);
    }

    private Object getIdString() {
        try {
            Field idField = this.getClass().getDeclaredField("id");
            return idField.get(this);
        } catch (SecurityException e) {
            return "";
        } catch (NoSuchFieldException e) {
            return "";
        } catch (IllegalArgumentException e) {
            return "";
        } catch (IllegalAccessException e) {
            return "";
        }
    }

    protected static boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }


    @Override
    public TenXResponse getLastResponse() {
        return lastResponse;
    }

    @Override
    public void setLastResponse(TenXResponse response) {
        this.lastResponse = response;
    }

    public static TenXObject deserializeTenXObject(
            String payload, TenXResponseGetter responseGetter) {
        JsonObject jsonObject = ApiResource.GSON.fromJson(payload, JsonObject.class).getAsJsonObject();
        return deserializeTenXObject(jsonObject, responseGetter);
    }

    static TenXObject deserializeTenXObject(
            JsonObject eventDataObjectJson, TenXResponseGetter responseGetter) {
        String type = eventDataObjectJson.getAsJsonObject().get("object").getAsString();
        Class<? extends TenXObject> cl = com.example.spring_boot.model.EventDataClassLookup.classLookup.get(type);
        return TenXObject.deserializeTenXObject(
                eventDataObjectJson, cl != null ? cl : TenXRawJsonObject.class, responseGetter);
    }

    public static TenXObject deserializeTenXObject(
            JsonObject payload, Type type, TenXResponseGetter responseGetter) {
        TenXObject object = ApiResource.INTERNAL_GSON.fromJson(payload, type);

        if (object instanceof TenXActiveObject) {
            ((TenXActiveObject) object).setResponseGetter(responseGetter);
        }

        return object;
    }


    public static TenXObject deserializeTenXObject(
            String payload, Type type, TenXResponseGetter responseGetter) {
        TenXObject object = ApiResource.INTERNAL_GSON.fromJson(payload, type);

        if (object instanceof TenXActiveObject) {
            ((TenXActiveObject) object).setResponseGetter(responseGetter);
        }

        return object;
    }
}
