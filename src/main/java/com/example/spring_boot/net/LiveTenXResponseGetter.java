package com.example.spring_boot.net;

import com.example.spring_boot.exception.ApiConnectionException;
import com.example.spring_boot.exception.ApiException;
import com.example.spring_boot.exception.TenXException;
import com.example.spring_boot.model.TenXObjectInterface;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class LiveTenXResponseGetter implements TenXResponseGetter {
    private final HttpClient httpClient;
    private final TenXResponseGetterOptions options;

    public LiveTenXResponseGetter(HttpClient httpClient) {
        this(null, httpClient);
    }

    public LiveTenXResponseGetter() {
        this(null, null);
    }


    @FunctionalInterface
    private interface RequestSendFunction<R> {
        R apply(TenXRequest request) throws TenXException;
    }

    @Override
    public <T extends TenXObjectInterface> T request(BaseAddress baseAddress, ApiResource.RequestMethod method, String path, Map<String, Object> params, Type typeToken, RequestOptions options) throws TenXException {
        return null;
    }

    public LiveTenXResponseGetter(TenXResponseGetterOptions options, HttpClient httpClient) {
        this.options = options != null ? options : GlobalTenXResponseGetterOptions.INSTANCE;
        this.httpClient = (httpClient != null) ? httpClient : buildDefaultHttpClient();
    }

    private TenXRequest toTenXRequest(ApiRequest apiRequest, RequestOptions mergedOptions)
            throws TenXException {
        String fullUrl = fullUrl(apiRequest);

        TenXRequest request =
                TenXRequest.create(
                        apiRequest.getMethod(),
                        fullUrl,
                        apiRequest.getParams(),
                        mergedOptions);

        return request;
    }

    @Override
    public <T extends TenXObjectInterface> T request(ApiRequest apiRequest, Type typeToken)
            throws TenXException {

        RequestOptions mergedOptions = RequestOptions.merge(this.options, apiRequest.getOptions());

        TenXRequest request = toTenXRequest(apiRequest, mergedOptions);
        TenXResponse response = httpClient.requestWithRetries(request);

        int responseCode = response.code();
        String responseBody = response.body();

        if (responseCode < 200 || responseCode >= 300) {
            handleError(response);
        }

        T resource = null;
        try {
            resource = (T) ApiResource.deserializeTenXObject(responseBody, typeToken, this);
        } catch (JsonSyntaxException e) {
            throw makeMalformedJsonError(responseBody, responseCode, e);
        }

        //Implement Collection Interface here if needed

        resource.setLastResponse(response);

        return resource;
    }

    private static HttpClient buildDefaultHttpClient() {
        return new HttpURLConnectionClient();
    }

    private void handleError(TenXResponse response) throws TenXException {
        JsonObject responseBody = ApiResource.GSON.fromJson(response.body(), JsonObject.class);
        throw new ApiConnectionException(responseBody.toString());
    }

    private static ApiException makeMalformedJsonError(
            String responseBody, int responseCode, Throwable e) throws ApiException {
        String details = e == null ? "none" : e.getMessage();
        throw new ApiException(
                String.format(
                        "Invalid response object from API: %s. (HTTP response code was %d). Additional details: %s.",
                        responseBody, responseCode, details),
                null,
                null,
                responseCode,
                e);
    }

    private String fullUrl(BaseApiRequest apiRequest) {
        BaseAddress baseAddress = apiRequest.getBaseAddress();
        RequestOptions options = apiRequest.getOptions();
        String relativeUrl = apiRequest.getPath();
        String baseUrl;
        switch (baseAddress) {
            case API:
                baseUrl = this.options.getApiBase();
                break;
            default:
                throw new IllegalArgumentException("Unknown base address " + baseAddress);
        }
        if (options != null && options.getBaseUrl() != null) {
            baseUrl = options.getBaseUrl();
        }
        return String.format("%s%s", baseUrl, relativeUrl);
    }
}
