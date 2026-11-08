package com.yingtu.plus.player.engine;

import androidx.media3.common.Format;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 一次播放事实快照（exo / ijk / mpv 引擎在 {@link PlayerEngine#getPlaybackFactsSnapshot}
 * 里返回）：当前选中的音视频轨、分析器看到的音视频轨、解码器名、解码种类等，
 * 供 {@link com.yingtu.plus.player.PlaybackMediaFactsCoordinator} 汇总进播放上下文。
 * 兼容实现只做字段搬运，不做任何推断。
 */
public final class PlaybackFactsSnapshot {

    private final Format selectedVideo;
    private final Format selectedAudio;
    private final Format analyticsVideo;
    private final Format analyticsAudio;
    private final String videoDecoderName;
    private final String audioDecoderName;
    private final DecoderKind decoderKind;
    private final Object extra;
    private final String first;
    private final String second;
    private final Boolean tunneling;

    public PlaybackFactsSnapshot(Format selectedVideo, Format selectedAudio,
                                 Format analyticsVideo, Format analyticsAudio,
                                 String videoDecoderName, String audioDecoderName,
                                 DecoderKind decoderKind, Object extra,
                                 String first, String second, Boolean tunneling) {
        this.selectedVideo = selectedVideo;
        this.selectedAudio = selectedAudio;
        this.analyticsVideo = analyticsVideo;
        this.analyticsAudio = analyticsAudio;
        this.videoDecoderName = videoDecoderName == null ? "" : videoDecoderName;
        this.audioDecoderName = audioDecoderName == null ? "" : audioDecoderName;
        this.decoderKind = decoderKind == null ? DecoderKind.UNKNOWN : decoderKind;
        this.extra = extra;
        this.first = first == null ? "" : first;
        this.second = second == null ? "" : second;
        this.tunneling = tunneling;
    }

    public Format selectedVideo() {
        return selectedVideo;
    }

    public Format selectedAudio() {
        return selectedAudio;
    }

    public Format analyticsVideo() {
        return analyticsVideo;
    }

    public Format analyticsAudio() {
        return analyticsAudio;
    }

    public String videoDecoderName() {
        return videoDecoderName;
    }

    public String audioDecoderName() {
        return audioDecoderName;
    }

    public DecoderKind decoderKind() {
        return decoderKind;
    }

    public Boolean tunneling() {
        return tunneling;
    }
}
