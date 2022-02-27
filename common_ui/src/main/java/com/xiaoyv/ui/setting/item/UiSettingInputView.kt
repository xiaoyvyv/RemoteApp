@file:Suppress("MemberVisibilityCanBePrivate")

package com.xiaoyv.ui.setting.item

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.xiaoyv.desktop.ui.R
import com.xiaoyv.desktop.ui.databinding.UiSettingInputBinding
import com.xiaoyv.ui.listener.setOnFastLimitClickListener
import com.xiaoyv.widget.dialog.UiNormalDialog
import com.xiaoyv.widget.utils.getActivity
import com.xiaoyv.widget.utils.isSoftInputModeAlwaysVisible

/**
 * UiSettingInputView
 *
 * @author why
 * @since 2021/07/11
 **/
class UiSettingInputView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : UiSettingTextView(context, attrs, defStyleAttr) {
    private var inputBinding: UiSettingInputBinding? = null

    var isEdit = false

    var inputLine = 1

    var inputType: Int = InputType.TYPE_CLASS_TEXT
        set(value) {
            field = value
            inputBinding?.etValue?.inputType = value
        }

    var filters: Array<InputFilter> = arrayOf()
        set(value) {
            field = value
            inputBinding?.etValue?.filters = value
        }

    var onInputDoneListener: (String) -> Unit = {}

    private val fragmentActivity: FragmentActivity?
        get() = getActivity() as? FragmentActivity

    private val uiHandler = Handler(Looper.getMainLooper())
    private var showKeyBoardRunnable: ShowKeyBoardRunnable? = null

    init {
        setOnFastLimitClickListener {
            // 输入框
            UiNormalDialog.Builder().apply {
                cancelText = null
                confirmText = null
                touchOutsideCancelable = true
                customView = R.layout.ui_setting_input
                onCustomViewInitListener = { dialog, view ->
                    val binding = UiSettingInputBinding.bind(view)
                    binding.tvTitle.text = uiTitle

                    binding.etValue.maxLines = inputLine
                    binding.etValue.minLines = inputLine
                    binding.etValue.isSingleLine = inputLine == 1
                    binding.etValue.ellipsize = TextUtils.TruncateAt.END
                    binding.etValue.inputType = inputType
                    binding.etValue.filters = filters
                    binding.etValue.setText(uiValue)
                    binding.etValue.setSelection(0, uiValue.orEmpty().length)

                    binding.tvClear.setOnClickListener {
                        dialog.dismiss()
                    }
                    binding.tvDone.setOnClickListener {
                        isEdit = true
                        uiValue = binding.etValue.text.toString()
                        onInputDoneListener.invoke(uiValue.orEmpty())

                        dialog.dismiss()
                    }
                }
                onStartListener = { dialog, window ->
                    window.isSoftInputModeAlwaysVisible = true
                    val binding = UiSettingInputBinding.bind(dialog.requireCustomView)

                    // 延时开启键盘
                    cancelRunnable()
                    showKeyBoardRunnable = ShowKeyBoardRunnable(binding.etValue).apply {
                        uiHandler.postDelayed(this, 100)
                    }
                }
                onDismissListener = {
                    cancelRunnable()
                }
            }.create().show(fragmentActivity ?: return@setOnFastLimitClickListener)
        }
    }

    private fun cancelRunnable() = showKeyBoardRunnable?.also {
        uiHandler.removeCallbacks(it)
    }

    inner class ShowKeyBoardRunnable(private val view: View) : Runnable {
        override fun run() {
            KeyboardUtils.showSoftInput(view)
        }
    }
}