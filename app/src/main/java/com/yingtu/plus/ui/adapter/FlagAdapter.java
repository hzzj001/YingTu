package com.yingtu.plus.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yingtu.plus.R;
import com.yingtu.plus.bean.Flag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 线路（Flag）适配器（VideoActivity 的线路条）：显示各线路名，
 * 点击切线路；记录当前选中线路（getActivated / getPosition），
 * 支持按线路名选中、切换选中、倒序。
 * 布局复用已恢复的旧线 item_series_flag。
 */
public class FlagAdapter extends RecyclerView.Adapter<FlagAdapter.VH> {

    public interface OnClickListener {
        void onItemClick(Flag flag);
    }

    private final OnClickListener listener;
    private final List<Flag> data = new ArrayList<>();
    private int selectedPosition = -1;

    public FlagAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void addAll(List<Flag> items) {
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

    public void add(Flag item) {
        if (item == null) return;
        data.add(item);
        notifyItemInserted(data.size() - 1);
    }

    public List<Flag> getItems() {
        return data;
    }

    public Flag get(int position) {
        return position >= 0 && position < data.size() ? data.get(position) : null;
    }

    /** 当前选中线路（未选中时返回 null）。 */
    public Flag getActivated() {
        return selectedPosition >= 0 && selectedPosition < data.size() ? data.get(selectedPosition) : null;
    }

    public int getPosition() {
        return selectedPosition;
    }

    public void setSelected(Flag item) {
        if (item == null) return;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) == item || TextUtils.equals(data.get(i).getShow(), item.getShow())) {
                selectedPosition = i;
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void setSelected(String flag) {
        if (flag == null) return;
        for (int i = 0; i < data.size(); i++) {
            if (TextUtils.equals(data.get(i).getFlag(), flag) || TextUtils.equals(data.get(i).getShow(), flag)) {
                selectedPosition = i;
                notifyDataSetChanged();
                return;
            }
        }
    }

    /** 切换选中到指定线路（与 setSelected 同义，调用点用它做切换）。 */
    public void toggle(Flag item) {
        setSelected(item);
    }

    /** 倒序排列线路。 */
    public void reverse() {
        Collections.reverse(data);
        if (selectedPosition >= 0) selectedPosition = data.size() - 1 - selectedPosition;
        notifyDataSetChanged();
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series_flag, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Flag flag = data.get(position);
        h.tvFlag.setText(flag.getShow());
        h.tvFlag.setSelected(position == selectedPosition);
        h.itemView.setOnClickListener(v -> {
            selectedPosition = h.getBindingAdapterPosition();
            notifyDataSetChanged();
            if (listener != null) listener.onItemClick(flag);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvFlag;

        VH(@NonNull View itemView) {
            super(itemView);
            tvFlag = itemView.findViewById(R.id.tvFlag);
        }
    }
}
