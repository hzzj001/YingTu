package com.yingtu.plus.player;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 系统音频诊断收集器：把系统音频输出状态（混音、可听度等）
 * 快照进诊断收集器。兼容实现只保留调用点用到的静态入口，
 * 不做任何真实采集。
 */
public final class SystemAudioDiagnosticCollector {

    private SystemAudioDiagnosticCollector() {
    }

    /** 把系统音频状态快照进指定诊断收集器（兼容实现：不做任何事）。 */
    public static void snapshot(PlaybackDiagnosticCollector collector, PlaybackDiagnosticCollector.Context context) {
    }
}
