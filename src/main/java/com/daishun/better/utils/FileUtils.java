package com.daishun.better.utils;

import com.daishun.better.exception.BetterException;
import lombok.SneakyThrows;

import java.io.File;

/**
 * @author daishun
 * @since 2019/8/2
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

    public static String getUserDir() {
        return System.getProperty("user.dir");
    }

    @SneakyThrows
    public static void move(File srcFile, File destFile) {
        if (srcFile.isFile()) {
            moveFile(srcFile, destFile);
        } else {
            moveDirectoryToDirectory(srcFile, destFile.getParentFile(), false);
        }
    }


    public static String getRelativePath(String localPath, String absolutePath) {
        String relativePath = absolutePath.replace(localPath, "");
        if (relativePath.startsWith(File.separator)) {
            return relativePath.substring(1, relativePath.length());
        } else {
            return relativePath;
        }
    }

    public static void requireEmptyDir(File dir) {
        if (!dir.exists()) {
            throw new BetterException("%s not exist", dir.getName());
        } else {
            File[] files = dir.listFiles();
            if (files != null && files.length != 0) {
                throw new BetterException("require a empty directory,but %s is not empty!", dir.getName());
            }
        }
    }
}
