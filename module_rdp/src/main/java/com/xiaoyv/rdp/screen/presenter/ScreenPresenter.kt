package com.xiaoyv.rdp.screen.presenter

import android.net.Uri
import android.os.Message
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.presentation.SessionActivity
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.utils.Mouse
import com.xiaoyv.busines.base.ImplBasePresenter
import com.xiaoyv.rdp.screen.contract.ScreenContract
import com.xiaoyv.rdp.screen.model.ScreenModel
import com.xiaoyv.rdp.screen.view.UIHandler

/**
 * Presenter
 *
 * @author why
 * @since 2020/12/02
 */
class ScreenPresenter : ImplBasePresenter<ScreenContract.View>(), ScreenContract.Presenter {
    private val model = ScreenModel()
    private val uiHandler = UIHandler()


    /**
     * 丢弃的移动事件
     */
    private var discardedMoveEvents = 0

    private var currentSession: RdpSession? = null

    override fun v2pConnectWithConfig(rdpConfig: RdpConfig) {
        // 配置信息
        model.p2mApplyConfig(rdpConfig)
        // 创建一个会话信息
        val rdpSession = RdpApp.createSession(rdpConfig, Utils.getApp())
        v2pStartConnect(rdpSession, false)
    }

    override fun v2pConnectWithUri(rdpUri: Uri) {
        val rdpSession = RdpApp.createSession(rdpUri, Utils.getApp())
        v2pStartConnect(rdpSession, false)
    }

    override fun v2pStartConnect(rdpSession: RdpSession, resumeConnect: Boolean) {
        val session = view.p2vStartConnect(rdpSession)
        // 若是恢复已经连接的实例，则不用重新连接
        if (resumeConnect) {
            view.p2vBindSession(session)
        } else {
            // 若存在实例，且不需要恢复则先取消再连接
            LibFreeRDP.cancelConnection(session.instance)
            // 连接
            ThreadUtils.getCachedPool()
                .submit {
                    session.connect()
                }
        }
        currentSession = session
        uiHandler.session = session
    }

    override fun v2pGetSession(empty: () -> Unit, callback: (RdpSession) -> Unit) {
        currentSession?.let { callback.invoke(it) }
    }

    /**
     * 发送光标移动事件
     */
    override fun v2pSendDelayedMoveEvent(x: Int, y: Int) {
        if (uiHandler.hasMessages(UIHandler.SEND_MOVE_EVENT)) {
            uiHandler.removeMessages(UIHandler.SEND_MOVE_EVENT)
            discardedMoveEvents++
        } else discardedMoveEvents = 0

        // 超过最大丢弃数则发送事件，负责继续延迟
        if (discardedMoveEvents > MAX_DISCARDED_MOVE_EVENTS) {
            currentSession?.let { session ->
                LibFreeRDP.sendCursorEvent(session.instance, x, y, Mouse.getMoveEvent())
            }
        } else uiHandler.sendMessageDelayed(
            Message.obtain(null, UIHandler.SEND_MOVE_EVENT, x, y), SEND_MOVE_EVENT_TIMEOUT
        )
    }

    /**
     * 移除光标移动事件
     */
    override fun v2pCancelDelayedMoveEvent() {
        uiHandler.removeMessages(UIHandler.SEND_MOVE_EVENT)
    }

    companion object {
        /**
         * 最大丢弃光标移动事件数
         */
        private const val MAX_DISCARDED_MOVE_EVENTS: Long = 3

        /**
         * 光标移动事件发送间隔
         */
        private const val SEND_MOVE_EVENT_TIMEOUT: Long = 150
    }

}