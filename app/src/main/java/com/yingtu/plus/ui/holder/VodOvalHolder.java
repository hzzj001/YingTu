/* 移植自上游 webhtv (https://github.com/fish2018/webhtv, HEAD 4e30ffa)：包名 com.fongmi.android.tv -> com.yingtu.plus，逻辑未改。 */
package com.yingtu.plus.ui.holder;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.yingtu.plus.bean.Vod;
import com.yingtu.plus.databinding.AdapterVodOvalBinding;
import com.yingtu.plus.ui.adapter.VodAdapter;
import com.yingtu.plus.ui.base.BaseVodHolder;
import com.yingtu.plus.utils.ImgUtil;

public class VodOvalHolder extends BaseVodHolder {

    private final VodAdapter.OnClickListener listener;
    private final AdapterVodOvalBinding binding;

    public VodOvalHolder(@NonNull AdapterVodOvalBinding binding, VodAdapter.OnClickListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
    }

    public VodOvalHolder size(int[] size) {
        binding.image.getLayoutParams().width = size[0];
        binding.image.getLayoutParams().height = size[1];
        return this;
    }

    @Override
    public void initView(Vod item) {
        binding.name.setText(item.getName());
        binding.name.setVisibility(item.getNameVisible());
        binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        binding.getRoot().setOnLongClickListener(v -> listener.onLongClick(item));
        ImgUtil.load(item.getName(), item.getPic(), binding.image);
    }

    @Override
    public void unbind() {
        Glide.with(binding.image).clear(binding.image);
    }
}
