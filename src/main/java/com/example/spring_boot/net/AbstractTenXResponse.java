package com.example.spring_boot.net;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import static java.util.Objects.requireNonNull;

@Accessors(fluent = true)
public class AbstractTenXResponse<T> {
    /** The HTTP status code of the response. */
    int code;

    /** The HTTP headers of the response. */
    HttpHeaders headers;

    /** The body of the response. */
    T body;

    /** Number of times the request was retried. Used for internal tests only. */
    @NonFinal
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    int numRetries;

    public final int code() {
        return this.code;
    }

    public final HttpHeaders headers() {
        return this.headers;
    }

    public final T body() {
        return this.body;
    }

    protected AbstractTenXResponse(int code, HttpHeaders headers, T body) {
        requireNonNull(headers);
        requireNonNull(body);

        this.code = code;
        this.headers = headers;
        this.body = body;
    }
}
