package org.n3r.biz.pagestatic.bean;

import lombok.Value;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Rsync需要同步的目录配置项。
 *
 * @author Bingoo
 */
@Value
public class RsyncDir {
    // 本地目录
    private final String localDir;
    // 远程主机
    // 可能为null，表示对应到所有RsyncConf中定义的相同远程主机上的remoteDir
    private final String remoteHost;
    // 远程主机上的目录
    private final String remoteDir;

    public RsyncDir(String localDir, String remoteDir) {
        this.localDir = trim(localDir);

        if (contains(remoteDir, ":")) {
            this.remoteHost = trim(substringBefore(remoteDir, ":"));
            this.remoteDir = trim(substringAfter(remoteDir, ":"));
        } else {
            this.remoteHost = "";
            this.remoteDir = trim(remoteDir);
        }
    }
}
