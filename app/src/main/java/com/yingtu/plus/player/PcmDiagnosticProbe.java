package com.yingtu.plus.player;

import java.nio.ByteBuffer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * PCM 诊断探针（exo 音频输出链路用）：采样 PCM 输出做可听度诊断。
 * 兼容实现为空壳，sample() 不做任何事。
 */
public final class PcmDiagnosticProbe {

    public PcmDiagnosticProbe() {
    }

    public void sample(PlaybackDiagnosticCollector collector, PlaybackDiagnosticCollector.Context context,
                       ByteBuffer buffer, long positionMs, int channels, int sampleRate, int encoding,
                       String detail, boolean flag) {
    }
}
