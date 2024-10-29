package com.example.spring_boot.net;

import com.example.spring_boot.exception.AuthenticationException;
import com.example.spring_boot.exception.TenXException;
import org.springframework.util.StringUtils;

public class DefaultAuthorization implements Authorization {

    private final String apiKey;

    public DefaultAuthorization(String apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("apiKey should be not-null");
        }
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    @Override
    public TenXRequest authenticate(TenXRequest request) throws TenXException {
        if (apiKey.isEmpty()) {
            throw new AuthenticationException(
                    "Your API key is invalid, as it is an empty string. You can double-check your API key ",
                    null,
                    null,
                    0);
        } else if (StringUtils.containsWhitespace(apiKey)) {
            throw new AuthenticationException(
                    "Your API key is invalid, as it contains whitespace. You can double-check your API key ",
                    null,
                    null,
                    0);
        }

        return request.withAdditionalHeader("x-apikey",apiKey);
    }
}

