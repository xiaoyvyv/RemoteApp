package com.xiaoyv.busines.ftp

import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import io.reactivex.rxjava3.core.Observable
import java.io.File

/**
 * BaseFtpContract
 *
 * @author why
 * @since 2022/2/28
 */
interface BaseFtpContract {
    interface View : IBaseView {
        /**
         * 当前工作目录
         */
        fun p2vShowPwdPath(pwdPath: String)

        /**
         * 当前目录列表查询成功
         */
        fun p2vShowFileListSuccess(fileList: List<BaseFtpFile>)
        fun p2vShowFileListError(errMsg: String)

        /**
         * 更新路径导航
         */
        fun p2vUpdatePathBar(pwdPath: List<String>)

        /**
         * 显示文件属性
         */
        fun p2vShowFileStat(ftpStat: BaseFtpStat)

        fun processItemClick(dataBean: BaseFtpFile, position: Int)
    }

    interface Presenter : IBasePresenter {
        /**
         * 查询当前目录
         */
        fun v2pQueryFileList(fileName: String = "", showLoading: Boolean = true)

        fun v2pUpdatePwdPath(pwdPath: String)

        fun v2pCanBack(): Boolean

        /**
         * 链接解析
         */
        fun v2pQueryFileStat(fileName: String)

        /**
         * 下载文件
         */
        fun v2pDownloadFile(dataBean: BaseFtpFile)
    }

    interface Model {
        fun p2mQueryFileList(dirName: String): Observable<BaseFtpBean>

        fun p2mQueryFileStat(verifyPath: String): Observable<BaseFtpStat> {
            return Observable.create { }
        }

        fun p2mDownloadFile(dataBean: BaseFtpFile): Observable<File>
    }
}