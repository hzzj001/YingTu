package com.yingtu.plus.player;

import com.yingtu.plus.player.PlaybackAutoContext.Kernel;
import com.yingtu.plus.player.PlaybackExperimentPolicy.State;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放画像 A/B 策略：按当前性能画像决定本次播放落到哪个实验臂，
 * 以及实验门控是否放行。兼容实现默认不入组、不放行实验臂
 * （armForProfile 返回 null，gateAllows 返回 false），
 * 即画像评估不改变任何播放行为。
 */
public final class PlaybackProfileAbPolicy {

    /** 实验臂。 */
    public enum Arm {
        UNKNOWN
    }

    /** 入组结论。 */
    public static final class EnrollmentResolution {
        private final boolean active;
        private final Enrollment enrollment;

        public EnrollmentResolution(boolean active, Enrollment enrollment) {
            this.active = active;
            this.enrollment = enrollment;
        }

        public boolean active() {
            return active;
        }

        public Enrollment enrollment() {
            return enrollment;
        }
    }

    /** 入组信息（设备摘要等）。 */
    public static final class Enrollment {
        private final String deviceDigest;

        public Enrollment(String deviceDigest) {
            this.deviceDigest = deviceDigest == null ? "" : deviceDigest;
        }

        public String deviceDigest() {
            return deviceDigest;
        }
    }

    private PlaybackProfileAbPolicy() {
    }

    /** 按性能画像取实验臂（兼容实现：不入组，返回 null）。 */
    public static Arm armForProfile(Object profile) {
        return null;
    }

    /** 实验门控是否放行（兼容实现：不放行）。 */
    public static boolean gateAllows(State state, Kernel kernel) {
        return false;
    }
}
