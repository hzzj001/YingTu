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

import com.yingtu.plus.player.PlayerManager;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * LUT 面板对话框（非全屏时 VideoActivity 用它管理 LUT 预设；
 * 全屏时改用悬浮的 LutQuickPanel）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，不展示预设。
 */
public class LutPanelDialog extends DialogFragment {

    private PlayerManager player;

    public static LutPanelDialog create() {
        return new LutPanelDialog();
    }

    public LutPanelDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "LutPanelDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
