package com.yingtu.plus.player;

import android.net.Uri;

import com.yingtu.plus.bean.Result;
import com.yingtu.plus.bean.Vod;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 播放源解析分发器（SiteApi / LiveApi 的统一入口）：按地址/站点特征把取流、解析交给
 * {@link Extractor} 实现（Force / JianPian / Push / Strm / TVBus / Thunder / Video / Youtube 均已实现该接口）。
 *
 * 兼容实现只保留分发骨架：没有可用的解析器时返回空串 / 不做任何事，不凭空实现任何站点的取流逻辑。
 * 真实分发（遍历 Extractor、按 match(Uri) 选取）待上游 webhtv 的 Source 源码对齐后补全。
 */
public final class Source {

    /** 单个取流/解析插件需实现的接口（各 extractor 子类已按此契约实现）。 */
    public interface Extractor {
        boolean match(Uri uri);

        String fetch(String url) throws Exception;

        void stop();

        void exit();
    }

    private static volatile Source instance;

    private Source() {
    }

    public static Source get() {
        if (instance == null) {
            synchronized (Source.class) {
                if (instance == null) instance = new Source();
            }
        }
        return instance;
    }

    /** 停止当前正在进行的取流/解析（兼容实现：不做任何事）。 */
    public void stop() {
    }

    /** 取点播结果的真实播放地址；无可用解析器时返回空串。 */
    public String fetch(Result result) {
        return "";
    }

    /** 取点播结果的真实播放地址（带目标播放器类型）；无可用解析器时返回空串。 */
    public String fetch(Result result, int playerType) {
        return "";
    }

    /** 对点播条目做解析前处理（兼容实现：不做任何事）。 */
    public void parse(Vod vod) {
    }
}
