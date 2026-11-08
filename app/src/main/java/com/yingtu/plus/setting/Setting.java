package com.yingtu.plus.setting;

import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.github.catvod.utils.Trans;
import com.yingtu.plus.util.Prefers;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class Setting {
    // Constants
    public static final int LANGUAGE_FOLLOW_SYSTEM = 0;
    public static final int LANGUAGE_SIMPLIFIED = 1;
    public static final int LANGUAGE_TRADITIONAL = 2;
    private static final int[] LANGUAGE_OPTIONS = {LANGUAGE_FOLLOW_SYSTEM, LANGUAGE_SIMPLIFIED, LANGUAGE_TRADITIONAL};
    public static final int CSP_WARMUP_DISABLED = 0;
    public static final int CSP_WARMUP_DEFAULT = 1;
    public static final int CSP_WARMUP_CUSTOM = 2;
    public static final int UI_SCALE_FOLLOW_SYSTEM = 0;
    public static final int UI_SCALE_STANDARD = 1;
    public static final int UI_SCALE_COMPACT = 2;
    public static final int UI_SCALE_SMALLER = 3;
    public static final int UI_SCALE_MILD_COMPACT = 4;
    public static final int UI_SCALE_MORE_COMPACT = 5;
    private static final int[] UI_SCALE_OPTIONS = {UI_SCALE_FOLLOW_SYSTEM, UI_SCALE_STANDARD, UI_SCALE_MILD_COMPACT, UI_SCALE_COMPACT, UI_SCALE_MORE_COMPACT, UI_SCALE_SMALLER};
    
    public static void putWall(int wall) { Prefers.put("wall", wall); }
    public static void putWallType(int type) { Prefers.put("wall_type", type); }
    public static int getCspWarmupMode() { return Prefers.getInt("csp_warmup_mode", CSP_WARMUP_DEFAULT); }
    public static List<String> getCspWarmupSites() { return new ArrayList<>(); }
    public static boolean isCspWarmup() { return getCspWarmupMode() != CSP_WARMUP_DISABLED; }
    public static boolean isCompactEpisodeTitle() { return Prefers.getBoolean("compact_episode_title", false); }
    public static void putCompactEpisodeTitle(boolean v) { Prefers.put("compact_episode_title", v); }
    public static String getUa() { return Prefers.getString("ua", ""); }
    public static boolean isAdblock() { return Prefers.getBoolean("adblock", false); }
    public static boolean isIncognito() { return Prefers.getBoolean("incognito", false); }
    public static boolean isPlaybackArtworkWall() { return Prefers.getBoolean("playback_artwork_wall", false); }
    public static boolean hasFileAccess() { return true; }
    public static boolean hasFileManager() { return true; }
    
    public static Object create() { return null; }
    public static void putReset(String key) { Prefers.remove(key); }

    // ---- 以下方法移植自上游 webhtv Setting（供 ui.base.BaseActivity 用：wrapDisplay / getDynamicColor） ----
    public static int getLanguage() {
        int language = Prefers.getInt("language", LANGUAGE_FOLLOW_SYSTEM);
        return isLanguage(language) ? language : LANGUAGE_FOLLOW_SYSTEM;
    }

    public static void putLanguage(int language) {
        int value = isLanguage(language) ? language : LANGUAGE_FOLLOW_SYSTEM;
        Prefers.put("language", value);
        applyLanguage(value);
    }

    public static void applyLanguage() {
        applyLanguage(getLanguage());
    }

    public static int getLanguageIndex() {
        int language = getLanguage();
        for (int i = 0; i < LANGUAGE_OPTIONS.length; i++) if (LANGUAGE_OPTIONS[i] == language) return i;
        return LANGUAGE_FOLLOW_SYSTEM;
    }

    public static void putLanguageIndex(int index) {
        putLanguage(index >= 0 && index < LANGUAGE_OPTIONS.length ? LANGUAGE_OPTIONS[index] : LANGUAGE_FOLLOW_SYSTEM);
    }

    private static boolean isLanguage(int language) {
        for (int option : LANGUAGE_OPTIONS) if (option == language) return true;
        return false;
    }

    private static void applyLanguage(int language) {
        if (language == LANGUAGE_SIMPLIFIED) Trans.setTraditional(false);
        else if (language == LANGUAGE_TRADITIONAL) Trans.setTraditional(true);
        else Trans.setTraditional(null);
    }

    public static Context wrapDisplay(Context context) {
        return wrapUiScale(wrapLanguage(context));
    }

    public static Context wrapLanguage(Context context) {
        int language = getLanguage();
        applyLanguage(language);
        if (language == LANGUAGE_FOLLOW_SYSTEM) return context;
        Locale locale = language == LANGUAGE_TRADITIONAL ? Locale.TRADITIONAL_CHINESE : Locale.SIMPLIFIED_CHINESE;
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        config.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(config);
    }

    public static int getUiScale() {
        int scale = Prefers.getInt("ui_scale", UI_SCALE_FOLLOW_SYSTEM);
        return isUiScale(scale) ? scale : UI_SCALE_FOLLOW_SYSTEM;
    }

    public static void putUiScale(int scale) {
        Prefers.put("ui_scale", isUiScale(scale) ? scale : UI_SCALE_FOLLOW_SYSTEM);
    }

    public static int getUiScaleIndex() {
        int scale = getUiScale();
        for (int i = 0; i < UI_SCALE_OPTIONS.length; i++) if (UI_SCALE_OPTIONS[i] == scale) return i;
        return UI_SCALE_FOLLOW_SYSTEM;
    }

    public static void putUiScaleIndex(int index) {
        putUiScale(index >= 0 && index < UI_SCALE_OPTIONS.length ? UI_SCALE_OPTIONS[index] : UI_SCALE_FOLLOW_SYSTEM);
    }

    private static boolean isUiScale(int scale) {
        for (int option : UI_SCALE_OPTIONS) if (option == scale) return true;
        return false;
    }

    public static Context wrapUiScale(Context context) {
        int scale = getUiScale();
        if (scale == UI_SCALE_FOLLOW_SYSTEM) return context;
        float factor = getUiScaleFactor(scale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int stableDensity = DisplayMetrics.DENSITY_DEVICE_STABLE > 0 ? DisplayMetrics.DENSITY_DEVICE_STABLE : metrics.densityDpi;
        int densityDpi = Math.max(DisplayMetrics.DENSITY_LOW, Math.round(stableDensity * factor));
        config.densityDpi = densityDpi;
        config.fontScale = 1.0f;
        config.screenWidthDp = pxToDp(metrics.widthPixels, densityDpi);
        config.screenHeightDp = pxToDp(metrics.heightPixels, densityDpi);
        config.smallestScreenWidthDp = Math.min(config.screenWidthDp, config.screenHeightDp);
        return context.createConfigurationContext(config);
    }

    private static float getUiScaleFactor(int scale) {
        return switch (scale) {
            case UI_SCALE_STANDARD -> 0.8f;
            case UI_SCALE_MILD_COMPACT -> 0.75f;
            case UI_SCALE_COMPACT -> 0.7f;
            case UI_SCALE_MORE_COMPACT -> 0.65f;
            case UI_SCALE_SMALLER -> 0.6f;
            default -> 1.0f;
        };
    }

    private static int pxToDp(int px, int densityDpi) {
        return Math.max(1, Math.round(px * (float) DisplayMetrics.DENSITY_DEFAULT / densityDpi));
    }

    public static int getDynamicColor() {
        int color = getThemeColor();
        if (color == -1) return 0;
        return color != 0 ? color : getWallColor();
    }

    public static int getThemeColor() {
        return Prefers.getInt("theme_color", -1);
    }

    public static void putThemeColor(int color) {
        Prefers.put("theme_color", color);
    }

    public static int getWallColor() {
        return Prefers.getInt("wall_color", 0);
    }

    public static void putWallColor(int color) {
        Prefers.put("wall_color", color);
    }

    // ---- 以下方法移植自上游 webhtv Setting（HEAD 4e30ffa），供 VodFragment / HomeWebController / WebHomeChromeStartup / WebHomeExtensionRegistry / SiteAdapter / SiteHealthStore 用 ----
    public static boolean isWebHomeFullscreen() { return Prefers.getBoolean("web_home_fullscreen", true); }
    public static void putWebHomeFullscreen(boolean fullscreen) { Prefers.put("web_home_fullscreen", fullscreen); }
    public static boolean isWebHomeExtension() { return Prefers.getBoolean("web_home_extension", true); }
    public static void putWebHomeExtension(boolean extension) { Prefers.put("web_home_extension", extension); }
    public static int getSiteColumn() { return Prefers.getInt("site_column", 1); }
    public static void putSiteColumn(int column) { Prefers.put("site_column", column); }
    public static int getSyncMode() { return Prefers.getInt("sync_mode"); }
    public static void putSyncMode(int mode) { Prefers.put("sync_mode", mode); }
    public static boolean isSiteHealthSort() { return Prefers.getBoolean("site_health_sort", true); }
    public static void putSiteHealthSort(boolean sort) { Prefers.put("site_health_sort", sort); }
    public static boolean isSiteHealthDialogSort() { return Prefers.getBoolean("site_health_dialog_sort"); }
    public static void putSiteHealthDialogSort(boolean sort) { Prefers.put("site_health_dialog_sort", sort); }
    // ---- 上游移植方法结束 ----
    // ---- 上游移植方法结束 ----
    public static void putLoginStateFindings(Object findings) {}
    public static void putLoginStatePaths(Object paths) {}
    public static void putLoginStatePendingPaths(Object paths) {}
    public static void putLoginStateSnapshot(Object snapshot) {}
    public static List<Object> getLoginStateFindings() { return new ArrayList<>(); }
    public static List<Object> getLoginStatePaths() { return new ArrayList<>(); }
    public static List<Object> getLoginStatePendingPaths() { return new ArrayList<>(); }
    public static Object getLoginStateSnapshot() { return null; }
    // 上游 Setting:308-314：同步路径以换行分隔的 String 存 Prefers（键 sync_paths，默认 "TV\nTVBox\nTVData"）；
    // 原桩返回 List<String>，与 SyncOptions/SyncFiles/LoginStateSync/OneKeySyncDialog/SyncPathDialog 的 String 用法不符，已按上游修正。
    public static String getSyncPaths() { return Prefers.getString("sync_paths", "TV\nTVBox\nTVData"); }
    public static void putSyncPaths(String paths) { Prefers.put("sync_paths", paths); }
}
