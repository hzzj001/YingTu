package com.yingtu.plus.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.media3.ui.PlayerView;

import com.yingtu.plus.player.PlayerManager;
import com.yingtu.plus.player.lut.LutPreset;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * LUT 快捷面板（全屏播放时浮在播放器上的 LUT 快速切换条，
 * VideoActivity 通过它切换 / 导入 LUT）。
 * 兼容实现只保留调用点用到的接口：切换与导入均为无操作，
 * 面板恒为隐藏（hideIfVisible 返回 false）。
 */
public class LutQuickPanel extends View {

    /** LUT 导入回调（VideoActivity 实现它处理导入与选目录）。 */
    public interface ImportCallback {
        void onImportLut();

        void onSelectLutDir();
    }

    public LutQuickPanel(Context context) {
        super(context);
    }

    public LutQuickPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** 切换面板显隐（兼容实现：不做任何事）。 */
    public void toggle(PlayerManager player, PlayerView exo, Runnable onChanged, ImportCallback callback) {
    }

    /** 选中已导入的 LUT 预设并应用（兼容实现：不做任何事）。 */
    public void selectImported(LutPreset preset, PlayerManager player, PlayerView exo, Runnable onChanged) {
    }

    /** 刷新 LUT 列表（兼容实现：不做任何事）。 */
    public void refreshList() {
    }

    /** 若面板可见则隐藏并返回 true；兼容实现恒为隐藏，返回 false。 */
    public boolean hideIfVisible() {
        return false;
    }
}
