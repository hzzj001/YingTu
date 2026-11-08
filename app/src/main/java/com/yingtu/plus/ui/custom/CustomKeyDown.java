package com.yingtu.plus.ui.custom;

import android.app.Activity;
import android.view.MotionEvent;

import androidx.media3.ui.PlayerView;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放页手势处理（VideoActivity 把播放器 View 的触摸事件交给它，
 * 由它识别单击 / 双击 / 滑动调亮度音量 / 快进快退 / 双指缩放等，
 * 再通过 {@link Listener} 回调给 Activity）。
 * 兼容实现只保留调用点用到的接口：手势一律不消费（onTouchEvent 返回 false），
 * 缩放恒为 1，不触发任何 Listener 回调。
 */
public final class CustomKeyDown {

    /** 手势回调（VideoActivity 实现它）。 */
    public interface Listener {
        void onScale(int tag);

        void onSpeedUp();

        void onSpeedEnd();

        void onBright(int progress);

        void onVolume(int progress);

        void onFlingUp();

        void onFlingDown();

        void onSeeking(long time);

        void onSeekEnd(long time);

        void onSingleTap();

        void onDoubleTap();

        void onTouchEnd();
    }

    private boolean locked;

    private CustomKeyDown() {
    }

    public static CustomKeyDown create(Activity activity, PlayerView playerView) {
        return new CustomKeyDown();
    }

    /** 处理触摸事件；兼容实现不消费，返回 false。 */
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    /** 锁定 / 解锁手势（兼容实现：只记状态）。 */
    public void setLock(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    /** 当前缩放倍数；兼容实现恒为 1。 */
    public float getScale() {
        return 1f;
    }

    /** 重置缩放（兼容实现：不做任何事）。 */
    public void resetScale() {
    }
}
