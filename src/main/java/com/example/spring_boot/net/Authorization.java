package com.example.spring_boot.net;

import com.example.spring_boot.exception.TenXException;

public interface Authorization {
    TenXRequest authenticate(TenXRequest request) throws TenXException;
}

