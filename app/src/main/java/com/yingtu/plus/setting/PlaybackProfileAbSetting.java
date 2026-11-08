package com.yingtu.plus.setting;

import com.yingtu.plus.player.PlaybackProfileAbPolicy;

public class PlaybackProfileAbSetting {
    public static boolean isEnrolled() { return PlaybackLightweightAssessmentSetting.isEnrolled(); }

    /**
     * 返回入组结论。注意：PlayerManager 要求返回
     * PlaybackProfileAbPolicy.EnrollmentResolution（要调 active()/enrollment().deviceDigest()），
     * 此前这里返回 int，与调用点对不上，故改为返回 EnrollmentResolution；
     * 入组与否仍以原有 isEnrolled() 开关为准，未入组时 deviceDigest 为空串。
     */
    public static PlaybackProfileAbPolicy.EnrollmentResolution getEnrollmentResolution() {
        return new PlaybackProfileAbPolicy.EnrollmentResolution(isEnrolled(),
                new PlaybackProfileAbPolicy.Enrollment(""));
    }
}
