package org.n3r.biz.pagestatic.spec;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Spec {
    @Getter @Setter private String name;
    private List<String> params = new ArrayList<String>();

    public String[] getParams() {
        return params.toArray(new String[0]);
    }

    public void addParam(String param) {
        params.add(param);
    }
}
