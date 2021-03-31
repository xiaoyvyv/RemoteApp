package com.xiaoyv.busines.base;

import com.xiaoyv.ui.status.ContentStatusView;

/**
 * BaseView
 *
 * @author why
 * @since 2020/11/28
 **/
public interface IBaseView {

    /**
     * 显示toast
     *
     * @param msg msg
     */
    void p2vShowToast(String msg);

    /**
     * 显示loading
     */
    void p2vShowLoading();

    /**
     * 显示loading
     *
     * @param msg msg
     */
    void p2vShowLoading(String msg);

    /**
     * 隐藏loading
     */
    void p2vHideLoading();

    /**
     * 正常布局
     */
    void p2vShowNormalView();

    /**
     * 空布局
     */
    void p2vShowEmptyView();

    /**
     * 载入中
     */
    void p2vShowLoadingView();

    /**
     * 重试
     */
    void p2vShowRetryView();

    /**
     * 点击状态布局
     */
    void p2vClickStatusView();

    /**
     * 状态布局
     *
     * @return ContentStatusView
     */
    ContentStatusView p2vGetStatusView();

    /**
     * 是否消费返回事件，仅 Fragment 重写该方法
     *
     * @return Fragment 是否消费返回事件
     */
    default boolean onFragmentBackPressed() {
        return false;
    }
}
