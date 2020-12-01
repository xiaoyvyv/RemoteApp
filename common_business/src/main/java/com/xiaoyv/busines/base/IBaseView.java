package com.xiaoyv.busines.base;

import android.view.View;

import com.xiaoyv.ui.status.ContentStatusView;

/**
 * BaseView
 *
 * @author why
 * @since 2020/11/28
 **/
public interface IBaseView {

    void p2vShowToast(String msg);

    void p2vShowLoading();

    void p2vShowLoading(String msg);

    void p2vHideLoading();

    void p2vShowNormalView();

    void p2vShowEmptyView();

    void p2vShowLoadingView();

    void p2vShowRetryView();

    void p2vClickStatusView();

    ContentStatusView p2vGetStatusView();
}
