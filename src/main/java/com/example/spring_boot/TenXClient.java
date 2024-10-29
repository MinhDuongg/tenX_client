package com.example.spring_boot;

import com.example.spring_boot.model.Product;
import com.example.spring_boot.net.*;
import com.example.spring_boot.service.ProductService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.PasswordAuthentication;
import java.net.Proxy;

public class TenXClient {
    private final TenXResponseGetter responseGetter;

    /**
     * Constructs a TenXClient with default settings, using the provided API key. Use the builder
     * instead if you require more complex configuration.
     */
    public TenXClient(String apiKey) {
        this.responseGetter =
                new LiveTenXResponseGetter(builder().setApiKey(apiKey).buildOptions(), null);
    }

    public TenXClient(TenXResponseGetter responseGetter) {
        this.responseGetter = responseGetter;
    }

    protected TenXResponseGetter getResponseGetter() {
        return responseGetter;
    }

    public static TenXClientBuilder builder() {
        return new TenXClientBuilder();
    }

    public ProductService product() {
        return new ProductService(this.responseGetter);
    }

    public

    static class ClientTenXResponseGetterOptions extends TenXResponseGetterOptions {
        @Getter(onMethod_ = {@Override})
        private final Authorization authenticator;

        @Getter(onMethod_ = {@Override})
        private final String actorID;

        @Getter(onMethod_ = {@Override})
        private final String actorType;

        @Getter(onMethod_ = {@Override})
        private final int connectTimeout;

        @Getter(onMethod_ = {@Override})
        private final int readTimeout;

        @Getter(onMethod_ = {@Override})
        private final int maxNetworkRetries;

        @Getter(onMethod_ = {@Override})
        private final Proxy connectionProxy;

        @Getter(onMethod_ = {@Override})
        private final PasswordAuthentication proxyCredential;

        @Getter(onMethod_ = {@Override})
        private final String apiBase;

        ClientTenXResponseGetterOptions(
                Authorization authenticator,
                String actorID,
                String actorType,
                int connectTimeout,
                int readTimeout,
                int maxNetworkRetries,
                Proxy connectionProxy,
                PasswordAuthentication proxyCredential,
                String apiBase) {
            this.authenticator = authenticator;
            this.actorID = actorID;
            this.actorType = actorType;
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            this.maxNetworkRetries = maxNetworkRetries;
            this.connectionProxy = connectionProxy;
            this.proxyCredential = proxyCredential;
            this.apiBase = apiBase;
        }

        @Override
        public String getActorId() {
            return this.actorID;
        }
    }

    @Accessors(fluent = true)
    @Setter
    public static final class TenXClientBuilder {
        private Authorization authenticator;
        private String actorID;
        private String actorType;
        private int connectTimeout = TenX.DEFAULT_CONNECT_TIMEOUT;
        private int readTimeout = TenX.DEFAULT_READ_TIMEOUT;
        private int maxNetworkRetries;
        private Proxy connectionProxy;
        private PasswordAuthentication proxyCredential;
        private String apiBase = TenX.LIVE_API_BASE;

        public TenXClientBuilder() {}

        public TenXClientBuilder setAuthenticator(Authorization authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public String getApiKey() {
            if (authenticator instanceof DefaultAuthorization) {
                return ((DefaultAuthorization) authenticator).getApiKey();
            }

            return null;
        }

        public TenXClientBuilder setApiKey(String apiKey) {
            if (apiKey == null) {
                this.authenticator = null;
            } else {
                this.authenticator = new DefaultAuthorization(apiKey);
            }
            return this;
        }

        public TenXClientBuilder clearApiKey() {
            this.authenticator = null;
            return this;
        }

        public TenXClient build() {
            return new TenXClient(new LiveTenXResponseGetter(buildOptions(), null));
        }

        TenXResponseGetterOptions buildOptions() {
            if (this.authenticator == null) {
                throw new IllegalArgumentException(
                        "No authentication settings provided. Use setApiKey to set the TenX API key");
            }
            return new ClientTenXResponseGetterOptions(
                    this.authenticator,
                    this.actorID,
                    this.actorType,
                    connectTimeout,
                    readTimeout,
                    maxNetworkRetries,
                    connectionProxy,
                    proxyCredential,
                    apiBase);
        }
    }
}
