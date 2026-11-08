// 移植自上游 fish2018/webhtv（HEAD 4e30ffa）：app/src/main/java/androidx/media3/mpvplayer/MpvAudioDecoderPolicy.java，仅包名与引用改名。
package androidx.media3.mpvplayer;

import java.util.List;

final class MpvAudioDecoderPolicy {

    private static final List<String> HARDWARE_FIRST_DECODERS = List.of(
            "aac_mediacodec",
            "mp3_mediacodec",
            "amrnb_mediacodec",
            "amrwb_mediacodec");

    private MpvAudioDecoderPolicy() {
    }

    static String hardwareFirstDecoderList() {
        return String.join(",", HARDWARE_FIRST_DECODERS);
    }
}
