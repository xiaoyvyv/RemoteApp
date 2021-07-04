package com.xiaoyv.busines.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import autodispose2.AutoDispose
import autodispose2.AutoDisposeConverter
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider

/**
 * Mvp Activity基类
 *
 * @author why
 * @since 2020/10/16
 */
abstract class BaseMvpActivity<V : IBaseView, T : ImplBasePresenter<V>> : BaseActivity(),
    IBaseView {

    protected lateinit var activity: AppCompatActivity
    protected lateinit var presenter: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        presenter = createPresenter()
        presenter.attachView(this as V, this)
        // 生命周期监听
        initLifecycleObserver(lifecycle)
        // P层创建完成
        onPresenterCreated()

        // 执行 BaseActivity 初始化事件
        super.initEvent()
    }

    override fun initEvent() {
        // 拦截 BaseActivity 初始化事件
    }

    /**
     * P 层
     *
     * @return T
     */
    protected abstract fun createPresenter(): T

    /**
     * P层初始化完成，在这使用Presenter进行数据请求
     */
    protected open fun onPresenterCreated() {

    }

    protected fun <O> bindLifecycle(): AutoDisposeConverter<O> {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this))
    }

    @CallSuper
    @MainThread
    protected fun initLifecycleObserver(lifecycle: Lifecycle) {
        presenter.setLifecycleOwner(this)
        // 监听生命周期，可在P层处理
        lifecycle.addObserver(presenter)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}