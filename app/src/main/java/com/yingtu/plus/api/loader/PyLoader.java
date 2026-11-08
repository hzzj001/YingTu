package com.yingtu.plus.api.loader;

import android.text.TextUtils;

import com.yingtu.plus.App;
import com.yingtu.plus.util.AppLog;
import com.fongmi.chaquo.Loader;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PyLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    private final Loader loader;
    private volatile String recent;

    public PyLoader() {
        spiders = new ConcurrentHashMap<>();
        loader = new Loader();
    }

    public void clear() {
        spiders.values().forEach(Spider::destroy);
        spiders.clear();
        recent = null;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public Spider getSpider(String key, String api, String ext) {
        // 无站点上下文的入口：没有 sha256，走“无哈希警告后加载”。
        return getSpider(key, api, ext, "");
    }

    public Spider getSpider(String key, String api, String ext, String sha256) {
        // P0：py 代码文件是远程地址时必须是 https（本机服务 http://127.0.0.1 除外），
        // 否则拒绝加载。注意只校验代码文件 api 本身，不校验配置 JSON 的地址。
        if (!CodeLoadVerifier.isAllowed(api)) {
            CodeLoadVerifier.rejectNonHttps(api, "PyLoader", "py site=" + key);
            return new SpiderNull();
        }
        // 注意：当前 com.fongmi.chaquo.Loader 只是返回 SpiderNull 的兼容空实现，
        // 并不实际下载 py 代码，所以这里收到的 sha256 暂无内容可验；先记警告留痕，
        // 等真实 Chaquopy 加载器落地后再按 JsLoader 的预取方式补验，不静默忽略。
        if (!CodeLoadVerifier.normalizeSha256(sha256).isEmpty()) {
            AppLog.w("PyLoader", "py 加载器当前为空实现，sha256 暂无法校验 site=%s", key);
        }
        return spiders.computeIfAbsent(key, k -> {
            try {
                Spider spider = loader.spider(api);
                spider.siteKey = key;
                spider.init(App.get(), normalizeExt(ext));
                return spider;
            } catch (Throwable e) {
                e.printStackTrace();
                return new SpiderNull();
            }
        });
    }

    private String normalizeExt(String ext) {
        String value = TextUtils.isEmpty(ext) ? "" : ext.trim();
        // Many live Python spiders treat ext as an option object and call .get().
        // TV-style configs commonly express an empty extension as [], which would
        // otherwise deserialize to a list and crash those spiders during init.
        return "[]".equals(value) ? "{}" : ext;
    }

    public Object[] proxy(Map<String, String> params) throws Exception {
        if (recent == null) return null;
        Spider spider = spiders.get(recent);
        return spider != null ? spider.proxy(params) : null;
    }
}
