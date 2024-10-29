package com.example.spring_boot.param.product;

import com.example.spring_boot.net.ApiRequestParams;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
public class ProductSummaryParams  extends ApiRequestParams {
    String productKey;
    String version;


    private ProductSummaryParams(String version, String productKey) {
        this.version = version;
        this.productKey = productKey;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String productKey;
        private String version;

        public Builder() {}

        public Builder productKey(String productKey) {
            this.productKey = productKey;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public ProductSummaryParams build() {
            return new ProductSummaryParams(version, productKey);
        }
    }
}
