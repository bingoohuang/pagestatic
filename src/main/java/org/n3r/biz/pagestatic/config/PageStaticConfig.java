package org.n3r.biz.pagestatic.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageStaticConfig implements Configable {
    private Logger log = LoggerFactory.getLogger(PageStaticConfig.class);
    private HashMap<String, Object> properties = Maps.newHashMap();

    public PageStaticConfig(String pageStaticSpec) {
        if (StringUtils.isEmpty(pageStaticSpec)) return;

        Iterable<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings()
                .split(pageStaticSpec);

        for (String line : lines) {
            if (line.startsWith("#")) continue;
            if (line.startsWith("//")) continue;

            int leftBracePos = line.indexOf('(');
            if (leftBracePos > 0) {
                int rightBracePos = line.indexOf(')');
                if (rightBracePos > leftBracePos) {
                    String key = line.substring(0, leftBracePos).trim();
                    String value = StringUtils.trim(StringUtils.substringBetween(line, "(", ")"));
                    put(key, value);
                } else {
                    log.warn("line {} found unmatched brace", line);
                }

                continue;
            }

            int leftEqual = line.indexOf('=');
            if (leftEqual > 0) {
                pareKeyValue(line, leftEqual);
                continue;
            }

            int leftColon = line.indexOf(':');
            if (leftColon > 0) {
                pareKeyValue(line, leftColon);
                continue;
            }

            put(line, "");
        }
    }

    private void pareKeyValue(String line, int leftEqual) {
        String key = line.substring(0, leftEqual).trim();
        String value = StringUtils.trim(StringUtils.substring(line, leftEqual + 1));
        put(key, value);
    }

    private void put(String key, String value) {
        Object existsValue = properties.get(key);
        if (existsValue == null) {
            properties.put(key, value);
            return;
        }

        if (existsValue instanceof List) {
            ((List) existsValue).add(value);
        } else {
            properties.put(key, Lists.newArrayList((String) existsValue, value));
        }
    }


    @Override
    public boolean exists(String key) {
        return properties.containsKey(key);
    }

    private static Pattern numberPattern = Pattern
            .compile("(-?[0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+|-?[0-9]+).*");

    @Override
    public int getInt(String key) {
        if (!exists(key))
            throw new RuntimeException(key + " not found in config system");

        String str = getStr(key);
        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new RuntimeException(key + "'s value [" + str + "] is not an int");

        String intStr = StringUtils.substringBefore(matcher.group(1), ".");
        if (StringUtils.isEmpty(intStr))
            return 0;

        return Integer.valueOf(intStr);
    }

    @Override
    public String getStr(String key) {
        Object value = properties.get(key);
        return value == null ? null : value.toString();
    }

    @Override
    public boolean getBool(String key) {
        if (!exists(key))
            throw new RuntimeException(key + " not found in config system");

        return toBool(getStr(key));
    }

    @Override
    public List<String> getList(String key) {
        Object value = properties.get(key);
        if (value == null) return Collections.emptyList();

        return value instanceof List ? (List<String>) value
                : Lists.newArrayList((String) value);

    }

    private boolean toBool(String str) {
        return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str)
                || "on".equalsIgnoreCase(str) || "y".equalsIgnoreCase(str);
    }
}
