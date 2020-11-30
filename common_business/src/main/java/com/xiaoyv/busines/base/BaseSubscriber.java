package com.xiaoyv.busines.base;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.exception.RxExceptionHandler;
import com.xiaoyv.business.R;

import org.jetbrains.annotations.NotNull;

import java.net.SocketTimeoutException;

import io.reactivex.rxjava3.observers.DisposableObserver;

/**
 * 基础订阅者
 */
public abstract class BaseSubscriber<T> extends DisposableObserver<T> {

    @Override
    protected void onStart() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onNext(@NotNull T t) {
        try {
            onSuccess(t);
        } catch (Throwable e) {
            e.printStackTrace();
            onError(e);
        }
    }

    @Override
    public final void onError(Throwable e) {
        try {
            if (e instanceof RxException) {
                LogUtils.e("RxException", e);
                onError((RxException) e);
            } else if (e instanceof SocketTimeoutException) {
                LogUtils.e("SocketTimeoutException", e);
                // 超时异常更换文案
                onError(RxExceptionHandler.handleException(new SocketTimeoutException(StringUtils.getString(R.string.ui_common_timeout))));
            } else {
                LogUtils.e("OtherException", e);
                // 全局异常处理，转为 RxException
                onError(RxExceptionHandler.handleException(e));
            }
        } catch (Throwable throwable) {
            e.printStackTrace();
        }
    }

    /**
     * 出错
     *
     * @param e exception
     */
    public abstract void onError(RxException e);

    /**
     * 安全版的{@link #onNext},自动做了try-catch
     *
     * @param t t
     */
    public abstract void onSuccess(T t);
}
