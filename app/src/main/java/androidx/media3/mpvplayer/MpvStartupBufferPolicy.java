// 移植自上游 fish2018/webhtv（HEAD 4e30ffa）：app/src/main/java/androidx/media3/mpvplayer/MpvStartupBufferPolicy.java，仅包名与引用改名。
package androidx.media3.mpvplayer;

final class MpvStartupBufferPolicy {

    static final String CACHE_PAUSE = "yes";
    static final String CACHE_PAUSE_INITIAL = "no";

    private MpvStartupBufferPolicy() {
    }

    static boolean shouldApplyPerformanceOverlay(boolean performanceOptionsPriority) {
        return performanceOptionsPriority;
    }
}
