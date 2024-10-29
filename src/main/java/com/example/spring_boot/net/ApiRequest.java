package com.example.spring_boot.net;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ApiRequest extends BaseApiRequest{
    private Map<String, Object> params;

    private ApiRequest(
            BaseAddress baseAddress,
            ApiResource.RequestMethod method,
            String path,
            RequestOptions options,
            List<String> usage,
            Map<String, Object> params) {
        super(baseAddress, method, path, options, usage);
        this.params = params;
    }

    public ApiRequest(
            BaseAddress baseAddress,
            ApiResource.RequestMethod method,
            String path,
            Map<String, Object> params,
            RequestOptions options) {
        this(baseAddress, method, path, options, null, params);
    }

    public ApiRequest addUsage(String usage) {
        List<String> newUsage = new ArrayList<String>();
        if (this.getUsage() != null) {
            newUsage.addAll(this.getUsage());
        }
        newUsage.add(usage);
        return new ApiRequest(
                this.getBaseAddress(),
                this.getMethod(),
                this.getPath(),
                this.getOptions(),
                newUsage,
                this.getParams());
    }
}
