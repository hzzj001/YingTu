package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.Fact;
import com.yingtu.plus.player.PlaybackAutoContext.ManifestFacts;
import com.yingtu.plus.player.PlaybackAutoContext.MemoryPressure;
import com.yingtu.plus.player.PlaybackAutoContext.MemorySnapshot;
import com.yingtu.plus.player.PlaybackAutoContext.PathFacts;
import com.yingtu.plus.player.PlaybackAutoContext.PathKind;
import com.yingtu.plus.player.PlaybackAutoContext.Protocol;
import com.yingtu.plus.player.PlaybackAutoContext.ResourceFacts;
import com.yingtu.plus.player.PlaybackAutoContext.StreamKind;
import com.yingtu.plus.player.PlaybackAutoContext.TransferUnit;
import com.yingtu.plus.player.PlaybackAutoContext.UpstreamState;

import java.util.Map;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 资源分类器：按播放地址 / 响应头 / 播放器状态，把一次播放请求归类为协议、流类型、
 * 清单、传输单元等事实，供 {@link PlaybackAutoContext} 与各策略消费。
 * 兼容实现只做最保守的分类（能从地址后缀直接看出的协议，其余一律未知），
 * 分类结果默认“不可用”（各 *Usable() 返回 false），不凭空推断站点特征。
 */
public final class PlaybackResourceClassifier {

    private PlaybackResourceClassifier() {
    }

    /** 一次播放请求的分类结果。 */
    public static final class Classification {
        private final Fact<Protocol> protocol;
        private final Fact<StreamKind> streamKind;
        private final Fact<ManifestFacts> manifest;
        private final Fact<TransferUnit> transferUnit;
        private final Fact<Long> targetBytes;
        private final long observedAtElapsedMs;

        private Classification(Fact<Protocol> protocol, Fact<StreamKind> streamKind,
                               Fact<ManifestFacts> manifest, Fact<TransferUnit> transferUnit,
                               Fact<Long> targetBytes, long observedAtElapsedMs) {
            this.protocol = protocol == null ? Fact.unknown() : protocol;
            this.streamKind = streamKind == null ? Fact.unknown() : streamKind;
            this.manifest = manifest == null ? Fact.unknown() : manifest;
            this.transferUnit = transferUnit == null ? Fact.unknown() : transferUnit;
            this.targetBytes = targetBytes == null ? Fact.unknown() : targetBytes;
            this.observedAtElapsedMs = observedAtElapsedMs;
        }

        public static Classification unknown(long observedAtElapsedMs) {
            return new Classification(null, null, null, null, null, observedAtElapsedMs);
        }

        public Fact<Protocol> protocol() {
            return protocol;
        }

        public Fact<StreamKind> streamKind() {
            return streamKind;
        }

        public Fact<ManifestFacts> manifest() {
            return manifest;
        }

        public Fact<TransferUnit> transferUnit() {
            return transferUnit;
        }

        public Fact<Long> targetBytes() {
            return targetBytes;
        }

        public Fact<MemoryPressure> memoryPressure() {
            return Fact.unknown();
        }

        public MemorySnapshot memorySnapshot() {
            return MemorySnapshot.unknown();
        }

        public Fact<PathKind> playerPath() {
            return Fact.unknown();
        }

        public Fact<PathKind> upstreamPath() {
            return Fact.unknown();
        }

        public Fact<UpstreamState> upstreamState() {
            return Fact.unknown();
        }

        public boolean performancePriority() {
            return false;
        }

        public boolean pressureUsable() {
            return false;
        }

        public boolean snapshotUsable() {
            return false;
        }

        public boolean protocolUsable() {
            return protocol.hasValue();
        }

        public boolean streamKindUsable() {
            return streamKind.hasValue();
        }

        public boolean playerPathUsable() {
            return false;
        }

        public boolean upstreamPathUsable() {
            return false;
        }

        public boolean upstreamStateUsable() {
            return false;
        }

        public long observedAtElapsedMs() {
            return observedAtElapsedMs;
        }

        public String logSummary() {
            return "protocol=" + protocol.label() + " stream=" + streamKind.label();
        }

        public Classification buildUpon() {
            return new Classification(protocol, streamKind, manifest, transferUnit, targetBytes, observedAtElapsedMs);
        }

        public PathFacts toPathFacts(PlaybackRoute.Resolution resolution, long nowElapsedMs) {
            return PathFacts.unknown();
        }

        public ResourceFacts toResourceFacts(long nowElapsedMs) {
            return ResourceFacts.unknown();
        }
    }

    /** 按播放请求（地址 + 格式）分类。 */
    public static Classification classifyRequest(String url, String format, String formatHint) {
        return classifyDataSource(url, url, format == null ? formatHint : format, null);
    }

    /** 合并两次分类结果（后一次优先，兼容实现返回后一次非空者）。 */
    public static Classification merge(Classification first, Classification second) {
        if (second == null) return first;
        if (first == null) return second;
        return second;
    }

    /** 结合播放器当前状态修正分类（是否直播、总时长）。 */
    public static Classification observePlayer(Classification current, boolean isLive, long durationMs) {
        if (current == null) return Classification.unknown(android.os.SystemClock.elapsedRealtime());
        return current;
    }

    /** 清单事实的有效期（毫秒）；清单未知时返回 0。 */
    public static long manifestTtlMs(Fact<ManifestFacts> manifest) {
        return 0;
    }

    /** 按数据源（请求地址、重定向后地址、Content-Type、响应头）分类。 */
    public static Classification classifyDataSource(String uri, String resolvedUri, String contentType, Map<String, String> headers) {
        String probe = resolvedUri != null ? resolvedUri : uri;
        Fact<Protocol> protocol = Fact.unknown();
        if (probe != null) {
            String lower = probe.toLowerCase(java.util.Locale.US);
            Protocol found = null;
            if (lower.contains(".m3u8")) found = Protocol.HLS;
            else if (lower.contains(".mpd")) found = Protocol.DASH;
            else if (lower.startsWith("rtmp://")) found = Protocol.RTMP;
            else if (lower.startsWith("rtsp://")) found = Protocol.RTSP;
            else if (lower.startsWith("file://") || lower.startsWith("content://")) found = Protocol.LOCAL;
            if (found != null) {
                protocol = Fact.forSession(found, PlaybackAutoContext.ValueSource.ROUTE_CLASSIFIER,
                        PlaybackAutoContext.Confidence.LOW, android.os.SystemClock.elapsedRealtime());
            }
        }
        return new Classification(protocol, null, null, null, null, android.os.SystemClock.elapsedRealtime());
    }
}
