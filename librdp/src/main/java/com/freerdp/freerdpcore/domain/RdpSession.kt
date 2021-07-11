package com.freerdp.freerdpcore.domain

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.services.LibRdpUiEventListener

/**
 * RdpSession
 *
 * @author why
 * @since 2021/07/03
 **/
class RdpSession {
    var instance: Long = 0
    var rdpConfig: RdpConfig? = null
    var rdpUri: Uri? = null
    var surface: BitmapDrawable? = null
    var libRdpUiEventListener: LibRdpUiEventListener? = null

    constructor(instance: Long, config: RdpConfig) {
        this.instance = instance
        this.rdpConfig = config
    }

    constructor(instance: Long, openUri: Uri) {
        this.instance = instance
        this.rdpUri = openUri
    }

    fun connect() {
        if (rdpUri != null) {
            LibFreeRDP.setConnectionInfo(instance, rdpUri!!)
        } else {
            LibFreeRDP.setConnectionInfo(instance, rdpConfig!!)
        }
        LibFreeRDP.connect(instance)
    }
}