package org.n3r.biz.pagestatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.n3r.biz.pagestatic.base.HttpClientCompleteListener;
import org.n3r.biz.pagestatic.base.RsyncCompleteListener;
import org.n3r.biz.pagestatic.bean.RsyncDir;
import org.n3r.biz.pagestatic.bean.RsyncRemote;
import org.n3r.biz.pagestatic.impl.PageStaticSpecParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageStaticBuilder {
    private List<RsyncRemote> rsyncRemotes = new ArrayList<RsyncRemote>();
    private List<RsyncDir> rsyncDirs = new ArrayList<RsyncDir>();
    private int uploadTriggerMaxFiles = 100;
    private int uploadTriggerMaxSeconds = 120;
    private boolean deleteLocalDirAfterRsync = true;
    private int maxGeneratingThreads = 1000;
    private int httpSocketTimeoutSeconds = 30;
    private int rsyncTimeoutSeconds = 30;
    private String rsyncOptions = "-az";
    private RsyncCompleteListener rsyncCompleteListener;
    private HttpClientCompleteListener httpClientCompleteListener;
    private String tempDir;
    private String proxyHost;
    private int proxyPort;
    private Logger logger = LoggerFactory.getLogger(PageStatic.class);
    private int rsyncRetryTimes = 3;

    public void validateConfig() {
        if (rsyncDirs.size() == 0 || rsyncRemotes.size() == 0)
            throw new RuntimeException("至少有一个addRsyncRemote和addRsyncDir的配置");
    }
    public PageStaticBuilder addRsyncRemote(String romoteHost, String remoteUser) {
        rsyncRemotes.add(new RsyncRemote(romoteHost, remoteUser));
        return this;
    }

    public PageStaticBuilder addRsyncDir(String localDir, String remoteDir) {
        rsyncDirs.add(new RsyncDir(localDir, remoteDir));
        return this;
    }

    public PageStaticBuilder triggerUploadWhenMaxFiles(int uploadTriggerMaxFiles) {
        if (uploadTriggerMaxFiles > 0) this.uploadTriggerMaxFiles =  uploadTriggerMaxFiles;
        return this;
    }

    public PageStaticBuilder triggerUploadWhenMaxSeconds(int uploadTriggerMaxSeconds) {
        if (uploadTriggerMaxSeconds > 0) this.uploadTriggerMaxSeconds  = uploadTriggerMaxSeconds;
        return this;
    }

    public PageStaticBuilder deleteLocalDirAfterRsync(boolean deleteLocalDirAfterRsync) {
        this.deleteLocalDirAfterRsync  = deleteLocalDirAfterRsync;
        return this;
    }

    public PageStaticBuilder rsyncOptions(String rsyncOptions) {
        if (StringUtils.isNotEmpty(rsyncOptions)) this.rsyncOptions = rsyncOptions;
        return this;
    }

    public PageStaticBuilder maxUrlContentGeneratingThreads(int maxGeneratingThreads) {
        if (maxGeneratingThreads > 0) this.maxGeneratingThreads = maxGeneratingThreads;
        return this;
    }

    public PageStatic build() {
        validateConfig();
        return new PageStatic(this);
    }

    public PageStaticBuilder fromSpec(String specConfig) {
        new PageStaticSpecParser(specConfig).parse(this);
        return this;
    }

    public List<RsyncRemote> getRsyncRemotes() {
        return rsyncRemotes;
    }

    public List<RsyncDir> getRsyncDirs() {
        return rsyncDirs;
    }

    public int getUploadTriggerMaxFiles() {
        return uploadTriggerMaxFiles;
    }

    public int getUploadTriggerMaxSeconds() {
        return uploadTriggerMaxSeconds;
    }

    public boolean isDeleteLocalDirAfterRsync() {
        return deleteLocalDirAfterRsync;
    }

    public int getMaxGeneratingThreads() {
        return maxGeneratingThreads;
    }

    public PageStaticBuilder httpSocketTimeoutSeconds(int httpSocketTimeoutSeconds) {
        if (httpSocketTimeoutSeconds > 0) this.httpSocketTimeoutSeconds = httpSocketTimeoutSeconds;
        return this;
    }

    public int getHttpSocketTimeoutSeconds() {
        return httpSocketTimeoutSeconds;
    }

    public PageStaticBuilder rsyncTimeoutSeconds(int rsyncTimeoutSeconds) {
        if (rsyncTimeoutSeconds > 0) this.rsyncTimeoutSeconds = rsyncTimeoutSeconds;
        return this;
    }

    public int getRsyncTimeoutSeconds() {
        return rsyncTimeoutSeconds;
    }

    public PageStaticBuilder rsyncCompleteListener(RsyncCompleteListener rsyncCompleteListener) {
        this.rsyncCompleteListener = rsyncCompleteListener;
        return this;
    }

    public RsyncCompleteListener getRsyncCompleteListener() {
        return rsyncCompleteListener;
    }

    public PageStaticBuilder httpClientCompleteListener(HttpClientCompleteListener httpClientCompleteListener) {
        this.httpClientCompleteListener = httpClientCompleteListener;
        return this;
    }

    public HttpClientCompleteListener getHttpClientCompleteListener() {
        return httpClientCompleteListener;
    }

    public String getRsyncOptions() {
        return rsyncOptions;
    }
    public PageStaticBuilder tempDir(String tempDir) {
        this.tempDir = tempDir;
        return this;
    }

    public String getTempDir() {
        return tempDir;
    }

    public PageStaticBuilder httpProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        return this;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }
    public PageStaticBuilder logger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        return this;
    }

    public Logger getLogger() {
        return logger;
    }

    public PageStaticBuilder logger(String str) {
        this.logger = LoggerFactory.getLogger(str);
        return this;
    }

    public PageStaticBuilder rsyncRetryTimes(int rsyncRetryTimes) {
        this.rsyncRetryTimes = rsyncRetryTimes;
        return this;
    }

    public int getRsyncRetryTimes() {
        return rsyncRetryTimes;
    }
}
