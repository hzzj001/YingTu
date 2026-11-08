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

import com.yingtu.plus.bean.Episode;

import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 选集网格对话框（VideoActivity 在网格模式下用它展示全部剧集）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，不展示剧集。
 */
public class EpisodeGridDialog extends DialogFragment {

    private boolean reverse;
    private List<Episode> episodes;

    public static EpisodeGridDialog create() {
        return new EpisodeGridDialog();
    }

    public EpisodeGridDialog reverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }

    public EpisodeGridDialog episodes(List<Episode> episodes) {
        this.episodes = episodes;
        return this;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "EpisodeGridDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
