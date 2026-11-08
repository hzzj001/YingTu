package com.yingtu.plus.playback;

import androidx.annotation.Nullable;

import com.yingtu.plus.bean.History;
import com.yingtu.plus.player.PlayerManager;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放事件收集器（单例）：VideoActivity 在播放进度变化、历史记录更新、
 * 播放停止时把事件发到这里，供播放历史 / 最近播放等能力消费。
 * 兼容实现只保留调用点用到的接口，不做任何收集与上报。
 */
public final class PlaybackEventCollector {

    private static final PlaybackEventCollector INSTANCE = new PlaybackEventCollector();

    private PlaybackEventCollector() {
    }

    public static PlaybackEventCollector get() {
        return INSTANCE;
    }

    /** 播放进度变化（兼容实现：不做任何事）。 */
    public void onProgress(History history, PlayerManager player) {
    }

    /** 历史记录已更新（兼容实现：不做任何事）。 */
    public void updateHistory(History history) {
    }

    /** 播放停止（兼容实现：不做任何事）。 */
    public void onStop(PlayerManager player) {
    }

    // 第 21 轮：以下 4 个方法照抄上游签名（空实现，与现有兼容实现行为一致，不触发 webhook）。
    public void setPlayer(@Nullable PlayerManager player) {
    }

    public synchronized void onPlaybackStateChanged(@Nullable PlayerManager player, int state) {
    }

    public synchronized void onIsPlayingChanged(@Nullable PlayerManager player, boolean isPlaying) {
    }

    public void onHistoryDeleted(PlaybackProgressDeleteInput input, int cid) {
    }
}
