package com.daishun.better;

import com.daishun.better.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author daishun
 * @since 2019/8/3
 */
public class BetterTest {

    @org.junit.Test
    public void main() throws IOException {
        FileUtils.cleanDirectory(new File("/Users/daishun/workspace/tengsaw/mybatis-plugins-example"));
        String args = "-G org.apache -A test-s -T spring-mybatis-simple-example";
        System.setProperty("user.dir", "/Users/daishun/workspace/tengsaw/mybatis-plugins-example");
        Better.main(args.split("\\ "));
    }
}