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

import com.yingtu.plus.player.PlayerManager;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 弹幕设置对话框（VideoActivity 在点弹幕时用它调弹幕开关、样式与弹幕源）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，不展示设置项。
 */
public class DanmakuDialog extends DialogFragment {

    /** 弹幕宿主回调（VideoActivity 实现它）。 */
    public interface Host {
        void onDanmakuPanel();
    }

    private PlayerManager player;
    private Host host;

    public static DanmakuDialog create() {
        return new DanmakuDialog();
    }

    public DanmakuDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Host) host = (Host) context;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "DanmakuDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
