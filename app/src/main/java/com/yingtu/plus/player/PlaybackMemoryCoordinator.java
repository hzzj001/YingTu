package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;
import com.yingtu.plus.player.exo.ExoMemoryPressurePolicy;

import java.util.function.Consumer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 内存压力协调器（单例）：内存压力策略变化时通知各订阅方
 * （PlayerManager 的 mpv 资源 / ijk 缓冲内存订阅、AutoTargetLoadControl 的内存订阅）。
 * 兼容实现只保留订阅接口，不主动推送任何更新。
 */
public final class PlaybackMemoryCoordinator {

    /** 内存压力更新的通知。 */
    public static final class Update {
        private final SessionToken session;
        private final ExoMemoryPressurePolicy.Decision decision;

        public Update(SessionToken session, ExoMemoryPressurePolicy.Decision decision) {
            this.session = session == null ? SessionToken.none() : session;
            this.decision = decision;
        }

        public SessionToken session() {
            return session;
        }

        public ExoMemoryPressurePolicy.Decision decision() {
            return decision;
        }

        public long generation() {
            return 0;
        }

        public String change() {
            return "";
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

    private static final PlaybackMemoryCoordinator PROCESS = new PlaybackMemoryCoordinator();

    private PlaybackMemoryCoordinator() {
    }

    public static PlaybackMemoryCoordinator process() {
        return PROCESS;
    }

    public Registration addListener(Consumer<Update> listener) {
        return new Registration();
    }
}
