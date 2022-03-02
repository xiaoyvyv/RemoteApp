package com.xiaoyv.ssh.sftp

import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.trilead.ssh2.SCPClient
import com.trilead.ssh2.SFTPv3DirectoryEntry
import com.trilead.ssh2.jenkins.SFTPClient
import com.xiaoyv.busines.ftp.*
import com.xiaoyv.busines.utils.PathKt
import com.xiaoyv.ssh.terminal.TerminalModel
import com.xiaoyv.ssh.utils.CMD_STAT
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * SftpModel
 *
 * @author why
 * @since 2022/2/28
 */
class SftpModel : BaseFtpModel(), SftpContract.Model {

    /**
     * 获取 SFTP 客户端
     */
    private var sftpClient: SFTPClient? = null

    private val requireSftpClient: SFTPClient
        get() = sftpClient ?: SFTPClient(TerminalModel.requireConnection).apply {
            sftpClient = this
        }

    /**
     * 获取 SCP 客户端
     */
    private var scpClient: SCPClient? = null

    private val requireScpClient: SCPClient
        get() = scpClient ?: SCPClient(TerminalModel.requireConnection).apply {
            scpClient = this
        }


    /**
     * SFTP 最大一次读取长度
     */
    private val sBufferSize = 32768

    override fun p2mDoCommand(command: String): Observable<String> {
        return Observable.create {
            val outputStream = ByteArrayOutputStream()
            val charsetName = StandardCharsets.UTF_8.name()
            TerminalModel.requireConnection.exec(command, outputStream)
            val result = ConvertUtils.outputStream2String(outputStream, charsetName).trim()
            it.onNext(result)
            it.onComplete()
        }
    }

    override fun p2mQueryFileList(dirName: String): Observable<BaseFtpBean> {
        return Observable.create {
            val canonicalPath = requireSftpClient.canonicalPath(dirName)

            // 列出目录
            val vector = requireSftpClient.ls(canonicalPath)
            val elements = vector.elements()
            val fileList = arrayListOf<BaseFtpFile>()
            while (elements.hasMoreElements()) {
                val element: Any = elements.nextElement()

                // 不显示 [.|..] 两个目录
                val ftpFile = convertToFtpFile(element)
                if (ftpFile.fileName.isBlank() || ftpFile.fileName == "." || ftpFile.fileName == "..") {
                    continue
                }
                ftpFile.fileFullName = canonicalPath + "/" + ftpFile.fileName
                fileList.add(ftpFile)
            }
            // 排序 正序，文件夹在上
            val sortFileList = fileList.sortedWith { o1, o2 ->
                o1.fileName.compareTo(o2.fileName)
            }.sortedBy { file ->
                file.isDirectory.not()
            }

            it.onNext(BaseFtpBean(dirName = canonicalPath, data = sortFileList))
            it.onComplete()
        }
    }

    override fun p2mQueryFileStat(verifyPath: String): Observable<BaseFtpStat> {
        return p2mDoCommand(CMD_STAT + verifyPath).map { statInfo ->
            GsonUtils.fromJson(statInfo, BaseFtpStat::class.java)
        }
    }

    override fun p2mDownloadFile(dataBean: BaseFtpFile): Observable<BaseFtpDownloadFile> {
        return Observable.create { emitter ->
            val fileFullName = dataBean.fileFullName

            val downloadFile = BaseFtpDownloadFile(dataBean.fileName)

            // 本地创建目标空白文件
            val localFilePath = PathKt.downloadDirPath + fileFullName
            FileUtils.createFileByDeleteOldFile(localFilePath)

            var last = 0L
            // 下载速率检测
            val threadPool = Executors.newScheduledThreadPool(1)
            threadPool.scheduleAtFixedRate({

            }, 0, 1000, TimeUnit.MILLISECONDS)

            requireScpClient.get(
                fileFullName,
                PathKt.downloadDirPath
            ) { srcFile: String, targetFile: String, current: Long, total: Long ->
                last = current

                Log.e(
                    "Download",
                    "srcFile:$srcFile, targetFile:$targetFile, current:$current, total:$total"
                )
            }

            downloadFile.downloadFilePath = localFilePath

            emitter.onNext(downloadFile)
            emitter.onComplete()
//
//            // 打开远程和本地文件流
//            val inputStream = requireSftpClient.read(fileFullName)
//            val outputStream = FileOutputStream(localFilePath)
//
//
//            inputStream.runCatching {
//                // 总长度
//                downloadFile.total = dataBean.size
//                downloadFile.current = 0
//
//                var len: Int
//                val tempArray = ByteArray(sBufferSize)
//                while (read(tempArray).apply { len = this } != -1) {
//                    outputStream.write(tempArray, 0, len)
//                    downloadFile.current += len
//
//                    // 回调进度
//                    emitter.onNext(downloadFile)
//                }
//            }.onSuccess {
//                IOUtils.closeQuietly(outputStream)
//                IOUtils.closeQuietly(inputStream)
//
//                downloadFile.downloadFilePath = localFilePath
//
//                emitter.onNext(downloadFile)
//                emitter.onComplete()
//            }.onFailure {
//                IOUtils.closeQuietly(outputStream)
//                IOUtils.closeQuietly(inputStream)
//
//                // 重置 SFTP 客户端
//                closeFtpQuietly()
//
//                emitter.onError(it)
//            }
        }
    }

    /**
     * 重置 SFTP 客户端
     */
    private fun closeFtpQuietly() = runCatching {
        sftpClient?.close()
        sftpClient = null
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
}