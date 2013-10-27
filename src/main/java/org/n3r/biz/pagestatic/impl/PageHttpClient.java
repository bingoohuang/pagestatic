package org.n3r.biz.pagestatic.impl;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.n3r.biz.pagestatic.base.HttpClientCompleteListener;
import org.n3r.biz.pagestatic.base.HttpClientSyncCompleteListener;
import org.n3r.biz.pagestatic.util.PageStaticUtils;
import org.n3r.core.lang.SameThreadExecutorService;
import org.slf4j.Logger;

/**
 * 根据页面URL，抓取页面内容。
 * @author Bingoo
 *
 */
public class PageHttpClient {
    private Logger log;

    // 多线程HTTP连接管理器
    private MultiThreadedHttpConnectionManager connectionManager;
    private HttpClient httpClient;
    private ThreadLocal<File> contentTL = new ThreadLocal<File>();
    private int httpSocketTimeoutSeconds;
    private HttpClientCompleteListener httpClientCompleteListener;
    private volatile ExecutorService syncExecutor = null;
    private File tempDir;

    private String proxyHost;
    private int proxyPort;

    private int maxGeneratingThreads;

    public PageHttpClient(Logger log) {
        this.log = log;
    }

    public void startup() {
        HttpClientParams params = new HttpClientParams();
        params.setParameter(HttpMethodParams.SO_TIMEOUT, httpSocketTimeoutSeconds * 1000);
        params.setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
        httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(maxGeneratingThreads); // 默认2
        httpConnectionManagerParams.setMaxTotalConnections(maxGeneratingThreads); // 默认20

        connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setParams(httpConnectionManagerParams);
        httpClient = new HttpClient(params, connectionManager);

        if (StringUtils.isNotBlank(proxyHost)) {
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
        }

        if (httpClientCompleteListener != null) {
            syncExecutor = httpClientCompleteListener instanceof HttpClientSyncCompleteListener
                    ? Executors.newSingleThreadExecutor()
                    : new SameThreadExecutorService();
        }
    }

    public void shutdown() {
        connectionManager.shutdown();
        if (syncExecutor != null) {
            syncExecutor.shutdown();
        }
    }

    // 本方法是多线程调用，一定要注意线程安全性。
    public boolean executeGetMethod(String url, Object[] callbackParams, String localFileName) {
        GetMethod getMethod = null;
        Exception ex = null;
        int statusCode = 0;
        double costsMillis = 0;

        try {
            getMethod = new GetMethod(url);
            long startMillis = System.currentTimeMillis();
            log.info("content get begin {} ", url);
            statusCode = httpClient.executeMethod(getMethod);
            costsMillis = (System.currentTimeMillis() - startMillis) / 1000.;
            if (statusCode != HttpStatus.SC_OK) {
                log.error("{} returned {}", url, statusCode);
            } else {
                log.info("content get successful {}, costs {} seconds", url, costsMillis);
            }

            File content = PageStaticUtils.createTmpFile(log, tempDir, url, localFileName,
                    getMethod.getResponseBodyAsStream());
            if (content == null) {
                return false;
            }
            contentTL.set(content);
        } catch (HttpException e) {
            ex = e;
            log.error("{} HttpException {}", url, e.getMessage());
        } catch (IOException e) {
            ex = e;
            log.error("{} IOException {}", url, e.getMessage());
        } catch (Exception e) {
            ex = e;
            log.error("{} exception {}", url, e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }

        return callListener(callbackParams, ex, statusCode, costsMillis);
    }

    private boolean callListener(final Object[] callbackParams, final Exception ex,
            final int statusCode, final double costsMillis) {
        Exception exp = ex;
        try {
            if (syncExecutor != null) {
                syncExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        httpClientCompleteListener.onComplete(
                                ex, statusCode, contentTL.get(), costsMillis, callbackParams);
                    }
                });
            }
        } catch (Exception e) {
            exp = e;
            log.error("call HttpClientCompleteListener failed", e);
        }

        return exp == null && statusCode == 200;
    }

    public File getContent() {
        return contentTL.get();
    }

    public void setHttpSocketTimeoutSeconds(int httpSocketTimeoutSeconds) {
        this.httpSocketTimeoutSeconds = httpSocketTimeoutSeconds;
    }

    public void setHttpClientCompleteListener(HttpClientCompleteListener httpClientCompleteListener) {
        this.httpClientCompleteListener = httpClientCompleteListener;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setMaxGeneratingThreads(int maxGeneratingThreads) {
        this.maxGeneratingThreads = maxGeneratingThreads;
    }

}
