package org.n3r.biz.pagestatic.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.n3r.biz.pagestatic.bean.Page;
import org.slf4j.Logger;

public class PageStaticUtils {
    public static File createTmpFile(Logger log, File tempDir, String url, String localFileName, InputStream is) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            File content = new File(tempDir, UUID.randomUUID().toString());
            fos = new FileOutputStream(content);
            bos = new BufferedOutputStream(fos);
            IOUtils.copy(is, bos);
            String lowercaseFileName = localFileName.toLowerCase();
            if (lowercaseFileName.endsWith(".html") || lowercaseFileName.endsWith(".htm"))
                IOUtils.write(PageStaticUtils.createTimestamp(), bos);

            log.info("file {} was created from url {}", localFileName, url);
            return content;
        } catch(Exception ex) {
            log.error("create tmp file failed", ex);
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }

        return null;
    }

    public static int createFile(Logger log, Page page) {
        if (!page.getTempFile().exists()) return 0;

        File pageFile = page.getLocalFile();

        if (pageFile.equals(page.getTempFile())) return countFiles(pageFile);

        File parentPath = pageFile.getParentFile();
        if (parentPath !=null && !parentPath.exists() && !parentPath.mkdirs()) {
            log.error("mkdir fail {}", parentPath);
            return 0;
        }

        try {
            if (pageFile.exists()) pageFile.delete();
            FileUtils.moveFile(page.getTempFile(), pageFile);
        } catch (IOException ex) {
            log.error("write content of {} to file {} failed {}",
                    new Object[] { page.getUrl(), page.getLocalFile(), ex });
            return 0;
        }

        return 1;
    }

    public static boolean hasFiles(Collection<String> dirs) {
        for(String localDir: dirs)
            if(hasFiles(new File(localDir))) return true;

        return false;
    }

    public static boolean hasFiles(File path) {
        if (!path.exists()) return false;

        for (File f : path.listFiles())
            if (!f.isDirectory() || hasFiles(f)) return true;

        return false;
    }

    public static int  countFiles(File path) {
        if (path == null || !path.exists()) return 0;
        if (!path.isDirectory()) return 1;
        int fileCount = 0;

        for (File f : path.listFiles())
            fileCount += countFiles(f);

        return fileCount;
    }

    public static final String DATEFMT = "yyyy-MM-dd HH:mm:ss";

    public static String createTimestamp() {
        return new StringBuilder()
                .append("<!-- staticized at ")
                .append(new SimpleDateFormat(DATEFMT).format(new Date()))
                .append(" by PageStatic(V0.3.5) program -->")
                .toString();
    }

    public static boolean deleteDirRecursively(File path) {
        if (path.exists() && path.isDirectory())
            for (File file : path.listFiles())
                if (file.isDirectory())
                    deleteDirRecursively(file);
                else
                    file.delete();

        return path.delete();
    }

    public static boolean isAlive(Process p) {
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public static void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
    }

    public static void sleepMilis(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
        }
    }

    private static final String os = System.getProperty("os.name").toLowerCase();
    public static boolean isWindowsOS() {
        return os.indexOf("windows") != -1 || os.indexOf("nt") != -1;
    }

    /**
     * Return the context classloader. BL: if this is command line operation, the classloading issues are more sane.
     * During servlet execution, we explicitly set the ClassLoader.
     *
     * @return The context classloader.
     */
    public static ClassLoader getClassLoader() {
        return Objects.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                PageStaticUtils.class.getClassLoader());
    }

    public static String classResourceToString(String classPath) {
        URL url = getClassLoader().getResource(classPath);
        if (url == null) return null;

        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {

        }

        return null;
    }
}
