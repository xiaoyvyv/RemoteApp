package com.freerdp.freerdpcore.services

import android.content.Context

object LibRdpSettings {
    @JvmStatic
    fun getDisconnectTimeout(context: Context?): Int {
        return 0
    }

    @JvmStatic
    fun getHideStatusBar(context: Context?): Boolean {
        return true
    }

    @JvmStatic
    fun getHideActionBar(context: Context?): Boolean {
        return true
    }

    @JvmStatic
    fun getAcceptAllCertificates(context: Context?): Boolean {
        return true
    }

    @JvmStatic
    fun getInvertScrolling(context: Context?): Boolean {
        return true
    }

    @JvmStatic
    fun getAskOnExit(context: Context?): Boolean {
        return true
    }

    @JvmStatic
    fun getAutoScrollTouchPointer(context: Context?): Boolean {
        return true
    }

    @JvmStatic
    fun getClientName(context: Context?): String {
        return "true"
    }
}