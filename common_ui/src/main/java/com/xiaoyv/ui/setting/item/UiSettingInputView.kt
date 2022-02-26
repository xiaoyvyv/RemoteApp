package com.xiaoyv.ui.setting.item

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.xiaoyv.desktop.ui.R
import com.xiaoyv.desktop.ui.databinding.UiSettingInputBinding
import com.xiaoyv.ui.dialog.BaseDialog
import com.xiaoyv.ui.listener.SimpleTextChangeListener
import me.jessyan.autosize.utils.AutoSizeUtils

/**
 * UiSettingInputView
 *
 * @author why
 * @since 2021/07/11
 **/
class UiSettingInputView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : UiSettingTextView(context, attrs, defStyleAttr) {

    private var inputDialog: BaseDialog
    private val inputBinding = UiSettingInputBinding.inflate(LayoutInflater.from(context))
    private var onClickListener: OnClickListener? = null

    var isEdit = false

    var inputType: Int = InputType.TYPE_CLASS_TEXT
        set(value) {
            field = value
            inputBinding.etValue.inputType = value
        }

    var filters: Array<InputFilter> = arrayOf()
        set(value) {
            field = value
            inputBinding.etValue.filters = value
        }

    var onInputDoneListener: (String) -> Unit = {}

    init {
        // 输入框
        inputDialog = BaseDialog(context)
        inputDialog.setView(inputBinding.root)
        inputDialog.setCanceledOnTouchOutside(true)
        inputBinding.tvClear.setOnClickListener {
            KeyboardUtils.hideSoftInput(inputBinding.etValue)
            inputDialog.dismiss()
        }

        setInputLine(1)

        super.setOnClickListener {
            onClickListener?.onClick(this)

            inputBinding.etValue.inputType = inputType
            inputBinding.etValue.filters = filters
            inputBinding.tvDone.setOnClickListener {
                isEdit = true
                KeyboardUtils.hideSoftInput(inputBinding.etValue)
                inputDialog.dismiss()

                uiValue = inputBinding.etValue.text.toString()
                onInputDoneListener.invoke(uiValue.orEmpty())
            }

            showDialog()
        }
    }

    private fun showDialog() {
        inputBinding.tvTitle.text = uiTitle
        inputBinding.etValue.setText(uiValue)
        uiValue?.let {
            inputBinding.etValue.setSelection(0, it.length)
        }

        inputDialog.show()
        val window = inputDialog.window
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.ui_transparent)
            window.attributes = window.attributes.apply {
                dimAmount = 0.2f
                width = ScreenUtils.getAppScreenWidth() - AutoSizeUtils.dp2px(Utils.getApp(), 80f)
            }
        }
        ThreadUtils.runOnUiThreadDelayed({
            KeyboardUtils.showSoftInput(inputBinding.etValue)
        }, 100)
    }

    fun setInputLine(line: Int): UiSettingInputView {
        inputBinding.etValue.maxLines = line
        inputBinding.etValue.minLines = line
        inputBinding.etValue.isSingleLine = line == 1
        if (line == 1) {
            inputBinding.etValue.ellipsize = TextUtils.TruncateAt.END
        } else {
            inputBinding.etValue.ellipsize = null
        }
        return this
    }

    fun addInputChangeListener(listener: SimpleTextChangeListener?): UiSettingInputView {
        inputBinding.etValue.addTextChangedListener(listener)
        return this
    }

    fun setInputHint(hint: String?): UiSettingInputView {
        inputBinding.etValue.hint = hint
        return this
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }
}