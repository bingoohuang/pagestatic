package org.n3r.biz.pagestatic.impl;

import org.n3r.biz.pagestatic.PageStaticBuilder;
import org.n3r.biz.pagestatic.base.HttpClientCompleteListener;
import org.n3r.biz.pagestatic.base.RsyncCompleteListener;
import org.n3r.biz.pagestatic.config.Configable;
import org.n3r.biz.pagestatic.config.PageStaticConfig;
import org.n3r.biz.pagestatic.spec.Specs;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * 页面静态化上传框架配置文件解析类。
 *
 * @author Bingoo
 */
public class PageStaticSpecParser {
    private String specName;

    public PageStaticSpecParser(String specName) {
        this.specName = specName;
    }

    public void parse(PageStaticBuilder pageStaticBuilder) {
        Configable config = new PageStaticConfig(specName);

        parseRsyncRemote(pageStaticBuilder, config);
        parseRsyncDir(pageStaticBuilder, config);

        // 以下是可选参数
        httpSocketTimeoutSeconds(pageStaticBuilder, config);
        triggerUploadWhenMaxFiles(pageStaticBuilder, config);
        triggerUploadWhenMaxSeconds(pageStaticBuilder, config);
        deleteLocalDirAfterRsync(pageStaticBuilder, config);
        maxUrlContentGeneratingThreads(pageStaticBuilder, config);
        rsyncTimeoutSeconds(pageStaticBuilder, config);
        rsyncCompleteListener(pageStaticBuilder, config);
        rsyncOptions(pageStaticBuilder, config);
        tempDir(pageStaticBuilder, config);
        httpProxy(pageStaticBuilder, config);
        logger(pageStaticBuilder, config);
        rsyncRetryTimes(pageStaticBuilder, config);
        httpCompleteListener(pageStaticBuilder, config);
    }

    private void rsyncRetryTimes(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "rsyncRetryTimes";
        if (!config.exists(key)) return;

        pageStaticBuilder.rsyncRetryTimes(config.getInt(key));
    }

    private void httpCompleteListener(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "httpClientCompleteListener";
        if (!config.exists(key)) return;

        List<HttpClientCompleteListener> listeners = Specs.newObjects(
                config.getStr(key), HttpClientCompleteListener.class);
        if (listeners.size() > 0)
            pageStaticBuilder.httpClientCompleteListener(listeners.get(0));
    }

    private void rsyncCompleteListener(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "rsyncCompleteListener";
        if (!config.exists(key)) return;

        List<RsyncCompleteListener> listeners = Specs.newObjects(
                config.getStr(key), RsyncCompleteListener.class);
        if (listeners.size() > 0)
            pageStaticBuilder.rsyncCompleteListener(listeners.get(0));
    }

    private void httpSocketTimeoutSeconds(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "httpSocketTimeoutSeconds";
        if (!config.exists(key)) return;

        pageStaticBuilder.httpSocketTimeoutSeconds(config.getInt(key));
    }

    private void httpProxy(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "httpProxy";
        if (!config.exists(key)) return;

        String httpProxy = config.getStr(key);
        String proxyHost = trim(substringBefore(httpProxy, ","));
        String proxyPort = trim(substringAfter(httpProxy, ","));
        pageStaticBuilder.httpProxy(proxyHost, Integer.valueOf(proxyPort));
    }

    private void logger(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "logger";
        if (!config.exists(key)) return;

        pageStaticBuilder.logger(config.getStr(key));
    }

    private void tempDir(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "tempDir";
        if (!config.exists(key)) return;

        pageStaticBuilder.tempDir(config.getStr(key));
    }

    private void triggerUploadWhenMaxFiles(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "triggerUploadWhenMaxFiles";
        if (!config.exists(key)) return;

        pageStaticBuilder.triggerUploadWhenMaxFiles(config.getInt(key));
    }

    private void triggerUploadWhenMaxSeconds(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "triggerUploadWhenMaxSeconds";
        if (!config.exists(key)) return;

        pageStaticBuilder.triggerUploadWhenMaxSeconds(config.getInt(key));
    }

    private void deleteLocalDirAfterRsync(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "deleteLocalDirAfterRsync";
        if (!config.exists(key)) return;

        pageStaticBuilder.deleteLocalDirAfterRsync(config.getBool(key));
    }

    private void maxUrlContentGeneratingThreads(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "maxUrlContentGeneratingThreads";
        if (!config.exists(key)) return;

        pageStaticBuilder.maxUrlContentGeneratingThreads(config.getInt(key));
    }

    private void rsyncTimeoutSeconds(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "rsyncTimeoutSeconds";
        if (!config.exists(key)) return;

        pageStaticBuilder.rsyncTimeoutSeconds(config.getInt(key));
    }

    private void rsyncOptions(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "rsyncOptions";
        if (!config.exists(key)) return;

        pageStaticBuilder.rsyncOptions(config.getStr(key));
    }

    private void parseRsyncRemote(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "addRsyncRemote";
        List<String> remotes = config.getList(key);
        for (Object value : remotes) {
            String rsyncRemote = (String) value;
            addRsyncRemote(pageStaticBuilder, rsyncRemote);
        }
    }

    private void parseRsyncDir(PageStaticBuilder pageStaticBuilder, Configable config) {
        String key = "addRsyncDir";
        List<String> remotes = config.getList(key);
        for (Object value : remotes) {
            String rsyncDir = (String) value;
            addRsyncDir(pageStaticBuilder, rsyncDir);
        }
    }

    private void addRsyncRemote(PageStaticBuilder pageStaticBuilder, String rsyncRemote) {
        if (isEmpty(rsyncRemote)) return;

        String remoteHost = trim(substringBefore(rsyncRemote, ","));
        String remoteUser = trim(substringAfter(rsyncRemote, ","));
        pageStaticBuilder.addRsyncRemote(remoteHost, remoteUser);
    }

    private void addRsyncDir(PageStaticBuilder pageStaticBuilder, String rsyncDir) {
        if (isEmpty(rsyncDir)) return;

        String localDir = trim(substringBefore(rsyncDir, ","));
        String remoteDir = trim(substringAfter(rsyncDir, ","));
        pageStaticBuilder.addRsyncDir(localDir, remoteDir);
    }
}
