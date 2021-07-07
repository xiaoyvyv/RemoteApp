package com.xiaoyv.rdp.screen.contract

import android.net.Uri
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.xiaoyv.busines.base.IBaseModel
import com.xiaoyv.busines.base.IBasePresenter
import com.xiaoyv.busines.base.IBaseView

/**
 * RdpListContract
 *
 * @author why
 * @since 2020/11/29
 */
interface ScreenContract {
    interface View : IBaseView {
        fun vProcessArgument()
        fun p2vStartConnect(rdpSession: RdpSession): RdpSession
        fun vSessionClose(result: Int)
        fun p2vBindSession(rdpSession: RdpSession)
        fun p2vScreenLandscape(): Boolean
        fun vSetLandscapeScreen(landscape: Boolean)
    }

    interface Presenter : IBasePresenter {
        fun v2pConnectWithConfig(rdpConfig: RdpConfig)
        fun v2pConnectWithUri(rdpUri: Uri)
        fun v2pStartConnect(rdpSession: RdpSession, resumeConnect: Boolean)
        fun v2pGetSession(empty: () -> Unit = {}, callback: (RdpSession) -> Unit)
        fun v2pSendDelayedMoveEvent(x: Int, y: Int)
        fun v2pCancelDelayedMoveEvent()
    }

    interface Model : IBaseModel {
        fun p2mApplyConfig(rdpConfig: RdpConfig, landscape: Boolean)
    }
}