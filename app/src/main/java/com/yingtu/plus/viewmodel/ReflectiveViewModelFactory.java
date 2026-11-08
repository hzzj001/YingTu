package com.yingtu.plus.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * 第 21 轮本地新建：反射创建无参构造 ViewModel 的工厂。
 *
 * 背景：CI 解析到的 lifecycle-viewmodel 版本里没有 ViewModelProvider.NewInstanceFactory
 *（本地声明的 2.7.0 里有，但某个传递依赖把它抬高了），第 20 轮的 NewInstanceFactory 写法
 * 在 CI 编译失败。ViewModelProvider.Factory 接口与 (ViewModelStore, Factory) 构造方法
 * 自 lifecycle 2.0 起各版本均有，本工厂只依赖这两者，与版本无关。
 */
public final class ReflectiveViewModelFactory implements ViewModelProvider.Factory {

    private static final ReflectiveViewModelFactory INSTANCE = new ReflectiveViewModelFactory();

    private ReflectiveViewModelFactory() {
    }

    public static ReflectiveViewModelFactory get() {
        return INSTANCE;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            return modelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create ViewModel " + modelClass.getName(), e);
        }
    }
}
