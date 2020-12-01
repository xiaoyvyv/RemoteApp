package com.xiaoyv.ui.listener;

import android.view.View;

/**
 * OnMultiClickListener
 *
 * @author why
 * @since 2020/11/29
 */
public abstract class SimpleFastClickListener implements View.OnClickListener {
    private final long interval;
    private long lastClickTime;

    public SimpleFastClickListener() {
        interval = 200;
    }

    public SimpleFastClickListener(long interval) {
        this.interval = interval;
    }

    public abstract void onMultiClick(View v);

    @Override
    public void onClick(View v) {
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= interval) {
            lastClickTime = curClickTime;
            onMultiClick(v);
        }
    }
}