package com.xiaoyv.ui.dialog.loading

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatDialog

/**
 * UiLoadingDialog
 *
 * @author why
 * @since 2021/07/03
 **/
class UiLoadingDialog : AppCompatDialog {
    constructor(context: Context) : super(context)

    constructor(context: Context, theme: Int) : super(context, theme)

    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    init {

    }
}
