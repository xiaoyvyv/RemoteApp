package com.xiaoyv.busines.base;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import org.jetbrains.annotations.NotNull;

/**
 * BasePresenter
 *
 * @author why
 * @since 2020/11/28
 **/
public interface IBasePresenter extends LifecycleObserver {

    void setLifecycleOwner(LifecycleOwner lifecycleOwner);

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    default void v2pOnCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    default void v2pOnStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    default void v2pOnResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    default void v2pOnPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    default void v2pOnStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    default void v2pOnDestroy() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onLifecycleChanged(@NotNull LifecycleOwner owner, @NotNull Lifecycle.Event event);
}