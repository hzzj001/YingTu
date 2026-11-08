package com.yingtu.plus.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yingtu.plus.R;
import com.yingtu.plus.bean.Vod;

import java.util.ArrayList;
import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 快搜结果适配器（VideoActivity 的快搜对话框）：显示搜索到的影片海报与名称，
 * 点击跳播；支持清空、追加、按位置取项与删除。
 * 布局复用已恢复的旧线 item_grid_movie，海报加载沿用 GridMovieAdapter 的 Glide 写法。
 */
public class QuickAdapter extends RecyclerView.Adapter<QuickAdapter.VH> {

    public interface OnClickListener {
        void onItemClick(Vod vod);
    }

    private final OnClickListener listener;
    private final List<Vod> data = new ArrayList<>();

    public QuickAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public List<Vod> getItems() {
        return data;
    }

    public Vod get(int position) {
        return position >= 0 && position < data.size() ? data.get(position) : null;
    }

    /** 最佳匹配位置（兼容实现：首项）。 */
    public int getBestPosition() {
        return data.isEmpty() ? -1 : 0;
    }

    public void addAll(List<Vod> items) {
        if (items == null) return;
        int start = data.size();
        data.addAll(items);
        notifyItemRangeInserted(start, items.size());
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position < 0 || position >= data.size()) return;
        data.remove(position);
        notifyItemRemoved(position);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_movie, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Vod vod = data.get(position);
        h.tvName.setText(vod.getName());
        Glide.with(h.itemView.getContext())
                .load(vod.getPic())
                .placeholder(R.drawable.bg_poster_placeholder)
                .error(R.drawable.bg_poster_placeholder)
                .centerCrop()
                .into(h.ivPoster);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(vod);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivPoster;
        final TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
