package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;
import com.yingtu.plus.player.PlaybackTelemetry.DecisionEvent;
import com.yingtu.plus.player.PlaybackTelemetry.RuntimeObservation;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放遥测的发布端（单例）：PlayerManager / 各策略把决策事件与周期性运行采样发到这里。
 * 兼容实现只接收不处理（不写日志、不上报、不落盘），各方法均为无操作。
 */
public final class PlaybackTelemetryCoordinator {

    private static final PlaybackTelemetryCoordinator PROCESS = new PlaybackTelemetryCoordinator();

    private PlaybackTelemetryCoordinator() {
    }

    public static PlaybackTelemetryCoordinator process() {
        return PROCESS;
    }

    public void publishDecision(SessionToken session, DecisionEvent event) {
    }

    public void publishDecision(String traceId, DecisionEvent event, long nowElapsedMs) {
    }

    public void beginSession(SessionToken session, long nowElapsedMs) {
    }

    public void publishRuntime(SessionToken session, RuntimeObservation observation, long nowElapsedMs) {
    }

    public void endSession(SessionToken session, String reason, RuntimeObservation observation, long nowElapsedMs) {
    }
}
