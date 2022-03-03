package com.xiaoyv.ssh.sftp

import com.blankj.utilcode.util.LogUtils
import com.xiaoyv.blueprint.base.subscribesWithPresenter
import com.xiaoyv.busines.ftp.BaseFtpPresenter
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.ssh.utils.CMD_PWD
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

/**
 * SftpPresenter
 *
 * @author why
 * @since 2022/2/28
 */
class SftpPresenter : BaseFtpPresenter<SftpContract.View>(), SftpContract.Presenter {

    override val sftpModel: SftpModel = SftpModel()

    override fun v2pInitSshEntity(sshEntity: SshEntity) {
        sftpModel.p2mInitConnection(sshEntity)

        // 开启心跳
        pStartHeartbeat()
    }

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

    override fun pStartHeartbeat() {
        Observable.interval(10, TimeUnit.SECONDS)
            .flatMap {
                Observable.create<Boolean> {
                    sftpModel.p2mSendHeartbeatPackets()
                    it.onNext(true)
                }
            }
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    LogUtils.i("SFTP SSH 心跳包")
                },
                onError = {
                    LogUtils.e("SFTP SSH 心跳结束")
                }
            )
    }

    override fun v2pCanBack(): Boolean {
        return super.v2pCanBack() && sftpModel.isConnect()
    }

    override fun v2pOnDestroy() {
        sftpModel.v2mCloseFtp()
    }
}