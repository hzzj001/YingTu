package com.yingtu.plus.ui.custom;

import android.view.View;

import com.yingtu.plus.player.PlayerManager;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放器 OSD（屏幕显示）控制器：VideoActivity 把 OSD 的 7 个区域 View
 * 与数据源交给它，由它控制 OSD 显隐、诊断信息开关与生命周期。
 * 兼容实现只做显隐切换与状态记录，不做任何 OSD 内容刷新。
 */
public final class PlayerOsdController {

    /** OSD 数据源（VideoActivity 实现它提供播放器与标题）。 */
    public interface Source {
        PlayerManager getPlayer();

        String getTitle();
    }

    private final View root;
    private final View topLeft;
    private final View topRight;
    private final View bottomLeft;
    private final View bottomRight;
    private final View diagnostics;
    private final View miniProgress;
    private final Source source;
    private final float textSize;
    private boolean diagnosticsVisible;

    public PlayerOsdController(View root, View topLeft, View topRight, View bottomLeft, View bottomRight,
                               View diagnostics, View miniProgress, Source source, float textSize) {
        this.root = root;
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.diagnostics = diagnostics;
        this.miniProgress = miniProgress;
        this.source = source;
        this.textSize = textSize;
    }

    public boolean isDiagnosticsVisible() {
        return diagnosticsVisible;
    }

    /** 显隐 OSD 控制区（兼容实现：只切 View 可见性）。 */
    public void setControlsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (root != null) root.setVisibility(visibility);
        if (topLeft != null) topLeft.setVisibility(visibility);
        if (topRight != null) topRight.setVisibility(visibility);
        if (bottomLeft != null) bottomLeft.setVisibility(visibility);
        if (bottomRight != null) bottomRight.setVisibility(visibility);
    }

    public void toggleDiagnostics() {
        setDiagnosticsVisible(!diagnosticsVisible);
    }

    public void setDiagnosticsVisible(boolean visible) {
        diagnosticsVisible = visible;
        if (diagnostics != null) diagnostics.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void start() {
    }

    public void stop() {
    }

    public void release() {
    }
}
