package com.yingtu.plus.ui.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 视频简介对话框（VideoActivity 在点简介时用它展示完整简介文本）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，不展示文本。
 */
public class VideoContentDialog extends DialogFragment {

    private CharSequence content;

    public static VideoContentDialog create() {
        return new VideoContentDialog();
    }

    public VideoContentDialog content(CharSequence content) {
        this.content = content;
        return this;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "VideoContentDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
