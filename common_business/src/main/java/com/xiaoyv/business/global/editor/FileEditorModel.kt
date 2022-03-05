package com.xiaoyv.business.global.editor

import com.blankj.utilcode.constant.MemoryConstants
import com.blankj.utilcode.util.FileUtils
import com.xiaoyv.blueprint.exception.RxException
import com.xiaoyv.business.utils.isPlaintext
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * FileEditorModel
 *
 * @author why
 * @since 2022/3/4
 */
class FileEditorModel : FileEditorContract.Model {

    override fun p2mLoadFileContent(file: File): Observable<String> {
        return Observable.create {
            if (!file.canRead()) {
                file.setReadable(true)
            }
            if (FileUtils.getLength(file) > 500 * MemoryConstants.KB) {
                it.onError(RxException("仅支持编辑500KB以下的文本文件"))
                return@create
            }

            if (file.isPlaintext().not()) {
                it.onError(RxException("不支持编辑二进制文件"))
                return@create
            }

            val readText = file.readText(StandardCharsets.UTF_8)
            it.onNext(readText)
            it.onComplete()
        }
    }

    override fun p2mSaveFileContent(content: String, filePath: String): Observable<Boolean> {
        return Observable.create {
            val file = File(filePath)
            FileUtils.createOrExistsFile(file)

            if (!file.canWrite()) {
                file.setWritable(true)
            }
            file.writeText(content, StandardCharsets.UTF_8)

            it.onNext(true)
            it.onComplete()
        }
    }
}