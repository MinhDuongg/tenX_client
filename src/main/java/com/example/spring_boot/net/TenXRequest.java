package com.example.spring_boot.net;

import com.example.spring_boot.TenX;
import com.example.spring_boot.exception.ApiConnectionException;
import com.example.spring_boot.exception.AuthenticationException;
import com.example.spring_boot.exception.TenXException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Value
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(fluent = true)
public class TenXRequest {
    ApiResource.RequestMethod method;

    URL url;

    HttpContent content;
    HttpHeaders headers;
    Map<String, Object> params;

    RequestOptions options;

    private TenXRequest(
            ApiResource.RequestMethod method,
            String url,
            HttpContent content,
            Map<String, Object> params,
            RequestOptions options)
            throws TenXException {
        try {
            this.content = content;
            this.params = (params != null) ? Collections.unmodifiableMap(params) : null;
            this.options = (options != null) ? options : RequestOptions.getDefault();
            this.method = method;
            this.url = buildURL(method, url, params);
            this.headers = buildHeaders(method, this.options, this.content);
        } catch (IOException e) {
            throw new ApiConnectionException(
                    String.format(
                            "IOException during API request to 10x (%s): %s",
                            TenX.getApiBase(), e.getMessage()),
                    e);
        }
    }

    TenXRequest(
            ApiResource.RequestMethod method,
            String url,
            Map<String, Object> params,
            RequestOptions options)
            throws TenXException {
        try {
            this.params = (params != null) ? Collections.unmodifiableMap(params) : null;
            this.options = options;
            this.method = method;
            this.url = buildURL(method, url, params);
            this.content = buildContent(method, params);
            this.headers = buildHeaders(method, this.options, this.content);
        } catch (IOException e) {
            throw new ApiConnectionException(
                    String.format(
                            "IOException during API request to 10X (%s): %s ",
                            TenX.getApiBase(), e.getMessage()),
                    e);
        }
    }

    public static TenXRequest create(
            ApiResource.RequestMethod method,
            String url,
            Map<String, Object> params,
            RequestOptions options)
            throws TenXException {
        if (options == null) {
            throw new IllegalArgumentException("options parameter should not be null");
        }

        TenXRequest request = new TenXRequest(method, url, params, options);
        Authorization authenticator = options.getAuthenticator();

        if (authenticator == null) {
            throw new AuthenticationException(
                    "No API key provided.",
                    null,
                    null,
                    0);
        }

        request = request.options().getAuthenticator().authenticate(request);
        request = request.withAdditionalHeader("actor",request.options().getActorId());
        request = request.withAdditionalHeader("actor-type",request.options().getActorType());

        return request;
    }


    public TenXRequest withAdditionalHeader(String name, String value) {
        return new TenXRequest(
                this.method,
                this.url,
                this.content,
                this.headers.withAdditionalHeader(name, value),
                this.params,
                this.options);
    }

    private static HttpContent buildContent(
            ApiResource.RequestMethod method, Map<String, Object> params)
            throws IOException {
        if (method != ApiResource.RequestMethod.POST) {
            return null;
        }

        return FormEncoder.createHttpContent(params);
    }

    private static URL buildURL(
            ApiResource.RequestMethod method, String spec, Map<String, Object> params)
            throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(spec);

        URL specUrl = new URL(spec);
        String specQueryString = specUrl.getQuery();

        if ((method != ApiResource.RequestMethod.POST) && (params != null)) {
            String queryString =
                    FormEncoder.createQueryString(params,false);

            if (queryString != null && !queryString.isEmpty()) {
                if (specQueryString != null && !specQueryString.isEmpty()) {
                    sb.append("&");
                } else {
                    sb.append("?");
                }
                sb.append(queryString);
            }
        }

        return new URL(sb.toString());
    }

    private static HttpHeaders buildHeaders(
            ApiResource.RequestMethod method,
            RequestOptions options,
            HttpContent content) {
        Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

        // Accept
        headerMap.put("Accept", Arrays.asList("application/json"));

        // Accept-Charset
        headerMap.put("Accept-Charset", Arrays.asList(ApiResource.CHARSET.name()));


        if (content != null) {
            headerMap.put("Content-Type", Arrays.asList(content.contentType()));
        }

        // Idempotency-Key
        if (options.getIdempotencyKey() != null) {
            headerMap.put("Idempotency-Key", Arrays.asList(options.getIdempotencyKey()));
        } else if (method == ApiResource.RequestMethod.POST
                || method == ApiResource.RequestMethod.PATCH
                || method == ApiResource.RequestMethod.PUT) {
            headerMap.put("Idempotency-Key", Arrays.asList(UUID.randomUUID().toString()));
        }

        return HttpHeaders.of(headerMap);
    }
}
