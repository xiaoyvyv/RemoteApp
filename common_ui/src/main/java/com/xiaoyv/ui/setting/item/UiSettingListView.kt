package com.xiaoyv.ui.setting.item

import android.content.Context
import android.util.AttributeSet
import com.xiaoyv.ui.R
import com.xiaoyv.ui.dialog.OptionsDialog
import com.xiaoyv.ui.dialog.OptionsDialogItemBinder

/**
 * UiSettingItemView
 *
 * @author why
 * @since 2021/07/11
 **/
class UiSettingListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : UiSettingTextView(context, attrs, defStyleAttr) {
    private var optionsDialog: OptionsDialog? = null

    var uiListValueDesc: Array<CharSequence?> = arrayOf()
    var uiListValue: Array<CharSequence?> = arrayOf()

    private var onClickListener: OnClickListener? = null

    var onSelectStringListener: (String, Int) -> Unit = { _, _ -> }
    var onSelectIntListener: (Int, Int) -> Unit = { _, _ -> }

    init {
        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.UiSettingListView)
            uiListValueDesc = array.getTextArray(R.styleable.UiSettingListView_ui_value_desc_list)
                ?: uiListValueDesc
            uiListValue = array.getTextArray(R.styleable.UiSettingListView_ui_value_list)
                ?: uiListValue
            array.recycle()
        }

        showByIndex()

        if (!isInEditMode) {
            this.optionsDialog = OptionsDialog(context).also {
                it.setOptions(*uiListValueDesc)
                it.setOnItemChildClickListener(object :
                    OptionsDialogItemBinder.OnItemChildClickListener {
                    override fun onItemChildClick(position: Int) {
                        binding.tvDesc.text = uiListValueDesc[position].toString()
                        val charSequence = uiListValue[position].toString()

                        onSelectStringListener.invoke(charSequence, position)
                        onSelectIntListener.invoke(charSequence.toIntOrNull() ?: 0, position)
                    }
                })
            }

            super.setOnClickListener {
                onClickListener?.onClick(this)
                optionsDialog?.show()
            }
        }
    }

    @JvmOverloads
    fun showByIndex(index: Int = 0) {
        if (uiListValueDesc.isNotEmpty() && index < uiListValueDesc.size && index >= 0) {
            binding.tvDesc.text = uiListValueDesc[index]
        }
    }

    fun showByValue(value: Any) {
        val v = value.toString()
        if (uiListValue.contains(v)) {
            showByIndex(uiListValue.indexOf(v))
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }

}