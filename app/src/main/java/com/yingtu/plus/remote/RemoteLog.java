// 移植自上游 fish2018/webhtv app/src/main/java/com/fongmi/android/tv/remote/RemoteLog.java @ HEAD 4e30ffa（2026-10-07），仅包名 com.fongmi.android.tv → com.yingtu.plus；逻辑未改。
package com.yingtu.plus.remote;

import com.github.catvod.crawler.SpiderDebug;

/** Debug logging for the remote-trust feature. Disabled profiles are completely silent. */
public final class RemoteLog {

    private RemoteLog() {
    }

    public static void log(String format, Object... args) {
        if (RemoteStore.hasEnabledProfile()) SpiderDebug.log("remote", format, args);
    }
}
