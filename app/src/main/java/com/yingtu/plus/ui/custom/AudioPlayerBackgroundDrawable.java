package com.yingtu.plus.ui.custom;

import android.graphics.drawable.GradientDrawable;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 纯音频播放时的背景 Drawable（VideoActivity 把它设为音频舞台的背景，
 * 并按唱片封面位置同步光晕锚点）。兼容实现只保留调用点用到的接口：
 * 构造参数仅保存，不做任何渐变 / 光晕绘制。
 */
public class AudioPlayerBackgroundDrawable extends GradientDrawable {

    public AudioPlayerBackgroundDrawable(int background, int artworkColor, boolean decorated,
                                         boolean lightEffect, boolean animated, int seed, int decorationSeed) {
        super();
        setColor(artworkColor);
    }

    /** 设置唱片光晕锚点（兼容实现：不做任何事）。 */
    public void setRecordHaloAnchor(float cx, float cy, float radius) {
    }
}
