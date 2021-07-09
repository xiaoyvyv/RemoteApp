package com.freerdp.freerdpcore.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.freerdp.freerdpcore.utils.GestureDetector
import com.xiaoyv.librdp.R
import kotlin.math.min

/**
 * 模拟鼠标自定义视图
 */
class RdpPointerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    // 指针资源
    private var pointerActive: Drawable
    private var pointerClose: Drawable
    private var pointerDefault: Drawable
    private var pointerExtKeyboard: Drawable
    private var pointerKeyboard: Drawable
    private var pointerLClick: Drawable
    private var pointerRClick: Drawable
    private var pointerReset: Drawable
    private var pointerScroll: Drawable

    private val pointerAreaRectArray = arrayOfNulls<RectF>(8)
    private var pointerRect: RectF
    private var translationMatrix: Matrix
    private var pointerMoving = false
    private var pointerScrolling = false
    private var listener: TouchPointerListener? = null
    private val uiHandler: UIHandler = UIHandler(Looper.getMainLooper())

    /**
     * 手势检测
     */
    private var gestureDetector: GestureDetector

    init {
        // 指针大小为屏幕短边的 2/5
        val pointerSize =
            (min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()) * 0.4f).toInt()
        pointerActive = zoomImage(R.drawable.touch_pointer_active, pointerSize, pointerSize)
        pointerClose = zoomImage(R.drawable.touch_pointer_close, pointerSize, pointerSize)
        pointerDefault = zoomImage(R.drawable.touch_pointer_default, pointerSize, pointerSize)
        pointerExtKeyboard =
            zoomImage(R.drawable.touch_pointer_extkeyboard, pointerSize, pointerSize)
        pointerKeyboard = zoomImage(R.drawable.touch_pointer_keyboard, pointerSize, pointerSize)
        pointerLClick = zoomImage(R.drawable.touch_pointer_lclick, pointerSize, pointerSize)
        pointerRClick = zoomImage(R.drawable.touch_pointer_rclick, pointerSize, pointerSize)
        pointerReset = zoomImage(R.drawable.touch_pointer_reset, pointerSize, pointerSize)
        pointerScroll = zoomImage(R.drawable.touch_pointer_scroll, pointerSize, pointerSize)


        gestureDetector = GestureDetector(context, TouchPointerGestureListener(), null, true)
        gestureDetector.setLongPressTimeout(500)


        scaleType = ScaleType.MATRIX

        translationMatrix = Matrix()
        imageMatrix = translationMatrix

        // 默认图片
        setImageDrawable(pointerDefault)

        // 指针整个区域
        val wholeWidth = drawable.intrinsicWidth.toFloat()
        val wholeHeight = drawable.intrinsicHeight.toFloat()
        // 操作栏的可点击的整个区域部分
        val clickableWidth = 17.0f * wholeWidth / 20.0f
        val clickableHeight = 17.0f * wholeHeight / 20.0f

        // 初始化
        pointerAreaRectArray[0] =
            RectF(0.0f, 0.0f, 3.0f * wholeWidth / 20.0f, 3.0f * wholeHeight / 20.0f)
        pointerAreaRectArray[1] =
            RectF(0.0f, 0.0f, clickableWidth / 3.0f, clickableHeight / 2.0f)
        pointerAreaRectArray[2] =
            RectF(clickableWidth / 3.0f, 0.0f, 2.0f * clickableWidth / 3.0f, clickableHeight / 3.0f)
        pointerAreaRectArray[3] =
            RectF(2.0f * clickableWidth / 3.0f, 0.0f, clickableWidth, clickableHeight / 3.0f)
        pointerAreaRectArray[4] =
            RectF(0.0f, clickableHeight / 2.0f, clickableWidth / 3.0f, clickableHeight)
        pointerAreaRectArray[5] = RectF(
            clickableWidth / 3.0f,
            clickableHeight / 3.0f,
            2.0f * clickableWidth / 3.0f,
            2.0f * clickableHeight / 3.0f
        )
        pointerAreaRectArray[6] = RectF(
            2.0f * clickableWidth / 3.0f,
            clickableHeight / 2.0f,
            clickableWidth,
            clickableHeight
        )
        pointerAreaRectArray[7] = RectF(
            clickableWidth / 3.0f,
            2.0f * clickableHeight / 3.0f,
            2.0f * clickableWidth / 3.0f,
            clickableHeight
        )

        for (i in 1 until pointerAreaRectArray.size) {
            pointerAreaRectArray[i]?.offset(3.0f * wholeWidth / 20.0f, 3.0f * wholeHeight / 20.0f)
        }

        // 指针整个区域
        pointerRect = RectF(
            0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat()
        )
    }

    fun setTouchPointerListener(listener: TouchPointerListener?) {
        this.listener = listener
    }

    val pointerWidth: Int
        get() = drawable.intrinsicWidth

    val pointerHeight: Int
        get() = drawable.intrinsicHeight

    val pointerPosition: FloatArray
        get() {
            val curPos = FloatArray(2)
            translationMatrix.mapPoints(curPos)
            return curPos
        }

    private fun movePointer(deltaX: Float, deltaY: Float) {
        translationMatrix.postTranslate(deltaX, deltaY)
        imageMatrix = translationMatrix
    }

    private fun ensureVisibility(screen_width: Int, screen_height: Int) {
        val curPos = FloatArray(2)
        translationMatrix.mapPoints(curPos)
        if (curPos[0] > screen_width - pointerRect.width()) {
            curPos[0] = screen_width - pointerRect.width()
        }
        if (curPos[0] < 0) {
            curPos[0] = 0f
        }
        if (curPos[1] > screen_height - pointerRect.height()) {
            curPos[1] = screen_height - pointerRect.height()
        }
        if (curPos[1] < 0) {
            curPos[1] = 0f
        }
        translationMatrix.setTranslate(curPos[0], curPos[1])
        imageMatrix = translationMatrix
    }

    private fun displayPointerImageAction(drawable: Drawable?) {
        setImageDrawable(drawable)
        uiHandler.sendEmptyMessageDelayed(0, DEFAULT_TOUCH_POINTER_RESTORE_DELAY)
    }

    private fun zoomImage(resId: Int, w: Int, h: Int): Drawable {
        val res = context.resources
        val oldBmp = BitmapFactory.decodeResource(res, resId, BitmapFactory.Options())
        val newBmp = Bitmap.createScaledBitmap(oldBmp, w, h, true)
        return BitmapDrawable(res, newBmp)
    }

    /**
     * 返回具有当前转换矩阵的指针区域
     */
    @Suppress("SameParameterValue")
    private fun getCurrentPointerArea(area: Int): RectF {
        val transRect = RectF(pointerAreaRectArray[area])
        translationMatrix.mapRect(transRect)
        return transRect
    }

    private fun pointerAreaTouched(event: MotionEvent, area: Int): Boolean {
        val transRect = RectF(pointerAreaRectArray[area])
        translationMatrix.mapRect(transRect)
        return transRect.contains(event.x, event.y)
    }

    private fun pointerTouched(event: MotionEvent): Boolean {
        val transRect = RectF(pointerRect)
        translationMatrix.mapRect(transRect)
        return transRect.contains(event.x, event.y)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 检查指针是否正在移动，或者我们是否处于滚动模式，或者是否触摸了指针
        return if (!pointerMoving && !pointerScrolling && !pointerTouched(event)) {
            false
        } else gestureDetector.onTouchEvent(event)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // 确保触摸指针可见
        if (changed) {
            ToastUtils.showShort((right - left).toString() + ": " + (bottom - top))
            ensureVisibility(right - left, bottom - top)
        }
    }

    // 触摸指针侦听器-如果操作字段为
    interface TouchPointerListener {
        fun onTouchPointerClose()
        fun onTouchPointerLeftClick(x: Int, y: Int, down: Boolean)
        fun onTouchPointerRightClick(x: Int, y: Int, down: Boolean)
        fun onTouchPointerMove(x: Int, y: Int)
        fun onTouchPointerScroll(down: Boolean)
        fun onTouchPointerToggleKeyboard()
        fun onTouchPointerToggleExtKeyboard()
        fun onTouchPointerResetScrollZoom()
    }

    private inner class UIHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            setImageDrawable(pointerDefault)
        }
    }

    private inner class TouchPointerGestureListener : GestureDetector.SimpleOnGestureListener() {
        private var prevEvent: MotionEvent? = null

        override fun onDown(e: MotionEvent): Boolean {
            if (pointerAreaTouched(e, POINTER_ACTION_MOVE)) {
                prevEvent = MotionEvent.obtain(e)
                pointerMoving = true
            } else if (pointerAreaTouched(e, POINTER_ACTION_SCROLL)) {
                prevEvent = MotionEvent.obtain(e)
                pointerScrolling = true
                setImageDrawable(pointerScroll)
            }
            return true
        }

        override fun onUp(e: MotionEvent): Boolean {
            prevEvent?.let {
                it.recycle()
                prevEvent = null
            }

            if (pointerScrolling) {
                setImageDrawable(pointerDefault)
            }
            pointerMoving = false
            pointerScrolling = false
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (pointerAreaTouched(e, POINTER_ACTION_L_CLICK)) {
                setImageDrawable(pointerActive)
                pointerMoving = true

                getCurrentPointerArea(POINTER_ACTION_CURSOR).let { rect ->
                    listener?.onTouchPointerLeftClick(
                        rect.centerX().toInt(), rect.centerY().toInt(), true
                    )
                }
            }
        }

        override fun onLongPressUp(e: MotionEvent) {
            if (pointerMoving) {
                setImageDrawable(pointerDefault)
                pointerMoving = false

                getCurrentPointerArea(POINTER_ACTION_CURSOR).let { rect ->
                    listener?.onTouchPointerLeftClick(
                        rect.centerX().toInt(), rect.centerY().toInt(), false
                    )
                }
            }
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            prevEvent?.also { event ->
                when {
                    pointerMoving -> {
                        // 移动指针图形
                        movePointer(e2.x - event.x, e2.y - event.y)
                        event.recycle()
                        prevEvent = MotionEvent.obtain(e2)

                        // 发送移动通知
                        val rect = getCurrentPointerArea(POINTER_ACTION_CURSOR)
                        listener?.onTouchPointerMove(rect.centerX().toInt(), rect.centerY().toInt())
                        return@onScroll true
                    }
                    pointerScrolling -> {
                        // 计算用户向上或向下滚动（或根本没有滚动）
                        val deltaY = e2.y - event.y
                        if (deltaY > SCROLL_DELTA) {
                            listener?.onTouchPointerScroll(true)
                            event.recycle()
                            prevEvent = MotionEvent.obtain(e2)
                        } else if (deltaY < -SCROLL_DELTA) {
                            listener?.onTouchPointerScroll(false)
                            event.recycle()
                            prevEvent = MotionEvent.obtain(e2)
                        }
                        return@onScroll true
                    }
                }
            }
            return false
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // 看什么地方被触摸，并采取相应的行动
            when {
                pointerAreaTouched(e, POINTER_ACTION_CLOSE) -> {
                    listener?.onTouchPointerClose()
                }
                pointerAreaTouched(e, POINTER_ACTION_L_CLICK) -> {
                    displayPointerImageAction(pointerLClick)
                    val rect = getCurrentPointerArea(POINTER_ACTION_CURSOR)
                    listener?.onTouchPointerLeftClick(
                        rect.centerX().toInt(),
                        rect.centerY().toInt(),
                        true
                    )
                    listener?.onTouchPointerLeftClick(
                        rect.centerX().toInt(),
                        rect.centerY().toInt(),
                        false
                    )
                }
                pointerAreaTouched(e, POINTER_ACTION_R_CLICK) -> {
                    displayPointerImageAction(pointerRClick)
                    val rect = getCurrentPointerArea(POINTER_ACTION_CURSOR)
                    listener?.onTouchPointerRightClick(
                        rect.centerX().toInt(),
                        rect.centerY().toInt(),
                        true
                    )
                    listener?.onTouchPointerRightClick(
                        rect.centerX().toInt(),
                        rect.centerY().toInt(),
                        false
                    )
                }
                pointerAreaTouched(e, POINTER_ACTION_KEYBOARD) -> {
                    displayPointerImageAction(pointerKeyboard)
                    listener?.onTouchPointerToggleKeyboard()
                }
                pointerAreaTouched(e, POINTER_ACTION_EXT_KEYBOARD) -> {
                    displayPointerImageAction(pointerExtKeyboard)
                    listener?.onTouchPointerToggleExtKeyboard()
                }
                pointerAreaTouched(e, POINTER_ACTION_RESET) -> {
                    displayPointerImageAction(pointerReset)
                    listener?.onTouchPointerResetScrollZoom()
                }
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // 如果在中心象限中执行，则发出双击通知
            if (pointerAreaTouched(e, POINTER_ACTION_L_CLICK)) {
                val rect = getCurrentPointerArea(POINTER_ACTION_CURSOR)
                listener?.onTouchPointerLeftClick(
                    rect.centerX().toInt(),
                    rect.centerY().toInt(),
                    true
                )
                listener?.onTouchPointerLeftClick(
                    rect.centerX().toInt(),
                    rect.centerY().toInt(),
                    false
                )
            }
            return true
        }
    }

    companion object {
        private const val POINTER_ACTION_CURSOR = 0
        private const val POINTER_ACTION_CLOSE = 1
        private const val POINTER_ACTION_SCROLL = 2
        private const val POINTER_ACTION_R_CLICK = 3
        private const val POINTER_ACTION_RESET = 4
        private const val POINTER_ACTION_L_CLICK = 5
        private const val POINTER_ACTION_MOVE = 5
        private const val POINTER_ACTION_EXT_KEYBOARD = 6
        private const val POINTER_ACTION_KEYBOARD = 7
        private const val SCROLL_DELTA = 10.0f
        private const val DEFAULT_TOUCH_POINTER_RESTORE_DELAY = 150L
    }
}