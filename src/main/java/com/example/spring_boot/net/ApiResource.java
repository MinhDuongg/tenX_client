package com.example.spring_boot.net;

import com.example.spring_boot.model.InstantDeserializer;
import com.example.spring_boot.model.TenXActiveObject;
import com.example.spring_boot.model.TenXObject;
import com.google.gson.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ApiResource extends TenXObject implements TenXActiveObject {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private static TenXResponseGetter globalResponseGetter = new LiveTenXResponseGetter();

    private transient TenXResponseGetter responseGetter;

    public static final Gson INTERNAL_GSON = createGson(false);
    public static final Gson GSON = createGson(true);

    public static void setGlobalResponseGetter(TenXResponseGetter srg) {
        ApiResource.globalResponseGetter = srg;
    }
    public static TenXResponseGetter getGlobalResponseGetter() {
        return ApiResource.globalResponseGetter;
    }


    private static Gson createGson(boolean shouldSetResponseGetter) {
        GsonBuilder builder =
                new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .registerTypeAdapter(Instant.class, new InstantDeserializer())
                        .addReflectionAccessFilter(
                                new ReflectionAccessFilter() {
                                    @Override
                                    public ReflectionAccessFilter.FilterResult check(Class<?> rawClass) {
                                        if (rawClass.getTypeName().startsWith("com.example.")) {
                                            return ReflectionAccessFilter.FilterResult.ALLOW;
                                        }
                                        return ReflectionAccessFilter.FilterResult.BLOCK_ALL;
                                    }
                                });

//        if (shouldSetResponseGetter) {
//            builder.registerTypeAdapterFactory(new TenXResponseGetterSettingTypeAdapterFactory());
//        }

        return builder.create();
    }

    @Override
    public void setResponseGetter(TenXResponseGetter responseGetter) {
        this.responseGetter = responseGetter;
    }

    protected TenXResponseGetter getResponseGetter() {
        if (this.responseGetter == null) {
            return getGlobalResponseGetter();
        }
        return this.responseGetter;
    }

    public enum RequestMethod {
        GET,
        POST,
        DELETE,
        PATCH,
        PUT
    }

    public static String urlEncode(String str) {
        // Preserve original behavior that passing null for an object id will lead
        // to us actually making a request to /v1/foo/null
        if (str == null) {
            return null;
        }

        try {
            // Don't use strict form encoding by changing the square bracket control
            // characters back to their literals. This is fine by the server, and
            // makes these parameter strings easier to read.
            return URLEncoder.encode(str, CHARSET.name()).replaceAll("%5B", "[").replaceAll("%5D", "]");
        } catch (UnsupportedEncodingException e) {
            // This can literally never happen, and lets us avoid having to catch
            // UnsupportedEncodingException in callers.
            throw new AssertionError("UTF-8 is unknown");
        }
    }

    /**
     * Invalidate null typed parameters.
     *
     * @param url request url associated with the given parameters.
     * @param params typed parameters to check for null value.
     */
    public static void checkNullTypedParams(String url, ApiRequestParams params) {
        if (params == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "Found null params for %s. "
                                    + "Please pass empty params using param builder via `builder().build()` instead.",
                            url));
        }
    }
}


