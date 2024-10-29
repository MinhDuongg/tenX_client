package com.example.spring_boot.exception;

public class AuthenticationException extends TenXException{

    public AuthenticationException(
            String message, String requestId, String code, Integer statusCode) {
        super(message, requestId, code, statusCode);
    }

    public AuthenticationException(
            String message, String requestId, String code, Integer statusCode, Throwable e) {
        super(message, requestId, code, statusCode, e);
    }
}
