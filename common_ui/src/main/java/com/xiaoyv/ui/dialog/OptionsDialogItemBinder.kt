package com.xiaoyv.ui.dialog

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.blankj.utilcode.util.ColorUtils
import com.xiaoyv.ui.R
import com.xiaoyv.ui.base.BaseItemBindingBinder
import com.xiaoyv.ui.databinding.UiDialogOptionsItemBinding

/**
 * OptionsDialog
 *
 * @author why
 * @since 2020/12/01
 */
class OptionsDialogItemBinder : BaseItemBindingBinder<String, UiDialogOptionsItemBinding>() {
    var clickListener: OnItemChildClickListener? = null

    @ColorInt
    var textColor: Int = ColorUtils.getColor(R.color.ui_text_c1)

    @ColorInt
    var lastTextColor: Int = ColorUtils.getColor(R.color.ui_text_c1)

    var textSize = 14f

    var textStyle: Typeface = Typeface.DEFAULT

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): UiDialogOptionsItemBinding {
        return UiDialogOptionsItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(
        holder: BinderVBHolder<UiDialogOptionsItemBinding>,
        binding: UiDialogOptionsItemBinding,
        data: String
    ) {
        binding.tvOptions.text = data
        binding.tvOptions.typeface = textStyle
        binding.tvOptions.textSize = textSize
        binding.tvOptions.setOnClickListener {
            clickListener?.onItemChildClick(holder.bindingAdapterPosition)
        }
        if (holder.bindingAdapterPosition == adapter.data.size - 1) {
            binding.uiView.visibility = View.GONE
            binding.tvOptions.setTextColor(lastTextColor)
        } else {
            binding.uiView.visibility = View.VISIBLE
            binding.tvOptions.setTextColor(textColor)
        }
    }

    interface OnItemChildClickListener {
        /**
         * 选项点击事件
         *
         * @param position 事件
         */
        fun onItemChildClick(position: Int)
    }
}