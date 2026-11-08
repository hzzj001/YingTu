package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.DecodeMode;
import com.yingtu.plus.player.PlaybackAutoContext.Fact;
import com.yingtu.plus.player.PlaybackAutoContext.Kernel;
import com.yingtu.plus.player.PlaybackAutoContext.PathFacts;
import com.yingtu.plus.player.PlaybackAutoContext.ResourceFacts;
import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放事实的集中存放处（单例）：一次播放会话开始时 {@link #beginSession}，播放过程中
 * PlayerManager 把内核 / 解码方式 / 资源 / 路径事实 {@link #publishPlaybackFacts} 进来，
 * 各策略通过 {@link #snapshot()} 只读消费；会话结束时 {@link #clear}。
 * 兼容实现只保留最近一次发布的事实快照，不做历史记录与跨会话合并。
 */
public final class PlaybackAutoContextStore {

    private static final PlaybackAutoContextStore PROCESS = new PlaybackAutoContextStore();
    private static final AtomicLong SESSION_IDS = new AtomicLong(1);

    private volatile PlaybackAutoContext current = PlaybackAutoContext.empty();

    private PlaybackAutoContextStore() {
    }

    public static PlaybackAutoContextStore process() {
        return PROCESS;
    }

    public PlaybackAutoContext snapshot() {
        return current;
    }

    /** 开始一次播放会话（traceId 仅作诊断标识）。 */
    public SessionToken beginSession(String traceId, long nowElapsedMs) {
        SessionToken token = SessionToken.of(SESSION_IDS.getAndIncrement());
        current = PlaybackAutoContext.of(token, ResourceFacts.unknown(), PathFacts.unknown());
        return token;
    }

    /** 发布一次播放事实；会话令牌无效时返回 false。 */
    public boolean publishPlaybackFacts(SessionToken session, Fact<Kernel> kernel, Fact<DecodeMode> decodeMode,
                                        ResourceFacts resource, PathFacts path, long nowElapsedMs) {
        if (session == null || !session.active()) return false;
        current = PlaybackAutoContext.of(session, resource, path);
        return true;
    }

    /** 清除指定会话的事实（与当前会话一致时恢复为空快照）。 */
    public void clear(SessionToken session) {
        if (session != null && session.equals(current.session())) {
            current = PlaybackAutoContext.empty();
        }
    }
}
