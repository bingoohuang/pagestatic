package org.n3r.biz.pagestatic.bean;

import lombok.Data;

@Data
public class RsyncRunInfo {
    private RsyncRemote conf;
    private RsyncDir dir;
    private String commandLine;
    private int exitValue;
    private String stdout;
    private String stderr;
    private long costMillis;
    private long rsyncTimeoutMilis;
}
