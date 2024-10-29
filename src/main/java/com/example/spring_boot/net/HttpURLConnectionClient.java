package com.example.spring_boot.net;

import com.example.spring_boot.TenX;
import com.example.spring_boot.exception.ApiConnectionException;
import com.example.spring_boot.exception.TenXException;
import lombok.Cleanup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpURLConnectionClient extends HttpClient {
    public HttpURLConnectionClient() {
        super();
    }

    @Override
    public TenXResponseStream requestStream(TenXRequest request) throws ApiConnectionException {
        try {
            final HttpURLConnection conn = createConnection(request);

            // Calling `getResponseCode()` triggers the request.
            final int responseCode = conn.getResponseCode();

            final HttpHeaders headers = HttpHeaders.of(conn.getHeaderFields());

            final InputStream responseStream =
                    (responseCode >= 200 && responseCode < 300)
                            ? conn.getInputStream()
                            : conn.getErrorStream();

            return new TenXResponseStream(responseCode, headers, responseStream);

        } catch (IOException e) {
            throw new ApiConnectionException(
                    String.format(
                            "IOException during API request to TenX (%s): %s ",
                            TenX.getApiBase(), e.getMessage()),
                    e);
        }
    }


    @Override
    public TenXResponse request(TenXRequest request) throws TenXException {
        final TenXResponseStream responseStream = requestStream(request);
        try {
            return responseStream.unstream();
        } catch (IOException e) {
            throw new ApiConnectionException(
                    String.format(
                            "IOException during API request to TenX (%s): %s ",
                            TenX.getApiBase(), e.getMessage()),
                    e);
        }
    }

    static HttpHeaders getHeaders(TenXRequest request) {
        Map<String, List<String>> userAgentHeadersMap = new HashMap<>();

        userAgentHeadersMap.put("User-Agent",Arrays.asList("Client"));
        return request.headers().withAdditionalHeaders(userAgentHeadersMap);
    }

    private static HttpURLConnection createConnection(TenXRequest request)
            throws IOException, ApiConnectionException {
        HttpURLConnection conn = null;

        if (request.options().getConnectionProxy() != null) {
            conn =
                    (HttpURLConnection) request.url().openConnection(request.options().getConnectionProxy());
            Authenticator.setDefault(
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return request.options().getProxyCredential();
                        }
                    });
        } else {
            conn = (HttpURLConnection) request.url().openConnection();
        }

        conn.setConnectTimeout(request.options().getConnectTimeout());
        conn.setReadTimeout(request.options().getReadTimeout());
        conn.setUseCaches(false);
        for (Map.Entry<String, List<String>> entry : getHeaders(request).map().entrySet()) {
            conn.setRequestProperty(entry.getKey(), String.join(",", entry.getValue()));
        }

        conn.setRequestMethod(request.method().name());

        if (request.content() != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", request.content().contentType());

            @Cleanup OutputStream output = conn.getOutputStream();
            output.write(request.content().byteArrayContent());
        }

        return conn;
    }
}
