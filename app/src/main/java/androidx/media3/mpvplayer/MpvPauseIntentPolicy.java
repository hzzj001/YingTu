// 移植自上游 fish2018/webhtv（HEAD 4e30ffa）：app/src/main/java/androidx/media3/mpvplayer/MpvPauseIntentPolicy.java，仅包名与引用改名。
package androidx.media3.mpvplayer;

final class MpvPauseIntentPolicy {

    enum Action {
        NONE,
        WAIT_FOR_ACTIVE_MEDIA,
        REASSERT_REQUESTED_STATE
    }

    private MpvPauseIntentPolicy() {
    }

    static Action resolve(boolean playWhenReady, boolean observedPaused, boolean activeMedia) {
        boolean requestedPaused = !playWhenReady;
        if (observedPaused == requestedPaused) return Action.NONE;
        return activeMedia ? Action.REASSERT_REQUESTED_STATE : Action.WAIT_FOR_ACTIVE_MEDIA;
    }
}
