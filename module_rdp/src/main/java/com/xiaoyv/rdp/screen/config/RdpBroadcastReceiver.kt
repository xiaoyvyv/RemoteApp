package com.xiaoyv.rdp.screen.config

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.freerdp.freerdpcore.application.RdpApp

/**
 * @author Admin
 */
class RdpBroadcastReceiver : BroadcastReceiver() {
    var onPrepareConnect: (context: Context) -> Unit = {}
    var onConnectionSuccess: (context: Context) -> Unit = {}
    var onConnectionFailure: (context: Context) -> Unit = {}
    var onDisconnecting: (context: Context) -> Unit = {}
    var onDisconnected: (context: Context) -> Unit = {}

    var currentInstance = -1L

    override fun onReceive(context: Context, intent: Intent?) {
        intent ?: return
        val extras = intent.extras ?: return
        val instance = extras.getLong(RdpApp.EVENT_PARAM, -1)
        val rdpEventType = extras.getInt(RdpApp.EVENT_TYPE, -1)

        // 判断是否为当前会话的事件
        if (currentInstance != instance) {
            return
        }

        // 判断事件类型
        when (rdpEventType) {
            RdpApp.FREERDP_EVENT_PREPARE_CONNECT -> onPrepareConnect.invoke(context)
            RdpApp.FREERDP_EVENT_CONNECTION_SUCCESS -> onConnectionSuccess.invoke(context)
            RdpApp.FREERDP_EVENT_CONNECTION_FAILURE -> onConnectionFailure.invoke(context)
            RdpApp.FREERDP_EVENT_DISCONNECTING -> onDisconnecting.invoke(context)
            RdpApp.FREERDP_EVENT_DISCONNECTED -> onDisconnected.invoke(context)
        }
    }
}