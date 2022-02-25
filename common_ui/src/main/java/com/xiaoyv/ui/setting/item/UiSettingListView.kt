package com.xiaoyv.ui.setting.item

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentActivity
import com.xiaoyv.ui.R
import com.xiaoyv.widget.dialog.UiOptionsDialog
import com.xiaoyv.widget.utils.getActivity

/**
 * UiSettingItemView
 *
 * @author why
 * @since 2021/07/11
 **/
class UiSettingListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : UiSettingTextView(context, attrs, defStyleAttr) {
    private var optionsDialog: UiOptionsDialog? = null

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
            this.optionsDialog = UiOptionsDialog.Builder().apply {
                itemDataList = uiListValueDesc.toList().map { toString() }
                onOptionsClickListener = {dialog, _, position ->
                    dialog.dismiss()

                    binding.tvDesc.text = uiListValueDesc[position].toString()
                    val charSequence = uiListValue[position].toString()

                    onSelectStringListener.invoke(charSequence, position)
                    onSelectIntListener.invoke(charSequence.toIntOrNull() ?: 0, position)
                    true
                }
            }.create()

            super.setOnClickListener {
                onClickListener?.onClick(this)

                val activity = getActivity()
                if (activity is FragmentActivity) {
                    optionsDialog?.show(activity)
                }
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