package com.yingtu.plus;

import androidx.fragment.app.FragmentActivity;

/**
 * 兼容实现（有意留空，非上游缺失）。
 *
 * 上游 webhtv 的 Updater 会向 webhtv 自己的更新通道查新版本并下载 APK；
 * 影图+ 是个人 fork，没有自己的更新通道，照搬会让应用去拉上游的更新，
 * 故此处有意做成空实现：create()/resume() 不做任何事。
 * ui.base.BaseActivity（上游移植）只在 onResume 调 Updater.create().resume(this)，
 * 空实现即可满足调用点。将来影图+ 有自己的更新地址时再按需实现。
 */
public class Updater {

    private static final Updater INSTANCE = new Updater();

    private Updater() {
    }

    public static Updater create() {
        return INSTANCE;
    }

    /** 恢复更新弹窗（空实现：无更新通道）。 */
    public void resume(FragmentActivity activity) {
    }

    /** 开始检查更新（空实现：无更新通道）。 */
    public void start(FragmentActivity activity) {
    }

    /** 强制检查更新（空实现：无更新通道）。 */
    public Updater force() {
        return this;
    }
}
