package com.xiaoyv.ssh.sftp

import com.xiaoyv.blueprint.base.subscribesWithPresenter
import com.xiaoyv.busines.ftp.BaseFtpPresenter
import com.xiaoyv.ssh.utils.CMD_PWD

/**
 * SftpPresenter
 *
 * @author why
 * @since 2022/2/28
 */
class SftpPresenter : BaseFtpPresenter<SftpContract.View>(), SftpContract.Presenter {

    override val sftpModel: SftpModel
        get() = SftpModel()

    override fun v2pQueryPwdPath() {
        sftpModel.p2mDoCommand(CMD_PWD)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vShowPwdPath(it.trim())
                },
                onError = {
                    requireView.p2vShowPwdPath("/home")
                }
            )
    }
}