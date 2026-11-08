package com.yingtu.plus.utils;

import android.app.Activity;
import android.view.View;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 画中画（PiP）辅助：VideoActivity 在视频尺寸 / 播放状态变化时同步 PiP 参数，
 * 在切后台时请求进入画中画。兼容实现只保留调用点用到的接口，
 * 进入画中画一律返回 false（不进入），其余调用均为无操作。
 */
public class PiP {

    public PiP() {
    }

    /** 按视频 View 同步 PiP 参数（兼容实现：不做任何事）。 */
    public void update(Activity activity, View view) {
    }

    /** 按播放状态同步 PiP 参数（兼容实现：不做任何事）。 */
    public void update(Activity activity, boolean isPlaying) {
    }

    /** 按视频尺寸与缩放同步 PiP 参数（兼容实现：不做任何事）。 */
    public void update(Activity activity, int width, int height, float scale) {
    }

    /** 请求进入画中画；兼容实现返回 false（不进入）。 */
    public boolean enter(Activity activity, int width, int height, float scale) {
        return false;
    }

    /** 设置音频模式下的 PiP 行为（兼容实现：不做任何事）。 */
    public void setAudioMode(Activity activity, boolean audioMode) {
    }
}
