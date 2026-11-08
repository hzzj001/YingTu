package com.yingtu.plus.utils;

import com.yingtu.plus.App;
import com.yingtu.plus.api.config.LiveConfig;
import com.yingtu.plus.api.config.VodConfig;
import com.yingtu.plus.api.config.WallConfig;
import com.yingtu.plus.api.loader.BaseLoader;
import com.yingtu.plus.bean.Backup;
import com.yingtu.plus.event.ConfigEvent;
import com.yingtu.plus.event.RefreshEvent;
import com.yingtu.plus.impl.Callback;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Path;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class AppBackup {

    public static final String PREFIX = "bak-";
    public static final String SUFFIX = ".zip";

    private static final String LEGACY_PREFIX = "webhtv-backup-";

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm", Locale.ROOT);
    private static final String DATA = "backup.json";
    private static final String MANIFEST = "manifest.json";
    /**
     * SHA-256 完整性清单文件名。内容每行 "文件名 + 空格 + sha256 小写 hex"，
     * 覆盖 zip 内除清单自身外的每个文件；生成时最后写入，恢复时先校验再写入。
     */
    private static final String SHA256_MANIFEST = "MANIFEST.sha256";
    private static final String SHARED = "shared-files.zip";
    private static final String LOGIN = "login-state.zip";
    private static final String APP_FILES = "app-files/";
    private static final int BUFFER_SIZE = 128 * 1024;

    private AppBackup() {
    }

    public static String fileName() {
        return PREFIX + LocalDateTime.now().format(STAMP) + SUFFIX;
    }

    public static boolean isBackup(File file) {
        if (file == null || !file.isFile()) return false;
        String name = file.getName();
        return isBackupZipName(name) || (name.startsWith("tv-") && name.endsWith(".bk.gz"));
    }

    public static boolean isBackupZipName(String name) {
        return hasBackupPrefix(name) && name.endsWith(SUFFIX);
    }

    public static boolean isBackupManifestName(String name) {
        return hasBackupPrefix(name) && name.endsWith(".json");
    }

    public static String backupSortKey(String name) {
        if (!isBackupZipName(name)) return "";
        int prefixLength = name.startsWith(PREFIX) ? PREFIX.length() : LEGACY_PREFIX.length();
        return name.substring(prefixLength, name.length() - SUFFIX.length());
    }

    private static boolean hasBackupPrefix(String name) {
        return name != null && (name.startsWith(PREFIX) || name.startsWith(LEGACY_PREFIX));
    }

    public static File createTemp() throws IOException {
        return createTemp(null);
    }

    public static File createTemp(Progress progress) throws IOException {
        File file = File.createTempFile(PREFIX, SUFFIX, Path.cache());
        create(file, progress);
        return file;
    }

    public static CreateResult create(File target) throws IOException {
        return create(target, null);
    }

    public static synchronized CreateResult create(File target, Progress progress) throws IOException {
        SyncFiles.Archive shared = null;
        LoginStateSync.Archive login = null;
        try {
            notifyProgress(progress, "整理数据库和设置", 5, 0, 0);
            Backup backup = Backup.create();
            if (backup.getConfig().isEmpty()) throw new IOException("没有可备份的接口配置");
            notifyProgress(progress, "整理共享数据文件", 15, 0, 0);
            StringBuilder warning = new StringBuilder();
            int customCspSourceFiles = 0;
            int customCspFiles = 0;
            try {
                customCspSourceFiles = SyncFiles.countFiles(SyncFiles.CUSTOM_CSP_PATH);
                shared = SyncFiles.createArchive(SyncFiles.getPaths(SyncFiles.DEFAULT_PATHS));
                customCspFiles = SyncFiles.countArchiveFiles(shared == null ? null : shared.getFile(), SyncFiles.CUSTOM_CSP_PATH);
                if (customCspFiles < customCspSourceFiles) appendWarning(warning, "站点注入文件未完整写入备份");
            } catch (Throwable e) {
                if (shared != null) shared.delete();
                shared = null;
                appendWarning(warning, "共享数据文件未写入备份");
                SpiderDebug.log("backup", "shared archive warning error=%s", e.getMessage());
            }
            notifyProgress(progress, "整理登录态和云盘凭据", 30, shared == null ? 0 : shared.getZipSize(), 0);
            try {
                login = LoginStateSync.createArchive();
            } catch (Throwable e) {
                login = null;
                appendWarning(warning, "登录态和云盘凭据未写入备份");
                SpiderDebug.log("backup", "login archive warning error=%s", e.getMessage());
            }
            String warningText = warning.toString();
            byte[] data = backup.toString().getBytes(StandardCharsets.UTF_8);
            byte[] manifest = manifest(shared, login, customCspSourceFiles, customCspFiles, backup.getWebHomeExtensionPreferenceCount(), backup.getWebHomeExtensionSourceCount(), warningText).getBytes(StandardCharsets.UTF_8);
            // 清单在最后写入：先把前面每个条目的 SHA-256 记下来，再生成清单。
            Map<String, String> sha256 = new LinkedHashMap<>();
            long total = data.length + manifest.length + appFilesSize(Path.files(), Path.files()) + 2048;
            if (shared != null) total += shared.getFile().length();
            if (login != null) total += login.getFile().length();
            Counter counter = new Counter(total, progress);
            try (ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(Path.create(target)), BUFFER_SIZE))) {
                addBytes(output, DATA, data, counter, sha256);
                addBytes(output, MANIFEST, manifest, counter, sha256);
                if (shared != null) addFile(output, SHARED, shared.getFile(), counter, sha256);
                if (login != null) addFile(output, LOGIN, login.getFile(), counter, sha256);
                addAppFiles(output, Path.files(), Path.files(), counter, sha256);
                // 清单自身不进清单：最后写入，hashes 传 null。
                addBytes(output, SHA256_MANIFEST, buildSha256Manifest(sha256).getBytes(StandardCharsets.UTF_8), counter, null);
            }
            notifyProgress(progress, warningText.isEmpty() ? "备份完成" : "备份完成，但部分数据未写入", 100, target.length(), target.length());
            SpiderDebug.log("backup", "create complete file=%s size=%d warning=%s", target.getAbsolutePath(), target.length(), warningText);
            return new CreateResult(warningText);
        } catch (Throwable e) {
            Path.clear(target);
            if (e instanceof IOException) throw (IOException) e;
            throw new IOException(e);
        } finally {
            if (shared != null) shared.delete();
            if (login != null) login.delete();
        }
    }

    public static RestoreResult restore(File source) throws IOException {
        return restore(source, null);
    }

    public static RestoreResult restore(File source, Progress progress) throws IOException {
        long sourceSize = source == null ? 0 : source.length();
        notifyProgress(progress, "读取备份文件", 5, sourceSize, sourceSize);
        if (!isZip(source)) return restoreLegacy(source, progress);
        File shared = null;
        File login = null;
        String json = "";
        String manifestJson = "";
        String shaManifestText = "";
        Map<String, String> actualSha256 = new LinkedHashMap<>();
        // 应用文件先解到临时文件、校验通过后再落盘；校验失败时不写任何数据。
        List<AppFileTemp> appFileTemps = new ArrayList<>();
        int appFiles = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            try (ZipInputStream input = new ZipInputStream(new BufferedInputStream(new FileInputStream(source), BUFFER_SIZE))) {
                ZipEntry entry;
                while ((entry = input.getNextEntry()) != null) {
                    String name = safe(entry.getName());
                    if (name.isEmpty() || entry.isDirectory()) {
                        input.closeEntry();
                        continue;
                    }
                    if (DATA.equals(name)) {
                        byte[] bytes = readEntry(input, buffer);
                        actualSha256.put(DATA, sha256Hex(bytes));
                        json = new String(bytes, StandardCharsets.UTF_8);
                    } else if (MANIFEST.equals(name)) {
                        byte[] bytes = readEntry(input, buffer);
                        actualSha256.put(MANIFEST, sha256Hex(bytes));
                        manifestJson = new String(bytes, StandardCharsets.UTF_8);
                    } else if (SHA256_MANIFEST.equals(name)) {
                        // 清单自身不参与哈希：只读文本，留待下面统一校验。
                        shaManifestText = new String(readEntry(input, buffer), StandardCharsets.UTF_8);
                    } else if (SHARED.equals(name)) {
                        MessageDigest digest = sha256Digest();
                        shared = extractTemp(input, "webhtv-shared-restore-", buffer, digest);
                        actualSha256.put(SHARED, toHex(digest.digest()));
                    } else if (LOGIN.equals(name)) {
                        MessageDigest digest = sha256Digest();
                        login = extractTemp(input, "webhtv-login-restore-", buffer, digest);
                        actualSha256.put(LOGIN, toHex(digest.digest()));
                    } else if (name.startsWith(APP_FILES)) {
                        String relative = safe(name.substring(APP_FILES.length()));
                        if (!relative.isEmpty()) {
                            // 只解到临时文件：校验失败时直接丢弃，不污染应用目录。
                            MessageDigest digest = sha256Digest();
                            File temp = extractTemp(input, "webhtv-appfile-restore-", buffer, digest);
                            actualSha256.put(name, toHex(digest.digest()));
                            appFileTemps.add(new AppFileTemp(relative, temp));
                        }
                    }
                    input.closeEntry();
                }
            }
            notifyProgress(progress, "校验备份内容", 45, sourceSize, sourceSize);
            // 先校验 SHA-256 清单再写入：任何不匹配都拒绝恢复，不写任何数据。
            String shaError = verifySha256Manifest(shaManifestText, actualSha256);
            if (shaError != null) {
                for (AppFileTemp temp : appFileTemps) Path.clear(temp.file);
                Path.clear(shared);
                Path.clear(login);
                SpiderDebug.log("backup", "restore refused sha256 error=%s", shaError);
                throw new IOException("备份校验失败：" + shaError + "，已拒绝恢复，请重新备份或换用其他备份文件");
            }
        } catch (Throwable e) {
            for (AppFileTemp temp : appFileTemps) Path.clear(temp.file);
            Path.clear(shared);
            Path.clear(login);
            if (e instanceof IOException) throw (IOException) e;
            throw new IOException(e);
        }
        Backup backup = Backup.objectFrom(json);
        try {
            if (backup.getConfig().isEmpty()) throw new IOException("备份缺少接口配置数据");
            String warning = validateManifest(manifestJson, shared, backup);
            StringBuilder restoreWarning = new StringBuilder(warning);
            // 老备份没有清单：不拒绝（保持兼容），但明确提示本次恢复未经完整性校验。
            if (shaManifestText.isEmpty()) appendWarning(restoreWarning, "备份缺少完整性清单，未做 SHA-256 校验");
            notifyProgress(progress, "恢复共享数据文件", 55, sourceSize, sourceSize);
            int sharedFiles = 0;
            try {
                sharedFiles = shared == null ? 0 : SyncFiles.restoreArchive(shared);
            } catch (Throwable e) {
                appendWarning(restoreWarning, "共享数据文件未能完整恢复");
                SpiderDebug.log("backup", "shared restore warning error=%s", e.getMessage());
            }
            notifyProgress(progress, "恢复登录态和云盘凭据", 70, sourceSize, sourceSize);
            int loginFiles = 0;
            try {
                loginFiles = login == null ? 0 : LoginStateSync.restoreArchive(login);
            } catch (Throwable e) {
                appendWarning(restoreWarning, "登录态和云盘凭据未能完整恢复");
                SpiderDebug.log("backup", "login restore warning error=%s", e.getMessage());
            }
            notifyProgress(progress, "恢复应用文件", 80, sourceSize, sourceSize);
            for (AppFileTemp temp : appFileTemps) {
                File target = new File(Path.files(), temp.relative).getCanonicalFile();
                if (inside(Path.files().getCanonicalFile(), target)) {
                    copyFile(temp.file, target);
                    appFiles++;
                }
            }
            notifyProgress(progress, "恢复数据库和设置", 85, sourceSize, sourceSize);
            backup.restore(true);
            reload();
            if (restoreWarning.length() > 0) restoreWarning.append("，其余可用数据已恢复");
            String warningText = restoreWarning.toString();
            notifyProgress(progress, warningText.isEmpty() ? "恢复完成" : "恢复完成，但部分数据缺失", 100, sourceSize, sourceSize);
            SpiderDebug.log("backup", "restore complete shared=%d login=%d app=%d warning=%s", sharedFiles, loginFiles, appFiles, warningText);
            return new RestoreResult(sharedFiles, loginFiles, appFiles, false, warningText);
        } finally {
            for (AppFileTemp temp : appFileTemps) Path.clear(temp.file);
            Path.clear(shared);
            Path.clear(login);
        }
    }

    private static RestoreResult restoreLegacy(File source, Progress progress) throws IOException {
        File restore = Path.cache("restore-legacy");
        try {
            FileUtil.gzipDecompress(source, restore);
            Backup backup = Backup.objectFrom(Path.read(restore));
            if (backup.getConfig().isEmpty()) throw new IOException("旧备份文件无有效配置数据");
            notifyProgress(progress, "恢复旧版数据库和设置", 75, source.length(), source.length());
            backup.restore();
            reload();
            notifyProgress(progress, "恢复完成", 100, source.length(), source.length());
            return new RestoreResult(0, 0, 0, true, "");
        } finally {
            Path.clear(restore);
        }
    }

    private static void reload() {
        BaseLoader.get().clear();
        VodConfig.get().clear().init().load(new Callback());
        LiveConfig.get().clear().init().load();
        WallConfig.get().init().load();
        ConfigEvent.common();
        RefreshEvent.keep();
        RefreshEvent.history();
        RefreshEvent.home();
        Backup.refreshWebHomeExtensions();
    }

    /**
     * 生成 SHA-256 清单：每行 "文件名 + 空格 + sha256 小写 hex"，
     * 覆盖 zip 内除清单自身外的每个文件（调用方负责最后写入清单本身）。
     */
    private static String buildSha256Manifest(Map<String, String> hashes) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            builder.append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
        }
        return builder.toString();
    }

    /**
     * 校验 SHA-256 清单：返回 null 表示通过；返回错误描述表示拒绝恢复。
     * 无清单的老备份返回 null（调用方另行提示未校验，不直接拒绝，保持兼容）。
     * 校验覆盖三点：清单列出的每个文件都在、哈希一致、zip 里没有清单外的文件。
     */
    private static String verifySha256Manifest(String text, Map<String, String> actual) {
        if (text == null || text.isEmpty()) return null;
        Map<String, String> expected = new LinkedHashMap<>();
        for (String raw : text.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            int sep = line.lastIndexOf(' ');
            if (sep <= 0) return "完整性清单格式损坏";
            expected.put(line.substring(0, sep), line.substring(sep + 1));
        }
        if (expected.isEmpty()) return "完整性清单为空";
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String got = actual.get(entry.getKey());
            if (got == null) return "备份缺少文件：" + entry.getKey();
            if (!got.equalsIgnoreCase(entry.getValue())) return "文件已被改动或损坏：" + entry.getKey();
        }
        for (String name : actual.keySet()) {
            if (!expected.containsKey(name)) return "备份含有清单外文件：" + name;
        }
        return null;
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private static String sha256Hex(byte[] data) {
        return toHex(sha256Digest().digest(data));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) builder.append(String.format(Locale.ROOT, "%02x", b));
        return builder.toString();
    }

    /** 扫描阶段解出的单个应用文件：相对路径 + 临时文件，校验通过后才落盘。 */
    private static final class AppFileTemp {

        private final String relative;
        private final File file;

        private AppFileTemp(String relative, File file) {
            this.relative = relative;
            this.file = file;
        }
    }

    private static String manifest(SyncFiles.Archive shared, LoginStateSync.Archive login, int customCspSourceFiles, int customCspFiles, int webHomeExtensionPrefs, int webHomeExtensionSources, String warning) {
        JsonObject object = new JsonObject();
        object.addProperty("app", "WebHTV");
        object.addProperty("version", 3);
        object.addProperty("createdAt", System.currentTimeMillis());
        object.addProperty("sharedFiles", shared == null ? 0 : shared.getCount());
        object.addProperty("loginStateFiles", login == null ? 0 : login.getCount());
        object.addProperty("customCspSourceFiles", customCspSourceFiles);
        object.addProperty("customCspFiles", customCspFiles);
        object.addProperty("webHomeExtensionPrefs", webHomeExtensionPrefs);
        object.addProperty("webHomeExtensionSources", webHomeExtensionSources);
        object.addProperty("warning", warning == null ? "" : warning);
        return App.gson().toJson(object);
    }

    private static String validateManifest(String text, File shared, Backup backup) {
        if (text == null || text.isEmpty()) return "";
        JsonObject object;
        try {
            object = App.gson().fromJson(text, JsonObject.class);
        } catch (Throwable e) {
            SpiderDebug.log("backup", "manifest parse warning error=%s", e.getMessage());
            return "备份清单损坏";
        }
        if (object == null || integer(object, "version") < 3) return "";
        StringBuilder warning = new StringBuilder(string(object, "warning"));
        int expectedCustomCsp = Math.max(integer(object, "customCspSourceFiles"), integer(object, "customCspFiles"));
        try {
            int actualCustomCsp = SyncFiles.countArchiveFiles(shared, SyncFiles.CUSTOM_CSP_PATH);
            if (actualCustomCsp < expectedCustomCsp) appendWarning(warning, "站点注入文件不完整");
        } catch (Throwable e) {
            appendWarning(warning, "站点注入文件无法校验");
        }
        int expectedWebHomePrefs = integer(object, "webHomeExtensionPrefs");
        if (backup.getWebHomeExtensionPreferenceCount() < expectedWebHomePrefs) appendWarning(warning, "WebHome 扩展配置不完整");
        int expectedWebHomeSources = integer(object, "webHomeExtensionSources");
        if (backup.getWebHomeExtensionSourceCount() < expectedWebHomeSources) appendWarning(warning, "WebHome 扩展源不完整");
        if (warning.length() > 0) SpiderDebug.log("backup", "restore manifest warning=%s", warning);
        return warning.toString();
    }

    private static void appendWarning(StringBuilder builder, String message) {
        if (builder.length() > 0) builder.append("、");
        builder.append(message);
    }

    private static int integer(JsonObject object, String key) {
        try {
            return object.has(key) ? Math.max(0, object.get(key).getAsInt()) : 0;
        } catch (Throwable e) {
            return 0;
        }
    }

    private static String string(JsonObject object, String key) {
        try {
            return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : "";
        } catch (Throwable e) {
            return "";
        }
    }

    private static void addAppFiles(ZipOutputStream output, File root, File file, Counter counter, Map<String, String> hashes) throws IOException {
        if (file == null || !file.exists() || skipAppFile(root, file)) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) addAppFiles(output, root, child, counter, hashes);
            return;
        }
        if (!file.isFile()) return;
        String relative = root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
        addFile(output, APP_FILES + relative, file, counter, hashes);
    }

    private static long appFilesSize(File root, File file) {
        if (file == null || !file.exists() || skipAppFile(root, file)) return 0;
        if (file.isFile()) return file.length();
        long size = 0;
        File[] children = file.listFiles();
        if (children != null) for (File child : children) size += appFilesSize(root, child);
        return size;
    }

    private static boolean skipAppFile(File root, File file) {
        String relative = root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
        if (relative.isEmpty()) return false;
        String first = relative.contains("/") ? relative.substring(0, relative.indexOf('/')) : relative;
        if ("mpv".equals(first)) return "mpv/subfont.ttf".equalsIgnoreCase(relative);
        if (first.startsWith("wallpaper_") && !"wallpaper_cache".equals(first)) return false;
        return true;
    }

    private static void addBytes(ZipOutputStream output, String name, byte[] data, Counter counter, Map<String, String> hashes) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(System.currentTimeMillis());
        output.putNextEntry(entry);
        output.write(data);
        output.closeEntry();
        counter.add(data.length);
        // hashes 为 null 表示清单自身：不进清单。
        if (hashes != null) hashes.put(name, sha256Hex(data));
    }

    private static void addFile(ZipOutputStream output, String name, File file, Counter counter, Map<String, String> hashes) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(file.lastModified());
        output.putNextEntry(entry);
        MessageDigest digest = hashes == null ? null : sha256Digest();
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileInputStream input = new FileInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                if (digest != null) digest.update(buffer, 0, read);
                output.write(buffer, 0, read);
                counter.add(read);
            }
        }
        output.closeEntry();
        if (digest != null) hashes.put(name, toHex(digest.digest()));
    }

    private static byte[] readEntry(ZipInputStream input, byte[] buffer) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        return output.toByteArray();
    }

    private static File extractTemp(ZipInputStream input, String prefix, byte[] buffer) throws IOException {
        return extractTemp(input, prefix, buffer, null);
    }

    /** 解出单个 zip 条目到临时文件；digest 非空时边读边算 SHA-256。 */
    private static File extractTemp(ZipInputStream input, String prefix, byte[] buffer, MessageDigest digest) throws IOException {
        File file = File.createTempFile(prefix, ".zip", Path.cache());
        try (FileOutputStream output = new FileOutputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                if (digest != null) digest.update(buffer, 0, read);
                output.write(buffer, 0, read);
            }
        }
        return file;
    }

    /** 校验通过后才调用：把已解出的临时文件落盘到应用目录。 */
    private static void copyFile(File source, File target) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(source), BUFFER_SIZE);
             BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(Path.create(target)), BUFFER_SIZE)) {
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        }
    }

    private static boolean isZip(File file) throws IOException {
        if (file == null || !file.isFile() || file.length() < 4) return false;
        try (FileInputStream input = new FileInputStream(file)) {
            return input.read() == 'P' && input.read() == 'K';
        }
    }

    private static String safe(String path) {
        if (path == null) return "";
        String value = path.replace('\\', '/');
        while (value.startsWith("/")) value = value.substring(1);
        if (value.isEmpty() || value.equals("..") || value.contains("../") || value.contains("/..")) return "";
        return value;
    }

    private static boolean inside(File root, File file) throws IOException {
        String base = root.getCanonicalPath();
        String path = file.getCanonicalPath();
        return path.equals(base) || path.startsWith(base + File.separator);
    }

    private static void notifyProgress(Progress progress, String stage, int percent, long bytes, long total) {
        if (progress != null) progress.onProgress(stage, percent, bytes, total);
    }

    public interface Progress {

        void onProgress(String stage, int percent, long bytes, long total);
    }

    public static final class CreateResult {

        public final String warning;

        private CreateResult(String warning) {
            this.warning = warning == null ? "" : warning;
        }

        public boolean hasWarning() {
            return !warning.isEmpty();
        }
    }

    private static final class Counter {

        private final long total;
        private final Progress progress;
        private long written;

        private Counter(long total, Progress progress) {
            this.total = total;
            this.progress = progress;
        }

        private void add(long size) {
            written += size;
            int percent = total <= 0 ? 40 : 40 + (int) Math.min(55, written * 55 / total);
            notifyProgress(progress, "写入完整备份", percent, written, total);
        }
    }

    public static final class RestoreResult {

        public final int sharedFiles;
        public final int loginFiles;
        public final int appFiles;
        public final boolean legacy;
        public final String warning;

        private RestoreResult(int sharedFiles, int loginFiles, int appFiles, boolean legacy, String warning) {
            this.sharedFiles = sharedFiles;
            this.loginFiles = loginFiles;
            this.appFiles = appFiles;
            this.legacy = legacy;
            this.warning = warning == null ? "" : warning;
        }

        public int fileCount() {
            return sharedFiles + loginFiles + appFiles;
        }

        public boolean hasWarning() {
            return !warning.isEmpty();
        }
    }
}
