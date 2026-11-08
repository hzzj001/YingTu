package com.yingtu.plus.utils;

import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.yingtu.plus.App;
import com.github.catvod.crawler.SpiderDebug;

import java.util.Set;

public class WebViewUtil {

    private static final String SYSTEM_SETTINGS_PACKAGE = "com.android.settings";

    private static final Set<String> BROWSER_PACKAGES = Set.of(
            "com.android.chrome",
            "com.mi.globalbrowser",
            "com.huawei.browser",
            "com.heytap.browser",
            "com.vivo.browser"
    );

    private static boolean installed(PackageManager pm, String pkg) {
        try {
            pm.getPackageInfo(pkg, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static String spoof() {
        PackageManager pm = App.get().getPackageManager();
        return BROWSER_PACKAGES.stream().filter(packageName -> installed(pm, packageName)).findFirst().orElse(SYSTEM_SETTINGS_PACKAGE);
    }

    public static boolean support() {
        try {
            CookieManager.getInstance();
            return App.get().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WEBVIEW);
        } catch (Throwable e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public static void configureBase(WebView webView, String role) {
        if (webView == null) return;
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        // P0 安全修复：混合内容改兼容模式。此前 MIXED_CONTENT_ALWAYS_ALLOW 会让 https 页面
        // 里的 http 脚本也照常执行，https 页面的安全性被 http 子资源拖垮。兼容模式只放行
        // Chromium 认为可接受的混合内容（如图片/媒体），仍拦截 http 脚本等主动混合内容；
        // 如个别 https 首页的图片仍加载失败，再按站点个案处理，不要直接改回 ALWAYS_ALLOW。
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        // P0 安全修复：默认关掉 file:// 访问。file:// 页面默认就能读本地文件的话，
        // 任意加载的本地 HTML 都可能越权读取应用私有文件；跨 file:// 访问与通用访问
        // 更是直接允许 file:// 页面读任意本地文件并向任意来源发请求，必须关掉。
        // 内置示例/本地 HTML 确实需要 file:// 时，由 configureHome() 再单独放开
        // setAllowFileAccess(true)，其余场景保持关闭。
        settings.setAllowFileAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        logSettings(webView, role);
    }

    public static void configureHome(WebView webView) {
        configureBase(webView, "webhome");
        WebSettings settings = webView.getSettings();
        // P0 安全修复说明：这里把 setAllowFileAccess 重新打开，是内置示例与本地 HTML
        // 必须的——file:///android_asset/webhome/*.html 和 filesDir/webhome/*.html
        // 都要靠 file:// 访问才能加载，关掉则内置示例页直接打不开。
        // 范围限定：只放开“WebView 能加载 file:// URL”这一项；
        // setAllowFileAccessFromFileURLs / setAllowUniversalAccessFromFileURLs
        // 仍保持关闭（见 configureBase），file:// 页面不能互相读取本地文件、
        // 也不能向任意来源发请求；能进 WebView 的 URL 另由 WebHomeActivity 的
        // 白名单（shouldOverrideUrlLoading）把关，只放行内置示例、内部存储 webhome
        // 目录与 http(s) 远程页。
        settings.setAllowFileAccess(true);
        // 兼容旧的 TYPE_LOCAL 条目：个别历史条目仍可能是用户 SAF 授权的 content://，
        // 需允许 WebView 经 ContentResolver 加载；新挑的文件已复制进内部存储走 file://。
        settings.setAllowContentAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) settings.setOffscreenPreRaster(true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        logSettings(webView, "webhome-final");
    }

    public static void logProvider(String role) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            SpiderDebug.log("webview", "role=%s provider unavailable sdk=%s", role, Build.VERSION.SDK_INT);
            return;
        }
        try {
            android.content.pm.PackageInfo info = WebView.getCurrentWebViewPackage();
            if (info == null) {
                SpiderDebug.log("webview", "role=%s provider unavailable", role);
            } else {
                SpiderDebug.log("webview", "role=%s provider package=%s version=%s code=%s", role, info.packageName, info.versionName, Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? info.getLongVersionCode() : info.versionCode);
            }
        } catch (Throwable e) {
            SpiderDebug.log("webview", e);
        }
    }

    private static void logSettings(WebView webView, String role) {
        try {
            WebSettings settings = webView.getSettings();
            SpiderDebug.log("webview", "role=%s hardware=%s layer=%s js=%s dom=%s db=%s cache=%s zoom=%s builtInZoom=%s wide=%s overview=%s textZoom=%s mixed=%s offscreen=%s ua=%s",
                    role,
                    webView.isHardwareAccelerated(),
                    webView.getLayerType(),
                    settings.getJavaScriptEnabled(),
                    settings.getDomStorageEnabled(),
                    settings.getDatabaseEnabled(),
                    settings.getCacheMode(),
                    settings.supportZoom(),
                    settings.getBuiltInZoomControls(),
                    settings.getUseWideViewPort(),
                    settings.getLoadWithOverviewMode(),
                    settings.getTextZoom(),
                    settings.getMixedContentMode(),
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && settings.getOffscreenPreRaster(),
                    settings.getUserAgentString());
        } catch (Throwable e) {
            SpiderDebug.log("webview", e);
        }
    }
}
