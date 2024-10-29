package com.example.spring_boot.net;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestOptions {
    @Getter
    Authorization authenticator;
    private final String idempotencyKey;
    private final String actorType;
    private final String actorId;
    private final String baseUrl;

    private final Integer connectTimeout;
    private final Integer readTimeout;
    private final Integer maxNetworkRetries;
    private final Proxy connectionProxy;
    private final PasswordAuthentication proxyCredential;

    public static RequestOptions getDefault() {
        return new RequestOptions(
                null,null, null,null,null,null,null,null,null,null
        );
    }

    protected RequestOptions(
            Authorization authenticator,
            String idempotencyKey,
            String actorType,
            String actorId,
            String baseUrl,
            Integer connectTimeout,
            Integer readTimeout,
            Integer maxNetworkRetries,
            Proxy connectionProxy,
            PasswordAuthentication proxyCredential) {
        this.authenticator = authenticator;
        this.idempotencyKey = idempotencyKey;
        this.actorType = actorType;
        this.actorId = actorId;
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxNetworkRetries = maxNetworkRetries;
        this.connectionProxy = connectionProxy;
        this.proxyCredential = proxyCredential;
    }

    public String getApiKey() {
        if (authenticator instanceof DefaultAuthorization) {
            return ((DefaultAuthorization) authenticator).getApiKey();
        }
        return null;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getActorType() {
        return actorType;
    }

    public String getActorId() {
        return actorId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public Integer getMaxNetworkRetries() {
        return maxNetworkRetries;
    }

    public Proxy getConnectionProxy() {
        return connectionProxy;
    }

    public PasswordAuthentication getProxyCredential() {
        return proxyCredential;
    }

    public static class RequestOptionsBuilder {
        protected Authorization authenticator;
        @Getter
        protected String idempotencyKey;
        @Getter
        protected String actorType;
        @Getter
        protected String actorId;
        @Getter
        protected String baseUrl;

        @Getter
        protected Integer connectTimeout;
        @Getter
        @Setter
        protected Integer readTimeout;
        @Getter
        protected Integer maxNetworkRetries;
        @Getter
        protected Proxy connectionProxy;
        @Getter
        protected PasswordAuthentication proxyCredential;

        public RequestOptionsBuilder() {}

        public Authorization getAuthorization() {
            return this.authenticator;
        }

        public RequestOptionsBuilder setAuthenticator(Authorization authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public String getApiKey() {
            if (authenticator instanceof DefaultAuthorization) {
                return ((DefaultAuthorization) authenticator).getApiKey();
            }
            return null;
        }

        public RequestOptionsBuilder setActorId(String actorId) {
            this.actorId = actorId;
            return this;
        }

        public RequestOptionsBuilder setProxyCredential(PasswordAuthentication proxyCredential) {
            this.proxyCredential = proxyCredential;
            return this;
        }

        public RequestOptionsBuilder setConnectionProxy(Proxy connectionProxy) {
            this.connectionProxy = connectionProxy;
            return this;
        }

        public RequestOptionsBuilder setMaxNetworkRetries(Integer maxNetworkRetries) {
            this.maxNetworkRetries = maxNetworkRetries;
            return this;
        }

        public RequestOptionsBuilder setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public RequestOptionsBuilder setBaseUrl(final String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public RequestOptionsBuilder setActorType(String actorType) {
            this.actorType = actorType;
            return this;
        }

        public RequestOptionsBuilder setIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public RequestOptions build() {
            return new RequestOptions(
                    authenticator,
                    normalizeIdempotencyKey(this.idempotencyKey),
                    normalizeActorType(this.actorType),
                    normalizeBaseUrl(this.baseUrl),
                    normalizeBaseUrl(this.baseUrl),
                    connectTimeout,
                    readTimeout,
                    maxNetworkRetries,
                    connectionProxy,
                    proxyCredential);
        }
    }

    protected static String normalizeApiKey(String apiKey) {
        // null apiKeys are considered "valid"
        if (apiKey == null) {
            return null;
        }
        return apiKey.trim();
    }

    protected static String normalizeActorId(String actorId) {
        // null actorId are considered "valid"
        if (actorId == null) {
            return null;
        }
        String normalized = actorId.trim();
        if (normalized.isEmpty()) {
            throw new InvalidRequestOptionsException("Empty actorId specified!");
        }
        return normalized;
    }

    protected static String normalizeActorType(String actorId) {
        final List<String> actorTypes = Arrays.asList("SYSTEM");

        if (actorId == null) {
            throw new  InvalidRequestOptionsException("Empty actor type!");
        }
        String normalized = actorId.trim();
        if (!actorTypes.contains(normalized)) {
            throw new InvalidRequestOptionsException("Empty actorId specified!");
        }
        return normalized;
    }


    protected static String normalizeBaseUrl(String baseUrl) {
        // null baseUrl is valid, and will fall back to e.g. TenX.connectBase
        // (depending on the method)
        if (baseUrl == null) {
            return null;
        }
        String normalized = baseUrl.trim();
        if (normalized.isEmpty()) {
            throw new InvalidRequestOptionsException("Empty baseUrl specified!");
        }
        return normalized;
    }

    protected static String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        if (normalized.isEmpty()) {
            throw new InvalidRequestOptionsException("Empty Idempotency Key Specified!");
        }
        if (normalized.length() > 255) {
            throw new InvalidRequestOptionsException(
                    String.format(
                            "Idempotency Key length was %d, which is larger than the 255 character " + "maximum!",
                            normalized.length()));
        }
        return normalized;
    }

    static RequestOptions merge(TenXResponseGetterOptions clientOptions, RequestOptions options) {
        if (options == null) {
            return new RequestOptions(
                    clientOptions.getAuthenticator(), // authenticator
                    null, // idempotencyKey
                    clientOptions.getActorType(),
                    clientOptions.getActorId(), // actorId
                    null, // baseUrl
                    clientOptions.getConnectTimeout(), // connectTimeout
                    clientOptions.getReadTimeout(), // readTimeout
                    clientOptions.getMaxNetworkRetries(), // maxNetworkRetries
                    clientOptions.getConnectionProxy(), // connectionProxy
                    clientOptions.getProxyCredential() // proxyCredential
            );
        }
        return new RequestOptions(
                options.getAuthenticator() != null
                        ? options.getAuthenticator()
                        : clientOptions.getAuthenticator(),
                options.getIdempotencyKey(),
                options.getActorType() != null ? options.getActorType() : clientOptions.getActorType(),
                options.getActorId() != null
                        ? options.getActorId()
                        : clientOptions.getActorId(),
                options.getBaseUrl(),
                options.getConnectTimeout() != null
                        ? options.getConnectTimeout()
                        : clientOptions.getConnectTimeout(),
                options.getReadTimeout() != null
                        ? options.getReadTimeout()
                        : clientOptions.getReadTimeout(),
                options.getMaxNetworkRetries() != null
                        ? options.getMaxNetworkRetries()
                        : clientOptions.getMaxNetworkRetries(),
                options.getConnectionProxy() != null
                        ? options.getConnectionProxy()
                        : clientOptions.getConnectionProxy(),
                options.getProxyCredential() != null
                        ? options.getProxyCredential()
                        : clientOptions.getProxyCredential());
    }

    public static class InvalidRequestOptionsException extends RuntimeException {

        public InvalidRequestOptionsException(String message) {
            super(message);
        }
    }
}
