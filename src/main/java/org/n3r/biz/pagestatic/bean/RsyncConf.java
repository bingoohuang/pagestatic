package org.n3r.biz.pagestatic.bean;

import lombok.Value;

@Value
public class RsyncConf {
    private final String destIp;
    private final String destUser;
    private final String destPass;
}
