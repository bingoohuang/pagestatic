package org.n3r.biz.pagestatic.impl;

import lombok.AllArgsConstructor;
import lombok.val;
import org.n3r.biz.pagestatic.PageStaticBuilder;
import org.n3r.biz.pagestatic.base.HttpClientCompleteListener;
import org.n3r.biz.pagestatic.base.RsyncCompleteListener;
import org.n3r.biz.pagestatic.config.Configable;
import org.n3r.biz.pagestatic.config.PageStaticConfig;
import org.n3r.biz.pagestatic.spec.Specs;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * 页面静态化上传框架配置文件解析类。
 *
 * @author Bingoo
 */
@AllArgsConstructor
public class PageStaticSpecParser {
    private final String config;

    public void parse(PageStaticBuilder pageStaticBuilder) {
        val config = new PageStaticConfig(this.config);

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
        val key = "rsyncRetryTimes";
        if (!config.exists(key)) return;

        pageStaticBuilder.rsyncRetryTimes(config.getInt(key));
    }

    private void httpCompleteListener(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "httpClientCompleteListener";
        if (!config.exists(key)) return;

        val listeners = Specs.newObjects(config.getStr(key), HttpClientCompleteListener.class);
        if (listeners.size() > 0)
            pageStaticBuilder.httpClientCompleteListener(listeners.get(0));
    }

    private void rsyncCompleteListener(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "rsyncCompleteListener";
        if (!config.exists(key)) return;

        val listeners = Specs.newObjects(config.getStr(key), RsyncCompleteListener.class);
        if (listeners.size() > 0)
            pageStaticBuilder.rsyncCompleteListener(listeners.get(0));
    }

    private void httpSocketTimeoutSeconds(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "httpSocketTimeoutSeconds";
        if (!config.exists(key)) return;

        pageStaticBuilder.httpSocketTimeoutSeconds(config.getInt(key));
    }

    private void httpProxy(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "httpProxy";
        if (!config.exists(key)) return;

        val httpProxy = config.getStr(key);
        val proxyHost = trim(substringBefore(httpProxy, ","));
        val proxyPort = trim(substringAfter(httpProxy, ","));
        pageStaticBuilder.httpProxy(proxyHost, Integer.valueOf(proxyPort));
    }

    private void logger(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "logger";
        if (!config.exists(key)) return;

        pageStaticBuilder.logger(config.getStr(key));
    }

    private void tempDir(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "tempDir";
        if (!config.exists(key)) return;

        pageStaticBuilder.tempDir(config.getStr(key));
    }

    private void triggerUploadWhenMaxFiles(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "triggerUploadWhenMaxFiles";
        if (!config.exists(key)) return;

        pageStaticBuilder.triggerUploadWhenMaxFiles(config.getInt(key));
    }

    private void triggerUploadWhenMaxSeconds(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "triggerUploadWhenMaxSeconds";
        if (!config.exists(key)) return;

        pageStaticBuilder.triggerUploadWhenMaxSeconds(config.getInt(key));
    }

    private void deleteLocalDirAfterRsync(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "deleteLocalDirAfterRsync";
        if (!config.exists(key)) return;

        pageStaticBuilder.deleteLocalDirAfterRsync(config.getBool(key));
    }

    private void maxUrlContentGeneratingThreads(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "maxUrlContentGeneratingThreads";
        if (!config.exists(key)) return;

        pageStaticBuilder.maxUrlContentGeneratingThreads(config.getInt(key));
    }

    private void rsyncTimeoutSeconds(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "rsyncTimeoutSeconds";
        if (!config.exists(key)) return;

        pageStaticBuilder.rsyncTimeoutSeconds(config.getInt(key));
    }

    private void rsyncOptions(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "rsyncOptions";
        if (!config.exists(key)) return;

        pageStaticBuilder.rsyncOptions(config.getStr(key));
    }

    private void parseRsyncRemote(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "addRsyncRemote";
        val remotes = config.getList(key);
        for (val value : remotes) {
            val rsyncRemote = value;
            addRsyncRemote(pageStaticBuilder, rsyncRemote);
        }
    }

    private void parseRsyncDir(PageStaticBuilder pageStaticBuilder, Configable config) {
        val key = "addRsyncDir";
        val remotes = config.getList(key);
        for (val value : remotes) {
            String rsyncDir = value;
            addRsyncDir(pageStaticBuilder, rsyncDir);
        }
    }

    private void addRsyncRemote(PageStaticBuilder pageStaticBuilder, String rsyncRemote) {
        if (isEmpty(rsyncRemote)) return;

        val remoteHost = trim(substringBefore(rsyncRemote, ","));
        val remoteUser = trim(substringAfter(rsyncRemote, ","));
        pageStaticBuilder.addRsyncRemote(remoteHost, remoteUser);
    }

    private void addRsyncDir(PageStaticBuilder pageStaticBuilder, String rsyncDir) {
        if (isEmpty(rsyncDir)) return;

        val localDir = trim(substringBefore(rsyncDir, ","));
        val remoteDir = trim(substringAfter(rsyncDir, ","));
        pageStaticBuilder.addRsyncDir(localDir, remoteDir);
    }
}
