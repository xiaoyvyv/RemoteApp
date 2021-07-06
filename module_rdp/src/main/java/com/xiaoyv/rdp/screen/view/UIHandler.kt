package com.xiaoyv.rdp.screen.view

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.services.LibFreeRDP.sendCursorEvent
import com.freerdp.freerdpcore.utils.Mouse

class UIHandler : Handler(Looper.getMainLooper()) {
    var session: RdpSession? = null

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
            // 光标移动
            SEND_MOVE_EVENT -> {
                session?.let { it ->
                    sendCursorEvent(it.instance, msg.arg1, msg.arg2, Mouse.getMoveEvent())
                }
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
        const val GRAPHICS_CHANGED = 6
        const val SCROLLING_REQUESTED = 7
    }
}