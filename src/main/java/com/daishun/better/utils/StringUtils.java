package com.daishun.better.utils;

/**
 * @author daishun
 * @since 2019/8/9
 */
public final class StringUtils {

    public static boolean isEmpty(String str){
        return str == null || "".equalsIgnoreCase(str);
    }
}
