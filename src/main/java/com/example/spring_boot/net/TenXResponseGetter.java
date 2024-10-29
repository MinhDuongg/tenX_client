package com.example.spring_boot.net;

import com.example.spring_boot.exception.TenXException;
import com.example.spring_boot.model.TenXObjectInterface;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

public interface TenXResponseGetter {

    <T extends TenXObjectInterface> T request(
            BaseAddress baseAddress,
            ApiResource.RequestMethod method,
            String path,
            Map<String, Object> params,
            Type typeToken,
            RequestOptions options)
            throws TenXException;


    default <T extends TenXObjectInterface> T request(ApiRequest request, Type typeToken)
            throws TenXException {
        return request(
                request.getBaseAddress(),
                request.getMethod(),
                request.getPath(),
                request.getParams(),
                typeToken,
                request.getOptions());
    };

    default void validateRequestOptions(RequestOptions options) {}
}
