package com.freerdp.freerdpcore.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Context

abstract class ClipboardManagerProxy {
    abstract fun setLocalClipboardData(data: String)
    abstract fun addClipboardChangedListener(listener: OnClipboardChangedListener?)
    abstract fun removeClipboardboardChangedListener(listener: OnClipboardChangedListener?)

    interface OnClipboardChangedListener {
        fun onLocalClipboardChanged(data: String)
    }

    private class HCClipboardManager(context: Context) : ClipboardManagerProxy(),
        OnPrimaryClipChangedListener {

        private val mClipboardManager: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        private var mListener: OnClipboardChangedListener? = null

        override fun setLocalClipboardData(data: String) {
            mClipboardManager.setPrimaryClip(
                ClipData.newPlainText("rdp-clipboard", data ?: "")
            )
        }

        override fun onPrimaryClipChanged() {
            mClipboardManager.primaryClip?.let {
                if (it.itemCount > 0) {
                    val firstItem = it.getItemAt(0).text
                    firstItem?.toString()?.let { copyText ->
                        mListener?.onLocalClipboardChanged(copyText)
                    }
                }
            }
        }

        override fun addClipboardChangedListener(listener: OnClipboardChangedListener?) {
            mListener = listener
            mClipboardManager.addPrimaryClipChangedListener(this)
        }

        override fun removeClipboardboardChangedListener(listener: OnClipboardChangedListener?) {
            mListener = null
            mClipboardManager.removePrimaryClipChangedListener(this)
        }

    }

    companion object {

        @JvmStatic
        fun getClipboardManager(ctx: Context): ClipboardManagerProxy {
            return HCClipboardManager(ctx)
        }
    }
}