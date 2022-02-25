package com.xiaoyv.rdp.screen.contract

import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.view.RdpPointerView
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.blueprint.base.IBaseModel
import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import com.xiaoyv.ui.scroll.FreeScrollView

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
        fun p2vGetRootParam(): Pair<Int, Int>
        fun vSetLandscapeScreen(landscape: Boolean)
        fun vOnSoftKeyBoardShow()
        fun vOnSoftKeyBoardClose()
    }

    interface Presenter : IBasePresenter {
        fun v2pConnectWithConfig(rdpConfig: RdpConfig)
        fun v2pConnectWithUri(rdpUri: Uri)
        fun v2pStartConnect(rdpSession: RdpSession, resumeConnect: Boolean)
        fun v2pGetSession(emptySession: () -> Unit = {}, callback: (RdpSession) -> Unit)
        fun v2pSendDelayedMoveEvent(x: Int, y: Int)
        fun v2pCancelDelayedMoveEvent()
        fun v2pFreeSession()
        fun v2pAutoScrollPointer(
            tpvPointer: RdpPointerView,
            rsvSession: RdpSessionView,
            rsvScroll: FreeScrollView
        )

        fun v2pProcessVirtualKey(virtualKeyCode: Int, down: Boolean)
        fun v2pProcessUnicodeKey(unicodeKey: Int)
        fun v2pTouchPointerMove(point: Point)
        fun v2pTouchPointerRightClick(point: Point, down: Boolean)
        fun v2pTouchPointerLeftClick(point: Point, down: Boolean)
        fun v2pTouchPointerScroll(down: Boolean)
        fun v2pGenericMotionScroll(vScroll: Float)
        fun v2pSessionViewScroll(down: Boolean)
        fun v2pSessionViewLeftTouch(x: Int, y: Int, down: Boolean, toggleMouseButtons: Boolean)
        fun v2pSendClipboardData(data: String)
        fun v2pGraphicsUpdate(
            sessionView: RdpSessionView, bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int
        )
    }

    interface Model : IBaseModel {
        fun p2mApplyConfig(rdpConfig: RdpConfig, landscape: Boolean)
    }
}