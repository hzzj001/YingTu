package com.yingtu.plus.api.loader;

import android.net.Uri;
import android.text.TextUtils;

import com.yingtu.plus.App;
import com.yingtu.plus.util.AppLog;
import com.yingtu.plus.utils.Notify;
import com.yingtu.plus.utils.UrlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * 远程代码加载校验（P0，第 4 步引入）。
 *
 * 三类加载器（JarLoader / JsLoader / PyLoader）共用，校验的是被加载的代码文件
 * （jar / py / js）本身的下载地址，不是配置 JSON 的地址：TVBox 配置源本身常是
 * http，但代码文件必须走加密通道或本机来源。
 *
 * 两条规则：
 * 1. 来源校验：远程地址必须是 https，否则拒绝加载。本机来源放行，包括 file://、
 *    assets 内置、无 scheme 的本地路径，以及经 UrlUtil.convert 转换后的本机服务
 *    地址（http://127.0.0.1:端口/...，WebServer.getAddress 的产物）。
 * 2. 完整性校验：配置里可选的 "sha256" 字段提供时，下载完成后算 SHA-256 比对，
 *    通过才加载，不通过拒绝加载；不提供时只记警告日志并照常加载——现存源站基本
 *    都没配哈希，直接断掉会让大量可用源不可用，校验是渐进收紧而非一刀切。
 */
public final class CodeLoadVerifier {

    private CodeLoadVerifier() {
    }

    /** 本机回环 host：UrlUtil.convert 会把 assets/file 转成本机服务地址，放行。 */
    private static boolean isLoopback(String host) {
        if (TextUtils.isEmpty(host)) return false;
        String h = host.toLowerCase(Locale.ROOT).trim();
        return "127.0.0.1".equals(h) || "localhost".equals(h) || "::1".equals(h) || "[::1]".equals(h);
    }

    /**
     * 地址是否允许作为代码来源：本机来源放行，远程只允许 https。
     * 无 scheme（本地路径 / 纯文件名）视为本机来源。
     */
    public static boolean isAllowed(String address) {
        if (TextUtils.isEmpty(address)) return true;
        String scheme = UrlUtil.scheme(address);
        if (TextUtils.isEmpty(scheme)) return true; // 无 scheme：本地路径，放行
        if ("file".equals(scheme) || "assets".equals(scheme)) return true; // 本机文件 / 内置，放行
        if ("https".equals(scheme)) return true;
        // http 只放行本机服务（UrlUtil.convert 的产物），其余一律拒绝。
        if ("http".equals(scheme)) return isLoopback(Uri.parse(address).getHost());
        return false; // 其他 scheme（如 ftp）不是合法的代码来源，拒绝
    }

    /** 地址是不是经 http(s) 拉取的：只有这类地址才需要下载后做 sha256 校验。 */
    public static boolean isHttp(String address) {
        if (TextUtils.isEmpty(address)) return false;
        String scheme = UrlUtil.scheme(address);
        return "http".equals(scheme) || "https".equals(scheme);
    }

    /** 归一化配置里的 sha256：去空白、转小写；未提供返回空串。 */
    public static String normalizeSha256(String raw) {
        if (TextUtils.isEmpty(raw)) return "";
        return raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    /** 期望的 sha256 是否合法：64 位小写 hex，否则配置本身有误、永远比对不上。 */
    private static boolean isValidSha256(String expected) {
        return expected.length() == 64 && expected.matches("[0-9a-f]{64}");
    }

    /**
     * 校验已下载文件的 SHA-256。
     *
     * @return true=通过加载；false=拒绝加载。未提供 sha256 时记警告并返回 true：
     * 现存源站基本都没配哈希，直接断掉会让大量可用源不可用，所以只警告不断连，
     * 等源站逐步补上哈希后再收紧；已提供但格式错误 / 比对不上时一律拒绝。
     */
    public static boolean verifyFile(File file, String expectedRaw, String tag, String label) {
        String expected = normalizeSha256(expectedRaw);
        if (TextUtils.isEmpty(expected)) {
            // 取舍：无哈希不断连，只警告（见类注释第 2 条）。
            AppLog.w(tag, "代码文件未提供 sha256，未做完整性校验直接加载：%s", label);
            return true;
        }
        if (!isValidSha256(expected)) {
            AppLog.e(tag, "代码文件 sha256 格式错误（应为 64 位 hex），已拒绝加载：%s", label);
            notifyUser("代码文件校验配置错误，已拒绝加载");
            return false;
        }
        if (file == null || !file.exists()) {
            AppLog.e(tag, "代码文件不存在，无法校验 sha256，已拒绝加载：%s", label);
            notifyUser("代码文件下载失败，已拒绝加载");
            return false;
        }
        try {
            String actual = sha256Hex(file);
            if (expected.equals(actual)) return true;
            AppLog.e(tag, "代码文件 sha256 不一致，已拒绝加载：%s", label);
            notifyUser("代码文件校验失败，已拒绝加载");
            return false;
        } catch (Throwable e) {
            AppLog.e(tag, "代码文件 sha256 计算失败，已拒绝加载：%s", label, e);
            notifyUser("代码文件校验失败，已拒绝加载");
            return false;
        }
    }

    /** 校验一段已下载内容的 SHA-256（js 源预取文本用），规则与 verifyFile 相同。 */
    public static boolean verifyBytes(byte[] data, String expectedRaw, String tag, String label) {
        String expected = normalizeSha256(expectedRaw);
        if (TextUtils.isEmpty(expected)) {
            // 取舍：无哈希不断连，只警告（见类注释第 2 条）。
            AppLog.w(tag, "代码文件未提供 sha256，未做完整性校验直接加载：%s", label);
            return true;
        }
        if (!isValidSha256(expected)) {
            AppLog.e(tag, "代码文件 sha256 格式错误（应为 64 位 hex），已拒绝加载：%s", label);
            notifyUser("代码文件校验配置错误，已拒绝加载");
            return false;
        }
        if (data == null) {
            AppLog.e(tag, "代码内容为空，无法校验 sha256，已拒绝加载：%s", label);
            notifyUser("代码文件下载失败，已拒绝加载");
            return false;
        }
        try {
            String actual = sha256Hex(data);
            if (expected.equals(actual)) return true;
            AppLog.e(tag, "代码文件 sha256 不一致，已拒绝加载：%s", label);
            notifyUser("代码文件校验失败，已拒绝加载");
            return false;
        } catch (Throwable e) {
            AppLog.e(tag, "代码文件 sha256 计算失败，已拒绝加载：%s", label, e);
            notifyUser("代码文件校验失败，已拒绝加载");
            return false;
        }
    }

    /** 来源不是 https 的拒绝日志 + 用户提示（只记地址的 scheme，不记完整 URL）。 */
    public static void rejectNonHttps(String address, String tag, String label) {
        AppLog.e(tag, "代码文件不是 https 来源，已拒绝加载：%s scheme=%s", label, UrlUtil.scheme(address));
        notifyUser("代码文件不是加密链接，已拒绝加载");
    }

    /** 用户可读提示：加载器常在后台线程跑，Toast 必须切回主线程。 */
    public static void notifyUser(String message) {
        App.post(() -> Notify.show(message));
    }

    /** 文件流式算 SHA-256 hex（小写），避免大 jar 一次读进内存。 */
    public static String sha256Hex(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) digest.update(buf, 0, n);
        }
        return toHex(digest.digest());
    }

    /** 字节数组算 SHA-256 hex（小写）。 */
    public static String sha256Hex(byte[] data) throws Exception {
        return toHex(MessageDigest.getInstance("SHA-256").digest(data));
    }

    /** 文本按 UTF-8 算 SHA-256 hex（小写），js 预取内容用。 */
    public static String sha256Hex(String text) throws Exception {
        return sha256Hex(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) builder.append(String.format(Locale.ROOT, "%02x", b));
        return builder.toString();
    }
}
