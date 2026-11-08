package com.yingtu.plus.player;

import android.text.TextUtils;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放链路追踪 ID（轻量诊断用）：PlayerManager / PlaySpec / 各播放引擎用它给一次播放过程打点。
 * 本实现只保留调用点用到的形态：NONE 为字符串常量、normalize 做空安全归一、log 为可变参空操作、
 * 实例方法维护一个内存中的当前 traceId 与 stage 集合（不落盘、不上报）。
 */
public class PlaybackTrace {

    /** 无追踪 ID 时的占位值（字符串常量，调用点直接把它赋给 String 字段）。 */
    public static final String NONE = "none";

    public enum Stage {
        AUDIO_PLAYABLE, FIRST_FRAME, PARSE_COMPLETE, PREPARE, READY, REQUEST, TRACKS
    }

    private String current = NONE;
    private final java.util.Set<Stage> stages = new java.util.HashSet<>();
    private final java.util.Map<Stage, Long> stageElapsed = new java.util.HashMap<>();

    public PlaybackTrace() {
    }

    public static String normalize(String traceId) {
        return TextUtils.isEmpty(traceId) ? NONE : traceId;
    }

    /** 诊断打点（兼容实现：不输出、不上报）。 */
    public static void log(String tag, Object... args) {
    }

    public String current() {
        return current;
    }

    public void mark(Stage stage, String detail) {
        if (stage == null) return;
        stages.add(stage);
        stageElapsed.put(stage, android.os.SystemClock.elapsedRealtime());
    }

    public boolean hasStage(Stage stage) {
        return stage != null && stages.contains(stage);
    }

    public long stageElapsedMs(Stage stage) {
        Long value = stage == null ? null : stageElapsed.get(stage);
        return value == null ? -1 : value;
    }

    public void clear() {
        current = NONE;
        stages.clear();
        stageElapsed.clear();
    }

    public PlaybackTrace ensure() {
        return this;
    }

    public PlaybackTrace begin() {
        return this;
    }
}
