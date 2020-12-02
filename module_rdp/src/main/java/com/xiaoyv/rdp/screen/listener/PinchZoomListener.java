package com.xiaoyv.rdp.screen.listener;

import android.view.ScaleGestureDetector;

import com.xiaoyv.librdp.view.FreeScrollView;
import com.xiaoyv.librdp.view.SessionView;

public class PinchZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private float scaleFactor = 1.0f;
    private final FreeScrollView freeScrollView;
    private final SessionView sessionView;

    public PinchZoomListener(FreeScrollView freeScrollView, SessionView sessionView) {
        this.freeScrollView = freeScrollView;
        this.sessionView = sessionView;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        freeScrollView.setScrollEnabled(false);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // 计算比例因子
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(SessionView.MIN_SCALE_FACTOR, Math.min(scaleFactor, SessionView.MAX_SCALE_FACTOR));
        sessionView.setZoom(scaleFactor);

        if (!sessionView.isAtMinZoom() && !sessionView.isAtMaxZoom()) {
            // 将滚动原点转换为新的缩放空间
            float transOriginX = freeScrollView.getScrollX() * detector.getScaleFactor();
            float transOriginY = freeScrollView.getScrollY() * detector.getScaleFactor();

            // 将中心点转换为缩放的空间
            float transCenterX = (freeScrollView.getScrollX() + detector.getFocusX()) * detector.getScaleFactor();
            float transCenterY = (freeScrollView.getScrollY() + detector.getFocusY()) * detector.getScaleFactor();

            //滚动变换后的中心/原点的距离与其旧距离之间的差（focusX / Y）
            freeScrollView.scrollBy((int) ((transCenterX - transOriginX) - detector.getFocusX()), (int) ((transCenterY - transOriginY) - detector.getFocusY()));
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector de) {
        freeScrollView.setScrollEnabled(true);
    }
}