package org.n3r.biz.pagestatic;

import lombok.Value;

@Value
public class HttpReqHeader {
    private final String name;
    private final String value;

    public HttpReqHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
