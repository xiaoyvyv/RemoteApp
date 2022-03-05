package com.xiaoyv.ssh.terminal

import com.romide.terminal.emulatorview.TermSession
import com.trilead.ssh2.Session
import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import com.xiaoyv.business.bean.ssh.KeyCodeBean
import com.xiaoyv.business.room.entity.SshEntity
import io.reactivex.rxjava3.core.Observable

/**
 * TerminalContract
 *
 * @author why
 * @since 2020/12/06
 */
interface TerminalContract {
    interface View : IBaseView {
        /**
         * 连接成功
         *
         * @param session session
         */
        fun p2vConnectSuccess(session: Session)
        fun p2vConnectFail(errMsg: String)
    }

    interface Presenter : IBasePresenter {
        /**
         * 连接SSH
         *
         * @param sshEntity ssh
         */
        fun v2pConnectSsh(sshEntity: SshEntity)

        /**
         * 获取符号
         *
         * @return 符号
         */
        fun v2pGetSymbol(): List<KeyCodeBean>

        fun v2pReleaseSession(termSession: TermSession)

        fun pStartHeartbeat()
    }

    interface Model {
        /**
         * 连接SSH
         *
         * @param sshEntity SSH信息
         * @return 观察者
         */
        fun p2mConnectSsh(sshEntity: SshEntity): Observable<Session>

        /**
         * 关闭连接
         */
        fun p2mReleaseSession()

        /**
         * 心跳包
         */
        fun p2mSendHeartbeatPackets()
    }
}