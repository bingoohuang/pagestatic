package org.n3r.biz.pagestatic;

public class HttpReqHeader {
    private final String name;
    private final String value;

    public HttpReqHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
