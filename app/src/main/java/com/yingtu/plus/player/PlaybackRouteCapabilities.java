package com.yingtu.plus.player;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 路由能力（由路由结论推导出的链路特征）：播放器看到的是哪一段链路、上游是否可见、
 * 控制范围到哪里。兼容实现只按结论返回“未知”档，不做真实的链路探测。
 */
public final class PlaybackRouteCapabilities {

    public enum ObservedLeg implements PlaybackAutoContext.Labeled {
        PLAYER_TO_REMOTE_HTTP, UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    public enum UpstreamVisibility implements PlaybackAutoContext.Labeled {
        APP_SERVICE_PATH, UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    public enum ControlScope implements PlaybackAutoContext.Labeled {
        UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    private final PlaybackRoute.Resolution resolution;

    private PlaybackRouteCapabilities(PlaybackRoute.Resolution resolution) {
        this.resolution = resolution;
    }

    public static PlaybackRouteCapabilities resolve(PlaybackRoute.Resolution resolution) {
        return new PlaybackRouteCapabilities(resolution);
    }

    public ObservedLeg observedLeg() {
        return ObservedLeg.UNKNOWN;
    }

    public UpstreamVisibility upstreamVisibility() {
        return UpstreamVisibility.UNKNOWN;
    }

    public ControlScope controlScope() {
        return ControlScope.UNKNOWN;
    }

    /** 上游是否不透明（本地服务之外的远端链路默认视为不透明）。 */
    public boolean externalUpstreamOpaque() {
        return resolution == null || resolution.route() != PlaybackRoute.APP_LOCAL_SERVICE;
    }

    public String logSummary() {
        return "leg=" + observedLeg().label()
                + " upstream=" + upstreamVisibility().label()
                + " scope=" + controlScope().label();
    }
}
