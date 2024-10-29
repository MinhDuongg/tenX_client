package com.example.spring_boot.exception;

public class TenXException extends Exception {
    private String code;
    private String requestId;
    private Integer statusCode;

    protected TenXException(String message, String requestId, String code, Integer statusCode) {
        this(message, requestId, code, statusCode, null);
    }

    /** Constructs a new TenX exception with the specified details. */
    protected TenXException(
            String message, String requestId, String code, Integer statusCode, Throwable e) {
        super(message, e);
        this.code = code;
        this.requestId = requestId;
        this.statusCode = statusCode;
    }
}
