package org.n3r.biz.pagestatic;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.biz.pagestatic.base.PageService;
import org.n3r.biz.pagestatic.bean.Page;
import org.n3r.biz.pagestatic.bean.PageStat;
import org.n3r.biz.pagestatic.impl.PageHttpClient;
import org.n3r.biz.pagestatic.impl.PageRsync;
import org.n3r.biz.pagestatic.impl.PageUploader;
import org.n3r.biz.pagestatic.util.PageStaticUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Staticize a page content retrieved from an URL and uploaded by rsync.
 *
 * @author Bingoo
 * <p>
 * 2013年07月05日 Bingoo v0.3.5 增强 Rsync命令调用完成后日志输出增加当前用户显示(有可能做rsync互信的用户不是启动程序的用户)。
 * 2013年05月29日 Bingoo v0.3.4 增强 支持已有独立文件或者目录的上传。
 * 2013年01月24日 Bingoo v0.3.3 增强 修复多线程下载内容可能混淆的BUG；修正HttpClient MaxConnectionPerHost参数设置。
 * 2013年01月19日 Bingoo v0.3.2 增强 定时上传修正为在第一个文件生成时开始计时；检查本地是否有未上传文件时不计算总数。
 * 2013年01月18日 Bingoo v0.3.1 增强 在启动rsync命令之前检查本地目录是否存在，如果不存在则不启动。
 */
public class PageStatic implements PageService {
    private Logger log;

    private volatile BlockingQueue<Page> pageQueue;
    private volatile ExecutorService threadPool4GetUrlContent;
    private volatile PageHttpClient pageHttpClient;

    private volatile PageRsync pageRsync;
    private volatile PageUploader pageUploader;
    private volatile PageStat pageStat;

    private PageStaticBuilder pageStaticBuilder;

    private File tempDir;

    PageStatic(PageStaticBuilder pageStaticBuilder) {
        this.pageStaticBuilder = pageStaticBuilder;
        log = pageStaticBuilder.getLogger();
    }

    /**
     * 注意:请在本地文件全部生成好后调用再调用, 边生成边上传会有问题.
     *
     * @param localFileName 文件或者文件目录
     */
    public void directUpload(File localFileName) {
        putQueue("direct://", localFileName, localFileName);
    }

    @SneakyThrows
    public void directContentUpload(String content, String localFileName) {
        @Cleanup val is = IOUtils.toInputStream(content);
        File file = PageStaticUtils.createTmpFile(log, tempDir, "direct://", localFileName, is);

        putQueue("direct://", file, new File(localFileName));
    }

    public void urlStaticAndUpload(final String url, final String localFileName,
                                   final Object... callbackParams) {
        checkPageHttpClientCreated(url);
        threadPool4GetUrlContent.submit(new Runnable() {
            @Override
            public void run() {
                if (!pageHttpClient.executeGetMethod(url, callbackParams, localFileName)) return;
                putQueue(url, pageHttpClient.getContent(), new File(localFileName));
            }
        });
    }

    public void startupBatch() {
        createThreadPool4GetUrlContent();
        createPageQueue();
        createPageUploader();
        createPageRsync();
        startupUploader();
    }

    private void createTmpDir() {
        tempDir = new File(StringUtils.defaultIfBlank(pageStaticBuilder.getTempDir(), pageStat.getBatchId()));
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            log.error("mkdir fail {}", tempDir);
            throw new RuntimeException("unable to create temp dir" + tempDir);
        }
    }

    private void deleteTempDir() {
        PageStaticUtils.deleteDirRecursively(tempDir);
    }

    private void createThreadPool4GetUrlContent() {
        threadPool4GetUrlContent = Executors.newFixedThreadPool(pageStaticBuilder.getMaxGeneratingThreads());
    }

    private void startupUploader() {
        pageUploader.startUpload(pageRsync);

        pageStat = new PageStat();

        log.info("{} page static started up!", pageStat.getBatchId());

        createTmpDir();
    }

    private void createPageRsync() {
        pageRsync = new PageRsync(log);
        pageRsync.setRsyncRemotes(pageStaticBuilder.getRsyncRemotes());
        pageRsync.setRsyncDirs(pageStaticBuilder.getRsyncDirs());
        pageRsync.setRsyncOptions(pageStaticBuilder.getRsyncOptions());
        pageRsync.setDeleteLocalDirAfterRsync(pageStaticBuilder.isDeleteLocalDirAfterRsync());
        pageRsync.setRsyncTimeoutSeconds(pageStaticBuilder.getRsyncTimeoutSeconds());
        pageRsync.setRsyncCompleteListener(pageStaticBuilder.getRsyncCompleteListener());
        pageRsync.setRsyncRetryTimes(pageStaticBuilder.getRsyncRetryTimes());
    }

    private void createPageUploader() {
        pageUploader = new PageUploader(log);
        pageUploader.setUploadTriggerMaxFiles(pageStaticBuilder.getUploadTriggerMaxFiles());
        pageUploader.setUploadTriggerMaxSeconds(pageStaticBuilder.getUploadTriggerMaxSeconds());
        pageUploader.setPageService(this);
        pageUploader.setPageQueue(pageQueue);
    }

    private BlockingQueue<Page> createPageQueue() {
        return pageQueue = new LinkedBlockingQueue<Page>();
    }

    private void checkPageHttpClientCreated(String url) {
        if (pageHttpClient != null) return;

        pageHttpClient = new PageHttpClient(log);
        pageHttpClient.setTempDir(tempDir);
        pageHttpClient.setHttpSocketTimeoutSeconds(pageStaticBuilder.getHttpSocketTimeoutSeconds());
        pageHttpClient.setHttpClientCompleteListener(pageStaticBuilder.getHttpClientCompleteListener());
        pageHttpClient.setProxyHost(pageStaticBuilder.getProxyHost());
        pageHttpClient.setProxyPort(pageStaticBuilder.getProxyPort());
        pageHttpClient.setMaxGeneratingThreads(pageStaticBuilder.getMaxGeneratingThreads());
        pageHttpClient.startup();
    }

    private void waitLastBatchFinish() {
        while (pageRsync != null) PageStaticUtils.sleepMilis(100);
    }

    private void putQueue(String url, File content, File file) {
        try {
            pageQueue.put(new Page(url, content, file));
        } catch (InterruptedException e) {
            log.error("put queue catched InterruptedException", e);
        }
    }


    public void finishBatch() {
        threadPool4GetUrlContent.shutdown();
        waitLastBatchFinish();
        deleteTempDir();
    }

    @Override
    public boolean isTerminated() {
        return threadPool4GetUrlContent.isTerminated() && pageQueue.isEmpty();
    }

    @Override
    public void shutdown() {
        threadPool4GetUrlContent = null;

        if (pageHttpClient != null) {
            pageHttpClient.shutdown();
            pageHttpClient = null;
        }

        pageUploader = null;
        pageQueue = null;
        pageRsync = null;
        pageStat.setEndupMillis(System.currentTimeMillis());

        log.info("{} page static shutted down!, cost {} seconds", pageStat.getBatchId(),
                pageStat.getCostMillis() / 1000.);
    }

    public PageStat getPageStat() {
        return pageStat;
    }
}
