package org.n3r.biz.pagestatic.base;

import java.io.File;

public interface HttpClientCompleteListener {

    /**
     * URL抓取后回调函数。
     * @param ex 异常
     * @param statusCode http返回状态码
     * @param content http抓取临时文件，如果处理中删除此文件，则不上传。
     * @param costsMillis http抓取话费时间
     * @param callbackParams 回调参数
     */
    void onComplete(Exception ex, int statusCode, File content,
            double costsMillis, Object[] callbackParams);

}
