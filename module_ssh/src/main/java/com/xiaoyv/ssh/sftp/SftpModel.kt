package com.xiaoyv.ssh.sftp

import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.trilead.ssh2.SFTPv3DirectoryEntry
import com.xiaoyv.busines.ftp.BaseFtpBean
import com.xiaoyv.busines.ftp.BaseFtpFile
import com.xiaoyv.busines.ftp.BaseFtpModel
import com.xiaoyv.ssh.terminal.TerminalModel
import io.reactivex.rxjava3.core.Observable
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * SftpModel
 *
 * @author why
 * @since 2022/2/28
 */
class SftpModel : BaseFtpModel(), SftpContract.Model {

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
            val sftpClient = TerminalModel.requireSftpClient
            val canonicalPath = sftpClient.canonicalPath(dirName)

            LogUtils.i("列出目录： canonicalPath: $canonicalPath")

            val vector = sftpClient.ls(canonicalPath)
            val elements = vector.elements()
            val fileList = arrayListOf<BaseFtpFile>()
            while (elements.hasMoreElements()) {
                val element: Any = elements.nextElement()
                fileList.add(convertToFtpFile(element))
            }
            // 排序 正序，文件夹在上
            val sortFileList = fileList.sortedWith { o1, o2 ->
                o1.fileName.compareTo(o2.fileName)
            }.sortedBy { file ->
                !file.isDirectory
            }
            sortFileList.forEach { file ->
                Log.e(
                    "TTT", String.format(
                        "fileName: %10s, " +
                                "isDirectory: %10s, " +
                                "isSymlink: %10s, " +
                                "isRegularFile: %10s, " +
                                "longEntry: %s, ",
                        file.fileName,
                        file.isDirectory,
                        file.isSymlink,
                        file.isRegularFile,
                        file.longEntry
                    )
                )
            }
            it.onNext(BaseFtpBean(dirName = canonicalPath, data = sortFileList))
            it.onComplete()
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
//            isDirectory = attributes.isDirectory
//            isRegularFile = attributes.isRegularFile
//            isSymlink = attributes.isSymlink
            isDirOrDirLink = isDirectory || isSymlink && !isRegularFile
        }
    }
}