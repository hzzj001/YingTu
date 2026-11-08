package com.yingtu.plus.utils;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放计时器（单例）：VideoActivity 在重播 / 切集时重置计时。
 * 兼容实现只保留调用点用到的接口，reset() 为无操作。
 */
public final class Timer {

    private static final Timer INSTANCE = new Timer();

    private Timer() {
    }

    public static Timer get() {
        return INSTANCE;
    }

    /** 重置计时（兼容实现：不做任何事）。 */
    public void reset() {
    }
}
