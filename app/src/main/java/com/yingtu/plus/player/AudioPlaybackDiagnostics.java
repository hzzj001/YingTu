package com.yingtu.plus.player;

import androidx.media3.common.Format;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 音频播放诊断（exo / ijk 引擎用它汇总“当前音频轨 + 解码方式 + 输出模式 + 失败原因”
 * 的快照，供网络保护等策略判断音频链路是否正常）。
 * 兼容实现只做字段搬运：track() 把 media3 Format 的编解码信息包成 Track，
 * failureReason() 按 media3 错误码映射到最接近的失败原因，其余一律返回“未知”档。
 */
public final class AudioPlaybackDiagnostics {

    public enum DecodeMode {
        HARDWARE, NONE, SOFTWARE, UNKNOWN
    }

    public enum OutputMode {
        OFFLOAD, PASSTHROUGH, PCM, UNKNOWN
    }

    public enum RuntimeState {
        FAILED, UNKNOWN
    }

    public enum FailureReason {
        DECODER_INIT, DECODER_RUNTIME, NONE, UNKNOWN
    }

    /** 一条音频轨的诊断视图（编解码名等）。 */
    public static final class Track {
        private final String codec;

        private Track(String codec) {
            this.codec = codec == null ? "" : codec;
        }

        public String codec() {
            return codec;
        }
    }

    /** 音频播放诊断快照（9 参为正常态，12 参追加失败态的尝试层级 / 运行状态 / 失败原因）。 */
    public static final class Snapshot {
        private final Track original;
        private final Track active;
        private final DecodeMode decodeMode;
        private final String decoderName;
        private final OutputMode outputMode;
        private final int channels;
        private final int sampleRate;
        private final boolean tunneling;
        private final String reason;
        private final String lastAttemptLevel;
        private final RuntimeState state;
        private final FailureReason failureReason;

        public Snapshot(Track original, Track active, DecodeMode decodeMode, String decoderName,
                        OutputMode outputMode, int channels, int sampleRate, boolean tunneling, String reason) {
            this(original, active, decodeMode, decoderName, outputMode, channels, sampleRate, tunneling, reason,
                    "", RuntimeState.UNKNOWN, FailureReason.NONE);
        }

        public Snapshot(Track original, Track active, DecodeMode decodeMode, String decoderName,
                        OutputMode outputMode, int channels, int sampleRate, boolean tunneling, String reason,
                        String lastAttemptLevel, RuntimeState state, FailureReason failureReason) {
            this.original = original;
            this.active = active;
            this.decodeMode = decodeMode == null ? DecodeMode.UNKNOWN : decodeMode;
            this.decoderName = decoderName == null ? "" : decoderName;
            this.outputMode = outputMode == null ? OutputMode.UNKNOWN : outputMode;
            this.channels = channels;
            this.sampleRate = sampleRate;
            this.tunneling = tunneling;
            this.reason = reason == null ? "" : reason;
            this.lastAttemptLevel = lastAttemptLevel == null ? "" : lastAttemptLevel;
            this.state = state == null ? RuntimeState.UNKNOWN : state;
            this.failureReason = failureReason == null ? FailureReason.NONE : failureReason;
        }

        public Track original() {
            return original;
        }

        public Track active() {
            return active;
        }

        public DecodeMode decodeMode() {
            return decodeMode;
        }

        public String decoderName() {
            return decoderName;
        }

        public OutputMode outputMode() {
            return outputMode;
        }

        public int channels() {
            return channels;
        }

        public int sampleRate() {
            return sampleRate;
        }

        public boolean tunneling() {
            return tunneling;
        }

        public String reason() {
            return reason;
        }

        public String lastAttemptLevel() {
            return lastAttemptLevel;
        }

        public RuntimeState state() {
            return state;
        }

        public FailureReason failureReason() {
            return failureReason;
        }
    }

    private AudioPlaybackDiagnostics() {
    }

    /** 把当前选中音频轨包成诊断视图；format 为空时返回空编解码名的 Track。 */
    public static Track track(Format format, String detail) {
        if (format == null) return new Track("");
        String codec = format.sampleMimeType != null ? format.sampleMimeType : "";
        if (codec.isEmpty() && format.codecs != null) codec = format.codecs;
        return new Track(codec);
    }

    /** 按 media3 错误码映射失败原因（解码器初始化 / 运行时失败，其余为 UNKNOWN）。 */
    public static FailureReason failureReason(int errorCode) {
        if (errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED
                || errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED) {
            return FailureReason.DECODER_INIT;
        }
        if (errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FAILED
                || errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED) {
            return FailureReason.DECODER_RUNTIME;
        }
        return FailureReason.UNKNOWN;
    }

    /** 最后一次尝试的层级（兼容实现：返回 "unknown"）。 */
    public static String lastAttemptLevel(FailureReason reason, OutputMode outputMode, DecodeMode decodeMode) {
        return "unknown";
    }

    /** 按输出编码把原始轨包成已编码轨视图。 */
    public static Track encodedTrack(Track original, int encoding) {
        return original == null ? new Track("") : original;
    }
}
