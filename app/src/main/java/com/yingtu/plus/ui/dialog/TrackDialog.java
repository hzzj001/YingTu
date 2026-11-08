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
 * 音轨 / 字幕选择对话框（VideoActivity 在点音轨、字幕或轨道面板时用它切换轨道，
 * type 区分音轨 / 字幕 / 视频轨）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，不展示轨道、不切换轨道。
 */
public class TrackDialog extends DialogFragment {

    /** 轨道面板回调（VideoActivity 实现它）。 */
    public interface Listener {
        void onTrackPanel(int type);

        // 第 21 轮：上游 TrackDialog.Listener 只声明 onSubtitleClick；本地 VideoActivity 已实现该方法，补声明只为编译。
        void onSubtitleClick();
    }

    private int type;
    private PlayerManager player;
    private Listener listener;

    public static TrackDialog create() {
        return new TrackDialog();
    }

    public TrackDialog type(int type) {
        this.type = type;
        return this;
    }

    public TrackDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "TrackDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
