package com.xiaoyv.rdp.screen.presenter

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Message
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.utils.RdpMouse
import com.freerdp.freerdpcore.view.RdpPointerView
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.rdp.screen.contract.ScreenContract
import com.xiaoyv.rdp.screen.model.ScreenModel
import com.xiaoyv.rdp.screen.config.RdpUiEventHandler
import com.xiaoyv.ui.scroll.FreeScrollView

/**
 * Presenter
 *
 * @author why
 * @since 2020/12/02
 */
class ScreenPresenter : ImplBasePresenter<ScreenContract.View>(), ScreenContract.Presenter {
    private val model = ScreenModel()
    private val uiHandler = RdpUiEventHandler()

    /**
     * 丢弃的移动事件
     */
    private var discardedMoveEvents = 0

    private var currentSession: RdpSession? = null

    override fun v2pConnectWithConfig(rdpConfig: RdpConfig) {
        // 配置信息
        model.p2mApplyConfig(rdpConfig, requireView.p2vScreenLandscape())
        // 创建一个会话信息
        val rdpSession = RdpApp.createSession(rdpConfig, Utils.getApp())
        v2pStartConnect(rdpSession, false)
    }

    override fun v2pConnectWithUri(rdpUri: Uri) {
        val rdpSession = RdpApp.createSession(rdpUri, Utils.getApp())
        v2pStartConnect(rdpSession, false)
    }

    override fun v2pStartConnect(rdpSession: RdpSession, resumeConnect: Boolean) {
        val session = requireView.p2vStartConnect(rdpSession)
        // 若是恢复已经连接的实例，则不用重新连接
        if (resumeConnect) {
            requireView.p2vBindSession(session)
        } else {
            // 若存在实例，且不需要恢复则先取消再连接
            LibFreeRDP.cancelConnection(session.instance)
            // 连接
            ThreadUtils.getCachedPool().submit {
                session.connect()
            }
        }
        currentSession = session
        uiHandler.session = session
    }

    override fun v2pFreeSession() {
        // 取消运行断开计时器
        RdpApp.cancelDisconnectTimer()

        // 断开所有剩余会话的连接
        val sessions: Collection<RdpSession> = RdpApp.getSessions()
        for (session in sessions) {
            LibFreeRDP.disconnect(session.instance)
        }

        RdpApp.freeSession(currentSession?.instance ?: return)
        currentSession = null
    }

    override fun v2pGetSession(empty: () -> Unit, callback: (RdpSession) -> Unit) {
        currentSession?.let { callback.invoke(it) }
    }

    /**
     * 发送光标移动事件
     */
    override fun v2pSendDelayedMoveEvent(x: Int, y: Int) {
        if (uiHandler.hasMessages(RdpUiEventHandler.HANDLER_EVENT_CURSOR_MOVING)) {
            uiHandler.removeMessages(RdpUiEventHandler.HANDLER_EVENT_CURSOR_MOVING)
            discardedMoveEvents++
        } else discardedMoveEvents = 0

        // 超过最大丢弃数则发送事件，负责继续延迟
        if (discardedMoveEvents > RdpUiEventHandler.HANDLER_EVENT_CURSOR_MOVING_MAX_COUNT) {
            currentSession?.let { session ->
                LibFreeRDP.sendCursorEvent(session.instance, x, y, RdpMouse.getMoveEvent())
            }
        } else uiHandler.sendMessageDelayed(
            Message.obtain(null, RdpUiEventHandler.HANDLER_EVENT_CURSOR_MOVING, x, y),
            RdpUiEventHandler.HANDLER_EVENT_CURSOR_MOVING_TIMEOUT
        )
    }

    override fun v2pProcessVirtualKey(virtualKeyCode: Int, down: Boolean) {
        LibFreeRDP.sendKeyEvent(currentSession?.instance ?: return, virtualKeyCode, down)
    }

    override fun v2pProcessUnicodeKey(unicodeKey: Int) {
        LibFreeRDP.sendUnicodeKeyEvent(currentSession?.instance ?: return, unicodeKey, true)
        LibFreeRDP.sendUnicodeKeyEvent(currentSession?.instance ?: return, unicodeKey, true)
    }

    /**
     * 移除光标移动事件
     */
    override fun v2pCancelDelayedMoveEvent() {
        uiHandler.removeMessages(RdpUiEventHandler.HANDLER_EVENT_CURSOR_MOVING)
    }

    override fun v2pTouchPointerLeftClick(point: Point, down: Boolean) {
        LibFreeRDP.sendCursorEvent(
            currentSession?.instance ?: return,
            point.x, point.y,
            RdpMouse.getLeftButtonEvent(down)
        )
    }

    override fun v2pTouchPointerScroll(down: Boolean) {
        LibFreeRDP.sendCursorEvent(
            currentSession?.instance ?: return, 0, 0, RdpMouse.getScrollEvent(down)
        )
    }

    override fun v2pGenericMotionScroll(vScroll: Float) {
        if (vScroll < 0) {
            LibFreeRDP.sendCursorEvent(
                currentSession?.instance ?: return, 0, 0, RdpMouse.getScrollEvent(false)
            )
        }
        if (vScroll > 0) {
            LibFreeRDP.sendCursorEvent(
                currentSession?.instance ?: return, 0, 0, RdpMouse.getScrollEvent(true)
            )
        }
    }

    override fun v2pSessionViewScroll(down: Boolean) {
        LibFreeRDP.sendCursorEvent(
            currentSession?.instance ?: return,
            0, 0, RdpMouse.getScrollEvent(down)
        )
    }

    override fun v2pSessionViewLeftTouch(
        x: Int,
        y: Int,
        down: Boolean,
        toggleMouseButtons: Boolean
    ) {
        LibFreeRDP.sendCursorEvent(
            currentSession?.instance ?: return, x, y,
            if (toggleMouseButtons) RdpMouse.getRightButtonEvent(down)
            else RdpMouse.getLeftButtonEvent(down)
        )
    }

    override fun v2pGraphicsUpdate(
        sessionView: RdpSessionView,
        bitmap: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        LibFreeRDP.updateGraphics(
            currentSession?.instance ?: return, bitmap, x, y, width, height
        )
        sessionView.addInvalidRegion(Rect(x, y, x + width, y + height))

        // 刷新 SessionView
        ThreadUtils.runOnUiThread {
            sessionView.invalidateRegion()
        }
    }

    /**
     * 将本地复制内容放到远程主机的剪切板上
     */
    override fun v2pSendClipboardData(data: String) {
        LibFreeRDP.sendClipboardData(currentSession?.instance ?: return, data)
    }

    override fun v2pTouchPointerRightClick(point: Point, down: Boolean) {
        LibFreeRDP.sendCursorEvent(
            currentSession?.instance ?: return,
            point.x, point.y,
            RdpMouse.getRightButtonEvent(down)
        )
    }

    override fun v2pTouchPointerMove(point: Point) {
        LibFreeRDP.sendCursorEvent(
            currentSession?.instance ?: return,
            point.x, point.y, RdpMouse.getMoveEvent()
        )
    }

    /**
     * 指针位于边缘时，画面自动往中心滚动
     */
    override fun v2pAutoScrollPointer(
        tpvPointer: RdpPointerView,
        rsvSession: RdpSessionView,
        rsvScroll: FreeScrollView
    ) {
        uiHandler.tpvPointer = tpvPointer
        uiHandler.rsvSession = rsvSession
        uiHandler.rsvScroll = rsvScroll

        // 根布局宽高
        val width = requireView.p2vGetRootParam().first
        val height = requireView.p2vGetRootParam().second

        // 指针位于边缘时，画面自动滚动
        if (!uiHandler.hasMessages(RdpUiEventHandler.HANDLER_EVENT_BORDER_SCROLL)) {
            uiHandler.sendMessageDelayed(
                Message.obtain(null, RdpUiEventHandler.HANDLER_EVENT_BORDER_SCROLL, width, height),
                RdpUiEventHandler.HANDLER_EVENT_BORDER_SCROLL_TIMEOUT
            )
        }
    }
}