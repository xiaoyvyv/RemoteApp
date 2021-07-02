package com.xiaoyv.ui.scroll;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * HorScrollView
 *
 * @author why
 * @since 2020/12/12
 **/
public class HorScrollView extends HorizontalScrollView {
    private OnScrollChangeListener onScrollChangeListener;

    public HorScrollView(Context context) {
        super(context);
    }

    public HorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnHorScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        this.onScrollChangeListener = onScrollChangeListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        super.onScrollChanged(l, t, oldL, oldT);
        if (onScrollChangeListener != null) {
            onScrollChangeListener.onScrollChanged(l, t, oldL, oldT);
        }
    }

    public interface OnScrollChangeListener {
        /**
         * 滑动监听
         *
         * @param l    l
         * @param t    t
         * @param oldL oldL
         * @param oldT oldT
         */
        void onScrollChanged(int l, int t, int oldL, int oldT);
    }
}
