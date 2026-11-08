package com.yingtu.plus.util;

import android.util.Log;

import com.yingtu.plus.BuildConfig;

/**
 * 应用统一日志工具（P0 诊断步骤引入，见 REMAINING_CATCHES.md）。
 *
 * 用途：把散落在各处的 {@code e.printStackTrace()} 与空 catch 收敛到一处，
 * 让失败可定位、可按 TAG 过滤，而不是静默丢弃。
 *
 * 约定：
 * - TAG 统一加 {@code YingTu/} 前缀，调用方传短模块名（如 "ApiConfig"、"LiveParser"）。
 * - debug/info 只在 debug 包输出（BuildConfig.DEBUG 控制），release 包不刷详细日志；
 *   warn/error 始终输出，因为它们对应用户可见的失败。
 * - 带 Throwable 的重载用于异常分支；无异常的用 format 重载，不要把异常拼进消息里。
 * - 本类只记日志、不弹提示；要不要告诉用户由调用方决定（Toast / 现有错误回调）。
 */
public final class AppLog {

    private static final String PREFIX = "YingTu/";

    private AppLog() {
    }

    /** 详细日志：仅 debug 包输出。最后一个参数是 Throwable 时带堆栈输出。 */
    public static void d(String tag, String format, Object... args) {
        if (!BuildConfig.DEBUG) return;
        Throwable t = lastThrowable(args);
        if (t == null) Log.d(PREFIX + tag, format(format, args));
        else Log.d(PREFIX + tag, format(format, dropLast(args)), t);
    }

    /** 一般信息：仅 debug 包输出。最后一个参数是 Throwable 时带堆栈输出。 */
    public static void i(String tag, String format, Object... args) {
        if (!BuildConfig.DEBUG) return;
        Throwable t = lastThrowable(args);
        if (t == null) Log.i(PREFIX + tag, format(format, args));
        else Log.i(PREFIX + tag, format(format, dropLast(args)), t);
    }

    /** 可恢复的异常 / 降级：始终输出。 */
    public static void w(String tag, String message, Throwable t) {
        Log.w(PREFIX + tag, message, t);
    }

    /** 可恢复的异常 / 降级（无堆栈）：始终输出。最后一个参数是 Throwable 时带堆栈输出。 */
    public static void w(String tag, String format, Object... args) {
        Throwable t = lastThrowable(args);
        if (t == null) Log.w(PREFIX + tag, format(format, args));
        else Log.w(PREFIX + tag, format(format, dropLast(args)), t);
    }

    /** 导致功能失败的异常：始终输出。 */
    public static void e(String tag, String message, Throwable t) {
        Log.e(PREFIX + tag, message, t);
    }

    /** 导致功能失败的情况（无堆栈）：始终输出。最后一个参数是 Throwable 时带堆栈输出。 */
    public static void e(String tag, String format, Object... args) {
        Throwable t = lastThrowable(args);
        if (t == null) Log.e(PREFIX + tag, format(format, args));
        else Log.e(PREFIX + tag, format(format, dropLast(args)), t);
    }

    /** 取出参数列表末尾的 Throwable（调用方把异常放最后即可带堆栈），没有则返回 null。 */
    private static Throwable lastThrowable(Object... args) {
        if (args == null || args.length == 0) return null;
        Object last = args[args.length - 1];
        return last instanceof Throwable ? (Throwable) last : null;
    }

    private static Object[] dropLast(Object... args) {
        Object[] kept = new Object[args.length - 1];
        System.arraycopy(args, 0, kept, 0, kept.length);
        return kept;
    }

    private static String format(String format, Object... args) {
        if (format == null) return "";
        if (args == null || args.length == 0) return format;
        try {
            return String.format(format, args);
        } catch (Throwable ignored) {
            return format;
        }
    }
}
