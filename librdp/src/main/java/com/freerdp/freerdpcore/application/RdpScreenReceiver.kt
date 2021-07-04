package com.freerdp.freerdpcore.application

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 亮屏息屏
 *
 * @author why
 */
class RdpScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                RdpApp.startDisconnectTimer()
            }
            Intent.ACTION_SCREEN_ON -> {
                RdpApp.cancelDisconnectTimer()
            }
        }
    }
}