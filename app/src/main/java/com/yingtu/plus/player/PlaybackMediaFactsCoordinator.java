package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.DisplayMode;
import com.yingtu.plus.player.PlaybackAutoContext.RenderTarget;
import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;
import com.yingtu.plus.player.engine.PlaybackFactsSnapshot;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 媒体事实协调器：PlayerManager 把渲染目标、显示模式、引擎事实快照
 * 汇总到这里，写进播放上下文供各策略消费。兼容实现只保留调用点用到的接口，
 * 不保存、不汇总任何事实。
 */
public final class PlaybackMediaFactsCoordinator {

    private final PlaybackAutoContextStore store;

    public PlaybackMediaFactsCoordinator(PlaybackAutoContextStore store) {
        this.store = store;
    }

    public void publishRenderTarget(SessionToken session, RenderTarget renderTarget, long nowElapsedMs) {
    }

    public void publishDisplayFacts(SessionToken session, DisplayMode currentMode, DisplayMode requestedMode, long nowElapsedMs) {
    }

    public void beginSession(SessionToken session) {
    }

    public void publishEngineFacts(SessionToken session, long trackSequence, PlaybackFactsSnapshot snapshot,
                                   boolean acceptDecoder, long nowElapsedMs) {
    }

    public void endSession(SessionToken session) {
    }
}
