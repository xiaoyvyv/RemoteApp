package com.xiaoyv.ssh.sftp

import com.xiaoyv.busines.ftp.BaseFtpContract
import com.xiaoyv.busines.ftp.BaseFtpFile

/**
 * SftpContract
 *
 * @author why
 * @since 2022/2/28
 */
interface SftpContract {
    interface View : BaseFtpContract.View {

    }

    interface Presenter : BaseFtpContract.Presenter {

    }

    interface Model : BaseFtpContract.Model {

        fun convertToFtpFile(any: Any): BaseFtpFile

    }
}