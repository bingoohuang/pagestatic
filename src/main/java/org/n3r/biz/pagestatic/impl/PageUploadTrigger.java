package org.n3r.biz.pagestatic.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * 页面上传触发条件类
 *
 * @author Bingoo
 */
public class PageUploadTrigger {
    // 达到多少最大文件生成数量，触发上传
    @Setter private int uploadTriggerMaxFiles;
    // 子上次上传达到多少最大秒数，触发上传
    @Setter private int uploadTriggerMaxSeconds;

    // 一个批次内所有页面上传并静态化开始时间点
    private long startupMillis = System.currentTimeMillis();
    // 一个批次内生成文件的数量
    @Getter private int totalFileCounting;

    // 一次上传开始时间点
    private long startMillis;
    // 一次上传累计文件计数
    @Getter private int fileCounting;

    public void reset() {
        fileCounting = 0;
    }

    public void incrFileCounting(int incr) {
        if (fileCounting == 0) startMillis = System.currentTimeMillis();

        fileCounting += incr;
        totalFileCounting += incr;
    }

    public boolean reachTrigger() {
        if (fileCounting == 0) return false;

        return System.currentTimeMillis() - startMillis > uploadTriggerMaxSeconds
                || fileCounting > uploadTriggerMaxFiles;
    }

    public double getTotalCostSeconds() {
        return (System.currentTimeMillis() - startupMillis) / 1000.;
    }
}
