package com.xiaoyv.ui.progress

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.ColorUtils
import com.xiaoyv.ui.R
import com.xiaoyv.ui.kotlin.dp


/**
 * UiWindowsProgressView
 *
 * @author why
 * @since 2021/7/8
 */
class UiWindowsProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val rect: Rect = Rect()
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null
    private var viewHeight = 0
    private var viewWidth = 0

    var strokeWidth = let { if (isInEditMode) 30 else 30.dp() }
    var progressBlockWidth = 80

    var progressColor = let {
        if (isInEditMode) Color.GREEN else ColorUtils.getColor(R.color.ui_status_success)
    }

    init {
        setBackgroundResource(R.drawable.ui_shape_window_progress_bg)

        mPaint.style = Paint.Style.FILL
        mPaint.color = progressColor
        mPaint.isDither = true
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = strokeWidth.toFloat()
        rect.top = 0
        rect.left = 0
        rect.right = progressBlockWidth
        rect.bottom = strokeWidth

        post { startProgressAnimation() }
    }

    override fun onDraw(canvas: Canvas) {
        drawProgress(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        viewHeight = height
        viewWidth = width
        progressBlockWidth = viewWidth / 3
    }

    private fun drawProgress(canvas: Canvas) {
        canvas.drawRect(rect, mPaint)
    }

    fun startProgressAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(-progressBlockWidth.toFloat(), viewWidth.toFloat()).apply {
            repeatCount = -1
            duration = 2000
            interpolator = LinearInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float
                rect.top = 0
                rect.left = value.toInt()
                rect.right = rect.left + progressBlockWidth
                rect.bottom = strokeWidth
                invalidate()
            }
        }
        animator?.start()
    }

    fun topProgressAnimation() {
        animator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}