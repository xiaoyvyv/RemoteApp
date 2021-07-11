package com.freerdp.freerdpcore.services
/**
 * EventListener
 */
interface LibRdpEventListener {
    fun onPreConnect(instance: Long)
    fun onConnectionSuccess(instance: Long)
    fun onConnectionFailure(instance: Long)
    fun onDisconnecting(instance: Long)
    fun onDisconnected(instance: Long)
}