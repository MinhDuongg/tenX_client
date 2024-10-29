package com.example.spring_boot;

import com.example.spring_boot.exception.TenXException;
import com.example.spring_boot.model.Product;
import com.example.spring_boot.param.product.ProductSummaryParams;

public class Main {
    public static void main(String[] args) {


//        TenXClient client = new TenXClient("");
        TenXClient client = TenXClient.builder()
                .actorType("SYSTEM")
                .actorID("40078832-9975-4dfd-a8e4-0ea334fd7340")
                .setApiKey("").build();
        ProductSummaryParams params = ProductSummaryParams.builder().productKey("").version("2").build();

        try {
            Product product = client.product().Summary(params,null);
            System.out.println(product);
        } catch (TenXException e) {
            System.out.println(e.getMessage());
        }
    }
}
