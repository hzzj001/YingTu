package com.yingtu.plus.server;

import com.yingtu.plus.web.WebServer;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 本地 HTTP 服务入口（BaseConfig / Core / Device / ScanTask / UrlUtil 用它取本机地址、拉起服务，
 * 供 TVBus、投屏发现等本地回环能力使用）。
 *
 * 兼容实现直接复用已恢复的旧线 {@link WebServer}：同一台本机服务、同一端口；
 * getAddress() / getAddress(boolean) 只是 WebServer.getAddress(String) 的便捷重载。
 */
public final class Server {

    private static volatile Server instance;

    private Server() {
    }

    public static Server get() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) instance = new Server();
            }
        }
        return instance;
    }

    public synchronized void start() {
        WebServer.get().start();
    }

    /** 本机服务根地址，等价于 getAddress("")。 */
    public String getAddress() {
        return WebServer.get().getAddress("");
    }

    /** 本机服务地址 + 指定路径，例如 getAddress("/tvbus")。 */
    public String getAddress(String path) {
        return WebServer.get().getAddress(path == null ? "" : path);
    }

    /**
     * 本机服务根地址。
     *
     * @param local 调用点传入 true 表示“只取本机地址做比对”（ScanTask.isLocal 用）；兼容实现不区分，始终返回根地址。
     */
    public String getAddress(boolean local) {
        return getAddress();
    }
}
