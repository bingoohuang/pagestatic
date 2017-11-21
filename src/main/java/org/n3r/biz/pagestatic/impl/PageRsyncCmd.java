package org.n3r.biz.pagestatic.impl;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.biz.pagestatic.bean.RsyncDir;
import org.n3r.biz.pagestatic.bean.RsyncRemote;
import org.n3r.biz.pagestatic.bean.RsyncRunInfo;
import org.n3r.biz.pagestatic.util.PageStaticUtils;
import org.n3r.biz.pagestatic.util.StreamGobbler;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * 单条rsync命令类。
 *
 * @author Bingoo
 */
public class PageRsyncCmd {
    private Logger log;
    private RsyncRemote conf;
    private RsyncDir dir;
    private ProcessBuilder processBuilder;
    private String commandLine;
    private Process process;
    private long startMillis;
    private boolean aliveFlag;
    private int exitValue;

    private StreamGobbler stdoutStreamGobbler;
    private StreamGobbler stderrStreamGobbler;
    private PageRsync pageRsync;
    private long costMillis;

    public PageRsyncCmd(Logger log, PageRsync pageRsync, RsyncRemote conf, RsyncDir dir) {
        this.log = log;
        this.pageRsync = pageRsync;
        this.conf = conf;
        this.dir = dir;

        val cmd = new String[]{"rsync", pageRsync.getRsyncOptions(), dir.getLocalDir(), createRemotePath()};
        commandLine = StringUtils.join(cmd, ' ');
        processBuilder = new ProcessBuilder(cmd);
    }

    private String createRemotePath() {
        return new StringBuilder()
                .append(conf.getRemoteUser()).append('@')
                .append(conf.getRemoteHost()).append(':')
                .append(dir.getRemoteDir())
                .toString();
    }

    public boolean execute() {
        exitValue = 0;
        costMillis = 0;
        aliveFlag = false;
        process = null;

        if (!new File(dir.getLocalDir()).exists()) return false;

        try {
            log.debug("start command line {}", commandLine);
            process = processBuilder.start();
            startStreamGobbler();

            startMillis = System.currentTimeMillis();
            aliveFlag = true;
        } catch (IOException e) {
            log.error("start up rsync exception", e);
        }

        return true;
    }

    private void startStreamGobbler() {
        stdoutStreamGobbler = new StreamGobbler(log, commandLine,
                process.getInputStream(), StreamGobbler.TYPE.STDOUT);
        stderrStreamGobbler = new StreamGobbler(log, commandLine,
                process.getErrorStream(), StreamGobbler.TYPE.STDERR);

        stdoutStreamGobbler.start();
        stderrStreamGobbler.start();
    }

    /**
     * Destroy process if expired.
     *
     * @param rysncTimeoutMilis
     * @return whether process was tagged as terminated just now.
     */
    public boolean destroyWhenExpired(long millisOfCheckRsyncExited, long rysncTimeoutMilis) {
        if (!aliveFlag) return false;

        // log.info("check {} was terminated or not after {} second(s)", commandLine, millisOfCheckRsyncExited / 1000.);
        if (PageStaticUtils.isAlive(process)) {
            val cost = System.currentTimeMillis() - startMillis;
            if (cost > rysncTimeoutMilis) {
                costMillis = cost;
                log.warn("{} exipred in {}s , kill it.", commandLine, cost / 1000.);
                process.destroy();
            }

            return false;
        }

        costMillis = System.currentTimeMillis() - startMillis;

        aliveFlag = false;
        exitValue = process.exitValue();
        log.info("{}: {} exited with value {}, cost {} seconds",
                new Object[]{getLoginUser(), commandLine, exitValue,
                        (System.currentTimeMillis() - startMillis) / 1000.});

        pageRsync.rsyncFailListenerCall(this, stdoutStreamGobbler.getOutput(), stderrStreamGobbler.getOutput());

        return true;
    }

    private String getLoginUser() {
        return System.getProperty("user.name");
    }

    public int getExitValue() {
        return exitValue;
    }

    public void initRsynRunInfoByRsyncCmd(RsyncRunInfo rsyncRunInfo) {
        rsyncRunInfo.setConf(conf);
        rsyncRunInfo.setDir(dir);
        rsyncRunInfo.setCommandLine(commandLine);
        rsyncRunInfo.setExitValue(exitValue);
        rsyncRunInfo.setCostMillis(costMillis);
    }
}
