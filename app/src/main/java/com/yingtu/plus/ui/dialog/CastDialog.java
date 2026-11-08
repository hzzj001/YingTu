package com.yingtu.plus.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.yingtu.plus.bean.CastVideo;
import com.yingtu.plus.bean.History;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 投屏对话框（VideoActivity 在点投屏时用它选择投屏设备）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，
 * 不展示设备、不触发投屏回调。
 */
public class CastDialog extends DialogFragment {

    /** 投屏完成回调（VideoActivity 实现它）。 */
    public interface Listener {
        void onCasted();
    }

    private History history;
    private CastVideo video;
    private boolean fm;
    private Listener listener;

    public static CastDialog create() {
        return new CastDialog();
    }

    public CastDialog history(History history) {
        this.history = history;
        return this;
    }

    public CastDialog video(CastVideo video) {
        this.video = video;
        return this;
    }

    public CastDialog fm(boolean fm) {
        this.fm = fm;
        return this;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "CastDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
