package com.yingtu.plus.setting;

import com.yingtu.plus.player.PlaybackProfileAbCoordinator;
import com.yingtu.plus.player.PlaybackProfileAbPolicy;
import com.yingtu.plus.util.Prefers;

public class PlaybackLightweightAssessmentSetting {
    public static boolean isEnrolled() { return Prefers.getBoolean("ab_enrolled", false); }

    /** 轻量评估共用 PlaybackProfileAbCoordinator 单例（PlayerManager 要求该类型）。 */
    public static PlaybackProfileAbCoordinator coordinator() { return PlaybackProfileAbCoordinator.process(); }

    /** 与 PlaybackProfileAbSetting.getEnrollmentResolution() 同形（PlayerManager 要求该类型）。 */
    public static PlaybackProfileAbPolicy.EnrollmentResolution getEnrollmentResolution() {
        return new PlaybackProfileAbPolicy.EnrollmentResolution(isEnrolled(),
                new PlaybackProfileAbPolicy.Enrollment(""));
    }
}
