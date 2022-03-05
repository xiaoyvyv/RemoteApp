package com.xiaoyv.business.utils

import androidx.fragment.app.*
import com.blankj.utilcode.util.StringUtils
import com.xiaoyv.desktop.business.R
import com.xiaoyv.desktop.ui.databinding.UiDialogBinding
import com.xiaoyv.widget.dialog.UiNormalDialog
import java.lang.reflect.Field

/**
 * DialogKt
 *
 * @author why
 * @since 2022/3/5
 */
class DialogKt {
}

fun Fragment.showDialog(
    title: String = "",
    content: String = "",
    onConfirmListener: (UiNormalDialog) -> Unit = { it.dismissAllowingStateLoss() },
    onCancelListener: (UiNormalDialog) -> Unit = { it.dismissAllowingStateLoss() }
) {
    createDialog(title, content, onConfirmListener, onCancelListener)
        .showAllowingStateLoss(childFragmentManager, UiNormalDialog::class.java.simpleName)
}

fun FragmentActivity.showDialog(
    title: String = "",
    content: String = "",
    onConfirmListener: (UiNormalDialog) -> Unit = { it.dismissAllowingStateLoss() },
    onCancelListener: (UiNormalDialog) -> Unit = { it.dismissAllowingStateLoss() }
) {
    createDialog(title, content, onConfirmListener, onCancelListener)
        .showAllowingStateLoss(supportFragmentManager, UiNormalDialog::class.java.simpleName)
}

private fun createDialog(
    title: String = "",
    content: String = "",
    onConfirmListener: (UiNormalDialog) -> Unit = { it.dismissAllowingStateLoss() },
    onCancelListener: (UiNormalDialog) -> Unit = { it.dismissAllowingStateLoss() }
) = UiNormalDialog.Builder().apply {
    cancelText = null
    confirmText = null
    customView = R.layout.ui_dialog
    onCustomViewInitListener = { dialog, view ->
        val dialogBinding = UiDialogBinding.bind(view)
        dialogBinding.tvTitle.text =
            title.ifBlank { StringUtils.getString(R.string.ui_common_dialog) }
        dialogBinding.tvMsg.text = content
        dialogBinding.tvDone.setOnClickListener {
            onConfirmListener.invoke(dialog)
        }
        dialogBinding.tvTemp.setOnClickListener {
            onCancelListener.invoke(dialog)
        }
    }
}.create()

/**
 * ### 解决 DialogFragment IllegalStateException
 *
 * ```java
 * java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
 * ```
 */
fun DialogFragment.showAllowingStateLoss(fm: FragmentManager, tag: String) {
    runCatching {
        val dismissed: Field = DialogFragment::class.java.getDeclaredField("mDismissed")
        dismissed.isAccessible = true
        dismissed.set(this, false)
    }
    runCatching {
        val dismissed: Field = DialogFragment::class.java.getDeclaredField("mShownByMe")
        dismissed.isAccessible = true
        dismissed.set(this, true)
    }
    val ft: FragmentTransaction = fm.beginTransaction()
    ft.add(this, tag)
    ft.commitAllowingStateLoss()
}
