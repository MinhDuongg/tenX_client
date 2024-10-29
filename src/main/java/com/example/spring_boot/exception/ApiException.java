package com.example.spring_boot.exception;

public class ApiException extends TenXException {
    public ApiException(
            String message, String requestId, String code, Integer statusCode, Throwable e) {
        super(message, requestId, code, statusCode, e);
    }
}
