package com.yingtu.plus.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yingtu.plus.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 选集分组适配器（VideoActivity 的剧集分组条）：剧集多时按 50 集一组
 * （如 1-50、51-100）分组展示，点击跳到对应分组。
 * 布局复用已恢复的旧线 item_category_tab。
 */
public class EpisodeGroupAdapter extends RecyclerView.Adapter<EpisodeGroupAdapter.VH> {

    public interface OnClickListener {
        void onItemClick(Group group);
    }

    /** 一组剧集：起止集号（VideoActivity 用 group.start / group.end 做范围判断）。 */
    public static final class Group {
        public final int start;
        public final int end;
        public final String name;
        public boolean selected;

        public Group(int start, int end) {
            this.start = start;
            this.end = end;
            this.name = (start + 1) + "-" + end;
        }
    }

    /** 按总集数分组（每组 50 集）；selectedPosition 所在组为选中组；reverse 为真时倒序。 */
    public static List<Group> build(int size, int selectedPosition, boolean reverse) {
        List<Group> groups = new ArrayList<>();
        for (int start = 0; start < size; start += 50) {
            int end = Math.min(start + 50, size);
            Group group = new Group(start, end);
            group.selected = selectedPosition >= start && selectedPosition < end;
            groups.add(group);
        }
        if (reverse) Collections.reverse(groups);
        return groups;
    }

    private final OnClickListener listener;
    private final List<Group> data = new ArrayList<>();
    private int selectedPosition = -1;

    public EpisodeGroupAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void addAll(List<Group> items) {
        data.clear();
        if (items != null) data.addAll(items);
        selectedPosition = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).selected) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public List<Group> getItems() {
        return data;
    }

    public void setSelected(Group group) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).selected = data.get(i) == group;
            if (data.get(i) == group) selectedPosition = i;
        }
        notifyDataSetChanged();
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_tab, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Group group = data.get(position);
        h.tvName.setText(group.name);
        h.tvName.setSelected(position == selectedPosition);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(group);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
