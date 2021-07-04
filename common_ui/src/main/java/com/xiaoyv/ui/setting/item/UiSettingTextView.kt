package com.xiaoyv.ui.setting.item

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.xiaoyv.ui.R
import com.xiaoyv.ui.databinding.UiSettingTextBinding
import com.xiaoyv.ui.kotlin.dp

/**
 * UiSettingTextView
 *
 * @author why
 * @since 2021/07/04
 **/
class UiSettingTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = UiSettingTextBinding.inflate(LayoutInflater.from(context), this)

    var uiKey: String = ""

    var uiTitle: String = ""
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var uiValue: String = ""
        set(value) {
            field = value
            binding.tvDesc.text = value

            if (!StringUtils.isTrimEmpty(value) && !StringUtils.isTrimEmpty(uiKey)) {
                SPUtils.getInstance().remove(uiKey)
                SPUtils.getInstance().put(uiKey, value)
            }
        }

    var uiHint: String = ""
        set(value) {
            field = value
            binding.tvDesc.hint = value
        }

    @ColorInt
    var uiValueColor: Int = ColorUtils.getColor(R.color.ui_text_c3)
        set(value) {
            field = value
            binding.tvDesc.setTextColor(value)
        }

    @ColorInt
    var uiHintColor: Int = ColorUtils.getColor(R.color.ui_text_c3)
        set(value) {
            field = value
            binding.tvDesc.setHintTextColor(value)
        }

    var uiDividerEnable = true
        set(value) {
            field = value
            binding.vDivider.isVisible = value
        }

    var uiDividerMargin = 20.dp()
        set(value) {
            field = value
            binding.vDivider.updateLayoutParams<MarginLayoutParams> {
                marginStart = value
                marginEnd = value
            }
        }

    init {
        attrs?.let {
            val array = context.obtainStyledAttributes(it, R.styleable.UiSettingTextView)
            uiKey = array.getString(R.styleable.UiSettingTextView_ui_key).orEmpty()
            uiTitle = array.getString(R.styleable.UiSettingTextView_ui_title).orEmpty()
            uiValue = array.getString(R.styleable.UiSettingTextView_ui_value).orEmpty()
            uiHint = array.getString(R.styleable.UiSettingTextView_ui_hint).orEmpty()
            uiHintColor =
                array.getColor(R.styleable.UiSettingTextView_ui_hint_color, uiHintColor)
            uiValueColor =
                array.getColor(R.styleable.UiSettingTextView_ui_value_color, uiValueColor)
            uiDividerEnable =
                array.getBoolean(R.styleable.UiSettingTextView_ui_divider_enable, true)
            uiDividerMargin =
                array.getDimensionPixelSize(
                    R.styleable.UiSettingTextView_ui_divider_enable, 20.dp()
                )
            array.recycle()
        }

        background = ResourceUtils.getDrawable(R.drawable.ui_selector_ripper)
        isClickable = true
        isFocusable = true
    }

}