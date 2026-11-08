package com.yingtu.plus.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yingtu.plus.App;
import com.yingtu.plus.R;
import com.yingtu.plus.bean.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 清晰度适配器（VideoActivity 的清晰度条）：一个播放结果里有多条清晰度地址时，
 * 每条清晰度一项（显示名取地址的 n 字段，如 720P / 1080P），点击切清晰度。
 * 布局复用已恢复的旧线 item_category_tab。
 */
public class QualityAdapter extends RecyclerView.Adapter<QualityAdapter.VH> {

    public interface OnClickListener {
        void onItemClick(Result result);
    }

    private final OnClickListener listener;
    private final List<Result> data = new ArrayList<>();
    private int selectedPosition = -1;

    public QualityAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    /** 按播放结果的多清晰度地址拆项（每项为原结果的深拷贝，url 位置指向对应清晰度）。 */
    public void addAll(Result result) {
        data.clear();
        selectedPosition = -1;
        if (result != null && result.getUrl() != null && !result.getUrl().getValues().isEmpty()) {
            for (int i = 0; i < result.getUrl().getValues().size(); i++) {
                Result item = Result.fromJson(App.gson().toJson(result));
                if (item == null || item.getUrl() == null) continue;
                item.getUrl().set(i);
                data.add(item);
            }
            selectedPosition = Math.min(result.getUrl().getPosition(), data.size() - 1);
        }
        notifyDataSetChanged();
    }

    public Result get(int position) {
        return position >= 0 && position < data.size() ? data.get(position) : null;
    }

    public int getPosition() {
        return selectedPosition;
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
        Result item = data.get(position);
        String name = item.getUrl() == null ? "" : item.getUrl().n(position);
        if (TextUtils.isEmpty(name)) name = "线路" + (position + 1);
        h.tvName.setText(name);
        h.tvName.setSelected(position == selectedPosition);
        h.itemView.setOnClickListener(v -> {
            selectedPosition = h.getBindingAdapterPosition();
            notifyDataSetChanged();
            if (listener != null) listener.onItemClick(item);
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
