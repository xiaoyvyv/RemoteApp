package com.xiaoyv.rdp.screen.config

import android.view.ScaleGestureDetector
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.ui.scroll.FreeScrollView
import kotlin.math.max
import kotlin.math.min

/**
 * SessionView 缩放
 */
class PinchZoomListener(
    private val rsvScroll: FreeScrollView,
    private val rsvSession: RdpSessionView
) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    private var scaleFactor = 1.0f

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        rsvScroll.setScrollEnabled(false)
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        // 计算比例因子
        scaleFactor *= detector.scaleFactor
        scaleFactor = max(
            RdpSessionView.MIN_SCALE_FACTOR,
            min(scaleFactor, RdpSessionView.MAX_SCALE_FACTOR)
        )
        rsvSession.zoom = scaleFactor
        if (rsvSession.isNotAtMinZoom && rsvSession.isNotAtMaxZoom) {
            // 将滚动原点转换为新的缩放空间
            val transOriginX: Float = rsvScroll.scrollX * detector.scaleFactor
            val transOriginY: Float = rsvScroll.scrollY * detector.scaleFactor

            // 将中心点转换为缩放空间
            val transCenterX: Float =
                (rsvScroll.scrollX + detector.focusX) * detector.scaleFactor
            val transCenterY: Float =
                (rsvScroll.scrollY + detector.focusY) * detector.scaleFactor

            // 滚动变换中心原点的距离与其旧距离 (focusXY) 之间的差异
            rsvScroll.scrollBy(
                (transCenterX - transOriginX - detector.focusX).toInt(),
                (transCenterY - transOriginY - detector.focusY).toInt()
            )
        }
        return true
    }

    override fun onScaleEnd(de: ScaleGestureDetector) {
        rsvScroll.setScrollEnabled(true)
    }
}
