package com.yingtu.plus.player;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 卡顿（rebuffer）追踪器：PlayerManager 在播放状态变化时喂给它当前状态，
 * 它汇总卡顿次数 / 总时长，并在卡顿开始 / 结束时产出 {@link Event}。
 * 兼容实现只做计数归零的空实现（不记录、不产出事件，update 一律返回 null），
 * 各策略据此认为“没有卡顿发生”。
 */
public final class PlaybackBufferingTracker {

    /** 一次卡顿事件。 */
    public static final class Event {
        public enum Type implements PlaybackAutoContext.Labeled {
            UNKNOWN;

            @Override
            public String label() {
                return name().toLowerCase(Locale.US);
            }
        }

        public enum Phase implements PlaybackAutoContext.Labeled {
            UNKNOWN;

            @Override
            public String label() {
                return name().toLowerCase(Locale.US);
            }
        }

        private final int playbackState;

        public Event(int playbackState) {
            this.playbackState = playbackState;
        }

        public Type type() {
            return Type.UNKNOWN;
        }

        public Phase phase() {
            return Phase.UNKNOWN;
        }

        public long durationMs() {
            return 0;
        }

        public long rebufferCount() {
            return 0;
        }

        public long rebufferTotalMs() {
            return 0;
        }

        public long positionMs() {
            return 0;
        }

        public long forwardBufferedMs() {
            return 0;
        }

        public int playbackState() {
            return playbackState;
        }

        public boolean loading() {
            return false;
        }
    }

    public PlaybackBufferingTracker() {
    }

    public void reset() {
    }

    public int getRebufferCount() {
        return 0;
    }

    public long getRebufferTotalMs(long nowElapsedMs) {
        return 0;
    }

    public boolean isBuffering() {
        return false;
    }

    /**
     * 喂入一次播放状态采样；兼容实现不记录、不产出事件，恒返回 null。
     */
    public Event update(boolean buffering, boolean startupComplete, long nowElapsedMs, int playbackState,
                        long positionMs, long forwardBufferedMs, boolean loading) {
        return null;
    }
}
