package com.xiaoyv.business.rx

import com.xiaoyv.blueprint.base.rxjava.event.RxEvent
import com.xiaoyv.blueprint.rxbus.RxBus

/**
 * RxEventTag
 *
 * @author why
 * @since 2022/2/27
 */
object RxEventTag {
    const val EVENT_ADD_RDP_SUCCESS = "EVENT_RDP_ADD_SUCCESS"
    const val EVENT_ADD_SSH_SUCCESS = "EVENT_ADD_SSH_SUCCESS"
    const val EVENT_ADD_FTP_SUCCESS = "EVENT_ADD_FTP_SUCCESS"


    const val EVENT_SSH_DISCONNECT = "EVENT_SSH_DISCONNECT"
}

/**
 * RxEventStack
 *
 * @author why
 * @since 2022/2/27
 */
object RxEventStack {

    @JvmStatic
    fun postEmpty(rxEventTag: String) {
        RxBus.getDefault().post(RxEvent(), rxEventTag)
    }
}