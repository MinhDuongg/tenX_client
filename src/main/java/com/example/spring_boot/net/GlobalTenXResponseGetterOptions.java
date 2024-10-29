package com.example.spring_boot.net;

import com.example.spring_boot.TenX;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public class GlobalTenXResponseGetterOptions extends TenXResponseGetterOptions {
    public static final GlobalTenXResponseGetterOptions INSTANCE =
            new GlobalTenXResponseGetterOptions();

    private GlobalTenXResponseGetterOptions() {}

    @Override
    public Authorization getAuthenticator() {
        if (TenX.apiKey == null) {
            return null;
        }
        return new DefaultAuthorization(TenX.apiKey);
    }

    @Override
    public String getActorType() {
        return TenX.getActorType();
    }

    @Override
    public int getReadTimeout() {
        return TenX.getReadTimeout();
    }

    @Override
    public int getConnectTimeout() {
        return TenX.getConnectTimeout();
    }

    @Override
    public int getMaxNetworkRetries() {
        return TenX.getMaxNetworkRetries();
    }

    @Override
    public PasswordAuthentication getProxyCredential() {
        return TenX.getProxyCredential();
    }

    @Override
    public Proxy getConnectionProxy() {
        return TenX.getConnectionProxy();
    }

    @Override
    public String getActorId() {
        return TenX.getActorId();
    }

    @Override
    public String getApiBase() {
        return TenX.getApiBase();
    }
}
