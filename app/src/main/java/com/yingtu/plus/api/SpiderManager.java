package com.yingtu.plus.api;

import android.text.TextUtils;

import com.yingtu.plus.api.loader.JarLoader;
import com.yingtu.plus.api.loader.JsLoader;
import com.yingtu.plus.bean.SourceBean;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.utils.Util;

import java.util.Map;

/**
 * Spider 统一调度（参考 webhtv 的 BaseLoader 精简）。
 *
 * 根据站点 api 前缀选择加载器：
 *  - csp_ 且 api 以 .js 结尾 / jar 以 .js 结尾 → JS Spider（QuickJS）
 *  - csp_ 其余 → JAR Spider（DexClassLoader + catvod 壳）
 *
 * jar 来源：配置顶层 "spider" 字段（可带 ;md5; 校验段）。
 */
public class SpiderManager {

    private static SpiderManager instance;

    private final JarLoader jarLoader = new JarLoader();
    private final JsLoader jsLoader = new JsLoader();

    private SpiderManager() {}

    public static SpiderManager get() {
        if (instance == null) {
            synchronized (SpiderManager.class) {
                if (instance == null) instance = new SpiderManager();
            }
        }
        return instance;
    }

    public void clear() {
        jarLoader.clear();
        jsLoader.clear();
    }

    /** 预加载配置里的 spider jar（在源解析后调用，异步执行更佳）。 */
    public void parseJar(String jar) {
        if (TextUtils.isEmpty(jar)) return;
        jarLoader.setRecent(jar);
        // 适配：现版 JarLoader 不再提供静态 md5()，改用 catvod 的 Util.md5() 生成缓存 key，与 JarLoader.dex() 内部一致。
        jarLoader.parseJar(Util.md5(jar), jar);
    }

    public boolean isCsp(String api) {
        return api != null && api.startsWith("csp_");
    }

    public boolean isJs(String api) {
        return api != null && (api.startsWith("csp_") && api.endsWith(".js")) || (api != null && api.contains(".js"));
    }

    /**
     * 获取某个站点的 Spider。
     * @param site 站点
     * @param jar  配置里的 spider jar 地址
     */
    public Spider getSpider(SourceBean site, String jar) {
        if (site == null) return new SpiderNull();
        String api = site.getApi();
        String key = site.getKey();
        String ext = site.getExt();
        if (!isCsp(api)) return new SpiderNull();

        if (api.endsWith(".js")) {
            // 适配：现版 JsLoader.getSpider() 第 4 参是 jar 地址字符串（内部再调 BaseLoader.dex(jar)），
            // 不再是旧版的 DexClassLoader；JS 源一般不需要预先传 dex，这里直接透传 jar。
            // 第 5 参是本站点的可选 sha256，验 js 代码文件（见 CodeLoadVerifier）。
            return jsLoader.getSpider(key, api, ext, jar, site.getSha256());
        }
        jarLoader.setRecent(jar);
        // 第 5 参是本站点的可选 sha256，验 jar 文件。
        return jarLoader.getSpider(key, api, ext, jar, site.getSha256());
    }

    public Object[] proxy(Map<String, String> params) {
        // 适配：现版 JarLoader.proxy() 声明 throws Exception，这里吞掉异常返回 null，保持旧签名不抛受检异常。
        try {
            return jarLoader.proxy(params);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
