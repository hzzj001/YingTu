package com.yingtu.plus.player;

import java.util.Locale;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 首播完成判定策略：按“播放器就绪 / 首帧已提交 / 有视频 / 有音频”
 * 判定本次启动完成到哪一步（首帧或音频可播）。
 * 兼容实现只做直接映射：就绪且有视频 → 首帧；就绪且只有音频 → 音频可播；其余未知。
 */
public final class PlaybackStartupPolicy {

    public enum Completion implements PlaybackAutoContext.Labeled {
        AUDIO_PLAYABLE, FIRST_FRAME, UNKNOWN;

        @Override
        public String label() {
            return name().toLowerCase(Locale.US);
        }
    }

    private PlaybackStartupPolicy() {
    }

    public static Completion resolve(boolean ready, boolean videoSubmitted, boolean hasVideo, boolean hasAudio) {
        if (!ready) return Completion.UNKNOWN;
        if (hasVideo && videoSubmitted) return Completion.FIRST_FRAME;
        if (hasAudio) return Completion.AUDIO_PLAYABLE;
        return Completion.UNKNOWN;
    }
}
