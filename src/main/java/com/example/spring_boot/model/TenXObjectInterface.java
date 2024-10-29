package com.example.spring_boot.model;

import com.example.spring_boot.net.TenXResponse;

public interface TenXObjectInterface {
    public TenXResponse getLastResponse();

    public void setLastResponse(TenXResponse response);
}
