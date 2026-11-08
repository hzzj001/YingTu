package com.yingtu.plus.player;

import com.github.catvod.crawler.diagnostics.DiagnosticEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放诊断收集器（exo / ijk / mpv 各引擎一个）：把播放过程中的诊断事件
 * （解码器、音频输出、缓冲等）以 {@link DiagnosticEvent} 形式记下来，
 * 供诊断桥接与问题排查使用。兼容实现只保留调用点用到的接口，
 * 所有 emit / error / evidence / baseline 调用均为无操作（不写日志、不上报）。
 */
public final class PlaybackDiagnosticCollector {

    /** 一次诊断上下文（一次播放 / 一个诊断会话）。 */
    public static final class Context {
        private final String mediaId;
        private final String trace;
        private final long attempt;
        private final long generation;

        public Context(String mediaId) {
            this.mediaId = mediaId == null ? "" : mediaId;
            // 兼容实现：begin(traceId, scope) 把 traceId 当 mediaId 传进来，故 trace 即 mediaId；
            // attempt / generation 暂无真实会话计数，均为 0。
            this.trace = this.mediaId;
            this.attempt = 0;
            this.generation = 0;
        }

        public String mediaId() {
            return mediaId;
        }

        /** 诊断 trace id（上游 DiagnosticControls.port 用到）。 */
        public String trace() {
            return trace;
        }

        /** 本次播放的尝试序号（兼容实现：0）。 */
        public long attempt() {
            return attempt;
        }

        /** 会话代数（兼容实现：0）。 */
        public long generation() {
            return generation;
        }
    }

    private final String name;
    private final String version;
    private final String instanceId;

    public PlaybackDiagnosticCollector(String name, String version) {
        this.name = name == null ? "" : name;
        this.version = version == null ? "" : version;
        this.instanceId = UUID.randomUUID().toString();
    }

    /** 诊断收集总开关（兼容实现：关闭）。 */
    public static boolean enabled() {
        return false;
    }

    /** 生成一个带前缀的诊断对象 ID。 */
    public static String id(String kind) {
        return (kind == null ? "unknown" : kind) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** 按对象 + 种类生成稳定可读的诊断对象 ID。 */
    public static String objectId(Object object, String kind) {
        if (object == null) return id(kind);
        return (kind == null ? "unknown" : kind) + "@" + Integer.toHexString(System.identityHashCode(object));
    }

    public Context context() {
        return new Context("");
    }

    public String instanceId() {
        return instanceId;
    }

    /** 发一条诊断事件（兼容实现：不做任何事）。 */
    public void emit(String category, String message, Consumer<DiagnosticEvent> fill) {
    }

    /** 发一条带上下文的诊断事件（兼容实现：不做任何事）。 */
    public void emit(Context context, String category, String name, String message, Consumer<DiagnosticEvent> fill) {
    }

    /** 记一条错误诊断事件（兼容实现：不做任何事）。 */
    public void error(Context context, String category, String message, Throwable error) {
    }

    /** 开始一个诊断上下文。 */
    public Context begin(String traceId, String scope) {
        return new Context(traceId);
    }

    /** 结束一个诊断范围（兼容实现：不做任何事）。 */
    public void end(String scope) {
    }

    /** 记一条证据（兼容实现：不做任何事）。 */
    public void evidence(Context context, boolean video, int code) {
    }

    /** 记一条基线（兼容实现：不做任何事）。 */
    public void baseline() {
    }

    /** 当前媒体是否受保护（DRM 等）；兼容实现返回 false。 */
    public boolean protectedMedia() {
        return false;
    }

    /** 本次收集是否已结束（兼容实现：永不结束；供上游 DiagnosticControls.port 用到）。 */
    public boolean ended() {
        return false;
    }

    /** 当前播放引擎名（兼容实现：未追踪，返回空串；供上游 DiagnosticControls.port 用到）。 */
    public String engine() {
        return "";
    }
}
