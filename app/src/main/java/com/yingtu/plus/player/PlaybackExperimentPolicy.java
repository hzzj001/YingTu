package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.Kernel;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放实验策略：哪些实验动作（网络限速、解码重建、预加载等）允许执行，
 * 以及当前实验策略快照 {@link State}。兼容实现默认全部允许
 * （allows 恒为 true），不做真实的实验分组。
 */
public final class PlaybackExperimentPolicy {

    public enum Action {
        EXO_AUTO_PRELOAD, EXO_DECODER_RUNTIME_REBUILD, EXO_FRAME_SCHEDULING_AB, EXO_NETWORK_SPEED,
        EXO_RTSP_RECOVERY, EXO_VIDEO_CONSTRAINT, IJK_BUFFER_RELOAD, IJK_BUFFER_SAFETY_RELOAD,
        IJK_DECODE_REBUILD, IJK_REALTIME_REBUILD, IJK_RUNTIME_KERNEL_FALLBACK, MPV_AUTO_PRELOAD,
        MPV_CACHE_EXPANSION, MPV_HLS_RUNTIME_RELOAD, SHARED_PROFILE_AB_VALIDATION
    }

    /** 当前实验策略快照。 */
    public static final class State {
        private final boolean enabled;
        private final String strategyId;

        public State(boolean enabled, String strategyId) {
            this.enabled = enabled;
            this.strategyId = strategyId == null ? "default" : strategyId;
        }

        public String strategyId() {
            return strategyId;
        }

        /** 该实验动作是否允许（兼容实现：策略启用时全部允许）。 */
        public boolean allows(Action action) {
            return enabled;
        }

        public boolean enabled() {
            return enabled;
        }
    }

    private PlaybackExperimentPolicy() {
    }

    public static State defaultState(boolean enabled) {
        return new State(enabled, "default");
    }
}
