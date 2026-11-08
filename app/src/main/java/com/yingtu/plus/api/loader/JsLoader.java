package com.yingtu.plus.api.loader;

import com.yingtu.plus.App;
import com.yingtu.plus.util.AppLog;
import com.fongmi.quickjs.crawler.Loader;
import com.fongmi.quickjs.utils.Module;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.net.OkHttp;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    private final Loader loader;
    private volatile String recent;

    public JsLoader() {
        spiders = new ConcurrentHashMap<>();
        loader = new Loader();
    }

    public void clear() {
        spiders.values().forEach(Spider::destroy);
        Module.get().clear();
        spiders.clear();
        recent = null;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public Spider getSpider(String key, String api, String ext, String jar) {
        // 无站点上下文的入口：没有 sha256，走“无哈希警告后加载”。
        return getSpider(key, api, ext, jar, "");
    }

    public Spider getSpider(String key, String api, String ext, String jar, String sha256) {
        // P0：js 代码文件是远程地址时必须是 https（本机服务 http://127.0.0.1 除外），
        // 否则拒绝加载。注意只校验代码文件 api 本身，不校验配置 JSON 的地址。
        if (!CodeLoadVerifier.isAllowed(api)) {
            CodeLoadVerifier.rejectNonHttps(api, "JsLoader", "js site=" + key);
            return new SpiderNull();
        }
        // P0：配了 sha256 且 api 走 http(s) 拉取时，先预取一份内容验哈希，通过才继续。
        // js 内容实际由 QuickJS 的 Module.fetch 在 init 时再拉一次（会重复下载一次，
        // js 文件一般很小，以正确性优先）；本地 assets/lib 文件不走网络，不在此验。
        if (!CodeLoadVerifier.normalizeSha256(sha256).isEmpty() && CodeLoadVerifier.isHttp(api)) {
            try {
                String content = OkHttp.string(api);
                if (!CodeLoadVerifier.verifyBytes(content == null ? null : content.getBytes(StandardCharsets.UTF_8), sha256, "JsLoader", "js site=" + key)) {
                    return new SpiderNull();
                }
            } catch (Throwable e) {
                AppLog.e("JsLoader", "JS 源文件预取失败，无法校验 sha256，已拒绝加载 site=%s", key, e);
                CodeLoadVerifier.notifyUser("JS 源文件下载失败，已拒绝加载");
                return new SpiderNull();
            }
        } else if (!CodeLoadVerifier.normalizeSha256(sha256).isEmpty()) {
            // 本地 js 配了 sha256 但无远程内容可验：记警告后照常加载，不静默忽略。
            AppLog.w("JsLoader", "本地 JS 源文件无法校验 sha256（无远程内容），直接加载 site=%s", key);
        }
        return spiders.computeIfAbsent(key, k -> {
            try {
                Spider spider = loader.spider(api, BaseLoader.get().dex(jar));
                spider.siteKey = key;
                spider.init(App.get(), ext);
                return spider;
            } catch (Throwable e) {
                SpiderDebug.log("JsLoader", "JS spider init failed: key=%s api=%s", key, api);
                SpiderDebug.log("JsLoader", e);
                e.printStackTrace();
                return new SpiderNull();
            }
        });
    }

    public Object[] proxy(Map<String, String> params) throws Exception {
        if (recent == null) return null;
        Spider spider = spiders.get(recent);
        return spider != null ? spider.proxy(params) : null;
    }
}
