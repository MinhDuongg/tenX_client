package com.example.spring_boot.net;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public abstract class TenXResponseGetterOptions {
    public abstract Authorization getAuthenticator();

    public abstract String getActorType();

    public abstract String getActorId();

    public abstract int getConnectTimeout();

    public abstract Proxy getConnectionProxy();

    public abstract int getMaxNetworkRetries();

    public abstract PasswordAuthentication getProxyCredential();

    public abstract String getApiBase();

    public abstract int getReadTimeout();
}
