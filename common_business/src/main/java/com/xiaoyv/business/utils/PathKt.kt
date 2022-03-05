package com.xiaoyv.business.utils

import com.blankj.utilcode.util.PathUtils
import okio.Buffer
import java.io.EOFException
import java.io.File

/**
 * PathKt
 *
 * @author why
 * @since 2022/3/2
 */
object PathKt {

    /**
     * 下载目录
     */
    val downloadDirPath = PathUtils.getFilesPathExternalFirst() + "/download/ssh"
}

fun File.isPlaintext(): Boolean {
    return inputStream().use { fileStream ->
        try {
            val buffer = Buffer().readFrom(fileStream)
            val prefix = Buffer()
            val byteCount: Long = if (buffer.size() < 64) buffer.size() else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint: Int = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            false // Truncated UTF-8 sequence.
        }
    }
}