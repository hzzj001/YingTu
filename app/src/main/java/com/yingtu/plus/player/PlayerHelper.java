package com.yingtu.plus.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放页用到的几个小工具（字幕 mime 推断、格式描述、默认 UA、Header 转 Bundle、系统分享/选择器、外播结果回填）。
 * 调用点只要求签名对得上、返回安全默认值；不实现真实的分享/外播逻辑。
 */
public final class PlayerHelper {

    private PlayerHelper() {
    }

    /** 按字幕文件名后缀推断 mime；未知返回空串。 */
    public static String getSubtitleMimeType(String name) {
        if (TextUtils.isEmpty(name)) return "";
        String lower = name.toLowerCase(java.util.Locale.US);
        if (lower.endsWith(".vtt")) return "text/vtt";
        if (lower.endsWith(".srt")) return "application/x-subrip";
        if (lower.endsWith(".ass") || lower.endsWith(".ssa")) return "text/x-ssa";
        if (lower.endsWith(".ttml") || lower.endsWith(".xml")) return "application/ttml+xml";
        return "";
    }

    /** 格式描述（日志/比对用）；兼容实现直接返回原值。 */
    public static String describeFormat(String format) {
        return format == null ? "" : format;
    }

    public static String getDefaultUa() {
        return "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36";
    }

    /** 请求头转 Media3 RequestMetadata 用的 Bundle；空入参返回空 Bundle。 */
    public static Bundle toBundle(Map<String, String> headers) {
        Bundle bundle = new Bundle();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey() != null) bundle.putString(entry.getKey(), entry.getValue());
            }
        }
        return bundle;
    }

    /** 弹出系统分享/打开方式选择器（兼容实现：不做任何事）。 */
    public static void choose(Activity activity, String url, Map<String, String> headers, boolean isVod, long position, CharSequence title) {
    }

    /** 系统分享当前播放地址（兼容实现：不做任何事）。 */
    public static void share(Activity activity, String url, Map<String, String> headers, CharSequence title) {
    }

    /**
     * 外播选择器返回后的回填（兼容实现：不做任何事）。
     *
     * @param data         选择器返回的 Intent（可能为 null）
     * @param dispatchNext 选中“下一个”时的回调
     * @param seekTo       回填播放进度时的回调（单位毫秒）
     */
    public static void onExternalResult(Intent data, Runnable dispatchNext, Consumer<Long> seekTo) {
    }
}
