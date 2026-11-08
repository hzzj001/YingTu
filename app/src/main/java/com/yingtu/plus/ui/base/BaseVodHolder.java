/* 移植自上游 webhtv (https://github.com/fish2018/webhtv, HEAD 4e30ffa)：包名 com.fongmi.android.tv -> com.yingtu.plus，逻辑未改。 */
package com.yingtu.plus.ui.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yingtu.plus.bean.Vod;

public abstract class BaseVodHolder extends RecyclerView.ViewHolder {

    public BaseVodHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void initView(Vod item);

    public abstract void unbind();
}
