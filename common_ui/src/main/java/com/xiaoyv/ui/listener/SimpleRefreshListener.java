package com.xiaoyv.ui.listener;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollState;
import me.everything.android.ui.overscroll.ListenerStubs;

/**
 * SimpleRefreshListener
 *
 * @author why
 * @since 2020/11/29
 **/
public abstract class SimpleRefreshListener extends ListenerStubs.OverScrollUpdateListenerStub {
    private final static int minOffset = 80;
    private boolean isRefresh = false;

    public SimpleRefreshListener() {
        super();
    }

    @Override
    public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
        if (offset > minOffset) {
            isRefresh = true;
        }
        if (state == IOverScrollState.STATE_BOUNCE_BACK && offset == 0 && isRefresh) {
            onRefresh();
            isRefresh = false;
        }
    }

    /**
     * 刷新
     */
    public abstract void onRefresh();
}
