package com.yingtu.plus.ui.custom;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 列表项间距装饰（VideoActivity 的线路 / 选集 / 清晰度 / 解析列表用它留白）。
 * 两个构造器：单间距，或“列数 + 间距”。
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount;
    private final int space;

    public SpaceItemDecoration(int space) {
        this(1, space);
    }

    public SpaceItemDecoration(int spanCount, int space) {
        this.spanCount = Math.max(1, spanCount);
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(space, space, space, space);
    }
}
