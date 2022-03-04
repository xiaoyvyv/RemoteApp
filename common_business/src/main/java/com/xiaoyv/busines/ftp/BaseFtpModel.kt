package com.xiaoyv.busines.ftp

import com.blankj.utilcode.util.FileUtils
import com.xiaoyv.busines.utils.PathKt
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * BaseFtpModel
 *
 * @author why
 * @since 2022/2/28
 */
abstract class BaseFtpModel : BaseFtpContract.Model {

    /**
     * 清除下载临时数据
     */
    override fun p2mCleanDownloadFile(baseFtpFile: BaseFtpFile) {
        Observable.create<Boolean> {
            val filePath = baseFtpFile.filePath
            val localFilePath = PathKt.downloadDirPath + filePath
            FileUtils.delete(localFilePath)
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe()
    }
}