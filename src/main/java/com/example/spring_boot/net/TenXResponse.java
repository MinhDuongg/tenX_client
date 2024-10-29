package com.example.spring_boot.net;

/** A response from TenX's API, with body represented as a String. */
public class TenXResponse extends AbstractTenXResponse<String> {

    protected TenXResponse(int code, HttpHeaders headers, String body) {
        super(code, headers, body);
    }
}
