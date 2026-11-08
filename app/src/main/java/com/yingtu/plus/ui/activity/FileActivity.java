package com.yingtu.plus.ui.activity;

import com.yingtu.plus.base.BaseActivity;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 文件选择页（FileChooser 用 Intent 启动它，支持普通选择与选目录两种模式，
 * 通过 "select_dir" extra 区分）。兼容实现为空页面：不展示文件列表，
 * 直接结束（不返回任何选择结果）。
 */
public class FileActivity extends BaseActivity {

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
