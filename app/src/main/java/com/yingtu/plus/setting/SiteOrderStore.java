// 移植自上游 fish2018/webhtv app/src/main/java/com/fongmi/android/tv/setting/SiteOrderStore.java @ HEAD 4e30ffa（2026-10-07），仅包名 com.fongmi.android.tv → com.yingtu.plus；逻辑未改。
// 原桩的 getOrdered/putOrdered（键 site_order）无调用方，已随整文件替换为上游版（按 cid 分键 site_dialog_order_<cid>，sortSites/save）。
package com.yingtu.plus.setting;

import android.text.TextUtils;

import com.yingtu.plus.App;
import com.yingtu.plus.api.config.VodConfig;
import com.yingtu.plus.bean.Site;
import com.github.catvod.utils.Prefers;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteOrderStore {

    private static final String KEY_PREFIX = "site_dialog_order_";
    private static final Type TYPE = new TypeToken<List<String>>() {}.getType();

    public static void sortSites(List<Site> sites) {
        if (sites == null || sites.size() < 2) return;
        List<String> keys = load();
        if (keys.isEmpty()) return;
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) indexes.put(keys.get(i), i);
        sites.sort((a, b) -> Integer.compare(indexes.getOrDefault(a.getKey(), Integer.MAX_VALUE), indexes.getOrDefault(b.getKey(), Integer.MAX_VALUE)));
    }

    public static void save(List<Site> sites) {
        if (sites == null) return;
        List<String> keys = new ArrayList<>();
        for (Site site : sites) {
            if (site == null || TextUtils.isEmpty(site.getKey())) continue;
            keys.add(site.getKey());
        }
        Prefers.put(key(), App.gson().toJson(keys));
    }

    private static List<String> load() {
        try {
            List<String> keys = App.gson().fromJson(Prefers.getString(key(), "[]"), TYPE);
            return keys == null ? new ArrayList<>() : keys;
        } catch (Throwable e) {
            return new ArrayList<>();
        }
    }

    private static String key() {
        return KEY_PREFIX + VodConfig.getCid();
    }
}
