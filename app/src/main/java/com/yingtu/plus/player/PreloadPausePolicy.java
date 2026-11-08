package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.NetworkSnapshot;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 预加载暂停策略：播放器未处于“可播”状态时，是否暂停后台预加载。
 * 兼容实现默认不暂停（Decision.allowed() 为 true），
 * 即预加载行为不受该策略影响。
 */
public final class PreloadPausePolicy {

    public enum Reason implements PlaybackAutoContext.Labeled {
        ALLOWED, UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    /** 一次暂停决策：是否允许继续预加载 + 原因。 */
    public static final class Decision {
        private final boolean allowed;
        private final Reason reason;

        public Decision(boolean allowed, Reason reason) {
            this.allowed = allowed;
            this.reason = reason == null ? Reason.UNKNOWN : reason;
        }

        public boolean allowed() {
            return allowed;
        }

        public Reason reason() {
            return reason;
        }
    }

    private PreloadPausePolicy() {
    }

    /**
     * @param playWhenReady 播放器是否处于可播状态
     * @param policy        预加载暂停策略档位（PreloadSetting.getPausePreloadPolicy() 的 int 值）
     * @param network       当前网络快照
     */
    public static Decision evaluate(boolean playWhenReady, int policy, NetworkSnapshot network) {
        return new Decision(true, Reason.ALLOWED);
    }
}
