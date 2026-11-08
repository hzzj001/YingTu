package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.NetworkSnapshot;
import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 系统条件监控（单例）：跟随一次播放会话的开始 / 结束做系统条件采样，
 * 并向预加载暂停策略提供当前网络快照。兼容实现只保留会话边界与快照接口，
 * 始终返回“未知”网络快照，不做任何真实采样。
 */
public final class PlaybackSystemConditionMonitor {

    private static final PlaybackSystemConditionMonitor PROCESS = new PlaybackSystemConditionMonitor();

    private PlaybackSystemConditionMonitor() {
    }

    public static PlaybackSystemConditionMonitor process() {
        return PROCESS;
    }

    public void beginSession(SessionToken session) {
    }

    public void endSession(SessionToken session) {
    }

    public NetworkSnapshot currentNetworkSnapshot() {
        return NetworkSnapshot.unknown();
    }

    /** 当前网络身份摘要（用于按网络查吞吐历史）；兼容实现返回空串。 */
    public String currentNetworkIdentityDigest() {
        return "";
    }
}
