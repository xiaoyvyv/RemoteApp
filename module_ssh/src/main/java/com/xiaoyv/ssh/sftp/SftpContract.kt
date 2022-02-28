package com.xiaoyv.ssh.sftp

import com.xiaoyv.busines.ftp.BaseFtpContract
import com.xiaoyv.busines.ftp.BaseFtpFile
import io.reactivex.rxjava3.core.Observable

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
        fun p2mDoCommand(command: String): Observable<String>

        fun convertToFtpFile(any: Any): BaseFtpFile

    }
}