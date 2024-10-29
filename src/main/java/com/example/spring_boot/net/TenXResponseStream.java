package com.example.spring_boot.net;

import com.example.spring_boot.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

public class TenXResponseStream extends AbstractTenXResponse<InputStream> {
    protected TenXResponseStream(int code, HttpHeaders headers, InputStream body) {
        super(code, headers, body);
    }

    TenXResponse unstream() throws IOException {
        final String bodyString = StreamUtils.readToEnd(this.body, ApiResource.CHARSET);
        this.body.close();
        return new TenXResponse(this.code, this.headers, bodyString);
    }
}
