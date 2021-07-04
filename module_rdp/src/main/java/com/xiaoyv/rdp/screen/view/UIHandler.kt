package com.xiaoyv.rdp.screen.view

import android.app.Dialog
import android.os.Handler
import android.os.Message

class UIHandler internal constructor() : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            GRAPHICS_CHANGED -> {
            }
            REFRESH_SESSIONVIEW -> {
            }
            DISPLAY_TOAST -> {
            }
            HIDE_ZOOMCONTROLS -> {
            }
            SEND_MOVE_EVENT -> {
            }
            SHOW_DIALOG -> {
                // 创建并显示对话框
                (msg.obj as Dialog).show()
            }
            SCROLLING_REQUESTED -> {
            }
        }
    }

    companion object {
        const val REFRESH_SESSIONVIEW = 1
        const val DISPLAY_TOAST = 2
        const val HIDE_ZOOMCONTROLS = 3
        const val SEND_MOVE_EVENT = 4
        const val SHOW_DIALOG = 5
        const val GRAPHICS_CHANGED = 6
        const val SCROLLING_REQUESTED = 7
    }
}