package com.yingtu.plus.player;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 输出进度诊断（exo 音频输出链路用）：记录音频渲染进度，
 * 供诊断收集器判断输出是否停滞。兼容实现为空壳。
 */
public final class OutputProgressDiagnostic {

    public OutputProgressDiagnostic(boolean video) {
    }
}
