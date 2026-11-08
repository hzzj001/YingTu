package com.yingtu.plus.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yingtu.plus.R;
import com.yingtu.plus.bean.Episode;

import java.util.ArrayList;
import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 选集列表适配器（VideoActivity 的剧集网格 / 列表）：显示一集的名称，
 * 点击切集；记录当前选中集（getActivated / getPosition）。
 * 布局复用已恢复的旧线 item_series。
 */
public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.VH> {

    public interface OnClickListener {
        void onItemClick(Episode episode);
    }

    private final OnClickListener listener;
    private final int viewType;
    private final List<Episode> data = new ArrayList<>();
    private int selectedPosition = -1;

    public EpisodeAdapter(OnClickListener listener, int viewType) {
        this.listener = listener;
        this.viewType = viewType;
    }

    public void addAll(List<Episode> items) {
        data.clear();
        if (items != null) data.addAll(items);
        selectedPosition = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSelected()) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public List<Episode> getItems() {
        return data;
    }

    public Episode get(int position) {
        return position >= 0 && position < data.size() ? data.get(position) : null;
    }

    /** 当前选中集（未选中时返回 null）。 */
    public Episode getActivated() {
        return selectedPosition >= 0 && selectedPosition < data.size() ? data.get(selectedPosition) : null;
    }

    public int getPosition() {
        return selectedPosition;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Episode episode = data.get(position);
        h.tvSeries.setText(episode.getName());
        h.tvSeries.setSelected(position == selectedPosition);
        h.itemView.setOnClickListener(v -> {
            selectedPosition = h.getBindingAdapterPosition();
            notifyDataSetChanged();
            if (listener != null) listener.onItemClick(episode);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvSeries;

        VH(@NonNull View itemView) {
            super(itemView);
            tvSeries = itemView.findViewById(R.id.tvSeries);
        }
    }
}
