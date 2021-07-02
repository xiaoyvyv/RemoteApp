package com.xiaoyv.rdp.screen.config

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.application.RdpSessionState

/**
 * @author Admin
 */
class LibFreeRDPBroadcastReceiver : BroadcastReceiver() {
    var onPrepareConnect: (context: Context) -> Unit = {}
    var onConnectionSuccess: (context: Context) -> Unit = {}
    var onConnectionFailure: (context: Context) -> Unit = {}
    var onDisconnecting: (context: Context) -> Unit = {}
    var onDisconnected: (context: Context) -> Unit = {}

    var rdpSession: RdpSessionState? = null

    override fun onReceive(context: Context, intent: Intent?) {
        intent ?: return
        val extras = intent.extras ?: return
        val instance = extras.getLong(RdpApp.EVENT_PARAM, -1)
        val rdpEventType = extras.getInt(RdpApp.EVENT_TYPE, -1)

        rdpSession?.let { session ->
            // 判断是否为当前会话的事件
            if (session.instance != instance) {
                return
            }

            // 判断事件类型
            when (rdpEventType) {
                RdpApp.FREERDP_EVENT_PREPARE_CONNECT -> onPrepareConnect(context)
                RdpApp.FREERDP_EVENT_CONNECTION_SUCCESS -> onConnectionSuccess(context)
                RdpApp.FREERDP_EVENT_CONNECTION_FAILURE -> onConnectionFailure(context)
                RdpApp.FREERDP_EVENT_DISCONNECTING -> onDisconnecting(context)
                RdpApp.FREERDP_EVENT_DISCONNECTED -> onDisconnected(context)
            }
        }
    }
}