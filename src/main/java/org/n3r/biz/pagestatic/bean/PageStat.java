package org.n3r.biz.pagestatic.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class PageStat {
    @Getter private String batchId = UUID.randomUUID().toString();
    // 一个批次内所有页面上传并静态化开始时间点
    @Getter private long startupMillis = System.currentTimeMillis();
    @Getter @Setter private long endupMillis;
    // 一个批次内生成文件的数量
    @Getter @Setter private int totalFileCounting;

    public long getCostMillis() {
        return endupMillis - startupMillis;
    }
}
