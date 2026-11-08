package com.yingtu.plus.web;

import com.yingtu.plus.bean.Result;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * WebHome 内联点播仓：SiteApi 在解析 WebHome 内联源时，
 * 按 key 查这里的详情 / 播放结果。兼容实现始终返回空结果
 * （Result.get() 的默认空结果），不做任何真实存储与解析。
 */
public final class WebHomeInlineVodStore {

    /** 内联源在 Site key 中的标识前缀。 */
    public static final String KEY = "webhome_inline";

    private WebHomeInlineVodStore() {
    }

    /** 取内联源的详情结果；兼容实现返回空结果。 */
    public static Result detail(String id) {
        return new Result();
    }

    /** 取内联源的播放结果；兼容实现返回空结果。 */
    public static Result player(String flag, String id) {
        return new Result();
    }
}
