package com.xiaoyv.rdp.screen.presenter

import android.content.res.Configuration
import android.net.Uri
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.xiaoyv.busines.base.ImplBasePresenter
import com.xiaoyv.rdp.screen.contract.ScreenContract
import com.xiaoyv.rdp.screen.model.ScreenModel
import kotlin.math.max

/**
 * Presenter
 *
 * @author why
 * @since 2020/12/02
 */
class ScreenPresenter : ImplBasePresenter<ScreenContract.View>(), ScreenContract.Presenter {
    private val model = ScreenModel()

    private var currentSession: RdpSession? = null

    override fun v2pConnectWithConfig(rdpConfig: RdpConfig) {
        val screenSettings = rdpConfig.screenSettings
        LogUtils.e("屏幕分辨率设置：", screenSettings.getResolutionString())

        if (screenSettings.isAutomatic()) {
            if (Utils.getApp().resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
            ) {
                // 大屏幕设备，即平板电脑：只需使用屏幕信息
                screenSettings.height = ScreenUtils.getScreenHeight()
                screenSettings.width = ScreenUtils.getScreenWidth()
            } else {
                // 小屏幕设备，即电话：自动使用屏幕的最大边长，并将其设置为 16:10 分辨率
                val screenMax: Int =
                    max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())
                screenSettings.height = screenMax
                screenSettings.width = (screenMax.toFloat() * 1.6f).toInt()
            }
        }
        // 适配屏幕大小
        if (screenSettings.isFitScreen()) {
            screenSettings.height = ScreenUtils.getScreenHeight()
            screenSettings.width = ScreenUtils.getScreenWidth()
        }
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
    }

    override fun v2pGetSession(callback: (RdpSession) -> Unit) {
        currentSession?.let { callback.invoke(it) }
    }
}