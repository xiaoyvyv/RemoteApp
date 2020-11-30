package com.xiaoyv.busines.base;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import autodispose2.AutoDispose;
import autodispose2.AutoDisposeConverter;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ImplBasePresenter
 *
 * @author why
 * @since 2020/11/29
 **/
public class ImplBasePresenter<V extends IBaseView> implements IBasePresenter {
    protected WeakReference<V> mBaseView;
    protected WeakReference<Context> mContext;

    /**
     * 注入V层的LifeCycleOwner,这样在P层也能处理生命周期变化
     */
    private LifecycleOwner lifecycleOwner;

    public V getView() {
        if (mBaseView != null) {
            return mBaseView.get();
        }
        return null;
    }

    /**
     * view，context绑定
     *
     * @param view    iView
     * @param context context
     */
    public void attachView(V view, Context context) {
        this.mBaseView = new WeakReference<>(view);
        this.mContext = new WeakReference<>(context);
    }

    /**
     * view,context,compositeDisposable解绑
     */
    public void detachView() {
        if (this.mBaseView != null) {
            this.mBaseView.clear();
        }
        if (this.mContext != null) {
            this.mContext.clear();
        }
    }

    @Override
    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public void onLifecycleChanged(@NotNull LifecycleOwner owner, @NotNull Lifecycle.Event event) {
    }

    /**
     * 绑定生命周期
     *
     * @param <O> o
     * @return o
     */
    public <O> AutoDisposeConverter<O> bindLifecycle() {
        if (null == lifecycleOwner) {
            throw new NullPointerException("lifecycleOwner == null");
        }
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner));
    }

    /**
     * 统一线程处理
     *
     * @param <O> 指定的泛型类型
     * @return ObservableTransformer
     */
    public <O> ObservableTransformer<O, O> bindTransformer() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
