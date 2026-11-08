package com.yingtu.plus.player;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放链路（播放器取流走哪条路）：直连远端、经本地回环代理、经 App 内置服务、其他。
 * {@link Resolution} 是“按地址解析出的路由结论”，由 {@link PlaybackResourceClassifier}
 * 与各引擎共享；兼容实现按地址做最保守的分类：本地回环地址 → 回环路由，其余 → 直连远端，
 * 无法判断时返回 OTHER。
 */
public enum PlaybackRoute {
    DIRECT_REMOTE_HTTP,
    EXTERNAL_LOOPBACK_PROXY,
    APP_LOCAL_SERVICE,
    OTHER;

    /** 路由的所有者。 */
    public enum Owner implements PlaybackAutoContext.Labeled {
        APP_MAIN_SERVER, APP_HLS_PROXY, UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    /** 路由结论的证据（诊断日志用）。 */
    public enum Evidence implements PlaybackAutoContext.Labeled {
        DIRECT_REMOTE, EXTERNAL_LOOPBACK, LOCAL_SERVICE, UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    /** 按地址解析出的路由结论。 */
    public static final class Resolution {
        private PlaybackRoute route;
        private Owner owner;
        private Evidence evidence;
        private PlaybackAutoContext.Confidence confidence;
        private boolean loopback;
        private String scheme;

        private Resolution(PlaybackRoute route, Owner owner, Evidence evidence,
                           PlaybackAutoContext.Confidence confidence, boolean loopback, String scheme) {
            this.route = route == null ? OTHER : route;
            this.owner = owner == null ? Owner.UNKNOWN : owner;
            this.evidence = evidence == null ? Evidence.UNKNOWN : evidence;
            this.confidence = confidence == null ? PlaybackAutoContext.Confidence.UNKNOWN : confidence;
            this.loopback = loopback;
            this.scheme = scheme == null ? "" : scheme;
        }

        public static Resolution of(PlaybackRoute route) {
            return new Resolution(route, Owner.UNKNOWN, Evidence.UNKNOWN,
                    PlaybackAutoContext.Confidence.UNKNOWN, false, "");
        }

        public PlaybackRoute route() {
            return route;
        }

        public Owner owner() {
            return owner;
        }

        public Evidence evidence() {
            return evidence;
        }

        public PlaybackAutoContext.Confidence confidence() {
            return confidence;
        }

        public boolean loopback() {
            return loopback;
        }

        public String scheme() {
            return scheme;
        }

        public String logSummary() {
            return "route=" + route.name().toLowerCase(Locale.US)
                    + " owner=" + owner.label()
                    + " evidence=" + evidence.label()
                    + " loopback=" + loopback;
        }

        /** 复制一份可继续调整的结论（分类器用它微调路由后写回上下文）。 */
        public Resolution buildUpon() {
            return new Resolution(route, owner, evidence, confidence, loopback, scheme);
        }

        void setRoute(PlaybackRoute route) {
            if (route != null) this.route = route;
        }

        void setOwner(Owner owner) {
            if (owner != null) this.owner = owner;
        }

        void setEvidence(Evidence evidence) {
            if (evidence != null) this.evidence = evidence;
        }

        void setConfidence(PlaybackAutoContext.Confidence confidence) {
            if (confidence != null) this.confidence = confidence;
        }

        void setLoopback(boolean loopback) {
            this.loopback = loopback;
        }

        void setScheme(String scheme) {
            this.scheme = scheme == null ? "" : scheme;
        }
    }

    /** 按播放地址解析路由；空地址返回 OTHER 结论。 */
    public static Resolution resolve(String url) {
        if (url == null || url.isEmpty()) return Resolution.of(OTHER);
        String lower = url.toLowerCase(Locale.US);
        String scheme = lower.contains("://") ? lower.substring(0, lower.indexOf("://")) : "";
        boolean loopback = lower.contains("127.0.0.1") || lower.contains("localhost") || lower.contains("[::1]");
        if (loopback) {
            return new Resolution(EXTERNAL_LOOPBACK_PROXY, Owner.APP_MAIN_SERVER,
                    Evidence.EXTERNAL_LOOPBACK, PlaybackAutoContext.Confidence.MEDIUM, true, scheme);
        }
        return new Resolution(DIRECT_REMOTE_HTTP, Owner.UNKNOWN,
                Evidence.DIRECT_REMOTE, PlaybackAutoContext.Confidence.LOW, false, scheme);
    }

    /** 该路由下预加载可用的并行线程数（兼容实现：不限制，返回请求数）。 */
    public int effectivePreloadThreads(int requested) {
        return Math.max(0, requested);
    }
}
