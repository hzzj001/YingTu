package com.yingtu.plus.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yingtu.plus.R;
import com.yingtu.plus.base.BaseActivity;
import com.yingtu.plus.web.HomeWebBridge;
import com.yingtu.plus.web.WebHomeSdk;
import com.yingtu.plus.web.WebServer;
import com.google.gson.JsonObject;

/**
 * WebHome 承载页：用内置 WebView 加载一个「网页首页」（远程 URL / 本地 HTML / 内置示例），
 * 并注入 App Native SDK（window.fm / window.fongmi），实现 webhtv 的 WebHome 能力。
 *
 * 这是本次按 https://github.com/webhtv/webhtv 补全的核心功能：
 * 让 HTML 文件（含用户自己的本地 .html）成为可调用原生搜索/播放/网络能力的首页应用。
 */
public class WebHomeActivity extends BaseActivity implements HomeWebBridge.UiCallback {

    public static final String KEY_URL = "webhome_url";
    public static final String KEY_TITLE = "webhome_title";

    private WebView webView;
    private ProgressBar progress;
    private TextView tvTitle;
    private View bar;

    private String chromeMode = "normal";
    private boolean sdkReady = false;

    // 缓存视口度量（在 UI 线程更新），供后台 bridge 线程安全读取
    private volatile int vpWidth = 0;
    private volatile int vpHeight = 0;
    private volatile int vpSafeTop = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_webhome;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void init() {
        webView = find(R.id.webView);
        progress = find(R.id.progress);
        tvTitle = find(R.id.tvTitle);
        bar = find(R.id.bar);

        String title = getIntent().getStringExtra(KEY_TITLE);
        String url = getIntent().getStringExtra(KEY_URL);
        tvTitle.setText(TextUtils.isEmpty(title) ? "网页首页" : title);

        find(R.id.ivBack).setOnClickListener(v -> handleBackPressed());
        find(R.id.ivReload).setOnClickListener(v -> reload());

        // 确保本地资源网关已启动（fm.res 依赖）
        WebServer.get().start();

        setupWebView();

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "首页地址为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // P0 安全修复：首屏地址也要过白名单。shouldOverrideUrlLoading 只拦截页面内的
        // 跳转，不拦截 loadUrl() 的首次加载，不在这里校验就等于白名单留了个入口。
        if (!isAllowedWebHomeUrl(url)) {
            Toast.makeText(this, "首页地址不受支持，已拦截", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        webView.loadUrl(url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        // P0 安全修复：setAllowFileAccess(true) 是内置示例与本地 HTML 必须的——
        // file:///android_asset/webhome/*.html 和 filesDir/webhome/*.html 都要靠
        // file:// 访问才能加载，关掉则内置示例页直接打不开。范围限定在下面两点：
        // 一是跨 file:// 访问与通用访问已关（file:// 页面不能读其他本地文件、
        // 也不能向任意来源发请求）；二是能进 WebView 的 URL 由本页的白名单把关。
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        // 兼容旧的 TYPE_LOCAL 条目：个别历史条目仍可能是用户 SAF 授权的 content://，
        // 需允许 WebView 经 ContentResolver 加载；新挑的文件已复制进内部存储走 file://。
        s.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // P0 安全修复：混合内容改兼容模式。此前 MIXED_CONTENT_ALWAYS_ALLOW 会让
            // https 首页里的 http 脚本也照常执行，https 页面的安全性被 http 子资源
            // 拖垮。兼容模式只放行 Chromium 认为可接受的混合内容（如图片/媒体），
            // 仍拦截 http 脚本等主动混合内容；不要直接改回 ALWAYS_ALLOW。
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setTextZoom(100);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // 第三方 Cookie
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cm.setAcceptThirdPartyCookies(webView, true);
        }

        webView.addJavascriptInterface(
                new HomeWebBridge(this, webView, this), WebHomeSdk.BRIDGE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // P0 安全修复：此前直接 return false，页面内任何链接（包括 intent://、
                // tel:、javascript: 等）都由 WebView 自行处理，可能跳出应用或触发
                // 外部 scheme。现在只放行白名单内的 URL，其余拦截并给用户提示，
                // 不静默失败。
                if (request == null || request.getUrl() == null) return true;
                String target = request.getUrl().toString();
                if (isAllowedWebHomeUrl(target)) return false; // 让 WebView 自己加载
                Toast.makeText(WebHomeActivity.this, "已拦截不支持的链接", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 兼容 API 24 以下的老设备，与上面同一套白名单。
                if (isAllowedWebHomeUrl(url)) return false;
                Toast.makeText(WebHomeActivity.this, "已拦截不支持的链接", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectSdk();
                progress.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage msg) {
                android.util.Log.d("webview", msg.message() + " @" + msg.lineNumber());
                return true;
            }
        });
    }

    /**
     * P0 安全修复：WebHome URL 白名单（首屏 loadUrl 与页面内跳转共用同一套）。
     * 放行：
     * - http/https：用户自填的远程首页（添加时已要求 http(s):// 开头）。这里只做
     *   scheme 校验、不做 host 白名单，因为首页地址是用户自己配置的任意源站，
     *   拦 host 会把正常源站首页也拦掉；防的是非 web scheme 逃出 WebView。
     * - file:///android_asset/：内置示例（webhome/basic.html、showcase.html）。
     * - file:// + filesDir/webhome/：用户 SAF 挑选后复制进内部存储的本地 HTML。
     *   file:// 只限这两个前缀，不放行任意本地路径，避免本地页面被诱导去读
     *   应用私有文件或其他目录的文件。
     * - content://：旧 TYPE_LOCAL 条目的遗留形态（用户 SAF 授权过的 URI）；
     *   新挑选的文件已复制进内部存储走 file://，这里只为兼容历史条目保留。
     * - about:blank：onDestroy() 里用来清空页面。
     * 其余 scheme（intent://、tel:、javascript:、data:、ftp: 等）一律拦截。
     */
    private boolean isAllowedWebHomeUrl(String url) {
        if (TextUtils.isEmpty(url)) return false;
        Uri uri;
        try {
            uri = Uri.parse(url);
        } catch (Throwable ignored) {
            return false;
        }
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) return false;
        scheme = scheme.toLowerCase(java.util.Locale.ROOT);
        switch (scheme) {
            case "http":
            case "https":
                return true;
            case "about":
                return "about:blank".equalsIgnoreCase(url);
            case "content":
                return true;
            case "file": {
                String path = uri.getPath();
                if (TextUtils.isEmpty(path)) return false;
                if (path.startsWith("/android_asset/")) return true;
                String webhomeDir = new java.io.File(getFilesDir(), "webhome").getAbsolutePath();
                return path.equals(webhomeDir) || path.startsWith(webhomeDir + "/");
            }
            default:
                return false;
        }
    }

    private void injectSdk() {
        webView.evaluateJavascript(WebHomeSdk.script(isLeanback()), value -> {
            sdkReady = true;
            pushViewport();
        });
    }

    private void pushViewport() {
        vpWidth = px2dp(webView.getWidth());
        vpHeight = px2dp(webView.getHeight());
        vpSafeTop = px2dp(getStatusBarHeight());
        String js = WebHomeSdk.viewportVars(vpWidth, vpHeight, vpSafeTop, 0, vpSafeTop, chromeMode);
        webView.evaluateJavascript(js, null);
    }

    private int px2dp(int px) {
        float d = getResources().getDisplayMetrics().density;
        return d <= 0 ? px : Math.round(px / d);
    }

    private int getStatusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : 0;
    }

    private boolean isLeanback() {
        return getPackageManager().hasSystemFeature("android.software.leanback");
    }

    private void handleBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    // ===== HomeWebBridge.UiCallback =====

    @Override
    public void setToolbar(boolean visible) {
        bar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setChrome(JsonObject payload) {
        String mode = payload != null && payload.has("mode") && !payload.get("mode").isJsonNull()
                ? payload.get("mode").getAsString() : "";
        if (!TextUtils.isEmpty(mode)) {
            chromeMode = mode;
            boolean edge = "edge".equals(mode) || "immersive".equals(mode)
                    || "tv-full".equals(mode) || "fullscreen".equals(mode);
            bar.setVisibility(edge ? View.GONE : View.VISIBLE);
        }
        pushViewport();
    }

    @Override
    public void restoreChrome() {
        chromeMode = "normal";
        bar.setVisibility(View.VISIBLE);
        pushViewport();
    }

    @Override
    public String getViewportJson() {
        // 该方法可能被 bridge 后台线程调用，读缓存值避免跨线程访问 View
        JsonObject o = new JsonObject();
        o.addProperty("webWidth", vpWidth);
        o.addProperty("webHeight", vpHeight);
        o.addProperty("safeTop", vpSafeTop);
        o.addProperty("safeBottom", 0);
        o.addProperty("statusBarHeight", vpSafeTop);
        o.addProperty("chromeMode", chromeMode);
        return o.toString();
    }

    @Override
    public void back() {
        handleBackPressed();
    }

    @Override
    public void reload() {
        progress.setVisibility(View.VISIBLE);
        if (webView != null) {
            webView.clearCache(true);
            webView.reload();
        }
    }

    @Override
    public void openVod() {
        finish();
    }

    @Override
    public void openSetting() {
        startActivity(new android.content.Intent(this, SettingActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            if (sdkReady) {
                webView.evaluateJavascript(
                        WebHomeSdk.dispatchEvent("fmresume", "{\"time\":" + System.currentTimeMillis() + "}"), null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            if (sdkReady) {
                webView.evaluateJavascript(
                        WebHomeSdk.dispatchEvent("fmpause", "{\"time\":" + System.currentTimeMillis() + "}"), null);
            }
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.removeJavascriptInterface(WebHomeSdk.BRIDGE);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
