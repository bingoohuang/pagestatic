package org.n3r.biz.pagestatic.bean;

public class RsyncRunInfo {
    private RsyncRemote conf;
    private RsyncDir dir;
    private String commandLine;
    private int exitValue;
    private String stdout;
    private String stderr;
    private long costMillis;
    private long rsyncTimeoutMilis;

    public RsyncRemote getConf() {
        return conf;
    }

    public void setConf(RsyncRemote conf) {
        this.conf = conf;
    }

    public RsyncDir getDir() {
        return dir;
    }

    public void setDir(RsyncDir dir) {
        this.dir = dir;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public int getExitValue() {
        return exitValue;
    }

    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public long getCostMillis() {
        return costMillis;
    }

    public void setCostMillis(long costMillis) {
        this.costMillis = costMillis;
    }

    public long getRsyncTimeoutMilis() {
        return rsyncTimeoutMilis;
    }

    public void setRsyncTimeoutMilis(long rsyncTimeoutMilis) {
        this.rsyncTimeoutMilis = rsyncTimeoutMilis;
    }

}
