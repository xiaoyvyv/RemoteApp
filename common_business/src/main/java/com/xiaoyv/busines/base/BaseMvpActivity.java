package com.xiaoyv.busines.base;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import org.jetbrains.annotations.NotNull;

import autodispose2.AutoDispose;
import autodispose2.AutoDisposeConverter;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;

/**
 * Mvp Activity基类
 *
 * @author why
 * @since 2020/10/16
 */
public abstract class BaseMvpActivity<V extends IBaseView, T extends ImplBasePresenter<V>> extends BaseActivity implements IBaseView {
    protected AppCompatActivity activity;
    public T presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        presenter = createPresenter();
        presenter.attachView((V) this, this);
        // 生命周期监听
        initLifecycleObserver(getLifecycle());
        // P层创建完成
        onPresenterCreated();

        // 执行 BaseActivity 初始化事件
        super.initEvent();
    }

    @Override
    protected final void initEvent() {
        // 拦截 BaseActivity 初始化事件
    }

    /**
     * P 层
     *
     * @return T
     */
    abstract protected T createPresenter();

    /**
     * P层初始化完成，在这使用Presenter进行数据请求
     */
    protected void onPresenterCreated() {

    }

    protected <O> AutoDisposeConverter<O> bindLifecycle() {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this));
    }

    @CallSuper
    @MainThread
    protected void initLifecycleObserver(@NotNull Lifecycle lifecycle) {
        presenter.setLifecycleOwner(this);
        // 监听生命周期，可在P层处理
        lifecycle.addObserver(presenter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
