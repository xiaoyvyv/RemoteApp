package com.xiaoyv.ssh.terminal

import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.romide.terminal.emulatorview.TermSession
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ServerHostKeyVerifier
import com.trilead.ssh2.Session
import com.trilead.ssh2.jenkins.SFTPClient
import com.trilead.ssh2.util.IOUtils
import com.xiaoyv.blueprint.exception.RxException
import com.xiaoyv.busines.config.SshLoginType
import com.xiaoyv.busines.room.entity.SshEntity
import io.reactivex.rxjava3.core.Observable
import java.io.File

/**
 * TerminalModel
 *
 * @author why
 * @since 2020/12/06
 */
class TerminalModel : TerminalContract.Model {
    private val sBufferSize = 524288

    private var connection: Connection? = null

    private val requireSftpClient: SFTPClient
        get() = SFTPClient(connection ?: throw NullPointerException("请先连接SSH客户端"))

    /**
     * 连接前验证
     */
    private val serverHostKeyVerifier = ServerHostKeyVerifier { sHost, sPort, sEntType, bytes ->
        true
    }

    override fun p2mConnectSsh(sshEntity: SshEntity): Observable<Session> {
        return Observable.create {
            try {
                val port = sshEntity.port.toIntOrNull() ?: 22

                val connection = Connection(sshEntity.ip, port)
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

                // 连接失败
                if (!authenticate) {
                    it.onError(RxException("SSH 认证失败"))
                    return@create
                }

                // 会话
                val session = connection.openSession().apply {
                    requestPTY("vt100")
                    startShell()
                }

                // 缓存对象
                this.connection = connection

                it.onNext(session)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }
    }

    override fun p2mReleaseSession(termSession: TermSession): Observable<Boolean> {
        return Observable.create {
            termSession.finish()
            connection?.close()
            connection = null

            it.onNext(true)
            it.onComplete()
        }
    }


    override fun p2mDoCommandLs(dirName: String): Observable<List<Any>> {
        return Observable.create { emitter ->
            val files: List<Any> = requireSftpClient.ls(dirName).toList()

//            val createFile = requireSftpClient.createFile("/home/www/test.txt")
            val outputStream = requireSftpClient.writeToFile("/home/www/test.txt")

            val progress: (Double) -> Unit = {}

            val inputStream = ConvertUtils.string2InputStream("xxxxxxxx", "UTF-8")

            var success = false
            outputStream.runCatching {
                val totalSize: Double = inputStream.available().toDouble()
                var curSize = 0
                progress.invoke(0.0)

                val data = ByteArray(sBufferSize)
                var len: Int
                while (inputStream.read(data).apply { len = this } != -1) {
                    outputStream.write(data, 0, len)
                    curSize += len
                    progress.invoke(curSize / totalSize)
                }
            }.onSuccess {
                IOUtils.closeQuietly(outputStream)
                IOUtils.closeQuietly(inputStream)
                success = true
            }.onFailure {
                IOUtils.closeQuietly(outputStream)
                IOUtils.closeQuietly(inputStream)
                success = false
            }
            LogUtils.e("上传：$success")
            emitter.onNext(files)
            emitter.onComplete()
        }
    }
}