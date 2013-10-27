package org.n3r.biz.pagestatic.bean;

import java.util.UUID;

public class PageStat {
    private String batchId = UUID.randomUUID().toString();
    // 一个批次内所有页面上传并静态化开始时间点
    private long startupMillis = System.currentTimeMillis();
    private long endupMillis;
    // 一个批次内生成文件的数量
    private int totalFileCounting;

    public long getStartupMillis() {
        return startupMillis;
    }

    public int getTotalFileCounting() {
        return totalFileCounting;
    }

    public void setTotalFileCounting(int totalFileCounting) {
        this.totalFileCounting = totalFileCounting;
    }

    public long getEndupMillis() {
        return endupMillis;
    }

    public void setEndupMillis(long endupMillis) {
        this.endupMillis = endupMillis;
    }

    public long getCostMillis() {
        return endupMillis - startupMillis;
    }

    public String getBatchId() {
        return batchId;
    }
}
