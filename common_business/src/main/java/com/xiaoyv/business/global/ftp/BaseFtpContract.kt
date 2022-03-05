package com.xiaoyv.business.global.ftp

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

        fun processItemClick(baseFtpFile: BaseFtpFile, position: Int)

        fun p2vShowDownloadProgress(downloadFile: BaseFtpDownloadFile)

        fun p2vShowDownloadError(errMsg: String)
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
        fun v2pDownloadFile(baseFtpFile: BaseFtpFile)

        /**
         * 取消下载
         */
        fun v2pCancelDownloadFile(baseFtpFile: BaseFtpFile)

        fun v2pUploadFile(filePath: String, targetPath: String = "")
    }

    interface Model {
        fun p2mQueryFileList(dirName: String): Observable<BaseFtpBean>

        fun p2mQueryFileStat(verifyPath: String): Observable<BaseFtpStat> {
            return Observable.create { }
        }

        fun p2mDownloadFile(baseFtpFile: BaseFtpFile): Observable<BaseFtpDownloadFile>

        fun p2mCleanDownloadFile(baseFtpFile: BaseFtpFile)

        /**
         * 关闭 FTP
         */
        fun v2mCloseFtp()

        fun p2mUploadFile(filePath: String, targetFilePath: String): Observable<Boolean>
    }
}