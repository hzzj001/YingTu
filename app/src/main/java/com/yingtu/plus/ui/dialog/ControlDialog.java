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

import com.yingtu.plus.bean.History;
import com.yingtu.plus.bean.Parse;
import com.yingtu.plus.databinding.ActivityVideoBinding;
import com.yingtu.plus.player.PlayerManager;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放控制对话框（VideoActivity 的控制中心：线路 / 选集 / 解析 / 倍速 /
 * 音轨字幕 / 弹幕 / 编解码 / 播放参数 / LUT 等入口）。
 * 兼容实现只保留调用点用到的链式接口与 setPlayer()；
 * 打开后为空白对话框，各入口按钮不做任何事。
 * Listener 的方法与 VideoActivity 里以 @Override 定义的方法一一对应
 * （面板类回调按调用点归属：控制对话框负责打开各子面板）。
 */
public class ControlDialog extends DialogFragment {

    /** 控制对话框回调（VideoActivity 实现它）。 */
    public interface Listener {
        ActivityVideoBinding getControlBinding();

        PlayerManager getControlPlayer();

        History getControlHistory();

        boolean isControlParseEnabled();

        boolean isControlAudioContent();

        boolean isDanmakuFullscreen();

        void onRepeatModeChanged(int repeatMode);

        void onParse(Parse item);

        void onEpisodeColumn(int column);

        void onCompactEpisodeTitleChanged();

        void onTrackPanel(int type);

        void onTitlePanel();

        void onDanmakuPanel();

        void onCodecCapabilityPanel();

        void onPlayParamsPanel();

        void onLutPanel();

        void onImmersiveAudioModeChanged();

        void onKaraokeModeChanged();

        void onKaraokeTrackPanel();
    }

    private ActivityVideoBinding parent;
    private History history;
    private boolean parse;
    private PlayerManager player;
    private Listener listener;

    public static ControlDialog create() {
        return new ControlDialog();
    }

    public ControlDialog parent(ActivityVideoBinding parent) {
        this.parent = parent;
        return this;
    }

    public ControlDialog history(History history) {
        this.history = history;
        return this;
    }

    public ControlDialog parse(boolean parse) {
        this.parse = parse;
        return this;
    }

    public ControlDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    /** 刷新当前播放器（VideoActivity 在播放器重建后调用；兼容实现：不做任何事）。 */
    public void setPlayer() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "ControlDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
