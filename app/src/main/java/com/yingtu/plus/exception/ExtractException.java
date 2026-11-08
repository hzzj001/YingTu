package com.yingtu.plus.exception;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 取流 / 解析失败异常（TVBus、Thunder 等 extractor 抛出，
 * SiteViewModel / LiveViewModel 捕获处理）。
 */
public class ExtractException extends Exception {

    public ExtractException(String message) {
        super(message);
    }

    public ExtractException(String message, Throwable cause) {
        super(message, cause);
    }
}
