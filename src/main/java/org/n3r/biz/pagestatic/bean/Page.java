package org.n3r.biz.pagestatic.bean;

import lombok.Value;

import java.io.File;

/**
 * 一个需要静态化并上传的页面.
 *
 * @author Bingoo
 */
@Value
public class Page {
    // 页面所在的URL
    private final String url;
    // 包含Client访问页面URL返回的响应体临时文件名称
    private final File tempFile;
    // 响应体所需要存储的本地文件名称
    private final File localFile;

    public String getUrl() {
        return url;
    }

    public File getTempFile() {
        return tempFile;
    }

    public File getLocalFile() {
        return localFile;
    }

}
