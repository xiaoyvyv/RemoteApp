package com.xiaoyv.busines.ftp

import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import io.reactivex.rxjava3.core.Observable

/**
 * BaseFtpContract
 *
 * @author why
 * @since 2022/2/28
 */
interface BaseFtpContract {
    interface View : IBaseView {

        /**
         * 当前目录查询成功
         */
        fun p2vShowFileListSuccess(fileList: List<BaseFtpFile>)
        fun p2vShowFileListError(errMsg: String)

    }

    interface Presenter : IBasePresenter {
        /**
         * 查询当前目录
         */
        fun v2pQueryFileList(dirName: String)
    }

    interface Model {

        fun p2mQueryFileList(dirName: String): Observable<List<BaseFtpFile>>

    }
}