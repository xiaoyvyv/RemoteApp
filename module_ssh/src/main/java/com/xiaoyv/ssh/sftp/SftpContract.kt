package com.xiaoyv.ssh.sftp

import com.xiaoyv.business.global.ftp.BaseFtpContract
import com.xiaoyv.business.global.ftp.BaseFtpFile
import com.xiaoyv.business.global.ftp.BaseFtpStat
import com.xiaoyv.business.room.entity.SshEntity
import io.reactivex.rxjava3.core.Observable

/**
 * SftpContract
 *
 * @author why
 * @since 2022/2/28
 */
interface SftpContract {
    interface View : BaseFtpContract.View {

        fun p2vShowLinkInfo(ftpStat: BaseFtpStat)

    }

    interface Presenter : BaseFtpContract.Presenter {
        fun v2pInitSshEntity(sshEntity: SshEntity)

        fun pStartHeartbeat()

        fun v2pCheckSymLinkType(baseFtpFile: BaseFtpFile)
    }

    interface Model : BaseFtpContract.Model {
        fun p2mInitConnection(sshEntity: SshEntity)

        fun p2mDoCommand(command: String): Observable<String>

        fun convertToFtpFile(any: Any): BaseFtpFile

        fun p2mSendHeartbeatPackets()

        fun isConnect(): Boolean
    }
}