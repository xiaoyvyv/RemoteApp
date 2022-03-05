package com.xiaoyv.business.utils

import io.reactivex.rxjava3.core.Observable
import java.net.InetSocketAddress
import java.net.Socket


/**
 * HostVerifyUtils 验证 Host 是否有通的
 *
 * @author why
 * @since 2022/2/27
 */
object HostVerifyUtils {

    @JvmStatic
    fun verifyHost(host: String, port: String, timeout: Int = 10000): Observable<Boolean> {
        return Observable.create { emitter ->
            var success = false

            Socket().runCatching {
                apply {
                    connect(InetSocketAddress(host, port.toInt()), timeout)
                }
            }.onSuccess {
                it.runCatching {
                    close()
                }
                success = true
            }.onFailure {
                success = false
            }

            emitter.onNext(success)
            emitter.onComplete()
        }
    }
}