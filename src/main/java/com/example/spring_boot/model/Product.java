package com.example.spring_boot.model;

import com.example.spring_boot.exception.TenXException;
import com.example.spring_boot.net.*;
import com.example.spring_boot.param.product.ProductSummaryParams;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Product extends ApiResource {

    @SerializedName("productKey")
    String productKey;

    @SerializedName("productCategory")
    String productCategory;

    @SerializedName("productName")
    String productName;

    @SerializedName("productType")
    String productType;

    @SerializedName("productDescription")
    String productDescription;

    @SerializedName("productSegment")
    String productSegment;

    @SerializedName("version")
    Integer version;

    @SerializedName("effectiveDate")
    Instant effectiveDate;

    @SerializedName("productLine")
    String productLine;

    @SerializedName("productGroup")
    String productGroup;

 }
