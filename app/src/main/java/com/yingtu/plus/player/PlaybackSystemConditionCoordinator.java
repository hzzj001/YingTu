package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;
import com.yingtu.plus.player.PlaybackAutoContext.SystemConditionTrigger;

import java.util.function.Consumer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 系统条件协调器（单例）：网络 / 电源 / 散热等系统条件变化时通知各订阅方
 * （PlayerManager 的 mpv 资源订阅、ExoThroughputCoordinator 的吞吐订阅）。
 * 兼容实现只保留订阅接口，不主动推送任何更新。
 */
public final class PlaybackSystemConditionCoordinator {

    /** 系统条件更新的通知。 */
    public static final class Update {
        private final SessionToken session;
        private final SystemConditionTrigger trigger;
        private final long publishedAtElapsedMs;

        public Update(SessionToken session, SystemConditionTrigger trigger, long publishedAtElapsedMs) {
            this.session = session == null ? SessionToken.none() : session;
            this.trigger = trigger == null ? SystemConditionTrigger.UNKNOWN : trigger;
            this.publishedAtElapsedMs = publishedAtElapsedMs;
        }

        public SessionToken session() {
            return session;
        }

        public SystemConditionTrigger trigger() {
            return trigger;
        }

        public long publishedAtElapsedMs() {
            return publishedAtElapsedMs;
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

    private static final PlaybackSystemConditionCoordinator PROCESS = new PlaybackSystemConditionCoordinator();

    private PlaybackSystemConditionCoordinator() {
    }

    public static PlaybackSystemConditionCoordinator process() {
        return PROCESS;
    }

    public Registration addListener(Consumer<Update> listener) {
        return new Registration();
    }
}
