package com.yingtu.plus.ui.dialog;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Consumer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放器内核切换对话框（VideoActivity 在点切换内核时用它在 exo / ijk / mpv 间选择，
 * 选中后通过回调把新内核类型交回 Activity）。
 * 兼容实现只保留调用点用到的静态入口，打开后为空白对话框，不展示内核选项、不触发回调。
 */
public class PlayerKernelDialog extends DialogFragment {

    public static void show(Activity activity, int playerType, Consumer<Integer> onSelected) {
        if (!(activity instanceof AppCompatActivity)) return;
        PlayerKernelDialog dialog = new PlayerKernelDialog();
        dialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), "PlayerKernelDialog");
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable android.os.Bundle savedInstanceState) {
        return new android.view.View(requireContext());
    }
}
