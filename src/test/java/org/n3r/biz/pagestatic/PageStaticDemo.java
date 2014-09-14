package org.n3r.biz.pagestatic;

import org.apache.commons.lang3.StringUtils;
import org.n3r.biz.pagestatic.base.RsyncCompleteListener;
import org.n3r.biz.pagestatic.util.PageStaticUtils;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * 在pom中放开maven-jar-plugin和maven-assembly-plugin两个plugin的注释
 * maven打包：mvn package -Dmaven.test.skip=true
 * <p/>
 * 需要连续运行24小时，看http连接释放情况，rsync调用情况
 * http连接情况  /usr/sbin/lsof -p 16786 |grep TCP|wc -l
 *
 * @author Bingoo
 */
public class PageStaticDemo {

    public static void main(String[] args) throws IOException {
        RsyncCompleteListener rsyncCompleteListener = new RsyncCompleteListenerDemo();
        // 配置上传相关参数
        /*PageStatic pageStatic =*/
        new PageStaticBuilder()
                // 必须有一个addRsyncRemote和一个addRsyncDir
                .addRsyncRemote("10.142.151.86", "mall")
                .addRsyncRemote("10.142.151.87", "mall")
                .addRsyncDir("/home/mall/pagestatic/pagehtml/", "10.142.151.86:/home/mall/pagestatic/")
                .addRsyncDir("/home/mall/pagestatic/pagehtml/", "10.142.151.87:/app/mallci/pagestatic/")

                        // 以下是可选参数
                .httpSocketTimeoutSeconds(60) // 不设置，默认30秒
                .triggerUploadWhenMaxFiles(100) // 不设置，默认100
                .triggerUploadWhenMaxSeconds(60) // 不设置，默认120
                .deleteLocalDirAfterRsync(true) // 不设置，默认true
                .maxUrlContentGeneratingThreads(10) // 不设置，默认1
                .rsyncTimeoutSeconds(60) // 不设置，默认30秒
                .rsyncCompleteListener(rsyncCompleteListener) // rsync监听器，可以在此监听器中探测是否失败，并在失败时实现短信发送等告警功能
                .httpClientCompleteListener(null) // http抓取页面完成时监听器
                .build();

        // 参数配置请参见pagestatic.ini配置说明文件。
        String pageStaticSpec = PageStaticUtils.classResourceToString("pagestatic.conf");
        PageStatic pageStatic = new PageStaticBuilder().fromSpec(pageStaticSpec).build();

        SecureRandom random = new SecureRandom();
        File file = new File("stop");

        while (true) {
            if (file.exists()) {
                file.deleteOnExit();
                break;
            }

            staticAndUpload(pageStatic);

            int seconds = random.nextInt(30) + 10;
            System.out.println("Sleeping " + seconds + " seconds");
            PageStaticUtils.sleepSeconds(seconds);
        }

        System.out.println("main thread exited!");
    }

    private static void staticAndUpload(PageStatic pageStatic) {
        // 开始上传
        pageStatic.startupBatch();

        HttpReqHeader httpReqHeader = new HttpReqHeader("X-Requested-With", "XMLHttpRequest");

        // 批量上传
        for (int i = 0, ii = urls.length; i < ii; ++i) {
            String url = urls[i];
            String fileName = StringUtils.substringAfterLast(url, "/");
            String localFile = "/home/mall/pagestatic/pagehtml/p" + i % 2 + "/" + fileName;

            // 静态化指定url，以及对应本地文件名称
            pageStatic.urlStaticAndUpload(url, localFile, httpReqHeader);
            // 直接给定静态化内容进行上传
            String content = "<html>我是静态内容</html>";
            localFile = "/home/mall/pagestatic/pagehtml/p" + i % 2 + "/direct" + fileName;
            pageStatic.directContentUpload(content, localFile);
        }

        // 等待上传完成
        pageStatic.finishBatch();
    }

    private static String[] urls = {
            "http://10.142.151.86:8105/goodsdetail/341211150601.html"
    };
}
