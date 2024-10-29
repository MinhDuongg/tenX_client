package com.example.spring_boot.net;

import com.example.spring_boot.exception.TenXException;
import com.example.spring_boot.model.TenXObjectInterface;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.InputStream;
import java.lang.reflect.Type;

public abstract class ApiService {
    @Getter(AccessLevel.PROTECTED)
    private final TenXResponseGetter responseGetter;

    protected ApiService(TenXResponseGetter responseGetter) {
        this.responseGetter = responseGetter;
    }

    protected <T extends TenXObjectInterface> T request(ApiRequest request, Type typeToken)
            throws TenXException {
        return this.getResponseGetter().request(request.addUsage("tenX_client"), typeToken);
    }
}
