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

    override fun v2pQueryFileList(dirName: String) {
        sftpModel.p2mQueryFileList(dirName)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vShowFileListSuccess(it)
                },
                onError = {
                    requireView.p2vShowFileListError(it.message.orEmpty())
                }
            )
    }

}