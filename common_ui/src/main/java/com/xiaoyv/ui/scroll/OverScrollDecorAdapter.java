package com.xiaoyv.ui.scroll;

import android.view.View;

import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter;

public class OverScrollDecorAdapter implements IOverScrollDecoratorAdapter {

    protected final View mView;

    public OverScrollDecorAdapter(View view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return !mView.canScrollVertically(-1);
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return !mView.canScrollVertically(1);
    }
}