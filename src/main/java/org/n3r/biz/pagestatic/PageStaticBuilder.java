package org.n3r.biz.pagestatic;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.n3r.biz.pagestatic.base.HttpClientCompleteListener;
import org.n3r.biz.pagestatic.base.RsyncCompleteListener;
import org.n3r.biz.pagestatic.bean.RsyncDir;
import org.n3r.biz.pagestatic.bean.RsyncRemote;
import org.n3r.biz.pagestatic.impl.PageStaticSpecParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PageStaticBuilder {
    @Getter private List<RsyncRemote> rsyncRemotes = new ArrayList<RsyncRemote>();
    @Getter private List<RsyncDir> rsyncDirs = new ArrayList<RsyncDir>();
    @Getter private int uploadTriggerMaxFiles = 100;
    @Getter private int uploadTriggerMaxSeconds = 120;
    @Getter private boolean deleteLocalDirAfterRsync = true;
    @Getter private int maxGeneratingThreads = 1000;
    @Getter private int httpSocketTimeoutSeconds = 30;
    @Getter private int rsyncTimeoutSeconds = 30;
    @Getter private String rsyncOptions = "-az";
    @Getter private RsyncCompleteListener rsyncCompleteListener;
    @Getter private HttpClientCompleteListener httpClientCompleteListener;
    @Getter private String tempDir;
    @Getter private String proxyHost;
    @Getter private int proxyPort;
    @Getter private Logger logger = LoggerFactory.getLogger(PageStatic.class);
    @Getter private int rsyncRetryTimes = 3;

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
        if (uploadTriggerMaxFiles > 0) this.uploadTriggerMaxFiles = uploadTriggerMaxFiles;
        return this;
    }

    public PageStaticBuilder triggerUploadWhenMaxSeconds(int uploadTriggerMaxSeconds) {
        if (uploadTriggerMaxSeconds > 0) this.uploadTriggerMaxSeconds = uploadTriggerMaxSeconds;
        return this;
    }

    public PageStaticBuilder deleteLocalDirAfterRsync(boolean deleteLocalDirAfterRsync) {
        this.deleteLocalDirAfterRsync = deleteLocalDirAfterRsync;
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

    public PageStaticBuilder httpSocketTimeoutSeconds(int httpSocketTimeoutSeconds) {
        if (httpSocketTimeoutSeconds > 0) this.httpSocketTimeoutSeconds = httpSocketTimeoutSeconds;
        return this;
    }

    public PageStaticBuilder rsyncTimeoutSeconds(int rsyncTimeoutSeconds) {
        if (rsyncTimeoutSeconds > 0) this.rsyncTimeoutSeconds = rsyncTimeoutSeconds;
        return this;
    }

    public PageStaticBuilder rsyncCompleteListener(RsyncCompleteListener rsyncCompleteListener) {
        this.rsyncCompleteListener = rsyncCompleteListener;
        return this;
    }

    public PageStaticBuilder httpClientCompleteListener(HttpClientCompleteListener httpClientCompleteListener) {
        this.httpClientCompleteListener = httpClientCompleteListener;
        return this;
    }

    public PageStaticBuilder tempDir(String tempDir) {
        this.tempDir = tempDir;
        return this;
    }

    public PageStaticBuilder httpProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        return this;
    }

    public PageStaticBuilder logger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        return this;
    }

    public PageStaticBuilder logger(String str) {
        this.logger = LoggerFactory.getLogger(str);
        return this;
    }

    public PageStaticBuilder rsyncRetryTimes(int rsyncRetryTimes) {
        this.rsyncRetryTimes = rsyncRetryTimes;
        return this;
    }
}
