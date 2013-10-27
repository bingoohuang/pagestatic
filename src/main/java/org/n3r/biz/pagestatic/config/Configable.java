package org.n3r.biz.pagestatic.config;

import java.util.List;

public interface Configable {
    boolean exists(String key);

    int getInt(String key);

    String getStr(String key);

    boolean getBool(String key);

    List<String> getList(String key);
}

