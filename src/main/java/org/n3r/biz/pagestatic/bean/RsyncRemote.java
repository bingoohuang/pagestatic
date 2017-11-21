package org.n3r.biz.pagestatic.bean;

import lombok.Value;

/**
 * 一个rsync同步的配置
 *
 * @author Bingoo
 */
@Value
public class RsyncRemote {
    // 远程主机名称或者IP
    private final String remoteHost;
    // 远程用户名
    private final String remoteUser;
}
