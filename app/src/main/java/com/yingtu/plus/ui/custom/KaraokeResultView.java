package com.yingtu.plus.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.yingtu.plus.player.karaoke.KaraokeResult;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * K 歌评分结果视图（VideoActivity 把它放进 MaterialAlertDialog 展示评分结果）。
 * 兼容实现只保留调用点用到的接口：setResult 存结果、setAction 存回调，
 * 不渲染任何评分内容。
 */
public class KaraokeResultView extends LinearLayout {

    private KaraokeResult result;
    private Runnable action;

    public KaraokeResultView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public KaraokeResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    /** 设置评分结果（返回自身供链式调用）。 */
    public KaraokeResultView setResult(KaraokeResult result) {
        this.result = result;
        return this;
    }

    public KaraokeResult getResult() {
        return result;
    }

    /** 设置点击结果视图后的动作。 */
    public void setAction(Runnable action) {
        this.action = action;
    }

    /** 对话框宽度建议（兼容实现：返回 WRAP_CONTENT 语义的 0，由系统自适应）。 */
    public int getPreferredDialogWidth() {
        return android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    /** 请求动作按钮焦点；兼容实现返回 false。 */
    public boolean requestActionFocus() {
        return false;
    }
}
