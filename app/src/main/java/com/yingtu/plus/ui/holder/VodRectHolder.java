/* 移植自上游 webhtv (https://github.com/fish2018/webhtv, HEAD 4e30ffa)：包名 com.fongmi.android.tv -> com.yingtu.plus，逻辑未改。 */
package com.yingtu.plus.ui.holder;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.yingtu.plus.bean.Vod;
import com.yingtu.plus.databinding.AdapterVodRectBinding;
import com.yingtu.plus.ui.adapter.VodAdapter;
import com.yingtu.plus.ui.base.BaseVodHolder;
import com.yingtu.plus.utils.ImgUtil;

public class VodRectHolder extends BaseVodHolder {

    private final VodAdapter.OnClickListener listener;
    private final AdapterVodRectBinding binding;

    public VodRectHolder(@NonNull AdapterVodRectBinding binding, VodAdapter.OnClickListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
    }

    public VodRectHolder size(int[] size) {
        binding.image.getLayoutParams().height = size[1];
        binding.getRoot().getLayoutParams().width = size[0];
        return this;
    }

    @Override
    public void initView(Vod item) {
        binding.name.setText(item.getName());
        binding.year.setText(item.getYear());
        binding.site.setText(item.getSiteName());
        binding.remark.setText(item.getRemarks());
        binding.site.setVisibility(item.getSiteVisible());
        binding.name.setVisibility(item.getNameVisible());
        binding.year.setVisibility(item.getYearVisible());
        binding.remark.setVisibility(item.getRemarkVisible());
        binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        binding.getRoot().setOnLongClickListener(v -> listener.onLongClick(item));
        ImgUtil.load(item.getName(), item.getPic(), binding.image);
    }

    @Override
    public void unbind() {
        Glide.with(binding.image).clear(binding.image);
    }
}
