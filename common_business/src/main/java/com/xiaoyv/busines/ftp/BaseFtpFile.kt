package com.xiaoyv.busines.ftp

/**
 * BaseFtpFile
 *
 * @author why
 * @since 2022/2/28
 */
data class BaseFtpFile(
    var fileName: String = "",
    var size: Long = 0,
    var modifierTime: Long = 0,
    var permission: String = "",

    var uid: Long = 0,
    var user: String = "",
    var gid: Long = 0,
    var group: String = "",

    var isDir: Boolean = false
)