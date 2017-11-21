package org.n3r.biz.pagestatic.impl;

import lombok.Setter;
import org.n3r.biz.pagestatic.base.PageService;
import org.n3r.biz.pagestatic.bean.Page;
import org.n3r.biz.pagestatic.util.PageStaticUtils;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 页面文件生成与上传类。
 *
 * @author Bingoo
 */
public class PageUploader {
    private Logger log;

    @Setter private BlockingQueue<Page> pageQueue;
    @Setter private PageService pageService;
    private PageUploadTrigger uploadTrigger = new PageUploadTrigger();

    private PageRsync pageRsync;

    public PageUploader(Logger log) {
        this.log = log;
    }

    public void startUpload(PageRsync pageRsync) {
        this.pageRsync = pageRsync;
        pageRsync.initialize();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    createFileAndRsyncUpload();
                } catch (Exception ex) {
                    log.error("createFileAndRsyncUpload has an error!", ex);
                }
            }
        }).start();
    }

    private void createFileAndRsyncUpload() {
        uploadTrigger.reset();

        while (true) {
            checkUploadTriggered();

            while (pollQueueAndCreateFile()) ;

            if (pageService.isTerminated()) break;
        }

        log.info("page uploader is going to shut down!");

        pageRsync.rsync(uploadTrigger);
        pageService.shutdown();
        log.info("page uploader shut down after processed {} files with {} seconds!",
                uploadTrigger.getTotalFileCounting(), uploadTrigger.getTotalCostSeconds());
    }

    private void checkUploadTriggered() {
        if (!uploadTrigger.reachTrigger()) return;

        pageRsync.rsync(uploadTrigger);
    }

    private boolean pollQueueAndCreateFile() {
        Page page = pollQueue();
        if (page == null) return false;

        int fileCount = PageStaticUtils.createFile(log, page);
        uploadTrigger.incrFileCounting(fileCount);

        return true;
    }

    private Page pollQueue() {
        try {
            return pageQueue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("poll queue catched InterruptedException {}", e);
            return null;
        }
    }

    public void setUploadTriggerMaxFiles(int uploadTriggerMaxFiles) {
        uploadTrigger.setUploadTriggerMaxFiles(uploadTriggerMaxFiles);
    }

    public void setUploadTriggerMaxSeconds(int uploadTriggerMaxSeconds) {
        uploadTrigger.setUploadTriggerMaxSeconds(uploadTriggerMaxSeconds);
    }
}
