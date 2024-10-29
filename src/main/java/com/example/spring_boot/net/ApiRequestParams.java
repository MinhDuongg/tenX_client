package com.example.spring_boot.net;

import java.util.Map;

public class ApiRequestParams {
    private static final ApiRequestParamsConverter PARAMS_CONVERTER = new ApiRequestParamsConverter();

    public interface EnumParam {
        String getValue();
    }

    public Map<String, Object> toMap() {
        return PARAMS_CONVERTER.convert(this);
    }

    public static Map<String, Object> paramsToMap(ApiRequestParams params) {
        if (params == null) {
            return null;
        }
        return params.toMap();
    }
}
