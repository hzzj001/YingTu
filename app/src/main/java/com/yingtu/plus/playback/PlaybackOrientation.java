package com.yingtu.plus.playback;

import android.content.pm.ActivityInfo;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放页方向策略：VideoActivity 在旋转、全屏进出、视频尺寸变化、
 * 自动旋转开关变化时问它“该请求哪个方向”。兼容实现直接映射到
 * ActivityInfo 的标准方向常量，不做任何设备姿态判断。
 */
public final class PlaybackOrientation {

    private PlaybackOrientation() {
    }

    /** 手动旋转按钮：横竖切换。 */
    public static int getRotateOrientation(boolean isRotate) {
        return isRotate ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    /** 进入全屏：竖屏视频保持竖屏（sensor），横屏视频锁定横屏。 */
    public static int getEnterFullscreenOrientation(boolean isPortrait) {
        return isPortrait ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    }

    /** 退出全屏：回到跟随系统方向。 */
    public static int getExitFullscreenOrientation(boolean isPort) {
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /** 竖屏下自动旋转：跟随传感器竖屏。 */
    public static int getPortAutoRotateOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
    }

    /** 横屏下自动旋转：跟随传感器横屏。 */
    public static int getLandAutoRotateOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    }

    /** 竖屏尺寸的视频：锁定竖屏。 */
    public static int getPortraitVideoSizeOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    /** 横屏尺寸的视频：锁定横屏。 */
    public static int getLandscapeVideoSizeOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }
}
