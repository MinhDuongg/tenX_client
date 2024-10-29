package com.example.spring_boot.net;

import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

@Value
@Accessors(fluent = true)
public class HttpContent {
    byte[] byteArrayContent;

    String contentType;

    private HttpContent(byte[] byteArrayContent, String contentType) {
        this.byteArrayContent = byteArrayContent;
        this.contentType = contentType;
    }

    public static HttpContent buildFormURLEncodedContent(
            Collection<KeyValuePair<String, String>> nameValueCollection) throws IOException {
        requireNonNull(nameValueCollection);

        return new HttpContent(
                FormEncoder.createQueryString(nameValueCollection).getBytes(ApiResource.CHARSET),
                String.format("application/x-www-form-urlencoded;charset=%s", ApiResource.CHARSET));
    }

    public static HttpContent buildFormURLEncodedContent(String content) throws IOException {
        return new HttpContent(
                content.getBytes(ApiResource.CHARSET),
                String.format("application/x-www-form-urlencoded;charset=%s", ApiResource.CHARSET));
    }

    public static HttpContent buildJsonContent(String json) {
        return new HttpContent(json.getBytes(StandardCharsets.UTF_8), "application/json");
    }
}
