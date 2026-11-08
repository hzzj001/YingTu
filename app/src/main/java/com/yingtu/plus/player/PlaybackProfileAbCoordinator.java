package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;
import com.yingtu.plus.player.PlaybackTelemetry.RuntimeObservation;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放画像 A/B 协调器（单例）：一次播放会话的画像评估（入组 / 臂 / 运行时观测 /
 * 结束）走这里；轻量评估（PlaybackLightweightAssessmentSetting.coordinator()）
 * 共用同一类型。兼容实现只保留会话边界接口，不做任何评估与上报。
 */
public final class PlaybackProfileAbCoordinator {

    /** 会话开始配置（是否入组、臂、设备摘要、实验世代、是否正常倍速）。 */
    public static final class StartConfig {
        public final boolean enrolled;
        public final PlaybackProfileAbPolicy.Arm arm;
        public final String deviceDigest;
        public final long generation;
        public final boolean normalSpeed;

        public StartConfig(boolean enrolled, PlaybackProfileAbPolicy.Arm arm, String deviceDigest,
                           long generation, boolean normalSpeed) {
            this.enrolled = enrolled;
            this.arm = arm;
            this.deviceDigest = deviceDigest;
            this.generation = generation;
            this.normalSpeed = normalSpeed;
        }
    }

    /** 运行时观测输入（是否入组、臂、实验世代、播放上下文快照、遥测采样等）。 */
    public static final class RuntimeInput {
        public final boolean enrolled;
        public final PlaybackProfileAbPolicy.Arm arm;
        public final long generation;
        public final PlaybackAutoContext context;
        public final RuntimeObservation observation;
        public final boolean playbackIntended;
        public final boolean frameSchedulingExperimentActive;
        public final boolean flag;

        public RuntimeInput(boolean enrolled, PlaybackProfileAbPolicy.Arm arm, long generation,
                            PlaybackAutoContext context, RuntimeObservation observation,
                            boolean playbackIntended, boolean frameSchedulingExperimentActive, boolean flag) {
            this.enrolled = enrolled;
            this.arm = arm;
            this.generation = generation;
            this.context = context;
            this.observation = observation;
            this.playbackIntended = playbackIntended;
            this.frameSchedulingExperimentActive = frameSchedulingExperimentActive;
            this.flag = flag;
        }
    }

    /** 会话结束配置（是否入组、臂、实验世代、结束原因）。 */
    public static final class EndConfig {
        public final boolean enrolled;
        public final PlaybackProfileAbPolicy.Arm arm;
        public final long generation;
        public final String reason;

        public EndConfig(boolean enrolled, PlaybackProfileAbPolicy.Arm arm, long generation, String reason) {
            this.enrolled = enrolled;
            this.arm = arm;
            this.generation = generation;
            this.reason = reason;
        }
    }

    public enum InvalidationReason implements PlaybackAutoContext.Labeled {
        GENERATION_CHANGED, UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    private static final PlaybackProfileAbCoordinator PROCESS = new PlaybackProfileAbCoordinator();

    private PlaybackProfileAbCoordinator() {
    }

    public static PlaybackProfileAbCoordinator process() {
        return PROCESS;
    }

    public void beginSession(SessionToken session, StartConfig config, long nowElapsedMs) {
    }

    public void observe(SessionToken session, RuntimeInput input, long nowElapsedMs) {
    }

    public void endSession(SessionToken session, EndConfig config, long nowElapsedMs, long nowWallMs) {
    }

    public void invalidate(SessionToken session, InvalidationReason reason) {
    }
}
