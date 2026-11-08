package com.yingtu.plus.ui.custom;

import android.view.View;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 拖动处理（VideoActivity 把可拖动的 View 交给它绑定拖动行为）。
 * 兼容实现为空壳：bind() 不做任何事。
 */
public final class CustomMovement {

    private CustomMovement() {
    }

    /** 给指定 View 绑定拖动行为（兼容实现：不做任何事）。 */
    public static void bind(View view) {
    }
}
