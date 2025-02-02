package com.example.spring_boot.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class FormEncoder {
    public static HttpContent createHttpContent(Map<String, Object> params) throws IOException {
        // If params is null, we create an empty HttpContent because we still want to send the
        // Content-Type header.
        if (params == null) {
            return HttpContent.buildFormURLEncodedContent(new ArrayList<KeyValuePair<String, String>>());
        }

        Collection<KeyValuePair<String, Object>> flatParams = flattenParams(params, false);

        // If all parameters have been encoded as strings, then the content can be represented
        // with application/x-www-form-url-encoded encoding. Otherwise, use
        // multipart/form-data encoding.
        if (flatParams.stream().allMatch(kvp -> kvp.getValue() instanceof String)) {
            Collection<KeyValuePair<String, String>> flatParamsString =
                    flatParams.stream()
                            .filter(kvp -> kvp.getValue() instanceof String)
                            .map(kvp -> new KeyValuePair<String, String>(kvp.getKey(), (String) kvp.getValue()))
                            .collect(Collectors.toList());
            return HttpContent.buildFormURLEncodedContent(flatParamsString);
        } else {
            throw new IOException("content encode not supported");
        }
    }


    public static String createQueryString(Map<String, Object> params, boolean arraysAsRepeated) {
        if (params == null) {
            return "";
        }

        Collection<KeyValuePair<String, String>> flatParams =
                flattenParams(params, arraysAsRepeated).stream()
                        .filter(kvp -> kvp.getValue() instanceof String)
                        .map(kvp -> new KeyValuePair<String, String>(kvp.getKey(), (String) kvp.getValue()))
                        .collect(Collectors.toList());
        return createQueryString(flatParams);
    }


    public static List<KeyValuePair<String, Object>> flattenParams(
            Map<String, Object> params, boolean arraysAsRepeated) {
        return flattenParamsValue(params, null, arraysAsRepeated);
    }

    private static List<KeyValuePair<String, Object>> flattenParamsValue(
            Object value, String keyPrefix, boolean arraysAsRepeated) {
        List<KeyValuePair<String, Object>> flatParams = null;

        if (value == null) {
            flatParams = singleParam(keyPrefix, "");

        } else if (value instanceof Map<?, ?>) {
            flatParams = flattenParamsMap((Map<?, ?>) value, keyPrefix, arraysAsRepeated);

        } else if (value instanceof String) {
            flatParams = singleParam(keyPrefix, value);

        } else if (value instanceof File) {
            flatParams = singleParam(keyPrefix, value);

        } else if (value instanceof InputStream) {
            flatParams = singleParam(keyPrefix, value);

        } else if (value instanceof Collection<?>) {
            flatParams = flattenParamsCollection((Collection<?>) value, keyPrefix, arraysAsRepeated);

        } else if (value.getClass().isArray()) {
            Object[] array = getArrayForObject(value);
            Collection<?> collection = Arrays.stream(array).collect(Collectors.toList());
            flatParams = flattenParamsCollection(collection, keyPrefix, arraysAsRepeated);

        } else if (value.getClass().isEnum()) {
            flatParams =
                    singleParam(keyPrefix, ApiResource.INTERNAL_GSON.toJson(value).replaceAll("\"", ""));

        } else {
            flatParams = singleParam(keyPrefix, value.toString());
        }

        return flatParams;
    }

    private static Object[] getArrayForObject(Object array) {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException("parameter is not an array");
        }

        // If element type is not a primitive, simply cast the object and return
        if (!array.getClass().getComponentType().isPrimitive()) {
            return (Object[]) array;
        }

        // Otherwise, initialize a new array of Object and copy elements one by one. Primitive
        // elements will be autoboxed.
        int length = Array.getLength(array);
        Object[] newArray = new Object[length];

        for (int index = 0; index < length; index++) {
            newArray[index] = Array.get(array, index);
        }

        return newArray;
    }

    private static List<KeyValuePair<String, Object>> singleParam(String key, Object value) {
        List<KeyValuePair<String, Object>> flatParams = new ArrayList<KeyValuePair<String, Object>>();
        flatParams.add(new KeyValuePair<String, Object>(key, value));
        return flatParams;
    }

    private static List<KeyValuePair<String, Object>> flattenParamsMap(
            Map<?, ?> map, String keyPrefix, boolean arraysAsRepeated) {
        List<KeyValuePair<String, Object>> flatParams = new ArrayList<KeyValuePair<String, Object>>();
        if (map == null) {
            return flatParams;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            String newPrefix = newPrefix(key, keyPrefix);

            flatParams.addAll(flattenParamsValue(value, newPrefix, arraysAsRepeated));
        }

        return flatParams;
    }

    private static String newPrefix(String key, String keyPrefix) {
        if (keyPrefix == null || keyPrefix.isEmpty()) {
            return key;
        }

        int i = key.indexOf("[");
        if (i == -1) {
            return String.format("%s[%s]", keyPrefix, key);
        } else {
            return String.format("%s[%s][%s]", keyPrefix, key.substring(0, i), key.substring(i));
        }
    }

    private static List<KeyValuePair<String, Object>> flattenParamsCollection(
            Collection<?> collection, String keyPrefix, boolean arraysAsRepeated) {
        List<KeyValuePair<String, Object>> flatParams = new ArrayList<KeyValuePair<String, Object>>();
        if (collection == null) {
            return flatParams;
        }

        int index = 0;
        for (Object value : collection) {
            String newPrefix = arraysAsRepeated ? keyPrefix : String.format("%s[%d]", keyPrefix, index);
            flatParams.addAll(flattenParamsValue(value, newPrefix, arraysAsRepeated));
            index += 1;
        }

        /* Because application/x-www-form-urlencoded cannot represent an empty list, convention
         * is to take the list parameter and just set it to an empty string. (E.g. A regular
         * list might look like `a[0]=1&b[1]=2`. Emptying it would look like `a=`.) */
        if (!arraysAsRepeated && flatParams.isEmpty()) {
            flatParams.add(new KeyValuePair<String, Object>(keyPrefix, ""));
        }

        return flatParams;
    }

    public static String createQueryString(
            Collection<KeyValuePair<String, String>> nameValueCollection) {
        if (nameValueCollection == null) {
            return "";
        }

        return String.join(
                "&",
                nameValueCollection.stream()
                        .map(kvp -> String.format("%s=%s", urlEncode(kvp.getKey()), urlEncode(kvp.getValue())))
                        .collect(Collectors.toList()));
    }

    private static String urlEncode(String value) {
        if (value == null) {
            return null;
        }

        try {
            // Don't use strict form encoding by changing the square bracket control
            // characters back to their literals. This is fine by the server, and
            // makes these parameter strings easier to read.
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                    .replaceAll("%5B", "[")
                    .replaceAll("%5D", "]");
        } catch (UnsupportedEncodingException e) {
            // This can literally never happen, and lets us avoid having to catch
            // UnsupportedEncodingException in callers.
            throw new AssertionError("UTF-8 is unknown");
        }
    }
}
