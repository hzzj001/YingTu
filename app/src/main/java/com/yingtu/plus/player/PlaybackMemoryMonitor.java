package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 内存侧会话监控（单例）：跟随一次播放会话的开始 / 结束做内存快照采集。
 * 兼容实现只保留会话边界接口，不做任何采集。
 */
public final class PlaybackMemoryMonitor {

    private static final PlaybackMemoryMonitor PROCESS = new PlaybackMemoryMonitor();

    private PlaybackMemoryMonitor() {
    }

    public static PlaybackMemoryMonitor process() {
        return PROCESS;
    }

    public void beginSession(SessionToken session) {
    }

    public void endSession(SessionToken session) {
    }
}
