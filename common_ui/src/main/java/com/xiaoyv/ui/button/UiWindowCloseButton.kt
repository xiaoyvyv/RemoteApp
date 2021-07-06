package com.xiaoyv.ui.button

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.xiaoyv.ui.R

/**
 * UiWindowCloseButton
 *
 * @author why
 * @since 2021/7/5
 */
class UiWindowCloseButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var l: OnClickListener? = null

    init {
        isClickable = true
        isFocusable = true
        setImageResource(R.drawable.ui_shape_window_close_normal)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                setBackgroundResource(R.color.ui_status_error)
                setImageResource(R.drawable.ui_shape_window_close_pressed)
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                l?.onClick(this)
                setBackgroundResource(R.color.ui_system_translate)
                setImageResource(R.drawable.ui_shape_window_close_normal)
            }
        }
        return true
    }

    override fun setOnClickListener(l: OnClickListener?) {
        this.l = l
    }
}