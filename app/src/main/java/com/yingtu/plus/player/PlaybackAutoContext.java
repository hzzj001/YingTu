package com.yingtu.plus.player;

import com.yingtu.plus.player.exo.ExoThroughputPathPolicy;
import com.yingtu.plus.player.ijk.IjkBufferPolicy;
import com.yingtu.plus.player.ijk.IjkDecodePressurePolicy;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 一次播放会话的“事实快照”：PlayerManager / 各 exo·ijk·mpv 策略在一次播放过程中
 * 不断把内核、解码方式、资源、路径、媒体、运行时等事实写进 {@link PlaybackAutoContextStore}，
 * 各策略只读这些事实做决策（码率、缓冲、预加载、恢复等）。
 *
 * 兼容实现只保留调用点用到的结构与签名：所有事实默认“未知”（Fact.hasValue() 为 false、
 * 计数为 0、枚举为 UNKNOWN），不做任何真实的采集与决策；枚举的 label() 一律返回小写 name。
 */
public final class PlaybackAutoContext {

    /** 所有带 label() 的枚举共用的接口。 */
    public interface Labeled {
        String label();
    }

    private static String lower(String name) {
        return name == null ? "" : name.toLowerCase(Locale.US);
    }

    public enum Confidence implements Labeled {
        HIGH, LOW, MEDIUM, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum DataSaverState implements Labeled {
        DISABLED, ENABLED, UNKNOWN, WHITELISTED;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum DecodeMode implements Labeled {
        HARDWARE, SOFTWARE, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum HdrType implements Labeled {
        UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum Kernel implements Labeled {
        EXO, IJK, MPV;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum ManifestKind implements Labeled {
        DASH_DYNAMIC, DASH_STATIC, HLS_MASTER, HLS_MEDIA;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum MemoryPressure implements Labeled {
        CRITICAL, MODERATE, NORMAL, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum NetworkCost implements Labeled {
        METERED, ROAMING, UNKNOWN, UNMETERED;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum NetworkTransport implements Labeled {
        UNKNOWN, VPN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum PathKind implements Labeled {
        APP_INTERNAL_SERVICE, EXTERNAL_LOOPBACK, LAN_PRIVATE, LOCAL, REMOTE, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum PlaybackPhase implements Labeled {
        BUFFERING, ENDED, ERROR, IDLE, PREPARING, READY, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum PowerState implements Labeled {
        NORMAL, POWER_SAVE, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum Protocol implements Labeled {
        DASH, HLS, LOCAL, OTHER, PROGRESSIVE_HTTP, RTMP, RTSP, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum RenderTarget implements Labeled {
        UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum StreamKind implements Labeled {
        LIVE, LOW_LATENCY_LIVE, UNKNOWN, VOD;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum ThermalState implements Labeled {
        CRITICAL, MODERATE, NOMINAL, SEVERE, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum TransferUnit implements Labeled {
        PART, SEGMENT, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum UpstreamState implements Labeled {
        OPAQUE, UNKNOWN, VISIBLE;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum ValueSource implements Labeled {
        ESTIMATOR, MANIFEST, NATIVE_RUNTIME, PLAYBACK_REQUEST, PLAYER_CALLBACK,
        PLAYER_MANAGER, PROXY, ROUTE_CLASSIFIER, SYSTEM_API, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    public enum SystemConditionTrigger implements Labeled {
        NETWORK_CALLBACK, UNKNOWN;

        @Override
        public String label() {
            return lower(name());
        }
    }

    /** 播放器显示模式（DisplayMode）：调用点只把它当作不透明类型传递，兼容实现为空类型。 */
    public static final class DisplayMode {
        public static final DisplayMode UNKNOWN = new DisplayMode();

        private DisplayMode() {
        }
    }

    /** 会话令牌：none() 表示无会话；active() 为 false。 */
    public static final class SessionToken {
        private static final SessionToken NONE = new SessionToken(0);
        private final long id;

        private SessionToken(long id) {
            this.id = id;
        }

        public static SessionToken none() {
            return NONE;
        }

        static SessionToken of(long id) {
            return new SessionToken(id);
        }

        public boolean active() {
            return id != 0;
        }

        public long id() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SessionToken)) return false;
            return id == ((SessionToken) o).id;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }

        @Override
        public String toString() {
            return "SessionToken(" + id + ")";
        }
    }

    /**
     * 单个事实：值 + 来源 + 置信度 + 采样时刻。
     * source() 返回来源的 label（字符串，如 "unknown"）；confidence() 返回置信度的 label（字符串，如 "low"）。
     */
    public static final class Fact<T> {
        private final T value;
        private final ValueSource valueSource;
        private final Confidence confidenceValue;
        private final long sampledAtElapsedMs;
        private final SessionToken session;

        private Fact(T value, ValueSource valueSource, Confidence confidenceValue, long sampledAtElapsedMs, SessionToken session) {
            this.value = value;
            this.valueSource = valueSource == null ? ValueSource.UNKNOWN : valueSource;
            this.confidenceValue = confidenceValue == null ? Confidence.UNKNOWN : confidenceValue;
            this.sampledAtElapsedMs = sampledAtElapsedMs;
            this.session = session == null ? SessionToken.none() : session;
        }

        public static <T> Fact<T> forSession(T value, ValueSource source, Confidence confidence, long sampledAtElapsedMs) {
            return new Fact<>(value, source, confidence, sampledAtElapsedMs, null);
        }

        public static <T> Fact<T> unknown() {
            return new Fact<>(null, ValueSource.UNKNOWN, Confidence.UNKNOWN, 0, null);
        }

        public T value() {
            return value;
        }

        /** 来源的 label（字符串）。 */
        public String source() {
            return valueSource.label();
        }

        public ValueSource valueSource() {
            return valueSource;
        }

        /** 置信度的 label（字符串）。 */
        public String confidence() {
            return confidenceValue.label();
        }

        public Confidence confidenceValue() {
            return confidenceValue;
        }

        public boolean hasValue() {
            return value != null;
        }

        public boolean isUsable(long nowElapsedMs) {
            return value != null;
        }

        public SessionToken session() {
            return session;
        }

        public long sampledAtElapsedMs() {
            return sampledAtElapsedMs;
        }

        /** 值的 label：枚举值取其 label()，其他取 toString()，无值取 "unknown"。 */
        public String label() {
            if (value == null) return "unknown";
            if (value instanceof Labeled) return ((Labeled) value).label();
            return String.valueOf(value);
        }
    }

    /** 设备 / 网络 / 内存侧事实。 */
    public static final class DeviceFacts {
        private DeviceFacts() {
        }

        public static DeviceFacts unknown() {
            return new DeviceFacts();
        }

        public long nowElapsedMs() {
            return 0;
        }

        public int rebufferCount() {
            return 0;
        }

        public PlaybackRoute route() {
            return PlaybackRoute.OTHER;
        }

        public long observedAtElapsedMs() {
            return 0;
        }

        public SessionToken session() {
            return SessionToken.none();
        }

        public long effectiveEstimateBitsPerSecond() {
            return 0;
        }

        public long shortEstimateBitsPerSecond() {
            return 0;
        }

        public long longEstimateBitsPerSecond() {
            return 0;
        }

        public int longSampleCount() {
            return 0;
        }

        public long longWindowMs() {
            return 0;
        }

        /** -1 表示未知。 */
        public int predictionErrorPermille() {
            return -1;
        }

        public Confidence confidence() {
            return Confidence.UNKNOWN;
        }

        public ExoThroughputPathPolicy.Trust pathTrust() {
            // Trust 只有 TRUSTED / LIMITED / BLOCKED，没有 UNKNOWN；未知时取中间值 LIMITED。
            return ExoThroughputPathPolicy.Trust.LIMITED;
        }

        public Confidence pathConfidence() {
            return Confidence.UNKNOWN;
        }

        public boolean preloadContended() {
            return false;
        }

        public NetworkSnapshot networkSnapshot() {
            return NetworkSnapshot.unknown();
        }

        public NetworkCost networkCost() {
            return NetworkCost.UNKNOWN;
        }

        public Fact<PowerState> powerState() {
            return Fact.unknown();
        }

        public Fact<ThermalState> thermalState() {
            return Fact.unknown();
        }

        public long sampledAtElapsedMs() {
            return 0;
        }

        public boolean known() {
            return false;
        }

        public long slopeMsPerSecond() {
            return 0;
        }

        public long timeToEmptyMs() {
            return 0;
        }

        public Fact<MemoryPressure> memoryPressure() {
            return Fact.unknown();
        }

        public Fact<MemorySnapshot> memorySnapshot() {
            return Fact.unknown();
        }

        /** 诊断用 PSS（进程实际占用内存）快照，未采集时为未知。 */
        public Fact<Long> diagnosticPssBytes() {
            return Fact.unknown();
        }
    }

    /** 资源（协议 / 流类型 / 清单）侧事实。 */
    public static final class ResourceFacts {
        private ResourceFacts() {
        }

        public static ResourceFacts unknown() {
            return new ResourceFacts();
        }

        public Fact<Protocol> protocol() {
            return Fact.unknown();
        }

        public Fact<StreamKind> streamKind() {
            return Fact.unknown();
        }

        public Fact<ManifestFacts> manifest() {
            return Fact.unknown();
        }

        public Fact<TransferUnit> transferUnit() {
            return Fact.unknown();
        }

        public Fact<Long> targetBytes() {
            return Fact.unknown();
        }
    }

    /** 路径（播放器 → 上游）侧事实。 */
    public static final class PathFacts {
        private PathFacts() {
        }

        public static PathFacts unknown() {
            return new PathFacts();
        }

        public String label() {
            return "unknown";
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

        public Fact<PlaybackRoute.Owner> owner() {
            return Fact.unknown();
        }

        public Fact<Boolean> loopback() {
            return Fact.unknown();
        }

        public Fact<PlaybackRouteCapabilities.ObservedLeg> observedLeg() {
            return Fact.unknown();
        }

        public PlaybackRoute route() {
            return PlaybackRoute.OTHER;
        }

        public Fact<PlaybackRouteCapabilities.UpstreamVisibility> upstreamVisibility() {
            return Fact.unknown();
        }
    }

    /** 媒体（解码器 / 音视频轨 / 输出）侧事实。 */
    public static final class MediaFacts {
        private MediaFacts() {
        }

        public static MediaFacts unknown() {
            return new MediaFacts();
        }

        public DecoderFacts decoder() {
            return new DecoderFacts();
        }

        public TrackFacts videoTrack() {
            return new TrackFacts();
        }

        public TrackFacts audioTrack() {
            return new TrackFacts();
        }

        public OutputFacts output() {
            return new OutputFacts();
        }

        public long trackSequence() {
            return 0;
        }
    }

    public static final class DecoderFacts {
        public Fact<Boolean> secureVideoDecoder() {
            return Fact.unknown();
        }

        public long trackSequence() {
            return 0;
        }

        public Fact<DecodeMode> videoDecodeMode() {
            return Fact.unknown();
        }

        public IjkDecodePressurePolicy.Config stagedConfig() {
            return null;
        }

        public IjkDecodePressurePolicy.Config targetConfig() {
            return null;
        }
    }

    public static final class TrackFacts {
        public Fact<String> mimeType() {
            return Fact.unknown();
        }

        public Fact<String> codecs() {
            return Fact.unknown();
        }

        public Fact<Integer> profile() {
            return Fact.unknown();
        }

        public Fact<Integer> level() {
            return Fact.unknown();
        }

        public Fact<Integer> width() {
            return Fact.unknown();
        }

        public Fact<Integer> height() {
            return Fact.unknown();
        }

        public Fact<Float> frameRate() {
            return Fact.unknown();
        }

        public Fact<HdrType> hdrType() {
            return Fact.unknown();
        }

        public Fact<ColorSnapshot> color() {
            return Fact.unknown();
        }

        public Fact<Long> peakBitrateBitsPerSecond() {
            return Fact.unknown();
        }

        public Fact<Long> averageBitrateBitsPerSecond() {
            return Fact.unknown();
        }
    }

    public static final class OutputFacts {
        public Fact<RenderTarget> renderTarget() {
            return Fact.unknown();
        }
    }

    public static final class ColorSnapshot {
        private ColorSnapshot() {
        }

        public static ColorSnapshot unknown() {
            return new ColorSnapshot();
        }

        public int colorSpace() {
            return 0;
        }

        public int colorRange() {
            return 0;
        }

        public int colorTransfer() {
            return 0;
        }

        public boolean hdrStaticMetadata() {
            return false;
        }
    }

    public static final class ManifestFacts {
        private ManifestFacts() {
        }

        public static ManifestFacts unknown() {
            return new ManifestFacts();
        }

        public Long holdBackMs() {
            return null;
        }

        public Long partDurationMs() {
            return null;
        }

        public Long targetDurationMs() {
            return null;
        }

        public Boolean lowLatency() {
            return null;
        }
    }

    public static final class MemorySnapshot {
        private MemorySnapshot() {
        }

        public static MemorySnapshot unknown() {
            return new MemorySnapshot();
        }

        public Boolean lowRamDevice() {
            return null;
        }

        public Long javaHeapLimitBytes() {
            return null;
        }

        public Long javaHeapHeadroomBytes() {
            return null;
        }

        /** 原生堆已分配字节数，未采集时为 null。 */
        public Long nativeHeapAllocatedBytes() {
            return null;
        }

        public Long systemAvailableBytes() {
            return null;
        }

        public Long systemThresholdBytes() {
            return null;
        }

        public Boolean systemLowMemory() {
            return null;
        }

        public boolean hasJavaMetrics() {
            return false;
        }

        public boolean hasSystemMetrics() {
            return false;
        }
    }

    public static final class NetworkSnapshot {
        private NetworkSnapshot() {
        }

        public static NetworkSnapshot unknown() {
            return new NetworkSnapshot();
        }

        public Boolean available() {
            return null;
        }

        public Boolean validated() {
            return null;
        }

        public Boolean metered() {
            return null;
        }

        public Boolean roaming() {
            return null;
        }

        public NetworkTransport transport() {
            return NetworkTransport.UNKNOWN;
        }

        public DataSaverState dataSaverState() {
            return DataSaverState.UNKNOWN;
        }
    }

    /** 运行时（当前播放进度 / 码率 / 卡顿）侧事实。 */
    public static final class RuntimeFacts {
        private RuntimeFacts() {
        }

        public static RuntimeFacts unknown() {
            return new RuntimeFacts();
        }

        public Fact<Long> mediaBitrateBitsPerSecond() {
            return Fact.unknown();
        }

        public Fact<Integer> rebufferCount() {
            return Fact.unknown();
        }

        public Fact<Long> liveLagMs() {
            return Fact.unknown();
        }

        public Fact<Long> bufferedDurationMs() {
            return Fact.unknown();
        }

        public Fact<Long> positionMs() {
            return Fact.unknown();
        }
    }

    private final SessionToken session;
    private final DeviceFacts deviceFacts;
    private final ResourceFacts resourceFacts;
    private final PathFacts pathFacts;
    private final MediaFacts mediaFacts;
    private final RuntimeFacts runtimeFacts;

    private PlaybackAutoContext(SessionToken session, DeviceFacts deviceFacts, ResourceFacts resourceFacts, PathFacts pathFacts, MediaFacts mediaFacts, RuntimeFacts runtimeFacts) {
        this.session = session == null ? SessionToken.none() : session;
        this.deviceFacts = deviceFacts == null ? DeviceFacts.unknown() : deviceFacts;
        this.resourceFacts = resourceFacts == null ? ResourceFacts.unknown() : resourceFacts;
        this.pathFacts = pathFacts == null ? PathFacts.unknown() : pathFacts;
        this.mediaFacts = mediaFacts == null ? MediaFacts.unknown() : mediaFacts;
        this.runtimeFacts = runtimeFacts == null ? RuntimeFacts.unknown() : runtimeFacts;
    }

    public static PlaybackAutoContext empty() {
        return new PlaybackAutoContext(SessionToken.none(), null, null, null, null, null);
    }

    static PlaybackAutoContext of(SessionToken session, ResourceFacts resourceFacts, PathFacts pathFacts) {
        return new PlaybackAutoContext(session, null, resourceFacts, pathFacts, null, null);
    }

    public SessionToken session() {
        return session;
    }

    public DeviceFacts device() {
        return deviceFacts;
    }

    public ResourceFacts resource() {
        return resourceFacts;
    }

    public PathFacts path() {
        return pathFacts;
    }

    public MediaFacts media() {
        return mediaFacts;
    }

    public RuntimeFacts runtime() {
        return runtimeFacts;
    }

    public long revision() {
        return 0;
    }

    public Fact<Kernel> kernel() {
        return Fact.unknown();
    }

    public Fact<DecodeMode> decodeMode() {
        return Fact.unknown();
    }

    public PathFacts currentPath() {
        return pathFacts;
    }

    public boolean active() {
        return session.active();
    }

    public String logSummary() {
        return "session=" + session.id() + " unknown";
    }

    public IjkBufferPolicy.Config stagedConfig() {
        return null;
    }

    public long reloadAttempts() {
        return 0;
    }

    public long successfulReloads() {
        return 0;
    }

    public long fallbackCount() {
        return 0;
    }

    public long recoveryAttempts() {
        return 0;
    }

    public long successfulDowngrades() {
        return 0;
    }

    public long rollbackCount() {
        return 0;
    }

    public long failedActions() {
        return 0;
    }
}
