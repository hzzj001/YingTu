// 移植自上游 fish2018/webhtv app/src/mobile/java/com/fongmi/android/tv/ui/adapter/ValueAdapter.java @ HEAD 4e30ffa（2026-10-07），仅包名 com.fongmi.android.tv → com.yingtu.plus；逻辑未改。
package com.yingtu.plus.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yingtu.plus.bean.Filter;
import com.yingtu.plus.bean.Value;
import com.yingtu.plus.databinding.AdapterValueBinding;
import com.yingtu.plus.impl.FilterListener;

import java.util.List;

public class ValueAdapter extends RecyclerView.Adapter<ValueAdapter.ViewHolder> {

    private final FilterListener listener;
    private final List<Value> mItems;
    private final String mKey;

    public ValueAdapter(FilterListener listener, Filter filter) {
        this.listener = listener;
        this.mItems = filter.getValue();
        this.mKey = filter.getKey();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterValueBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Value item = mItems.get(position);
        holder.binding.text.setText(item.getN());
        holder.binding.text.setSelected(item.isSelected());
        holder.binding.text.setOnClickListener(v -> onItemClick(item));
    }

    private void onItemClick(Value value) {
        for (Value item : mItems) item.setSelected(value);
        notifyItemRangeChanged(0, getItemCount());
        listener.setFilter(mKey, value);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterValueBinding binding;

        ViewHolder(@NonNull AdapterValueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}