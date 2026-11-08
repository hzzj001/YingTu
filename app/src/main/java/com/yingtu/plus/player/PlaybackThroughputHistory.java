package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.Confidence;
import com.yingtu.plus.player.PlaybackAutoContext.PathKind;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 吞吐历史（单例）：记录每次播放的带宽估计，供 mpv HLS 选档等策略
 * 在下次播放时查“上次同类链路跑多快”。兼容实现只保留记录 / 查询接口，
 * 不保存任何历史，查询一律返回不可用（Match.unavailable）。
 */
public final class PlaybackThroughputHistory {

    /** 一条吞吐证据（一次带宽估计的快照）。 */
    public static final class Evidence {
        private final long effectiveEstimateBitsPerSecond;
        private final long shortEstimateBitsPerSecond;
        private final long longEstimateBitsPerSecond;
        private final int longSampleCount;
        private final long longWindowMs;
        private final int predictionErrorPermille;
        private final Confidence confidence;
        private final boolean trustedPath;
        private final boolean preloadContended;

        public Evidence(long effectiveEstimateBitsPerSecond, long shortEstimateBitsPerSecond,
                        long longEstimateBitsPerSecond, int longSampleCount, long longWindowMs,
                        int predictionErrorPermille, Confidence confidence,
                        boolean trustedPath, boolean preloadContended) {
            this.effectiveEstimateBitsPerSecond = effectiveEstimateBitsPerSecond;
            this.shortEstimateBitsPerSecond = shortEstimateBitsPerSecond;
            this.longEstimateBitsPerSecond = longEstimateBitsPerSecond;
            this.longSampleCount = longSampleCount;
            this.longWindowMs = longWindowMs;
            this.predictionErrorPermille = predictionErrorPermille;
            this.confidence = confidence == null ? Confidence.UNKNOWN : confidence;
            this.trustedPath = trustedPath;
            this.preloadContended = preloadContended;
        }

        public long effectiveEstimateBitsPerSecond() {
            return effectiveEstimateBitsPerSecond;
        }

        public Confidence confidence() {
            return confidence;
        }
    }

    public enum Reason implements PlaybackAutoContext.Labeled {
        NO_MATCH;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    /** 一次历史查询的结果。 */
    public static final class Match {
        private final Reason reason;

        private Match(Reason reason) {
            this.reason = reason == null ? Reason.NO_MATCH : reason;
        }

        public static Match unavailable(Reason reason) {
            return new Match(reason);
        }

        public boolean usable() {
            return false;
        }

        public long ageMs() {
            return 0;
        }

        public Confidence confidence() {
            return Confidence.UNKNOWN;
        }

        public Reason reason() {
            return reason;
        }

        public long bitsPerSecond() {
            return 0;
        }

        public PathKind pathKind() {
            return PathKind.UNKNOWN;
        }
    }

    private static final PlaybackThroughputHistory PROCESS = new PlaybackThroughputHistory();

    /**
     * 公开无参构造器：ExoThroughputCoordinator 以 new 方式持有独立实例
     *（而 process() 单例另供 PlayerManager 等调用方使用）。
     */
    public PlaybackThroughputHistory() {
    }

    public static PlaybackThroughputHistory process() {
        return PROCESS;
    }

    /** 记录一条吞吐证据（兼容实现：不保存）。 */
    public void record(PlaybackAutoContext context, Evidence evidence, long nowElapsedMs) {
    }

    /** 按播放上下文与网络身份查历史（兼容实现：不保存历史，一律返回不可用）。 */
    public Match lookup(PlaybackAutoContext context, String networkIdentityDigest, long nowElapsedMs) {
        return Match.unavailable(Reason.NO_MATCH);
    }

    /** 使历史失效（兼容实现：不做任何事）。 */
    public void invalidate() {
    }
}
