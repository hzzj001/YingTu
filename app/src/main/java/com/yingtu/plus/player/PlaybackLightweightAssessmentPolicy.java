package com.yingtu.plus.player;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 轻量画像评估策略：与 {@link PlaybackProfileAbPolicy} 共用实验臂类型，
 * 按性能画像决定轻量评估落到哪个臂。兼容实现默认不入组（返回 null），
 * 即轻量评估不改变任何播放行为。
 */
public final class PlaybackLightweightAssessmentPolicy {

    private PlaybackLightweightAssessmentPolicy() {
    }

    /** 按性能画像取实验臂（兼容实现：不入组，返回 null）。 */
    public static PlaybackProfileAbPolicy.Arm armForProfile(Object profile) {
        return null;
    }
}
