package com.fongmi.hook;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 应用签名钩子描述（Core 按配置的签名与包名生成它，TVBus 在取流前后
 * 通过 App.setHook() 设置 / 清除）。兼容实现只保留数据本身，
 * 不做任何真实的 Hook 安装。
 */
public class Hook {

    private final String sign;
    private final String pkg;

    public Hook(String sign, String pkg) {
        this.sign = sign == null ? "" : sign;
        this.pkg = pkg == null ? "" : pkg;
    }

    public String getSign() {
        return sign;
    }

    public String getPkg() {
        return pkg;
    }
}
