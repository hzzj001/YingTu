/* 移植自上游 webhtv (https://github.com/fish2018/webhtv, HEAD 4e30ffa)：包名 com.fongmi.android.tv -> com.yingtu.plus，逻辑未改。 */
package com.yingtu.plus.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.yingtu.plus.bean.Style;
import com.yingtu.plus.bean.Vod;
import com.yingtu.plus.databinding.AdapterVodListBinding;
import com.yingtu.plus.databinding.AdapterVodOvalBinding;
import com.yingtu.plus.databinding.AdapterVodRectBinding;
import com.yingtu.plus.ui.base.BaseVodHolder;
import com.yingtu.plus.ui.base.ViewType;
import com.yingtu.plus.ui.holder.VodListHolder;
import com.yingtu.plus.ui.holder.VodOvalHolder;
import com.yingtu.plus.ui.holder.VodRectHolder;

public class VodAdapter extends BaseDiffAdapter<Vod, BaseVodHolder> {

    private final OnClickListener listener;
    private final Style style;
    private final int[] size;

    public VodAdapter(OnClickListener listener, Style style, int[] size) {
        this.listener = listener;
        this.style = style;
        this.size = size;
    }

    public interface OnClickListener {

        void onItemClick(Vod item);

        boolean onLongClick(Vod item);
    }

    public Style getStyle() {
        return style;
    }

    @Override
    public int getItemViewType(int position) {
        return style.getViewType();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseVodHolder holder, int position) {
        holder.initView(getItem(position));
    }

    @Override
    public void onViewRecycled(@NonNull BaseVodHolder holder) {
        holder.unbind();
    }

    @NonNull
    @Override
    public BaseVodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return switch (viewType) {
            case ViewType.LIST -> new VodListHolder(AdapterVodListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener);
            case ViewType.OVAL -> new VodOvalHolder(AdapterVodOvalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener).size(size);
            default -> new VodRectHolder(AdapterVodRectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener).size(size);
        };
    }
}
