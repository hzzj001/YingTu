package com.yingtu.plus.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.yingtu.plus.player.lyrics.LyricsLine;
import com.yingtu.plus.player.lyrics.LyricsResult;

import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 歌词悬浮视图（LyricsController / DesktopLyricsWindow 用它在播放页与桌面悬浮窗
 * 展示歌词）。兼容实现只保留调用点用到的接口，不渲染任何歌词。
 */
public class LyricsOverlayView extends View {

    private boolean desktopMode;

    public LyricsOverlayView(Context context) {
        super(context);
    }

    public LyricsOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** 是否为桌面悬浮窗模式（兼容实现：只记状态）。 */
    public void setDesktopMode(boolean desktopMode) {
        this.desktopMode = desktopMode;
    }

    public boolean isDesktopMode() {
        return desktopMode;
    }

    /** 按播放进度更新高亮行（兼容实现：不做任何事）。 */
    public void update(long positionMs) {
    }

    /** 按播放进度与播放状态更新高亮行（兼容实现：不做任何事）。 */
    public void update(long positionMs, boolean playing) {
    }

    /** 刷新歌词样式（兼容实现：不做任何事）。 */
    public void refreshStyle() {
    }

    /** 设置歌词（兼容实现：不做任何事）。 */
    public void setLyrics(LyricsResult result, List<LyricsLine> lines) {
    }

    /** 清空歌词（兼容实现：不做任何事）。 */
    public void clear() {
    }
}
