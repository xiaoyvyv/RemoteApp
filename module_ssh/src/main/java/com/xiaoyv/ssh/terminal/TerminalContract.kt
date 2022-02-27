package com.xiaoyv.ssh.terminal

import com.romide.terminal.emulatorview.TermSession
import com.trilead.ssh2.Session
import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import com.xiaoyv.busines.bean.ssh.KeyCodeBean
import com.xiaoyv.busines.room.entity.SshEntity
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

        fun p2vReleaseSuccess(success: Boolean)
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

        fun v2pDoCommandLs(dirName: String)
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
        fun p2mReleaseSession(termSession: TermSession): Observable<Boolean>

        fun p2mDoCommandLs(dirName: String): Observable<List<Any>>

    }
}