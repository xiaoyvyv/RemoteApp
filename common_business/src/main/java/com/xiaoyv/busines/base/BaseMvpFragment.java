package com.xiaoyv.busines.base;

import android.os.Bundle;

import androidx.annotation.Nullable;

import autodispose2.AutoDispose;
import autodispose2.AutoDisposeConverter;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;

/**
 * Mvp Fragment 基类
 *
 * @author why
 * @since  2020/10/20
 */
public abstract class BaseMvpFragment<V extends IBaseView, T extends ImplBasePresenter<V>> extends BaseFragment implements IBaseView {

    public T presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = createPresenter();
        if (presenter == null) {
            throw new NullPointerException("mPresenter is null");
        }
        presenter.attachView((V) this, activity);

        presenter.setLifecycleOwner(this);
        // 监听生命周期，可在P层处理
        getLifecycle().addObserver(presenter);
    }

    abstract protected T createPresenter();

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    protected <O> AutoDisposeConverter<O> bindLifecycle() {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this));
    }
}
