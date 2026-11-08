package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.Confidence;
import com.yingtu.plus.player.PlaybackAutoContext.PlaybackPhase;
import com.yingtu.plus.player.PlaybackAutoContext.ValueSource;

import java.util.List;
import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放决策遥测的数据结构：各策略做决策时把“决策域 / 结果 / 输入事实”记成
 * {@link DecisionEvent}，把周期性运行状态记成 {@link RuntimeObservation}，
 * 交 {@link PlaybackTelemetryCoordinator} 发布。兼容实现只保留数据结构本身，
 * 不做任何上报、采样或持久化。
 */
public final class PlaybackTelemetry {

    private PlaybackTelemetry() {
    }

    /** 诊断标签安全化（去空白、去非法字符）；空输入返回 "unknown"。 */
    public static String safeLabel(String label) {
        if (label == null) return "unknown";
        String value = label.trim().replaceAll("[^A-Za-z0-9_.-]+", "_");
        return value.isEmpty() ? "unknown" : value;
    }

    public enum DecisionDomain implements PlaybackAutoContext.Labeled {
        CACHE, IJK_BUFFER, IJK_DECODE_PRESSURE, IJK_REALTIME_RECOVERY, IJK_RUNTIME_PROFILE,
        LOAD_CONTROL, MPV_BACK_CACHE, MPV_CACHE, MPV_FORWARD_CACHE, MPV_HLS_VARIANT,
        MPV_OUTPUT, MPV_PRELOAD, MPV_RESOURCE_PRESSURE, NETWORK_PROTECTION, PRELOAD,
        RTSP_LIVE_RECOVERY, THROUGHPUT;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    public enum DecisionOutcome implements PlaybackAutoContext.Labeled {
        APPLIED, FAILED, HELD, OBSERVED, REQUESTED, SELECTED, SUPPRESSED;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    /** 一项决策输入事实（键 + 值 + 来源 + 置信度）。 */
    public static final class DecisionInput {
        private final String name;
        private final Object value;

        private DecisionInput(String name, Object value) {
            this.name = name == null ? "" : name;
            this.value = value;
        }

        public static DecisionInput bool(String name, boolean value, ValueSource source, Confidence confidence) {
            return new DecisionInput(name, value);
        }

        public static DecisionInput bool(String name, boolean value, String source, String confidence) {
            return new DecisionInput(name, value);
        }

        public static DecisionInput number(String name, long value, ValueSource source, Confidence confidence) {
            return new DecisionInput(name, value);
        }

        public static DecisionInput number(String name, long value, String source, String confidence) {
            return new DecisionInput(name, value);
        }

        public static DecisionInput unknown(String name) {
            return new DecisionInput(name, null);
        }

        public static DecisionInput decimal(String name, double value, ValueSource source, Confidence confidence) {
            return new DecisionInput(name, value);
        }

        public static DecisionInput text(String name, String value, String source, String confidence) {
            return new DecisionInput(name, value);
        }

        public static DecisionInput text(String name, String value, ValueSource source, Confidence confidence) {
            return new DecisionInput(name, value);
        }

        public String name() {
            return name;
        }

        public Object value() {
            return value;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    /** 一次决策事件（域 / 结果 / 会话 / 组件 / 动作 / 详情 / 输入事实列表）。 */
    public static final class DecisionEvent {
        private final DecisionDomain domain;
        private final DecisionOutcome outcome;
        private final String first;
        private final String second;
        private final String third;
        private final String fourth;
        private final String fifth;
        private final List<DecisionInput> inputs;

        public DecisionEvent(DecisionDomain domain, DecisionOutcome outcome,
                             String first, String second, String third, String fourth, String fifth,
                             List<DecisionInput> inputs) {
            this.domain = domain;
            this.outcome = outcome;
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fifth = fifth;
            this.inputs = inputs;
        }

        public DecisionDomain domain() {
            return domain;
        }

        public DecisionOutcome outcome() {
            return outcome;
        }

        public List<DecisionInput> inputs() {
            return inputs;
        }
    }

    /** 一个带来源与置信度的度量值。 */
    public static final class Metric<T> {
        private final T value;
        private final ValueSource source;
        private final Confidence confidence;

        private Metric(T value, ValueSource source, Confidence confidence) {
            this.value = value;
            this.source = source == null ? ValueSource.UNKNOWN : source;
            this.confidence = confidence == null ? Confidence.UNKNOWN : confidence;
        }

        public static <T> Metric<T> of(T value, ValueSource source, Confidence confidence) {
            return new Metric<>(value, source, confidence);
        }

        public static <T> Metric<T> unknown() {
            return new Metric<>(null, ValueSource.UNKNOWN, Confidence.UNKNOWN);
        }

        public boolean known() {
            return value != null;
        }

        public T value() {
            return value;
        }

        public ValueSource source() {
            return source;
        }

        public Confidence confidence() {
            return confidence;
        }
    }

    /** 一次周期性运行状态采样（全部为 Metric 包装的度量值）。 */
    public static final class RuntimeObservation {
        private final Metric<PlaybackPhase> phase;
        private final Metric<Boolean> playing;
        private final Metric<Long> position;
        private final Metric<Long> duration;
        private final Metric<Long> buffered;
        private final Metric<Long> bandwidth;
        private final Metric<Long> mediaBitrate;
        private final Metric<Float> renderedFrameRate;
        private final Metric<Long> droppedFrames;
        private final Metric<Integer> rebufferCount;
        private final Metric<Long> rebufferTotalMs;
        private final Metric<Long> firstFrameMs;
        private final Metric<Long> liveLagMs;

        public RuntimeObservation(Metric<PlaybackPhase> phase, Metric<Boolean> playing,
                                  Metric<Long> position, Metric<Long> duration, Metric<Long> buffered,
                                  Metric<Long> bandwidth, Metric<Long> mediaBitrate,
                                  Metric<Float> renderedFrameRate, Metric<Long> droppedFrames,
                                  Metric<Integer> rebufferCount, Metric<Long> rebufferTotalMs,
                                  Metric<Long> firstFrameMs, Metric<Long> liveLagMs) {
            this.phase = phase == null ? Metric.unknown() : phase;
            this.playing = playing == null ? Metric.unknown() : playing;
            this.position = position == null ? Metric.unknown() : position;
            this.duration = duration == null ? Metric.unknown() : duration;
            this.buffered = buffered == null ? Metric.unknown() : buffered;
            this.bandwidth = bandwidth == null ? Metric.unknown() : bandwidth;
            this.mediaBitrate = mediaBitrate == null ? Metric.unknown() : mediaBitrate;
            this.renderedFrameRate = renderedFrameRate == null ? Metric.unknown() : renderedFrameRate;
            this.droppedFrames = droppedFrames == null ? Metric.unknown() : droppedFrames;
            this.rebufferCount = rebufferCount == null ? Metric.unknown() : rebufferCount;
            this.rebufferTotalMs = rebufferTotalMs == null ? Metric.unknown() : rebufferTotalMs;
            this.firstFrameMs = firstFrameMs == null ? Metric.unknown() : firstFrameMs;
            this.liveLagMs = liveLagMs == null ? Metric.unknown() : liveLagMs;
        }

        public Metric<PlaybackPhase> phase() {
            return phase;
        }

        public Metric<Boolean> playing() {
            return playing;
        }

        public Metric<Long> positionMs() {
            return position;
        }

        public Metric<Long> durationMs() {
            return duration;
        }

        public Metric<Long> bufferedDurationMs() {
            return buffered;
        }

        public Metric<Long> bandwidthBitsPerSecond() {
            return bandwidth;
        }

        public Metric<Long> mediaBitrateBitsPerSecond() {
            return mediaBitrate;
        }

        public Metric<Float> renderedFrameRate() {
            return renderedFrameRate;
        }

        public Metric<Long> droppedFrames() {
            return droppedFrames;
        }

        public Metric<Integer> rebufferCount() {
            return rebufferCount;
        }

        public Metric<Long> rebufferTotalMs() {
            return rebufferTotalMs;
        }

        public Metric<Long> firstFrameMs() {
            return firstFrameMs;
        }

        public Metric<Long> liveLagMs() {
            return liveLagMs;
        }
    }
}
