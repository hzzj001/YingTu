// 移植自上游 fish2018/webhtv (commit 4e30ffa)：app/src/main/java/com/fongmi/android/tv/setting/PlaybackPerformanceOption.java
package com.yingtu.plus.setting;

public final class PlaybackPerformanceOption {

    private final String id;
    private final String section;
    private final String title;
    private final String description;

    public PlaybackPerformanceOption(String id, String section, String title, String description) {
        this.id = id;
        this.section = section;
        this.title = title;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String section() {
        return section;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }
}
