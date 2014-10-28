package org.n3r.biz.pagestatic.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.n3r.biz.pagestatic.base.RsyncCompleteListener;
import org.n3r.biz.pagestatic.bean.RsyncDir;
import org.n3r.biz.pagestatic.bean.RsyncRemote;
import org.n3r.biz.pagestatic.bean.RsyncRunInfo;
import org.n3r.biz.pagestatic.util.PageStaticUtils;
import org.slf4j.Logger;

/**
 * 页面内容上传管理类。
 * @author Bingoo
 *
 */
public class PageRsync {
    private Logger log;

    private List<RsyncRemote> rsyncRemotes;
    private List<RsyncDir> rsyncDirs;
    private boolean deleteLocalDirAfterRsync;
    private long millisOfCheckRsyncExited = 1000;
    private long rsyncTimeoutMilis;

    private List<String> localDirs;
    private ArrayList<PageRsyncCmd> rsyncCmds;
    private RsyncCompleteListener rsyncCompleteListener;

    private String rsyncOptions;
    private int rsyncRetryTimes;

    public PageRsync(Logger log) {
        this.log = log;
    }

    public void initialize() {
        buildRsyncCmds(); // 构建Rsync上传命令
        buildUniqueLocalDirs(); // 构建本地目录（排重）
    }

    private void buildUniqueLocalDirs() {
        if (!deleteLocalDirAfterRsync) return;

        localDirs = new ArrayList<String>(rsyncDirs.size());
        for(RsyncDir dir: rsyncDirs)
            if (!localDirs.contains(dir.getLocalDir()))
                localDirs.add(dir.getLocalDir());
    }

    private boolean hasUnrsyncedFiles() {
        if (deleteLocalDirAfterRsync) return false;

        return PageStaticUtils.hasFiles(localDirs);
    }

    private void buildRsyncCmds() {
        rsyncCmds = new ArrayList<PageRsyncCmd>();
        for(RsyncRemote remote: rsyncRemotes)
            for(RsyncDir dir: rsyncDirs)
                addRsyncCmd(remote, dir);

        rsyncCmds.trimToSize();
    }

    private void addRsyncCmd(RsyncRemote conf, RsyncDir path) {
        if (StringUtils.isEmpty(path.getRemoteHost())
                || StringUtils.equals(path.getRemoteHost(), conf.getDestHost()))
            rsyncCmds.add(new PageRsyncCmd(log, this, conf, path));
    }

    public void rsync(PageUploadTrigger uploadTrigger) {
        // 没有文件上传并且本地没有未同步的文件时，不调用rsync命令
        if (uploadTrigger.getFileCounting() <= 0 && !hasUnrsyncedFiles()) return;

        startAndWaitRsync();
        retryFailedRsyncs(); // 重试失败的rsync
        deleteLocalDirsAsRequired();
        uploadTrigger.reset();
    }

    private void startAndWaitRsync() {
        log.info("rsync began");
        if (!PageStaticUtils.isWindowsOS())
            waitAndCheckTerminate(startExecuteAllRsyncCmds());

        log.info("rsync finished");
    }

    private void retryStartAndWaitRsync(int retryTimes) {
        log.info("retry {} rsync began", retryTimes);
        if (!PageStaticUtils.isWindowsOS())
            waitAndCheckTerminate(retryStartExecuteAllRsyncCmds());

        log.info("retry {} rsync finished", retryTimes);
    }

    private void waitAndCheckTerminate(int rsyncCmdNum) {
        int totalAlives = rsyncCmdNum;
        while (totalAlives > 0) {
            PageStaticUtils.sleepMilis(millisOfCheckRsyncExited);

            for (PageRsyncCmd cmd : rsyncCmds)
                if (cmd.destroyWhenExpired(millisOfCheckRsyncExited, rsyncTimeoutMilis))
                    --totalAlives;
        }
    }

    private void retryFailedRsyncs() {
        for(int retryTimes = 1;
                retryTimes <= rsyncRetryTimes && !isAllRsyncExitNormally();
                ++retryTimes)
            retryStartAndWaitRsync(retryTimes);
    }

    /**
     * 所有上次rsync命令是否都成功返回。
     * @return
     */
    private boolean isAllRsyncExitNormally() {
        for (PageRsyncCmd cmd : rsyncCmds)
            if (cmd.getExitValue() != 0) return false;

        return true;
    }

    private int startExecuteAllRsyncCmds() {
        int rsyncCmdNum = 0;
        for(PageRsyncCmd cmd: rsyncCmds)
            if (cmd.execute()) ++rsyncCmdNum;

        return rsyncCmdNum;
    }

    private int retryStartExecuteAllRsyncCmds() {
        int rsyncCmdNum = 0;
        for (PageRsyncCmd cmd : rsyncCmds) {
            if (cmd.getExitValue() == 0) continue;

            cmd.execute();
            ++rsyncCmdNum;
        }

        return rsyncCmdNum;
    }

    private void deleteLocalDirsAsRequired() {
        if (!deleteLocalDirAfterRsync) return;
        if (!isAllRsyncExitNormally()) {
            log.warn("it's not performed to delete local dirs after rsync" +
                    "because that not all rsyncs exited normally.");
            return;
        }

        // log.info("delete src path begin");
        for(String dir: localDirs) {
            log.info("deleting {}", dir);
            PageStaticUtils.deleteDirRecursively(new File(dir));
        }

        // log.info("delete src path finish");
    }

    public void setRsyncTimeoutSeconds(int rsyncTimeoutSeconds) {
        rsyncTimeoutMilis = rsyncTimeoutSeconds * 1000;
    }

    public void setRsyncRemotes(List<RsyncRemote> rsyncRemotes) {
        this.rsyncRemotes = rsyncRemotes;
    }

    public void setRsyncDirs(List<RsyncDir> rsyncDirs) {
        this.rsyncDirs = rsyncDirs;
    }

    public void setDeleteLocalDirAfterRsync(boolean deleteLocalDirAfterRsync) {
        this.deleteLocalDirAfterRsync = deleteLocalDirAfterRsync;
    }

    public void setRsyncCompleteListener(RsyncCompleteListener rsyncCompleteListener) {
        this.rsyncCompleteListener = rsyncCompleteListener;
    }

    public void rsyncFailListenerCall(PageRsyncCmd pageRsyncCmd,
            String stdout, String stderr) {
        if (rsyncCompleteListener == null) return;

        RsyncRunInfo rsyncRunInfo = new RsyncRunInfo();
        pageRsyncCmd.initRsynRunInfoByRsyncCmd(rsyncRunInfo);
        rsyncRunInfo.setStdout(stdout);
        rsyncRunInfo.setStderr(stderr);
        rsyncRunInfo.setRsyncTimeoutMilis(rsyncTimeoutMilis);

        rsyncCompleteListener.onComplete(rsyncRunInfo);
    }

    public String getRsyncOptions() {
        return rsyncOptions;
    }

    public void setRsyncOptions(String rsyncOptions) {
        this.rsyncOptions = rsyncOptions;
    }

    public void setRsyncRetryTimes(int rsyncRetryTimes) {
        this.rsyncRetryTimes = rsyncRetryTimes;
    }
}
