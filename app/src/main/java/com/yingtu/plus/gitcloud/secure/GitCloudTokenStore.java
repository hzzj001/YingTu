package com.yingtu.plus.gitcloud.secure;

import com.google.gson.JsonObject;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * Git 云端令牌仓（LoginStateSync 在登录状态同步时导出 / 导入令牌）。
 * 兼容实现不保存任何令牌：导出返回空对象，导入返回 0（表示导入 0 条）。
 */
public final class GitCloudTokenStore {

    private GitCloudTokenStore() {
    }

    /** 导出全部令牌为 JSON 对象；兼容实现返回空对象。 */
    public static JsonObject exportTokens() {
        return new JsonObject();
    }

    /** 从 JSON 对象导入令牌；兼容实现返回 0（导入 0 条）。 */
    public static int importTokens(JsonObject tokens) {
        return 0;
    }
}
