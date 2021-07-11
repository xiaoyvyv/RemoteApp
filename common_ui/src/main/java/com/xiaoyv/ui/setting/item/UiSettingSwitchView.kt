package com.xiaoyv.ui.setting.item

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import com.suke.widget.SwitchButton
import com.xiaoyv.ui.kotlin.dp

/**
 * UiSettingSwitchView
 *
 * @author why
 * @since 2021/07/10
 **/
class UiSettingSwitchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : UiSettingTextView(context, attrs, defStyleAttr), Checkable {

    private val switchBtn = SwitchButton(context, attrs)
    private var onClickListener: OnClickListener? = null

    init {
        binding.clRight.addView(
            switchBtn,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.topToTop = LayoutParams.PARENT_ID
                it.bottomToBottom = LayoutParams.PARENT_ID
                it.endToEnd = LayoutParams.PARENT_ID
                it.startToStart = LayoutParams.PARENT_ID
                it.marginEnd = if (isInEditMode) 40 else 20.dp()
                it.marginStart = if (isInEditMode) 40 else 20.dp()
                it.height = if (isInEditMode) 80 else 30.dp()
            }
        )

        super.setOnClickListener {
            onClickListener?.onClick(this)
            toggle()
        }
    }

    override fun setChecked(checked: Boolean) {
        switchBtn.isChecked = checked
    }

    override fun isChecked() = switchBtn.isChecked

    override fun toggle() {
        switchBtn.isChecked = !switchBtn.isChecked
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }
}