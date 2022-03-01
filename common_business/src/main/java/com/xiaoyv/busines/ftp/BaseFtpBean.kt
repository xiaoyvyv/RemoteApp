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
    var fileName: String = "",
    var isDirectory: Boolean = false,

    ) : Serializable