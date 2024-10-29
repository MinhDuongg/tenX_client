package com.example.spring_boot.exception;

public class ApiConnectionException extends TenXException {
    public ApiConnectionException(String message) {
        this(message, null);
    }

    public ApiConnectionException(String message, Throwable e) {
        super(message, null, null, 0, e);
    }
}
