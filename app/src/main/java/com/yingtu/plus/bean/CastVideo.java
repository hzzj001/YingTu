package com.yingtu.plus.bean;

import java.io.Serializable;
import java.util.Map;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 投屏视频条目（VideoActivity 把当前播放内容包成它交给投屏对话框）。
 * 仅含调用点用到的四参构造器与取值方法。
 */
public class CastVideo implements Serializable {

    private final String name;
    private final String url;
    private final long position;
    private final Map<String, String> headers;

    public CastVideo(String name, String url, long position, Map<String, String> headers) {
        this.name = name;
        this.url = url;
        this.position = position;
        this.headers = headers;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getPosition() {
        return position;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
