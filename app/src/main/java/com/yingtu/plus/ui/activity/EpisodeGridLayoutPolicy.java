/* 移植自上游 webhtv (https://github.com/fish2018/webhtv, HEAD 4e30ffa)：包名 com.fongmi.android.tv -> com.yingtu.plus，逻辑未改。 */
package com.yingtu.plus.ui.activity;

final class EpisodeGridLayoutPolicy {

    private EpisodeGridLayoutPolicy() {
    }

    static int getMaxSpan(boolean landscapeLayout, boolean pad) {
        return landscapeLayout || pad ? 6 : 4;
    }

    static int getAvailableWidth(int measuredWidth, int screenWidth, int screenHeight, int fallbackInset, boolean landscapeLayout, boolean landscapeConfiguration) {
        if (measuredWidth > 0 && landscapeLayout == landscapeConfiguration) return measuredWidth;
        int layoutWidth = landscapeLayout ? Math.max(screenWidth, screenHeight) : Math.min(screenWidth, screenHeight);
        return Math.max(1, layoutWidth - Math.max(0, fallbackInset));
    }
}
