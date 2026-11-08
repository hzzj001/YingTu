package com.yingtu.plus.ui.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.yingtu.plus.bean.Vod;
import com.yingtu.plus.ui.adapter.QuickAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 快搜对话框（VideoActivity 在播放页内快速搜索影片时用它展示结果）。
 * 兼容实现只保留调用点用到的链式接口与实例方法：结果列表存在内存里，
 * clear()/addAll() 只改内存列表，isActive() 按对话框是否已显示判断，
 * 打开后为空白对话框，不展示结果、不触发搜索回调。
 */
public class QuickSearchDialog extends DialogFragment {

    /** 快搜输入回调（VideoActivity 以 this::onQuickSearch 传入）。 */
    public interface SearchListener {
        void onSearch(String keyword);
    }

    private String title;
    private String keyword;
    private QuickAdapter.OnClickListener listener;
    private SearchListener searchListener;
    private final List<Vod> items = new ArrayList<>();

    public static QuickSearchDialog create() {
        return new QuickSearchDialog();
    }

    public QuickSearchDialog title(String title) {
        this.title = title;
        return this;
    }

    public QuickSearchDialog keyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public QuickSearchDialog listener(QuickAdapter.OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public QuickSearchDialog searchListener(SearchListener searchListener) {
        this.searchListener = searchListener;
        return this;
    }

    public QuickSearchDialog items(List<Vod> items) {
        this.items.clear();
        if (items != null) this.items.addAll(items);
        return this;
    }

    public void show(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            show(((AppCompatActivity) activity).getSupportFragmentManager(), "QuickSearchDialog");
        }
    }

    /** 清空结果（兼容实现：只清内存列表）。 */
    public void clear() {
        items.clear();
    }

    /** 追加结果（兼容实现：只改内存列表）。 */
    public void addAll(List<Vod> items) {
        if (items != null) this.items.addAll(items);
    }

    /** 对话框当前是否已显示。 */
    public boolean isActive() {
        return isAdded() && getDialog() != null && getDialog().isShowing();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }
}
