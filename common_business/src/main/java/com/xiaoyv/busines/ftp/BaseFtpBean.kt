package com.xiaoyv.busines.ftp

import java.io.Serializable

data class BaseFtpBean(
    var dirName: String = "",
    var data: List<BaseFtpFile> = arrayListOf()
) : Serializable

/**
 * BaseFtpFile
 *
 * @author why
 * @since 2022/2/28
 */
data class BaseFtpFile(
    var fileName: String = "",
    var fileFullName: String = "",
    var longEntry: String = "",
    var size: Long = 0,
    var modifierTime: Long = 0,
    var permission: String = "",

    var uid: Long = 0,
    var user: String = "",
    var gid: Long = 0,
    var group: String = "",

    var isDirectory: Boolean = false,
    var isRegularFile: Boolean = false,
    var isSymlink: Boolean = false,
    var isBlock: Boolean = false,
    var isChar: Boolean = false,
    var isPipe: Boolean = false
) : Serializable


/**
 * 文件属性
 */
data class BaseFtpStat(
    var block: Long = 0,
    var device: String = "",
    var fileAcTime: Long = 0,
    var fileGroup: String = "",
    var fileGroupId: String = "",
    var fileMoTime: Long = 0,
    var fileFullName: String = "",
    var fileName: String = "",
    var filePermission: String = "",
    var filePermissionText: String = "",
    var fileSize: Long = 0,
    var fileType: String = "",
    var fileUser: String = "",
    var fileUserId: String = "",
    var hardLink: Int = 0,
    var inode: Long = 0,
    var ioBlock: Long = 0
) : Serializable {
    val isSymlink: Boolean
        get() = fileFullName.contains("->")

    val linkTargetPath: String
        get() {
            // ‘/symlink’ -> ‘bin’
            if (fileFullName.contains("->")) {
                return fileFullName.split("->")
                    .lastOrNull()
                    .orEmpty()
                    .replace("‘", "")
                    .trim()
            }
            return ""
        }
}

data class BaseFtpDownloadFile(
    var fileName: String = "",
    var current: Long = 0,
    var total: Long = 0,
    var downloadFilePath: String? = null,
) : Serializable {
    /**
     * 是否下载完成
     */
    val finish: Boolean
        get() = current == total && total != 0L && downloadFilePath != null

    /**
     * 下载进度
     */
    val progress: Float
        get() = if (total == 0L) 0f else current / total.toFloat()
}