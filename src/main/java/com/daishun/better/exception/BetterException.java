package com.daishun.better.exception;

/**
 * @author daishun
 * @since 2019/8/3
 */
public class BetterException extends RuntimeException {

    public BetterException(String message) {
        super(message);
    }

    public BetterException(String message, Object... args) {
        super(String.format(message, args));
    }
}
