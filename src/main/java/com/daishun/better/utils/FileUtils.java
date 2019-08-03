package com.daishun.better.utils;

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
}
