package com.daishun.better.utils;

import lombok.SneakyThrows;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author daishun
 * @since 2019/8/2
 */
public class GitUtils {


    @SneakyThrows
    public static void cloneRepository(String localPath, String gitUrl) {
        CloneCommand cc = Git.cloneRepository()
                .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out)))
                .setURI(gitUrl).setCloneSubmodules(true);
        cc.setDirectory(new File(localPath)).call();

    }
}
