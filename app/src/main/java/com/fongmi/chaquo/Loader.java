package com.fongmi.chaquo;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * Chaquopy Python 加载器（PyLoader 用它按 api 名加载 Python 爬虫）。
 * 兼容实现不加载任何 Python 代码：spider() 一律返回空爬虫 SpiderNull，
 * 调用方（PyLoader）已按“加载失败返回 SpiderNull”处理。
 */
public class Loader {

    public Loader() {
    }

    /** 按 api 名加载 Python 爬虫；兼容实现返回空爬虫。 */
    public Spider spider(String api) {
        return new SpiderNull();
    }
}
