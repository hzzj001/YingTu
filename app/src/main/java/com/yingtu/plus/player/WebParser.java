package com.yingtu.plus.player;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yingtu.plus.App;
import com.yingtu.plus.util.AppLog;
import com.yingtu.plus.util.Sniffer;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 隐藏式 WebView 嗅探器（参考 FongMi/webhtv 的 CustomWebView 精简）。
 *
 * 原理：用一个不显示的 WebView 加载网页播放页，
 * 在 {@link WebViewClient#shouldInterceptRequest} 里拦截所有子请求，
 * 命中视频直链特征（Sniffer.isVideoFormat）即回调成功并带上请求头。
 *
 * 这是“网页解析（type=0）”的核心，用来处理无法直接拿到直链、
 * 需要在浏览器环境里执行 JS 才会发出真实视频请求的站点。
 */
public class WebParser {

    public interface Callback {
        void onResult(Map<String, String> headers, String url);

        void onError();
    }

    private WebView webView;
    private final AtomicReference<Callback> callbackRef = new AtomicReference<>();
    private final Set<String> seen = new LinkedHashSet<>();
    private WebResourceResponse empty;

    public static WebParser create() {
        return new WebParser();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void start(String url, Map<String, String> headers, Callback callback) {
        callbackRef.set(callback);
        empty = new WebResourceResponse("text/plain", "utf-8",
                new ByteArrayInputStream("".getBytes()));

        webView = new WebView(App.context());
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        // P0 安全修复：第 2 步同类残留。此前 MIXED_CONTENT_ALWAYS_ALLOW 会让 https
        // 播放页里的 http 脚本也照常执行，页面安全性被 http 子资源拖垮。兼容模式只放行
        // Chromium 认为可接受的混合内容（如图片/媒体），仍拦截 http 脚本等主动混合内容；
        // 不要直接改回 ALWAYS_ALLOW（与 WebHomeActivity / WebViewUtil 保持一致）。
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setJavaScriptCanOpenWindowsAutomatically(false);

        // 自定义 UA
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                if ("User-Agent".equalsIgnoreCase(e.getKey())) {
                    s.setUserAgentString(e.getValue());
                } else if ("Cookie".equalsIgnoreCase(e.getKey())) {
                    CookieManager.getInstance().setCookie(url, e.getValue());
                }
            }
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String reqUrl = request.getUrl().toString();
                String host = request.getUrl().getHost();
                if (TextUtils.isEmpty(host)) return empty;
                if (Sniffer.isVideoFormat(reqUrl) && seen.add(reqUrl)) {
                    Map<String, String> h = new HashMap<>(request.getRequestHeaders());
                    // 补一个 Referer 便于播放
                    if (!h.containsKey("Referer")) h.put("Referer", view.getUrl() == null ? reqUrl : view.getUrl());
                    deliver(h, reqUrl);
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String pageUrl) {
                super.onPageFinished(view, pageUrl);
                if (pageUrl == null || pageUrl.equals("about:blank")) return;
                // 注入命中域名规则的辅助脚本（如自动点击播放按钮）
                List<String> scripts = Sniffer.getScript(pageUrl);
                for (String js : scripts) {
                    if (!TextUtils.isEmpty(js)) {
                        try {
                            view.evaluateJavascript(js, null);
                        } catch (Throwable e) {
                            AppLog.d("WebParser", "注入嗅探脚本失败 url=%s", pageUrl);
                        }
                    }
                }
            }

            @Override
            @SuppressLint("WebViewClientOnReceivedSslError")
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // P0 安全修复：第 2 步同类残留。此前无条件 handler.proceed() 等于信任所有
                // 证书（含过期/域名不符/自签），中间人可窃听或篡改播放页。默认拒绝加载，
                // 并走现有解析错误回调通知上层（上层会按解析失败提示用户），不要无条件放行。
                AppLog.w("WebParser", "网页证书校验失败，已停止加载 url=%s error=%s",
                        view == null || view.getUrl() == null ? "" : view.getUrl(),
                        error == null ? "unknown" : String.valueOf(error.getPrimaryError()));
                handler.cancel();
                Callback cb = callbackRef.getAndSet(null);
                if (cb != null) cb.onError();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        Map<String, String> loadHeaders = headers == null ? new HashMap<>() : headers;
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.loadUrl(url, loadHeaders);
    }

    private void deliver(Map<String, String> headers, String url) {
        Callback cb = callbackRef.getAndSet(null);
        if (cb != null) cb.onResult(headers, url);
    }

    public void destroy() {
        callbackRef.set(null);
        if (webView != null) {
            try {
                webView.stopLoading();
                webView.loadUrl("about:blank");
                webView.removeAllViews();
                webView.destroy();
            } catch (Throwable e) {
                AppLog.d("WebParser", "销毁 WebView 失败", e);
            }
            webView = null;
        }
    }
}
