// 移植自上游 fish2018/webhtv（HEAD 4e30ffa）：app/src/main/java/androidx/media3/mpvplayer/MpvOsdSurfacePolicy.java，仅包名与引用改名。
package androidx.media3.mpvplayer;

final class MpvOsdSurfacePolicy {

    private MpvOsdSurfacePolicy() {
    }

    static boolean requiresSurface(boolean subtitlesVisible,
                                   String primaryCurrent,
                                   String primarySelection,
                                   String secondaryCurrent,
                                   String secondarySelection) {
        if (!subtitlesVisible) return false;
        return isSelected(primaryCurrent)
                || isSelected(primarySelection)
                || isSelected(secondaryCurrent)
                || isSelected(secondarySelection);
    }

    static boolean needsCurrentTrackQuery(boolean subtitlesVisible,
                                          String primarySelection,
                                          String secondarySelection) {
        if (!subtitlesVisible) return false;
        return !isDisabled(primarySelection) || !isDisabled(secondarySelection);
    }

    static boolean shouldKeepSurface(boolean requestedNow, boolean usedForCurrentMedia) {
        return requestedNow || usedForCurrentMedia;
    }

    static boolean shouldDeferDestroyedSurfaceDetach(boolean requested,
                                                     boolean videoAttached) {
        return requested && videoAttached;
    }

    private static boolean isDisabled(String value) {
        if (value == null) return false;
        String normalized = value.trim();
        return "no".equalsIgnoreCase(normalized)
                || "none".equalsIgnoreCase(normalized);
    }

    private static boolean isSelected(String value) {
        if (value == null) return false;
        String normalized = value.trim();
        return !normalized.isEmpty()
                && !"no".equalsIgnoreCase(normalized)
                && !"none".equalsIgnoreCase(normalized)
                && !"auto".equalsIgnoreCase(normalized);
    }
}
