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

import com.yingtu.plus.event.CastEvent;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 投屏接收对话框（收到投屏事件时弹出，确认是否接播）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，不展示事件、不接播。
 */
public class ReceiveDialog extends DialogFragment {

    private CastEvent event;

    public static ReceiveDialog create() {
        return new ReceiveDialog();
    }

    public ReceiveDialog event(CastEvent event) {
        this.event = event;
        return this;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "ReceiveDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
