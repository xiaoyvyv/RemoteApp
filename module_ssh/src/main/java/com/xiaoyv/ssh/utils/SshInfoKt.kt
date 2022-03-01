package com.xiaoyv.ssh.utils

/**
 * ## 文件状态格式化
 *
 * ### stat -c `<format>` `<file>`，format 可选格式化占位符号
 *
 * - %a     八进制表示的访问权限
 * - %A     可读格式表示的访问权限
 * - %b     分配的块数（参见 %B）
 * - %B     %b 参数显示的每个块的字节数
 * - %d     十进制表示的设备号
 * - %D     十六进制表示的设备号
 * - %f     十六进制表示的 Raw 模式
 * - %F     文件类型
 * - %g     属主的组 ID
 * - %G     属主的组名
 * - %h     硬连接数
 * - %i     Inode 号
 * - %n     文件名
 * - %N     如果是符号链接，显示器所链接的文件名
 * - %o     I/O 块大小
 * - %s     全部占用的字节大小
 * - %t     十六进制的主设备号
 * - %T     十六进制的副设备号
 * - %u     属主的用户 ID
 * - %U     属主的用户名
 * - %x     最后访问时间
 * - %X     最后访问时间，自 Epoch 开始的秒数
 * - %y     最后修改时间
 * - %Y     最后修改时间，自 Epoch 开始的秒数
 * - %z     最后改变时间
 * - %Z     最后改变时间，自 Epoch 开始的秒数
 *
 * ### 针对文件系统还有如下格式选项：
 *
 * - %a     普通用户可用的块数
 * - %b     文件系统的全部数据块数
 * - %c     文件系统的全部文件节点数
 * - %d     文件系统的可用文件节点数
 * - %f     文件系统的可用节点数
 * - %C     SELinux 的安全上下文
 * - %i     十六进制表示的文件系统 ID
 * - %l     文件名的最大长度
 * - %n     文件系统的文件名
 * - %s     块大小（用于更快的传输）
 * - %S     基本块大小（用于块计数）
 * - %t     十六进制表示的文件系统类型
 * - %T     可读格式表示的文件系统类型
 */
private const val FORMAT_SFTP_STAT: String = ""
/*
{"fileName":"%N","fileType":"","fileSize":"","filePermission":"","fileUser":"","fileGroup":"","fileAcTime":%Y,"fileMoTime":%Y,
"fileMd5":"","fileSha1":"","inodeLink":1,"inodeNodeNumber":1,"inodeProtectedMode":1,"inodeResidentDevice":1,}
 */

/**
 * 查询文件状态
 */
const val CMD_STAT = "stat -c $FORMAT_SFTP_STAT %s"

/**
 * 查询工作目录
 */
const val CMD_PWD = "pwd"
