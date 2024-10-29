package com.example.spring_boot.service;

import com.example.spring_boot.exception.TenXException;
import com.example.spring_boot.model.Product;
import com.example.spring_boot.net.*;
import com.example.spring_boot.param.product.ProductSummaryParams;

public class ProductService extends ApiService {
    public ProductService(TenXResponseGetter responseGetter) {
        super(responseGetter);
    }

    public Product Summary(ProductSummaryParams params, RequestOptions options) throws TenXException {
        String path = String.format("/v2/products/%s/versions/%s/summary", params.getProductKey(),params.getVersion());
        ApiRequest request = new ApiRequest(BaseAddress.API, ApiResource.RequestMethod.GET,path, null,options);
        return getResponseGetter().request(request,Product.class);
    }
}
