package com.xiaoyv.ui.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.KeyboardUtils

/**
 * BaseDialog
 *
 * @author why
 * @since 2021/07/11
 */
class BaseDialog : AlertDialog {
    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    override fun dismiss() {
        currentFocus?.let {
            KeyboardUtils.hideSoftInput(it)
        }
        super.dismiss()
    }
}