package com.example.spring_boot.net;

import com.example.spring_boot.exception.ApiConnectionException;
import com.example.spring_boot.exception.TenXException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public abstract class HttpClient {
    public static final Duration maxNetworkRetriesDelay = Duration.ofSeconds(5);
    public static final Duration minNetworkRetriesDelay = Duration.ofMillis(500);
    boolean networkRetriesSleep = true;

    protected HttpClient() {}

    public abstract TenXResponse request(TenXRequest request) throws TenXException;

    public TenXResponseStream requestStream(TenXRequest request) throws TenXException {
        throw new UnsupportedOperationException("requestStream is unimplemented for this HttpClient");
    }

    public TenXResponse requestWithRetries(TenXRequest request) throws TenXException {
        return sendWithRetries(request, (r) -> this.request(r));
    }

    public TenXResponseStream requestStreamWithRetries(TenXRequest request)
            throws TenXException {
        return sendWithRetries(request, (r) -> this.requestStream(r));
    }

    @FunctionalInterface
    private interface RequestSendFunction<R> {
        R apply(TenXRequest request) throws TenXException;
    }


    public <T extends AbstractTenXResponse<?>> T sendWithRetries(
            TenXRequest request, RequestSendFunction<T> send) throws TenXException {
        ApiConnectionException requestException = null;
        T response = null;
        int retry = 0;

        while (true) {
            requestException = null;

            try {
                response = send.apply(request);
            } catch (ApiConnectionException e) {
                requestException = e;
            }

            if (!this.shouldRetry(retry, requestException, request, response)) {
                break;
            }

            retry += 1;

            try {
                Thread.sleep(this.sleepTime(retry).toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (requestException != null) {
            throw requestException;
        }

        response.numRetries(retry);

        return response;
    }

    private <T extends AbstractTenXResponse<?>> boolean shouldRetry(
            int numRetries, Exception exception, TenXRequest request, T response) {
        // Do not retry if we are out of retries.
        if (numRetries >= request.options().getMaxNetworkRetries()) {
            return false;
        }

        // Retry on connection error.
        if ((exception != null)
                && (exception.getCause() != null)
                && (exception.getCause() instanceof ConnectException
                || exception.getCause() instanceof SocketTimeoutException)) {
            return true;
        }

        // The API may ask us not to retry (eg; if doing so would be a no-op)
        // or advise us to retry (eg; in cases of lock timeouts); we defer to that.
        if ((response != null) && (response.headers() != null)) {
            String value = response.headers().firstValue("TenX-Should-Retry").orElse(null);

            if ("true".equals(value)) {
                return true;
            }

            if ("false".equals(value)) {
                return false;
            }
        }

        // Retry on conflict errors.
        if ((response != null) && (response.code() == 409)) {
            return true;
        }

        // Retry on 500, 503, and other internal errors.
        //
        // Note that we expect the Stripe-Should-Retry header to be false
        // in most cases when a 500 is returned, since our idempotency framework
        // would typically replay it anyway.
        if ((response != null) && (response.code() >= 500)) {
            return true;
        }

        return false;
    }


    private Duration sleepTime(int numRetries) {
        // We disable sleeping in some cases for tests.
        if (!this.networkRetriesSleep) {
            return Duration.ZERO;
        }

        // Apply exponential backoff with MinNetworkRetriesDelay on the number of numRetries
        // so far as inputs.
        Duration delay =
                Duration.ofNanos((long) (minNetworkRetriesDelay.toNanos() * Math.pow(2, numRetries - 1)));

        // Do not allow the number to exceed MaxNetworkRetriesDelay
        if (delay.compareTo(maxNetworkRetriesDelay) > 0) {
            delay = maxNetworkRetriesDelay;
        }

        // Apply some jitter by randomizing the value in the range of 75%-100%.
        double jitter = ThreadLocalRandom.current().nextDouble(0.75, 1.0);
        delay = Duration.ofNanos((long) (delay.toNanos() * jitter));

        // But never sleep less than the base sleep seconds.
        if (delay.compareTo(minNetworkRetriesDelay) < 0) {
            delay = minNetworkRetriesDelay;
        }

        return delay;
    }
}
