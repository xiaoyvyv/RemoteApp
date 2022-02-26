package com.xiaoyv.ui.dialog.normal

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.xiaoyv.desktop.ui.R
import com.xiaoyv.desktop.ui.databinding.UiDialogBinding
import me.jessyan.autosize.utils.AutoSizeUtils

/**
 * NormalDialog
 *
 * @author why
 * @since 2021/07/04
 **/
class NormalDialog : AlertDialog {
    private val binding = UiDialogBinding.inflate(LayoutInflater.from(context))

    var title: String? = StringUtils.getString(R.string.ui_common_title)
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var message: String? = ""
        set(value) {
            field = value
            binding.tvMsg.text = value
        }

    var customView: View? = null
        set(value) {
            field = value
            value?.let {
                binding.tvMsg.visibility = View.GONE
                binding.flView.addView(value)
            }
        }

    var cancelText: String? = StringUtils.getString(R.string.ui_common_cancel)
        set(value) {
            field = value
            binding.tvTemp.text = value
        }

    var doneText: String? = StringUtils.getString(R.string.ui_common_done)
        set(value) {
            field = value
            binding.tvDone.text = value
        }

    var cancelClickListener: () -> Boolean = { true }

    var doneClickListener: () -> Boolean = { true }

    constructor(context: Context) : super(context)

    constructor(context: Context, theme: Int) : super(context, theme)

    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    init {
        setView(binding.root)
        binding.tvTemp.setOnClickListener {
            if (cancelClickListener.invoke()) {
                dismiss()
            }
        }

        binding.tvDone.setOnClickListener {
            if (doneClickListener.invoke()) {
                dismiss()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        window?.setBackgroundDrawableResource(R.color.ui_transparent)
        window?.attributes?.let {
            it.dimAmount = 0.2f
            it.width = ScreenUtils.getScreenWidth() - AutoSizeUtils.dp2px(Utils.getApp(), 80f)
        }
    }
}