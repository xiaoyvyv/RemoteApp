package com.xiaoyv.ssh.sftp

import com.xiaoyv.busines.ftp.BaseFtpPresenter

/**
 * SftpPresenter
 *
 * @author why
 * @since 2022/2/28
 */
class SftpPresenter : BaseFtpPresenter<SftpContract.View>(), SftpContract.Presenter {

    override val sftpModel: SftpModel
        get() = SftpModel()

}