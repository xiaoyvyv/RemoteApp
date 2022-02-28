package com.xiaoyv.busines.ftp

import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.blueprint.base.subscribesWithPresenter

/**
 * BaseFtpPresenter
 *
 * @author why
 * @since 2022/2/28
 */
abstract class BaseFtpPresenter<V : BaseFtpContract.View> : ImplBasePresenter<V>(),
    BaseFtpContract.Presenter {

    abstract val sftpModel: BaseFtpModel

    /**
     * 当前目录
     */
    private var pwdPath = "/home"
        set(value) {
            field = value
            requireView.p2vUpdatePathBar(value)
        }

    abstract fun v2pQueryPwdPath()

    override fun v2pUpdatePwdPath(pwdPath: String) {
        this.pwdPath = pwdPath
    }

    override fun v2pQueryFileList(filename: String) {
        // 拼接验证路径格式
        val verifyPath = when {
            filename.isBlank() -> pwdPath
            filename == "/" -> "/"
            pwdPath == "/" -> "/$filename"
            else -> "$pwdPath/$filename"
        }
        sftpModel.p2mQueryFileList(verifyPath)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    pwdPath = it.dirName

                    requireView.p2vShowFileListSuccess(it.data)
                },
                onError = {
                    requireView.p2vShowFileListError(it.message.orEmpty())
                }
            )
    }

    /**
     * 能否返回上一级
     */
    override fun v2pCanBack(): Boolean {
        return pwdPath.isNotBlank() && pwdPath != "/"
    }
}