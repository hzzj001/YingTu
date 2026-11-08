package com.github.catvod.net;

import androidx.collection.ArrayMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.catvod.net.interceptor.AuthInterceptor;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.interceptor.RequestInterceptor;
import com.github.catvod.net.interceptor.ResponseInterceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttp {

    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private ResponseInterceptor responseInterceptor;
    private RequestInterceptor requestInterceptor;
    private AuthInterceptor authInterceptor;
    private OkAuthenticator authenticator;
    private OkProxySelector selector;
    private OkHttpClient client;
    private OkHttpClient player;
    private OkDns dns;

    public static OkHttp get() {
        return Loader.INSTANCE;
    }

    public static OkDns dns() {
        if (get().dns != null) return get().dns;
        return get().dns = new OkDns();
    }

    public static ResponseInterceptor responseInterceptor() {
        if (get().responseInterceptor != null) return get().responseInterceptor;
        return get().responseInterceptor = new ResponseInterceptor();
    }

    public static RequestInterceptor requestInterceptor() {
        if (get().requestInterceptor != null) return get().requestInterceptor;
        return get().requestInterceptor = new RequestInterceptor();
    }

    public static AuthInterceptor authInterceptor() {
        if (get().authInterceptor != null) return get().authInterceptor;
        return get().authInterceptor = new AuthInterceptor();
    }

    public static OkAuthenticator authenticator() {
        if (get().authenticator != null) return get().authenticator;
        return get().authenticator = new OkAuthenticator(selector());
    }

    public static OkProxySelector selector() {
        if (get().selector != null) return get().selector;
        return get().selector = new OkProxySelector();
    }

    public static void closeIdleConnections() {
        new Thread(OkHttp::evictIdleConnections, "okhttp-evict-idle").start();
    }

    private static synchronized void evictIdleConnections() {
        try {
            if (get().client != null) get().client.connectionPool().evictAll();
            if (get().player != null) get().player.connectionPool().evictAll();
            SpiderDebug.log("proxy", "connection pool evicted");
        } catch (Throwable e) {
            SpiderDebug.log("proxy", "connection pool evict failed error=%s", e.getMessage());
        }
    }

    public static synchronized OkHttpClient client() {
        if (get().client != null) return get().client;
        return get().client = getBuilder().build();
    }

    public static synchronized OkHttpClient player() {
        if (get().player != null) return get().player;
        return get().player = getBuilder()
                .addInterceptor(new KuaishouMediaFallbackInterceptor())
                .eventListenerFactory(call -> SpiderDebug.isEnabled() ? new DebugEventListener() : EventListener.NONE)
                .build();
    }

    public static OkHttpClient client(long timeout) {
        return client().newBuilder().connectTimeout(timeout, TimeUnit.MILLISECONDS).readTimeout(timeout, TimeUnit.MILLISECONDS).writeTimeout(timeout, TimeUnit.MILLISECONDS).build();
    }

    public static OkHttpClient noRedirect() {
        return noRedirect(TIMEOUT);
    }

    public static OkHttpClient noRedirect(long timeout) {
        return client().newBuilder().connectTimeout(timeout, TimeUnit.MILLISECONDS).readTimeout(timeout, TimeUnit.MILLISECONDS).writeTimeout(timeout, TimeUnit.MILLISECONDS).followRedirects(false).followSslRedirects(false).build();
    }

    public static OkHttpClient client(boolean redirect, long timeout) {
        return redirect ? client(timeout) : noRedirect(timeout);
    }

    public static String string(String url) {
        if (!url.startsWith("http")) return "";
        try (Response res = newCall(url).execute()) {
            return res.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String string(String url, long timeout) {
        if (!url.startsWith("http")) return "";
        try (Response res = newCall(client(timeout), url).execute()) {
            return res.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String string(String url, Map<String, String> headers) {
        if (!url.startsWith("http")) return "";
        try (Response res = newCall(url, headers).execute()) {
            return res.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String string(String url, Map<String, String> headers, long timeout) {
        if (!url.startsWith("http")) return "";
        try (Response res = newCall(client(timeout), url, headers).execute()) {
            return res.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Call newCall(String url) {
        return client().newCall(new Request.Builder().url(url).build());
    }

    public static Call newCall(String url, String tag) {
        return client().newCall(new Request.Builder().url(url).tag(tag).build());
    }

    public static Call newCall(OkHttpClient client, String url) {
        return client.newCall(new Request.Builder().url(url).build());
    }

    public static Call newCall(OkHttpClient client, String url, String tag) {
        return client.newCall(new Request.Builder().url(url).tag(tag).build());
    }

    public static Call newCall(String url, Map<String, String> headers) {
        return client().newCall(new Request.Builder().url(url).headers(Headers.of(headers)).build());
    }

    public static Call newCall(OkHttpClient client, String url, Map<String, String> headers) {
        return client.newCall(new Request.Builder().url(url).headers(Headers.of(headers)).build());
    }

    public static Call newCall(String url, Map<String, String> headers, ArrayMap<String, String> params) {
        return client().newCall(new Request.Builder().url(buildUrl(url, params)).headers(Headers.of(headers)).build());
    }

    public static Call newCall(String url, Map<String, String> headers, RequestBody body) {
        return client().newCall(new Request.Builder().url(url).headers(Headers.of(headers)).post(body).build());
    }

    public static Call newCall(String url, RequestBody body, String tag) {
        return client().newCall(new Request.Builder().url(url).post(body).tag(tag).build());
    }

    public static Call newCall(OkHttpClient client, String url, RequestBody body) {
        return client.newCall(new Request.Builder().url(url).post(body).build());
    }

    public static void cancel(String tag) {
        cancel(client(), tag);
    }

    public static void cancel(OkHttpClient client, String tag) {
        for (Call call : client.dispatcher().queuedCalls()) if (tag.equals(call.request().tag())) call.cancel();
        for (Call call : client.dispatcher().runningCalls()) if (tag.equals(call.request().tag())) call.cancel();
    }

    public static void cancelAll() {
        cancelAll(client());
    }

    public static void cancelAll(OkHttpClient client) {
        client.dispatcher().cancelAll();
    }

    public static FormBody toBody(ArrayMap<String, String> params) {
        FormBody.Builder body = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) body.add(entry.getKey(), entry.getValue());
        return body.build();
    }

    private static HttpUrl buildUrl(String url, ArrayMap<String, String> params) {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) builder.addQueryParameter(entry.getKey(), entry.getValue());
        return builder.build();
    }

    private static OkHttpClient.Builder getBuilder() {
        // P0 安全修复：不再自定义 hostnameVerifier / SSLSocketFactory，改用 OkHttp 默认行为，
        // 即系统 TrustManager 校验服务器证书链 + 校验 hostname 与证书是否匹配。
        // 此前 hostnameVerifier 恒返回 true 且 trustAllCertificates() 空实现，等于接受任意
        // 自签/过期/错 host 证书，https 形同虚设，中间人可窃听或篡改源站抓取流量。
        // 源站抓取仍走同一个 client：http 源站不受影响，只有 https 源站现在会被正常校验，
        // 证书有问题的 https 源站会请求失败（而不是静默接受），这是有意为之。
        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(requestInterceptor()).addInterceptor(authInterceptor()).addNetworkInterceptor(responseInterceptor()).connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS).readTimeout(TIMEOUT, TimeUnit.MILLISECONDS).writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS).dns(dns());
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.proxyAuthenticator(authenticator());
        //builder.addNetworkInterceptor(logging);
        builder.proxySelector(selector());
        return builder;
    }

    // P0 安全修复：getSSLContext() / trustAllCertificates() 已删除，不再保留接受任意证书的
    // TrustManager。OkHttp 未配置 sslSocketFactory 时默认使用系统 TrustManager。

    private static class DebugEventListener extends EventListener {

        private final long startNs;

        private DebugEventListener() {
            this.startNs = System.nanoTime();
        }

        @Override
        public void callStart(@NonNull Call call) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "start", "method=" + call.request().method() + OkHttpLogPolicy.requestMetadata(call.request().headers()));
        }

        @Override
        public void connectStart(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull java.net.Proxy proxy) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "connectStart", "host=" + OkHttpLogPolicy.redactHost(inetSocketAddress.getHostString()) + ", proxyType=" + proxy.type());
        }

        @Override
        public void connectEnd(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull java.net.Proxy proxy, @Nullable Protocol protocol) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "connectEnd", "host=" + OkHttpLogPolicy.redactHost(inetSocketAddress.getHostString()) + ", proxyType=" + proxy.type() + ", protocol=" + protocol);
        }

        @Override
        public void connectFailed(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull java.net.Proxy proxy, @Nullable Protocol protocol, @NonNull IOException ioe) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "connectFailed", "host=" + OkHttpLogPolicy.redactHost(inetSocketAddress.getHostString()) + ", proxyType=" + proxy.type() + ", protocol=" + protocol + ", error=" + error(ioe));
        }

        @Override
        public void connectionAcquired(@NonNull Call call, @NonNull Connection connection) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "connectionAcquired", "protocol=" + connection.protocol() + ", proxyType=" + connection.route().proxy().type());
        }

        @Override
        public void responseHeadersEnd(@NonNull Call call, @NonNull Response response) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "response", "code=" + response.code() + ", message=" + response.message() + ", contentLength=" + response.header("Content-Length") + ", contentType=" + response.header("Content-Type") + ", contentRange=" + response.header("Content-Range"));
        }

        @Override
        public void callEnd(@NonNull Call call) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "end", "elapsedMs=" + elapsedMs());
        }

        @Override
        public void callFailed(@NonNull Call call, @NonNull IOException ioe) {
            if (!SpiderDebug.isEnabled()) return;
            log(call, "failed", "elapsedMs=" + elapsedMs() + ", error=" + error(ioe));
        }

        private void log(Call call, String event, String message) {
            SpiderDebug.log("okhttp-player", "%s url=%s %s", event, OkHttpLogPolicy.redactUrl(call.request().url()), message);
        }

        private long elapsedMs() {
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        }

        private String error(Throwable error) {
            return OkHttpLogPolicy.errorChain(error);
        }
    }

    public void clear() {
        cancelAll();
        dns().clear();
        selector().clear();
        authInterceptor().clear();
        requestInterceptor().clear();
        responseInterceptor().clear();
    }

    private static class Loader {
        static volatile OkHttp INSTANCE = new OkHttp();
    }
}
