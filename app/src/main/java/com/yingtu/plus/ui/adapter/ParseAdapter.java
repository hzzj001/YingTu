package com.yingtu.plus.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yingtu.plus.R;
import com.yingtu.plus.api.config.VodConfig;
import com.yingtu.plus.bean.Parse;

import java.util.ArrayList;
import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 解析源适配器（VideoActivity 控制栏里的解析线路条）：显示各解析源名，
 * 点击切换解析源；reload() 从 VodConfig 重新拉取解析源列表。
 * 布局复用已恢复的旧线 item_category_tab。
 */
public class ParseAdapter extends RecyclerView.Adapter<ParseAdapter.VH> {

    public interface OnClickListener {
        void onItemClick(Parse parse);
    }

    private final OnClickListener listener;
    private final int viewType;
    private final List<Parse> data = new ArrayList<>();
    private int selectedPosition = -1;

    public ParseAdapter(OnClickListener listener, int viewType) {
        this.listener = listener;
        this.viewType = viewType;
    }

    /** 从 VodConfig 重新拉取解析源列表。 */
    public void reload() {
        data.clear();
        data.addAll(VodConfig.get().getParses());
        selectedPosition = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSelected()) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public Parse first() {
        return data.isEmpty() ? null : data.get(0);
    }

    public Parse get(int position) {
        return position >= 0 && position < data.size() ? data.get(position) : null;
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
        Parse parse = data.get(position);
        h.tvName.setText(parse.getName());
        h.tvName.setSelected(position == selectedPosition);
        h.itemView.setOnClickListener(v -> {
            selectedPosition = h.getBindingAdapterPosition();
            notifyDataSetChanged();
            if (listener != null) listener.onItemClick(parse);
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
