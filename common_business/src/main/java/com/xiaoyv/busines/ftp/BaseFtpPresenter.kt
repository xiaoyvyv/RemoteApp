package com.xiaoyv.busines.ftp

import com.blankj.utilcode.util.LogUtils
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

    abstract val sftpModel: BaseFtpContract.Model

    /**
     * 当前目录
     */
    private var pwdPath = "/home"
        set(value) {
            field = value

            if (value == "/") {
                requireView.p2vUpdatePathBar(arrayListOf(NAME_TOP_DIR))
                return
            }

            // 路径分片
            val paths = value.split("/").map {
                // 替换顶级挂载目录名称
                it.ifEmpty { NAME_TOP_DIR }
            }
            requireView.p2vUpdatePathBar(paths)
        }

    abstract fun v2pQueryPwdPath()

    override fun v2pUpdatePwdPath(pwdPath: String) {
        this.pwdPath = pwdPath
    }

    override fun v2pQueryFileList(fileName: String, showLoading: Boolean) {
        // 拼接验证路径格式
        val verifyPath = spliceWholePathByPwd(fileName)

        if (showLoading) {
            requireView.p2vShowLoading()
        }

        sftpModel.p2mQueryFileList(verifyPath)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    pwdPath = it.dirName
                    requireView.p2vHideLoading()
                    requireView.p2vShowFileListSuccess(it.data)
                },
                onError = {
                    requireView.p2vHideLoading()
                    requireView.p2vShowFileListError(it.message.orEmpty())
                }
            )
    }

    override fun v2pQueryFileStat(fileName: String) {
        // 拼接验证路径格式
        val verifyPath = spliceWholePathByPwd(fileName)

        requireView.p2vShowLoading("获取信息中")

        sftpModel.p2mQueryFileStat(verifyPath)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vHideLoading()
                    requireView.p2vShowFileStat(it)
                },
                onError = {
                    requireView.p2vHideLoading()
                    requireView.p2vShowToast("无法获取文件属性")
                }
            )
    }

    override fun v2pDownloadFile(dataBean: BaseFtpFile) {
        sftpModel.p2mDownloadFile(dataBean)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {

                },
                onError = {
                    LogUtils.e(it)
                }
            )
    }

    /**
     * 根据当前目录文件名称和 pwd 路径，拼接完整路径
     */
    private fun spliceWholePathByPwd(filename: String) = when {
        filename.isBlank() -> pwdPath
        filename == "/" -> "/"
        pwdPath == "/" -> "/$filename"
        else -> "$pwdPath/$filename"
    }


    /**
     * 能否返回上一级
     */
    override fun v2pCanBack(): Boolean {
        return pwdPath.isNotBlank() && pwdPath != "/"
    }

    companion object {

        const val NAME_TOP_DIR = "根目录"
    }

}