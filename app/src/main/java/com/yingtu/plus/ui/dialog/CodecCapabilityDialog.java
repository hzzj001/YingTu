package com.yingtu.plus.ui.dialog;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yingtu.plus.player.PlayerManager;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 编解码能力对话框（VideoActivity 在点编解码能力时用它展示当前设备的编解码支持情况）。
 * 兼容实现只保留调用点用到的静态入口，打开后为空白对话框，不展示能力信息。
 */
public class CodecCapabilityDialog extends DialogFragment {

    private PlayerManager player;

    public static void show(Activity activity, PlayerManager player) {
        if (!(activity instanceof AppCompatActivity)) return;
        CodecCapabilityDialog dialog = new CodecCapabilityDialog();
        dialog.player = player;
        dialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), "CodecCapabilityDialog");
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable android.os.Bundle savedInstanceState) {
        return new android.view.View(requireContext());
    }
}
