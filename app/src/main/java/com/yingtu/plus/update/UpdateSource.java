// 移植自上游 fish2018/webhtv (commit 4e30ffa)：app/src/main/java/com/fongmi/android/tv/update/UpdateSource.java，包名已改写为 com.yingtu.plus。
package com.yingtu.plus.update;

public final class UpdateSource {

    public static final String GITHUB = "github";
    public static final String OCI = "oci";

    private UpdateSource() {
    }

    public static String normalize(String value) {
        if (GITHUB.equals(value)) return GITHUB;
        return OCI;
    }
}
