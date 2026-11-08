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

import java.util.Map;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放信息对话框（VideoActivity 在点“信息”时用它展示标题、请求头与播放地址）。
 * 兼容实现只保留调用点用到的链式接口，打开后为空白对话框，
 * 不展示信息、不触发分享回调。
 */
public class InfoDialog extends DialogFragment {

    /** 分享回调（VideoActivity 实现它）。 */
    public interface Listener {
        void onShare(CharSequence title);
    }

    private CharSequence title;
    private Map<String, String> headers;
    private String url;
    private Listener listener;

    public static InfoDialog create() {
        return new InfoDialog();
    }

    public InfoDialog title(CharSequence title) {
        this.title = title;
        return this;
    }

    public InfoDialog headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public InfoDialog url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "InfoDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
