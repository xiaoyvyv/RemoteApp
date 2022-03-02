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
    override fun p2mCleanDownloadFile(dataBean: BaseFtpFile) {
        Observable.create<Boolean> {
            val fileFullName = dataBean.fileFullName
            val localFilePath = PathKt.downloadDirPath + fileFullName
            FileUtils.delete(localFilePath)
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe()
    }
}