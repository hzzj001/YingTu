package com.yingtu.plus.bean;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.yingtu.plus.Constant;
import com.yingtu.plus.api.config.VodConfig;
import com.yingtu.plus.db.AppDatabase;

import java.io.Serializable;
import java.util.List;

/**
 * 播放/观看历史记录。Room 实体（供 HistoryDao 查询），用于「观看历史」页与续播。
 */
@Entity
public class History implements Serializable {

    /** 唯一键：sourceKey + "@@" + vodId */
    @NonNull
    @PrimaryKey
    public String key;
    /** 配置 ID（HistoryDao 按 cid 查，见 History.get(cid)） */
    public int cid;
    /** 创建时间戳（毫秒；HistoryDao 按 createTime 过滤最近记录） */
    public long createTime;
    public String sourceKey;
    public String vodId;
    public String vodName;
    public String vodPic;

    /** 当前线路下标 / 剧集下标 */
    public int playFlag;
    public int playIndex;
    /** 当前集名，用于历史列表角标显示（如「观看至 第3集」） */
    public String episodeName;

    /** 播放进度与时长（毫秒），用于续播 */
    public long position;
    public long duration;

    /** 最近一次观看时间戳 */
    public long updateTime;

    public History() {}
    // 移植自上游 webhtv bean/History.java:116-138（HEAD 4e30ffa），供 SyncDialog 用；仅包名改名。
    public static List<History> get() {
        return get(VodConfig.getCid());
    }

    public static List<History> get(int cid) {
        return AppDatabase.get().getHistoryDao().find(cid, System.currentTimeMillis() - Constant.HISTORY_TIME);
    }

    public static void delete(int cid) {
        AppDatabase.get().getHistoryDao().delete(cid);
    }


    public static String buildKey(String sourceKey, String vodId) {
        return sourceKey + "@@" + vodId;
    }

    /** 已观看百分比（0-100），无时长时返回 0 */
    public int percent() {
        if (duration <= 0) return 0;
        long p = Math.min(position, duration);
        return (int) (p * 100 / duration);
    }

    /** 是否可保存（上游 webhtv：position > 0 才保存；本类 position 为公开字段，等价于上游 getPosition() > 0）。 */
    public boolean canSave() {
        return position > 0;
    }
}
