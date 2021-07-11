package com.xiaoyv.rdp.screen.config

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.utils.RdpMouse
import com.freerdp.freerdpcore.view.RdpPointerView
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.ui.scroll.FreeScrollView

class RdpUiEventHandler : Handler(Looper.getMainLooper()) {
    var session: RdpSession? = null
    var tpvPointer: RdpPointerView? = null
    var rsvSession: RdpSessionView? = null
    var rsvScroll: FreeScrollView? = null

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            // 光标移动
            HANDLER_EVENT_CURSOR_MOVING -> {
                session?.let { it ->
                    LibFreeRDP.sendCursorEvent(
                        it.instance,
                        msg.arg1,
                        msg.arg2,
                        RdpMouse.getMoveEvent()
                    )
                }
            }
            HANDLER_EVENT_BORDER_SCROLL -> {
                val tpvPointer = tpvPointer ?: return
                val rsvSession = rsvSession ?: return
                val rsvScroll = rsvScroll ?: return

                var scrollX = 0
                var scrollY = 0

                val pointerPos: FloatArray = tpvPointer.pointerPosition
                val pointerX = pointerPos[0]
                val pointerY = pointerPos[1]

                val rootViewWidth = msg.arg1
                val rootViewHeight = msg.arg2

                if (pointerX > rootViewWidth - tpvPointer.pointerWidth) {
                    scrollX = HANDLER_EVENT_BORDER_SCROLL_DISTANCE
                } else if (pointerX < 0) {
                    scrollX = -HANDLER_EVENT_BORDER_SCROLL_DISTANCE
                }

                if (pointerY > rootViewHeight - tpvPointer.pointerHeight) {
                    scrollY = HANDLER_EVENT_BORDER_SCROLL_DISTANCE
                } else if (pointerY < 0) {
                    scrollY = -HANDLER_EVENT_BORDER_SCROLL_DISTANCE
                }

                rsvScroll.scrollBy(scrollX, scrollY)

                // 看看我们是否达到了 min/max 滚动位置
                if (rsvScroll.scrollX == 0 ||
                    rsvScroll.scrollX == rsvSession.width - rsvScroll.width
                ) {
                    scrollX = 0
                }
                if (rsvScroll.scrollY == 0 ||
                    rsvScroll.scrollY == rsvSession.height - rsvScroll.height
                ) {
                    scrollY = 0
                }
                if (scrollX != 0 || scrollY != 0) {
                    sendMessageDelayed(
                        Message.obtain(
                            null, HANDLER_EVENT_BORDER_SCROLL, rootViewWidth, rootViewHeight
                        ), HANDLER_EVENT_BORDER_SCROLL_TIMEOUT
                    )
                }
            }
        }
    }

    companion object {
        /**
         * 移动光标位置
         */
        const val HANDLER_EVENT_CURSOR_MOVING = 4

        /**
         * 移动光标位置，事件发送间隔
         */
        const val HANDLER_EVENT_CURSOR_MOVING_TIMEOUT = 150L

        /**
         * 移动光标位置，事件最大丢弃数
         */
        const val HANDLER_EVENT_CURSOR_MOVING_MAX_COUNT = 3

        /**
         * 指针位于边缘时，画面自动滚动
         */
        const val HANDLER_EVENT_BORDER_SCROLL = 7

        /**
         * 指针位于边缘时，画面自动滚动，事件发送间隔
         */
        const val HANDLER_EVENT_BORDER_SCROLL_TIMEOUT = 5L

        /**
         * 指针位于边缘时，画面自动滚动步进长度
         */
        const val HANDLER_EVENT_BORDER_SCROLL_DISTANCE = 4
    }
}