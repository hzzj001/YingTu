// 移植自上游 fish2018/webhtv app/src/main/java/com/fongmi/android/tv/web/WebHomeChrome.java @ HEAD 4e30ffa（2026-10-07），仅包名 com.fongmi.android.tv → com.yingtu.plus；逻辑未改。
package com.yingtu.plus.web;

import android.text.TextUtils;

import java.util.Locale;

public final class WebHomeChrome {

    public static final String NORMAL = "normal";
    public static final String EDGE = "edge";
    public static final String IMMERSIVE = "immersive";

    private WebHomeChrome() {
    }

    public static String normalize(String mode, String fallback) {
        String value = TextUtils.isEmpty(mode) ? fallback : mode.trim().toLowerCase(Locale.ROOT);
        if (NORMAL.equals(value) || EDGE.equals(value) || IMMERSIVE.equals(value)) return value;
        return TextUtils.isEmpty(fallback) ? NORMAL : fallback;
    }

    public static boolean hidesNativeChrome(String mode) {
        return EDGE.equals(mode) || IMMERSIVE.equals(mode);
    }

    public static boolean hidesSystemBars(String mode) {
        return IMMERSIVE.equals(mode);
    }
}
