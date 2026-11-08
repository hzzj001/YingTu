package com.yingtu.plus.setting;

import com.yingtu.plus.player.PlaybackExperimentPolicy;
import com.yingtu.plus.util.Prefers;

public class PlaybackExperimentSetting {
    private static final String KEY_SCHEMA = "playback_experiment_schema";
    private static final String KEY_ENABLED = "playback_experiment_enabled";
    private static final int CURRENT_SCHEMA = 1;

    public static void ensureInitialized() {
        if (!hasState()) putState(true, false, false);
    }

    /**
     * 返回当前实验策略快照。
     * 注意：PlayerManager 两处调用点（实验策略变更处理、Profile AB 门控）都要求返回
     * PlaybackExperimentPolicy.State（要调 strategyId()/allows()），此前这里返回 boolean，
     * 与调用点对不上，故改为返回 State；是否启用仍以原有 Prefers 开关为准。
     */
    public static PlaybackExperimentPolicy.State getState() {
        return PlaybackExperimentPolicy.defaultState(isEnabled());
    }
    public static boolean isAllowed(String action) { return true; }

    private static boolean hasState() {
        return Prefers.getString(KEY_SCHEMA, null) != null;
    }

    public static void putState(boolean enabled, boolean exoEnabled, boolean mpvEnabled) {
        Prefers.put(KEY_SCHEMA, CURRENT_SCHEMA);
        Prefers.put(KEY_ENABLED, enabled);
    }

    public static boolean isEnabled() { return Prefers.getBoolean(KEY_ENABLED, false); }
}
