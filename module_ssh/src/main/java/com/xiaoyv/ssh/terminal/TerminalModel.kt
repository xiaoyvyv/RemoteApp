@file:Suppress("MemberVisibilityCanBePrivate")

package com.xiaoyv.ssh.terminal

import androidx.annotation.WorkerThread
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ServerHostKeyVerifier
import com.trilead.ssh2.Session
import com.xiaoyv.blueprint.base.rxjava.event.RxEvent
import com.xiaoyv.blueprint.rxbus.RxBus
import com.xiaoyv.busines.config.SshLoginType
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.busines.rx.RxEventTag
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException

/**
 * TerminalModel
 *
 * @author why
 * @since 2020/12/06
 */
class TerminalModel : TerminalContract.Model {

    private var sshConnection: Connection? = null
    private var sshSession: Session? = null

    override fun p2mConnectSsh(sshEntity: SshEntity): Observable<Session> {
        return Observable.create {
            val connection = createConnect(sshEntity)

            // 会话
            val session = connection.openSession().apply {
                requestPTY("xterm")
                startShell()
            }

            // 监听
            connection.addConnectionMonitor {
                RxBus.getDefault().post(RxEvent(), RxEventTag.EVENT_SSH_DISCONNECT)
            }

            // 缓存对象
            sshSession = session
            sshConnection = connection

            it.onNext(session)
            it.onComplete()
        }
    }

    override fun p2mReleaseSession() {
        Observable.create<Boolean> {
            closeConnectionQuietly()

            it.onNext(true)
            it.onComplete()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    override fun p2mSendHeartbeatPackets() {
        sshConnection?.sendIgnorePacket()
    }

    /**
     * 静默关闭连接
     */
    private fun closeConnectionQuietly() {
        runCatching {
            sshSession?.close()
            sshSession = null
        }

        runCatching {
            sshConnection?.close()
            sshConnection = null
        }
    }

    companion object {

        /**
         * 连接前验证
         */
        private val serverHostKeyVerifier = ServerHostKeyVerifier { sHost, sPort, sEntType, bytes ->
            true
        }

        @JvmStatic
        @WorkerThread
        @Throws(IOException::class)
        fun createConnect(sshEntity: SshEntity): Connection {
            val port = sshEntity.port.toIntOrNull() ?: 22
            val connection = Connection(sshEntity.ip, port)
            try {
                connection.connect(serverHostKeyVerifier, 5000, 5000)

                var authenticate = false

                // 授权类型
                when (sshEntity.authType) {
                    SshLoginType.TYPE_NONE -> {
                        authenticate = connection.authenticateWithNone(sshEntity.account)
                    }
                    SshLoginType.TYPE_PASSWORD -> {
                        authenticate =
                            connection.authenticateWithPassword(
                                sshEntity.account,
                                sshEntity.password
                            )
                    }
                    SshLoginType.TYPE_PUBLIC_KEY -> {
                        authenticate =
                            connection.authenticateWithPublicKey(
                                sshEntity.account,
                                File(""),
                                sshEntity.password
                            )
                    }
                }

                if (authenticate) {
                    return connection
                }
                throw IOException("无法认证 SSH")
            } catch (e: Exception) {
                throw IOException("无法连接 SSH", e)
            }
        }
    }
}