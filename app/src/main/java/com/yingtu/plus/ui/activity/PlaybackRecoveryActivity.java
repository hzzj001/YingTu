package com.yingtu.plus.ui.activity;

import com.yingtu.plus.base.BaseActivity;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放恢复页（PlaybackRecoveryMonitor 在检测到主线程心跳停滞时，
 * 以新任务方式启动它做恢复，Intent 带 EXTRA_TOKEN）。
 * 兼容实现为空页面：启动后直接结束，不做任何恢复操作。
 */
public class PlaybackRecoveryActivity extends BaseActivity {

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void init() {
    }
}
