package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放实验协调器（单例）：各策略用它做 A/B 实验的令牌管理——
 * {@link #capture} 在一次决策开始时取当前实验世代的令牌，
 * {@link #isCurrent} 判断令牌是否仍是当前世代（过期则放弃该决策），
 * {@link #addListener} 订阅实验策略变更。兼容实现只维护一个单调递增的世代计数，
 * 令牌永不过期（isCurrent 恒为 true），策略变更不主动推送。
 */
public final class PlaybackExperimentCoordinator {

    /** 实验策略变更的通知。 */
    public static final class Update {
        private final long generation;
        private final String change;

        public Update(long generation, String change) {
            this.generation = generation;
            this.change = change == null ? "" : change;
        }

        public long generation() {
            return generation;
        }

        public String change() {
            return change;
        }
    }

    /** 监听注册（调用 close() 取消订阅）。 */
    public static final class Registration {
        private boolean closed;

        public void close() {
            closed = true;
        }

        public boolean closed() {
            return closed;
        }
    }

    /** 一次实验决策的令牌。 */
    public static final class Token {
        private final long generation;

        private Token(long generation) {
            this.generation = generation;
        }

        public long generation() {
            return generation;
        }
    }

    private static final PlaybackExperimentCoordinator PROCESS = new PlaybackExperimentCoordinator();
    private final AtomicLong generation = new AtomicLong(1);

    private PlaybackExperimentCoordinator() {
    }

    public static PlaybackExperimentCoordinator process() {
        return PROCESS;
    }

    public Registration addListener(Consumer<Update> listener) {
        return new Registration();
    }

    /** 取当前世代的实验令牌。 */
    public Token capture(PlaybackExperimentPolicy.Action action) {
        return new Token(generation.get());
    }

    /** 令牌是否仍是当前世代（兼容实现：恒为 true）。 */
    public boolean isCurrent(Token token) {
        return token != null;
    }

    public long generation() {
        return generation.get();
    }
}
