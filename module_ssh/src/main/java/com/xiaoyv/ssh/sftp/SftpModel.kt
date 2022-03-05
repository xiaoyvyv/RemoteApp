package com.xiaoyv.ssh.sftp

import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.trilead.ssh2.Connection
import com.trilead.ssh2.SCPClient
import com.trilead.ssh2.SFTPv3DirectoryEntry
import com.trilead.ssh2.jenkins.SFTPClient
import com.xiaoyv.business.global.ftp.*
import com.xiaoyv.business.room.entity.SshEntity
import com.xiaoyv.business.utils.PathKt
import com.xiaoyv.ssh.terminal.TerminalModel
import com.xiaoyv.ssh.utils.CMD_STAT
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * SftpModel
 *
 * @author why
 * @since 2022/2/28
 */
class SftpModel : BaseFtpModel(), SftpContract.Model {
    private var sshEntity: SshEntity = SshEntity()

    /**
     * 获取 SFTP 客户端
     */
    private var connection: Connection? = null
    private val requireConnection: Connection
        get() = connection ?: TerminalModel.createConnect(sshEntity).also {
            it.addConnectionMonitor {
                runCatching {
                    connection?.close()
                    connection = null
                }
            }
            connection = it
        }

    /**
     * 获取 SFTP 客户端
     */
    private var sftpClient: SFTPClient? = null
    private val requireSftpClient: SFTPClient
        get() = sftpClient ?: SFTPClient(requireConnection).also {
            sftpClient = it
        }

    /**
     * 获取 SCP 客户端
     */
    private var scpClient: SCPClient? = null
    private val requireScpClient: SCPClient
        get() = scpClient ?: SCPClient(requireConnection).apply {
            scpClient = this
        }

    override fun p2mInitConnection(sshEntity: SshEntity) {
        this.sshEntity = sshEntity
    }

    override fun p2mDoCommand(command: String): Observable<String> {
        return Observable.create {
            val outputStream = ByteArrayOutputStream()
            val charsetName = StandardCharsets.UTF_8.name()
            requireConnection.exec(command, outputStream)

            val result = ConvertUtils.outputStream2String(outputStream, charsetName).trim()
            it.onNext(result)
            it.onComplete()
        }
    }

    override fun p2mQueryFileList(dirName: String): Observable<BaseFtpBean> {
        return Observable.create {
            val dirPath = requireSftpClient.canonicalPath(dirName)

            // 列出目录
            val vector = requireSftpClient.ls(dirPath)
            val elements = vector.elements()
            val fileList = arrayListOf<BaseFtpFile>()
            while (elements.hasMoreElements()) {
                val element = elements.nextElement()

                // 不显示 [.|..] 两个目录
                val ftpFile = convertToFtpFile(element)
                if (ftpFile.fileName.isBlank() || ftpFile.fileName == "." || ftpFile.fileName == "..") {
                    continue
                }
                ftpFile.filePath = dirPath + (if (dirPath == "/") "" else "/") + ftpFile.fileName
                fileList.add(ftpFile)
            }
            // 排序 正序，文件夹在上
            val sortFileList = fileList.sortedWith { o1, o2 ->
                o1.fileName.compareTo(o2.fileName)
            }.sortedBy { file ->
                file.isDirectory.not()
            }

            it.onNext(BaseFtpBean(dirName = dirPath, data = sortFileList))
            it.onComplete()
        }
    }

    override fun isConnect(): Boolean {
        return connection != null
    }

    override fun p2mQueryFileStat(verifyPath: String): Observable<BaseFtpStat> {
        return p2mDoCommand(CMD_STAT + verifyPath).map { statInfo ->
            LogUtils.e("文件路径：$verifyPath", "属性：$statInfo")

            GsonUtils.fromJson(statInfo, BaseFtpStat::class.java).also {
                it.filePath = verifyPath
            }
        }.flatMap {
            val filePath = it.filePath
            val fileLink = it.fileLink

            Observable.create { emitter ->
                // ‘/symlink’ -> ‘bin’
                if (fileLink.contains("->")) {
                    val linkPath = fileLink.split("->")
                        .lastOrNull()
                        .orEmpty()
                        .replace("‘", "")
                        .replace("’", "")
                        .trim()

                    // 绝对路径
                    if (linkPath.startsWith("/")) {
                        it.linkTargetPath = linkPath
                    } else {
                        val temp = FileUtils.getDirName(filePath) + linkPath

                        // 规整路径
                        it.linkTargetPath = requireSftpClient.canonicalPath(temp)

                        LogUtils.e("链接目标原路径：$temp", "链接目标规整路径：${it.linkTargetPath}")
                    }
                }

                emitter.onNext(it)
                emitter.onComplete()
            }
        }
    }

    override fun p2mDownloadFile(baseFtpFile: BaseFtpFile): Observable<BaseFtpDownloadFile> {
        return Observable.create { emitter ->
            val filePath = baseFtpFile.filePath

            val downloadFile = BaseFtpDownloadFile(baseFtpFile.fileName)

            // 本地创建目标空白文件
            val localFilePath = PathKt.downloadDirPath + filePath
            FileUtils.createFileByDeleteOldFile(localFilePath)

            var preTimeLength = 0L

            // 下载速率检测
            val speedListen = Observable.interval(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe({
                    if (emitter.isDisposed) {
                        throw InterruptedException("Download service is end!")
                    }
                    synchronized(downloadFile) {
                        val nowLength = downloadFile.current
                        // 下载速度
                        val speed = nowLength - preTimeLength
                        downloadFile.downloadSpeed = speed
                        emitter.onNext(downloadFile)

                        preTimeLength = nowLength
                    }
                }, {
                    LogUtils.e("Download Speed: $it")
                })

            // 下载
            runCatching {
                requireScpClient.get(
                    filePath,
                    PathKt.downloadDirPath
                ) { srcFile: String, _: String, current: Long, total: Long ->
                    synchronized(downloadFile) {
                        downloadFile.fileName = srcFile
                        downloadFile.current = current
                        downloadFile.total = total

                        // 停止速率监听
                        if (current >= total) {
                            speedListen.dispose()
                        }
                    }
                }

            }.onFailure {
                if (emitter.isDisposed) {
                    LogUtils.e("Download service is end! ==> $it")
                    return@create
                }
                throw it
            }

            if (!speedListen.isDisposed) {
                speedListen.dispose()
            }

            downloadFile.downloadFilePath = localFilePath

            emitter.onNext(downloadFile)
            emitter.onComplete()

        }
    }

    override fun convertToFtpFile(any: Any) = BaseFtpFile().apply {
        if (any is SFTPv3DirectoryEntry) {
            // 该扩展文件格式不是标准，官方不建议解析
            val longEntryArray = any.longEntry.orEmpty().split(Regex(" +"))
            val attributes = any.attributes

            // 第三字段为拥有者，第四字段为所属组
            if (longEntryArray.size >= 4) {
                // 用户
                uid = attributes?.uid?.toLong() ?: 0
                user = longEntryArray[2]

                // 组
                gid = attributes?.gid?.toLong() ?: 0
                group = longEntryArray[3]
            }

            if (longEntryArray.isNotEmpty()) {
                // 当为 d 则是目录
                // 当为 - 则是文件；
                // 若是 l 则表示为链接文档(link file)；
                // 若是 b 则表示为装置文件里面的可供储存的接口设备(可随机存取装置)；
                // 若是 c 则表示为装置文件里面的串行端口设备，例如键盘、鼠标(一次性读取装置)。
                // 若是 p 则表示为(pipe)管道文件主要用于进程间通信。
                when (longEntryArray.first().trim().toCharArray().firstOrNull().toString()) {
                    "d" -> isDirectory = true
                    "-" -> isRegularFile = true
                    "l" -> isSymlink = true
                    "b" -> isBlock = true
                    "c" -> isChar = true
                    "p" -> isPipe = true
                }
            }
            // 基本属性
            fileName = any.filename.orEmpty()
            longEntry = any.longEntry.orEmpty()
            size = attributes?.size ?: 0
            permission = attributes.octalPermissions.orEmpty()
            modifierTime = (attributes?.mtime ?: 0L) * 1000L
        }
    }

    override fun p2mSendHeartbeatPackets() {
        connection?.sendIgnorePacket()
    }

    override fun v2mCloseFtp() {
        Observable.create<Boolean> {
            // 关闭 SFTP 客户端
            closeFtpQuietly()

            it.onNext(true)
            it.onComplete()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    /**
     * 重置 SFTP 客户端
     */
    private fun closeFtpQuietly() {
        runCatching {
            sftpClient?.close()
            sftpClient = null
        }

        runCatching {
            connection?.close()
            connection = null
        }
    }

}