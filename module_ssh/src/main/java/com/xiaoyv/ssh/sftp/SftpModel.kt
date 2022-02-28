package com.xiaoyv.ssh.sftp

import com.blankj.utilcode.util.ConvertUtils
import com.trilead.ssh2.SFTPv3DirectoryEntry
import com.xiaoyv.busines.ftp.BaseFtpFile
import com.xiaoyv.busines.ftp.BaseFtpModel
import com.xiaoyv.ssh.terminal.TerminalModel
import io.reactivex.rxjava3.core.Observable
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * SftpModel
 *
 * @author why
 * @since 2022/2/28
 */
class SftpModel : BaseFtpModel(), SftpContract.Model {

    override fun p2mQueryFileList(dirName: String): Observable<List<BaseFtpFile>> {
        return Observable.create {
            val sftpClient = TerminalModel.requireSftpClient
            val canonicalPath = sftpClient.canonicalPath(dirName)
            val vector = sftpClient.ls(if (dirName == "~") dirName else canonicalPath)
            val elements = vector.elements()

            val outputStream = ByteArrayOutputStream()
            TerminalModel.requireConnection.exec("pwd",outputStream)
            val dir = ConvertUtils.outputStream2String(outputStream, "utf-8")

            val fileList = arrayListOf<BaseFtpFile>()
            while (elements.hasMoreElements()) {
                val element: Any = elements.nextElement()
                fileList.add(convertToFtpFile(element))
            }

            // 排序 正序，文件夹在上
            it.onNext(fileList.sortedWith { o1, o2 ->
                o1.fileName.compareTo(o2.fileName)
            }.sortedBy { file ->
                !file.isDir
            })
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

            // 基本属性
            fileName = any.filename.orEmpty()
            size = attributes?.size ?: 0
            permission = attributes.octalPermissions.orEmpty()
            modifierTime = (attributes?.mtime ?: 0L) * 1000L
            isDir = attributes.isDirectory
        }
    }
}

inline fun String.AAA(
    crossinline a: () -> Unit
) {

}