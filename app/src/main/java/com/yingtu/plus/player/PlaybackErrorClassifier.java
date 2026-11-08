package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.SessionToken;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放失败分类器：把一次播放异常按“阶段 / 路由 / 证据 / 错误码”归类，
 * 供 PlayerManager 决定重试、降级或报错文案。
 * 兼容实现只做最保守的分类：阶段 UNKNOWN、路由沿用传入的结论、错误码 0，
 * 不凭空推断失败原因。
 */
public final class PlaybackErrorClassifier {

    public enum Stage {
        DECODER, DRM, LOCAL_ENDPOINT, MEDIA_PARSING, NETWORK_IO, OUTPUT, UNKNOWN
    }

    /** 一次失败的分类结果。 */
    public static final class Failure {
        private final PlaybackRoute.Resolution route;
        private final int errorCode;

        private Failure(PlaybackRoute.Resolution route, int errorCode) {
            this.route = route;
            this.errorCode = errorCode;
        }

        public Stage stage() {
            return Stage.UNKNOWN;
        }

        public PlaybackRoute.Resolution route() {
            return route;
        }

        /** 失败证据（字符串，如 "mpv-video-output-marker"）；兼容实现返回空串。 */
        public String evidence() {
            return "";
        }

        public int errorCode() {
            return errorCode;
        }

        public String logSummary() {
            return "stage=" + stage().name().toLowerCase(java.util.Locale.US)
                    + " errorCode=" + errorCode;
        }
    }

    private PlaybackErrorClassifier() {
    }

    /**
     * 分类一次播放异常。
     *
     * @param error 播放异常（PlaybackException 等 Throwable）
     * @param route 当前路由结论
     */
    public static Failure classify(Throwable error, PlaybackRoute.Resolution route) {
        int code = 0;
        if (error instanceof androidx.media3.common.PlaybackException) {
            code = ((androidx.media3.common.PlaybackException) error).errorCode;
        }
        return new Failure(route, code);
    }
}
