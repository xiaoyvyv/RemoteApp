package com.xiaoyv.busines.ftp

import io.reactivex.rxjava3.core.Observable

/**
 * BaseFtpModel
 *
 * @author why
 * @since 2022/2/28
 */
open class BaseFtpModel : BaseFtpContract.Model {

    override fun p2mQueryFileList(dirName: String): Observable<List<BaseFtpFile>> {
        return Observable.create {
            it.onNext(emptyList())
            it.onComplete()
        }
    }
}